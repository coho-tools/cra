package coho.geom.twodim.ops;

import java.util.*;
import coho.geom.*;
import coho.geom.twodim.*;
import coho.common.number.*;


/*
 * This class is similar with Reduce. 
 * However, it add the constraint that the maxDistance of new points to original convex hull
 * is less than maxDist. This is used to avoid open polygon problem. 
 * In coho, after each lp_project, we want to limit the number of vertices of each projection. 
 * However, this will introduce infeasible point which results in open polygon. 
 * After this constraint, all points are not far infeasible (<maxDist). 
 * So for each face, we bloat it by maxDist and then forward it, the open polygon will not occur.
 * 
 * For each edge, we remember its maxdistanct to convex hull for all points on the segment. 
 * If the dist is greater than maxDist, we can not remove it. 
 * The dist is compute as
 *   dist(edge) = dist(newPt if edge reduced, edge)+max(dist(prevEdge),dist(nextEdge));
 * 
 */
/*
 * NOTE: Round off new vertex.
 * evalEdgeCost() is the only place that produces new points. The new points uses same type as input, usually is Double.
 * When the intersection point is close to the edge of polygon, for example, prev and next are nearly parallel. 
 * Error might be introduced by rounding off.
 * 
 * 1)The real intersection point is in the polygon. This segment should not be removed. 
 * If the rounded point is out of the polygon, this edge might by removed and a new point out of polygon is introduced. This introduces little over-approximation. 
 * If the rouned points is on the segment, an exception is thrown and will never by removed 
 * 
 * 2)The real intersection point is out the polygon. This segment should be removed with little cost.
 * If the rounded point is in the polygon, this segment can not be remove in the algorithm. This is not reasonable but happens rarely.  
 * If the rounded point is on the segment, an exception is thrown and can not be removed. This is not reasonable but happens rarely.  
 * 
 * 3)The prev and next are parallel, with no intersection. intersect() function must return the correct result becaue it use APR. 
 * 
 * 4)The intersection is a segment for colinear points. This segment(two points) should be removed with not cost. 
 * This is impossible for input because colinear points are removed by the constructor. 
 * For colinear points are introduced during reduction. The cost of new points should be zero. 
 * The intersect() function will return APR result!
 * 
 */
/*
 * NOTE: Update of hull.
 * Round off is not a problem. 
 * 
 * If the new rounded vertex is above the hull segment, it must increase the hull area and change the 
 * hull. The new point is added to the hull and concave one are removed. 
 * See evalEdgeCost and reduceEdge. 
 * 
 * If the new rounded vertex is on or below the hull segment. We ASSUME it is in the hull and does not 
 * increase hull and change the hull. The new point is not added to the hull but hull is also updated 
 * if end-point are on the convex hull. 
 * 
 * TODO: However, the assumption may not be true. In this case, the new polygon is not a simple polygon anymore.
 * I don't know how to avoid this case. But it should be rarely. Think about it if it happens. 
 */
/*
 * NOTE: ConvexPolygon reduce. 
 * 
 * For convex polygon, point can not be redueced. We don't need to consider the point event.
 * Therefore, I added a notConvexPolygon variable. If it is not true, we don't need to update point. See constructor, reduceEdge.
 */
/*
 *2014-06-06
 *NOTE: This is not true. The remove function is based on the object reference, not compareTo function. 
 *So don't need to complicate the compareTo function for deleting correct objects. 
 *In fact, the hasCode() will make the result random. 
 *I made this stupid mistake long time ago as I was not very familiar with Java at that time. 
 *
 *NOTE: Remove reduceEvent from costQueue. See ReduceEvent
 *
 *It seem that the remove() function of PriorityQueue is based on compareTo not equals
 *(I have tested, during the remove() only compareTo() not equals() function is called)
 *At the beginning, compareTo use the cost to compare however equals is the default one.
 *Therefore, if two events has the same cost, the first one will be removed which causes problem
 *Our solution is to make the equals and compareTo consistent with each other.
 * 
 */
//TODO: we want to redefine the cost as c1Ap+c2Ah+3d^2, Ap is the increased area of polygon, Ah is the increase area of hull
//d is the increased diameter of convex hull
public class OutwardReduce{
	/**
	 * data structure for vertex of Reduce
	 * It's a four way linked list. Double Link for polygon and double link for its convex hull.
	 * In addition, it save the area increased if this point/edge removed and also the intersection of adjacent edges
	 */
	static class ReduceVertex extends Point{
		private ReduceVertex prev, next;//previous and next point on the polygon.  next is for anti-clock-wise order
		private ReducePoint reducePoint;//link for the reduce point event
		private ReduceEdge reduceEdgeAsStart, reduceEdgeAsEnd;//link for the reduce edge event
		private double dist = 0;//dist >=0
		
		public ReduceVertex(Point p,ReduceVertex prev, ReduceVertex next, 
				ReducePoint reducePoint, ReduceEdge reduceEdgeAsStart, ReduceEdge reduceEdegeAsEnd){
			super(p.x(),p.y());
			this.prev = prev;
			this.next = next;
			this.reducePoint = reducePoint;
			this.reduceEdgeAsStart = reduceEdgeAsStart;
			this.reduceEdgeAsEnd = reduceEdegeAsEnd;
		}
		public ReduceVertex(Point p, ReduceVertex prev, ReduceVertex next){
			super(p.x(),p.y());
			this.prev = prev;
			this.next = next;
		}
		public ReduceVertex(Point p, double dist){
			super(p.x(),p.y());
			this.dist = dist;
		}
		public ReduceVertex(Point p){
			super(p.x(),p.y());
		}
		
		public boolean isConvex(){
			return Point.isLeftTurn(prev, this, next);
		}
		public boolean isConcave(){
			return Point.isRightTurn(prev, this, next);
		}
		public boolean isStraight(){
			return Point.isStraight(prev,this,next);
		}
		public double dist(){
			return dist;
		}
	}
	static class ReduceEvent implements Comparable<ReduceEvent>{
		private final double cost,area;
		protected ReduceEvent(double area, double cost){
			this.area = area;
			this.cost = cost;
		}
		protected ReduceEvent(double area){
			this.area = area;
			this.cost = area;
		}
		public double cost(){
			return cost;
		}
		public double area(){
			return area;
		}
		public int compareTo(ReduceEvent event){
			int result = (int)Math.signum(cost() - event.cost());
			return result;
			// NOTE: The following code make the result random
		  /*
			if(result!=0)
				return result;
			else if(this.equals(event))
				return 0;
			else{//objects are not the same, but with same cost
				//NOTE: ReduceEvent use the default hashCode() of Object. It converts the inteval address
				//to integer as hashCode(). Therefore, it provides distinct hashCodes for distinct Object. 
				//though it is not required by the Java language.
				result = hashCode()-event.hashCode();
				if(result ==0 )
					throw new RuntimeException("ReduceEvent.compareTo. It's impossible. The hashCode for different object are the same.");
				return result;
			}
			*/
		}
		public String toString(){
			return "increase area by: "+area+" the cost is "+cost;
		}
	}
	static class ReducePoint extends ReduceEvent{
		private ReduceVertex point;
		public ReducePoint(ReduceVertex point, double area){
			super(area);
			this.point = point;
		}
		public ReducePoint(ReduceVertex point, double area, double cost){
			super(area,cost);
			this.point = point;
		}
		public String toString(){
			return "The event is for point"+point+super.toString();
		}
		public boolean equals(Object o){
			if(o instanceof ReducePoint){
				ReducePoint rp = (ReducePoint)o;
				return point.equals(rp.point);
			}
			return false;
		}
	}	
	static class ReduceEdge extends ReduceEvent{
		private ReduceVertex start, end;
		private GeomObj2 intersectObj;
		public ReduceEdge(ReduceVertex start, ReduceVertex end, 
				double area, double cost, GeomObj2 intersectObj){
			super(area, cost);
			this.start = start;
			this.end = end;
			this.intersectObj = intersectObj;
		}
		public String toString(){
			return "The event is for segment"+start+", "+end+"]"+"intersection "+intersectObj+" "+super.toString();
		}
		public boolean equals(Object o){
			if(o instanceof ReduceEdge){
				ReduceEdge re = (ReduceEdge)o;
				return start.equals(re.start) && end.equals(re.end);
			}
			return false;
		}
	}
	
	private ReduceVertex head;
	private int degree;
	private CohoType type;
	private double area;//the area for the reduced polygon and convex hull;
	
	private boolean pointReducible = true;//point is reduceable?
	private boolean edgeReducible = true;//edge is reduceable?
	public static double maxNewPointDist = 0.05;//Double.MAX_VALUE;//not used now
	// NOTE we do not want to remove a sequence of concave points to make concave polygon convex after some steps.  
	// However, it is quite common that there is nearly colinear (but concave) point on a long edge. 
	// Therefore, we want to disable this options by setting it to a huge number. See evalPointCost() function;
	public static double maxNewEdgeLen = Double.MAX_VALUE;//0.1; //maximum length introduced.
	
	public static final double hullWeight = 1;//we don't want to introduce very thin triangle which might increase the area of convex hull a lot
	public int degree(){
		return degree;
	}
	public double area(){
		return area;
	}
	private PriorityQueue<ReduceEvent> costQueue=null;
	//private TreeSet<ReduceEvent> costQueue=null;
	private void listAllEvents(){
		Iterator<ReduceEvent> re = costQueue.iterator();
		System.out.println("List all Events in costQueue");
		while(re.hasNext()){
			System.out.println(re.next().toString());
		}
		System.out.println("");
	}
	private boolean addReduceEvent(ReduceEvent reduceEvent){
		//System.out.println("adding events"+reduceEvent);
		if(Double.isInfinite(reduceEvent.cost()))
			return false;		
		return costQueue.add(reduceEvent);//no duplication
	}
	private boolean removeReduceEvent(ReduceEvent reduceEvent){
		return costQueue.remove(reduceEvent);
	}
	ReduceEvent getMinCostEvent(){
		return costQueue.peek();
		//return costQueue.first();
	}
	
	//CONSIDER: Will round off cause problem?
	public static double[] areaDist(Point prev, Point curr, Point next){
		double x1 = prev.doubleX();
		double x2 = curr.doubleX();
		double x3 = next.doubleX();
		double y1 = prev.doubleY();
		double y2 = curr.doubleY();
		double y3 = next.doubleY();
		//NOTE: reduce the area computation error.
		double area = ((x1*y2-x2*y1)+(x2*y3-x3*y2)+(x3*y1-x1*y3))/2.0;
		if(area<=0){//0 might be cause by round off.
			Point[] points=new Point[]{prev.specifyType(CohoAPR.type),curr.specifyType(CohoAPR.type),next.specifyType(CohoAPR.type)};
			CohoNumber result = points[0].x().zero();//get the type
			for (int i=0; i<points.length; i++){
				CohoNumber x_i = points[i].x();
				CohoNumber y_i = points[i].y();
				CohoNumber x_inc = points[(i+1)%points.length].x();
				CohoNumber y_inc = points[(i+1)%points.length].y();
				result = result.add(x_i.mult(y_inc).sub(x_inc.mult(y_i)));
			}
			area =  result.div(2).doubleValue();
		}
		double length = Math.sqrt((x3-x1)*(x3-x1)+(y3-y1)*(y3-y1));
		double dist = 2*area/length;
		return new double[]{area,dist};
	}
	public static double segLen(Point from, Point to){
		double x1 = from.doubleX(); double x2 = to.doubleX();
		double y1 = from.doubleY(); double y2 = to.doubleY();
		double len = Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
		return len;
	}
	public OutwardReduce(SimplePolygon poly){
		this(poly,true,true);
	}
	public OutwardReduce(SimplePolygon poly,boolean pointReducible, boolean edgeReducible){
		this.pointReducible = pointReducible;
		this.edgeReducible = edgeReducible;
		type = poly.type();
		area = poly.area().doubleValue();		

		//remove colinear point
		int n = poly.degree();
		Point[] allPoints = poly.points();
		ArrayList<Point> nonColinearPoints = new ArrayList<Point>();
		for(int i=0; i<n; i++){
			if(!Point.isStraight(allPoints[(n+i-1)%n], allPoints[i], allPoints[(i+1)%n])){
				nonColinearPoints.add(allPoints[i]);
			}
		}
		n = nonColinearPoints.size();
		degree = n;
		assert degree>2:"poly"+poly+"has less than three points";
		
		// create vetices
		ReduceVertex[] vertices = new ReduceVertex[n];
		for(int i=0; i<n; i++){
			vertices[i] = new ReduceVertex(nonColinearPoints.get(i));
		}		
		// link vertices for polyogn
		for(int i=0; i<n; i++){
			vertices[i].prev = vertices[(n+i-1)%n];
			vertices[i].next = vertices[(i+1)%n];
		}
		head = vertices[0];
		
		// compute the cost and link the reduce event with vertex
		costQueue = new PriorityQueue<ReduceEvent>(2*n);//create the prority queue
		for(int i=0; i<n; i++){
			if(pointReducible){
				ReducePoint reducePoint = evalPointCost(vertices[i]);
				vertices[i].reducePoint = reducePoint;
				addReduceEvent(reducePoint);
			}
			if(edgeReducible){
				ReduceEdge reduceEdge = evalEdgeCost(vertices[i]);
				vertices[i].reduceEdgeAsStart = reduceEdge;
				vertices[(i+1)%n].reduceEdgeAsEnd = reduceEdge;
				addReduceEvent(reduceEdge);
			}
		}
	}
	public OutwardReduce(ConvexPolygon poly){//point can't be reduced
		this.pointReducible = false;
		this.edgeReducible = true;
		type = poly.type();
		area = poly.area().doubleValue();		

		//remove colinear point
		int n = poly.degree();
		Point[] allPoints = poly.points();
		ArrayList<Point> nonColinearPoints = new ArrayList<Point>();
		for(int i=0; i<n; i++){
			if(!Point.isStraight(allPoints[(n+i-1)%n], allPoints[i], allPoints[(i+1)%n])){
				nonColinearPoints.add(allPoints[i]);
			}
		}
		n = nonColinearPoints.size();
		degree = n;
		assert degree>2:"poly"+poly+"has less than three points";
		
		// create vetices
		ReduceVertex[] vertices = new ReduceVertex[n];
		for(int i=0; i<n; i++){
			vertices[i] = new ReduceVertex(nonColinearPoints.get(i));
		}		
		// link vertices for polyogn
		for(int i=0; i<n; i++){
			vertices[i].prev = vertices[(n+i-1)%n];
			vertices[i].next = vertices[(i+1)%n];
		}
		head = vertices[0];
		
		// compute the cost and link the reduce event with vertex
		costQueue = new PriorityQueue<ReduceEvent>(2*n);//create the prority queue
		for(int i=0; i<n; i++){
			ReduceEdge reduceEdge = evalEdgeCost(vertices[i]);
			vertices[i].reduceEdgeAsStart = reduceEdge;
			vertices[(i+1)%n].reduceEdgeAsEnd = reduceEdge;
			addReduceEvent(reduceEdge);
		}
	}

	
	ReducePoint evalPointCost(ReduceVertex vertex){
		if(!pointReducible)
			throw new RuntimeException("Algorithm Error: try to remove non-reducible point");
		double area=0;
		double cost = 0;
		if(vertex.isConvex()){
			area = Double.POSITIVE_INFINITY;
			cost = Double.POSITIVE_INFINITY;
		}else if(vertex.isConcave()){
			//NOTE:The area of vertex.prev, vertex, vertex.next should be negative because vertex is concave.
			//areaCost recompute use apr if area is less than zero to remove computation error. 
			//To speed up. We use the area of vertex.next, vertex, vertex.prev as the cost .
			area = areaDist(vertex.next,vertex,vertex.prev)[0];//don't change convex hull, only use area.
			area = Math.max(Double.MIN_VALUE, area);//NOTE: use abs to avoid round off error?
			//NOTE: We prefer to remove point whose prev/next points are also conCave().
			//Therefore cost == area is no longer correct
			//It tries but not guarantee to not produce non simple polygon 
			cost = area;
			if(vertex.prev.isConcave())
				cost = cost/2;
			if(vertex.next.isConcave())
				cost = cost/2;
			if(segLen(vertex.prev,vertex.next)>maxNewEdgeLen)
				cost = Double.POSITIVE_INFINITY;
			//System.out.println("new event: "+ new ReducePoint(vertex,area,cost));
		}else{//on the same line, cost is zero
		}
		//System.out.println("new event: "+ new ReducePoint(vertex,area));
		//return new ReducePoint(vertex,area);
		return new ReducePoint(vertex,area,cost);
	}

	/*
	 * NOTE: This is the only place that produces new points. The new points uses same type as input, usually is Double.
	 * When the intersection point is close to the edge of polygon, for example, prev and next are nearly parallel. 
	 * Error might be introduced by rounding off.
	 * 
	 * 1)The real intersection point is in the polygon. This segment should not be removed. 
	 * If the rounded point is out of the polygon, this edge might by removed and a new point out of polygon is introduced. This introduces little over-approximation. 
	 * If the rouned points is on the segment, an exception is thrown and will never by removed 
	 * 
	 * 2)The real intersection point is out the polygon. This segment should be removed with little cost.
	 * If the rounded point is in the polygon, this segment can not be remove in the algorithm. This is not reasonable but happens rarely.  
	 * If the rounded point is on the segment, an exception is thrown and can not be removed. This is not reasonable but happens rarely.  
	 * 
	 * 3)The prev and next are parallel, with no intersection. intersect() function must return the correct result becaue it use APR. 
	 * 
	 * 4)The intersection is a segment for colinear points. This segment(two points) should be removed with not cost. 
	 * This is impossible for input because colinear points are removed by the constructor. 
	 * For colinear points are introduced during reduction. The cost of new points should be zero. 
	 * The intersect() function will return APR result!
	 * 
	 */
	ReduceEdge evalEdgeCost(ReduceVertex start){
		if(!edgeReducible)
			throw new RuntimeException("Algorithm Error: try to remove non-reducible edge");
		double area = 0, cost=0;
		ReduceVertex end = start.next;
		//if(start.isConcave() && end.isConcave()){//remmove a special case
		if(start.isConcave() || end.isConcave()){//remmove a special case
			//NOTE: If the two endpoints of a segment are concave,although the cost to remove such an edge is finite, 
			//it will never used because two endpoints can be removed with less cost. Here, we force to remove points
			//This makes the following code simplier.
			//NOTE: If one of its endpoints of a segment is concave, we force to remove this point first.
			//Although remove the edge may increase less area. Remove edge is more expensive. It compute new point which 
			//might introduce lots of error. Of course, usually, even the cost is computed, it is larger than that of concave vertex.
			//we remove this case to simplify the algorithm.
			return new ReduceEdge(start,end, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,null);
		}
		Segment seg1 = Segment.create(start.prev, start);
		Segment seg2 = Segment.create(end, end.next);
		GeomObj2 obj = edgeIntersect(seg1,seg2);
		try{
			if(obj instanceof Point){//intersection is point
				Point p = (Point)obj;
				if(Point.isRightTurn(start, p, end)){//intersection in polygon, can't remove
					area = Double.POSITIVE_INFINITY;
					cost = Double.POSITIVE_INFINITY;
				//NOTE After removing co-linear points, the intersection point can only be left/right turn. 
				// However, it might be on the same line because of rounding off, which usually happens when start end and p are almost the same.
				// We can remove such points because the error should be ignorable. 	
				}else{	
				//}else if(Point.isLeftTurn(start, p, end)){//intersection out of polygon, can remove
					//update polyCost
					//NOTE remove negative or 0 area because of round off
					//NOTE: use the increase length to avoid sharp corner.
					
					//double areaDist[] = areaDist(start,p,end);
					//area = Math.max(Double.MIN_VALUE, areaDist[0]);
					area = Math.max(Double.MIN_VALUE, areaDist(start,p,end)[0]);

					double d = segLen(start,end);
					double d1 = segLen(start,p); double d2 = segLen(p,end);
					double dist = d1+d2-d; 
					//double dist = areaDist[1];
					dist += Math.max(start.dist(),end.dist());
					if(dist>maxNewPointDist){
						cost = Double.POSITIVE_INFINITY;
					}else{
						cost = Math.max(area, dist*dist*hullWeight/2);
					}
					//NOTE: assign the dist of this pt to convex hull (over approximated).
					obj = new ReduceVertex((Point)obj,dist);
				//}else{//intersection on edge, impossible
				//	//This may caused by floating point error.
				//	throw new RuntimeException("Impossible, intersection is segment");
				}
			}else if(obj instanceof Empty){//parallel, can't remove
				area = Double.POSITIVE_INFINITY;
				cost = Double.POSITIVE_INFINITY;
			}else if(obj instanceof Line){//on the same line, remove with no penalty
				//NOTE: The special case of colinear point which can also be reduced by point. 
				//Here, we change the cost from zero to infinity to force to use reducePoint. 
				//That's because if we reduce by edge, we should handle this case specially because it reduce two points and doesn't introduce a new point.
				//In the constructor we remove the colinear case however it's possible caused during the reduction
				area = Double.POSITIVE_INFINITY;
				cost = Double.POSITIVE_INFINITY;
			}else{
				throw new GeomException("Impossible");
			}
		}catch(Exception e){//if anything bad happens, we just don't remove this segment. This may not produce the optimal result but it rarely happens and who cares.\
			area = Double.POSITIVE_INFINITY;
			cost = Double.POSITIVE_INFINITY;
		}
		//System.out.println("create event "+new ReduceEdge(start,end,area,cost,obj));
		return new ReduceEdge(start,end,area,cost,obj);
	}
	
	GeomObj2 edgeIntersect(Segment seg1, Segment seg2){
		//FIXED: the error here may make the polygon not simple
		GeomObj2 obj=null;
		try{
			Line prev = seg1.specifyType(DoubleInterval.type).line();
			Line next = seg2.specifyType(DoubleInterval.type).line();
			obj = prev.intersect(next);//see note above
			if(obj.maxError()> GeomObj2.eps){
				throw new ArithmeticException("too large interval");
			}
		}catch(ArithmeticException e){
			Line prev = seg1.specifyType(CohoAPR.type).line();
			Line next = seg2.specifyType(CohoAPR.type).line();
			obj = prev.intersect(next);//see note above
		}
		return obj.specifyType(type);//create point with same type
	}

	boolean reducePoint(ReducePoint reducePoint){
		if(!pointReducible)
			throw new RuntimeException("Algorithm error: try to reduce non-reducible point");
		if(reducePoint.cost()==Double.POSITIVE_INFINITY )//if(vertex.isConvex())
			throw new GeomException("Can't remove an convex point which will reduce the area");

		//update polygon
		degree--;
		area += reducePoint.area();//update area
		//delete from polygon
		ReduceVertex vertex = reducePoint.point;
		ReduceVertex prevVertex = vertex.prev;
		ReduceVertex nextVertex = vertex.next;
		prevVertex.next = nextVertex;
		nextVertex.prev = prevVertex;
		//FIXED: change head if it's removed
		if(vertex==head)
			head = nextVertex;
		
		//update convex hull
		//nothing here
		
		//update event
		//points
		removeReduceEvent(prevVertex.reducePoint);
		ReducePoint p = evalPointCost(prevVertex);
		prevVertex.reducePoint = p;
		addReduceEvent(p);
		
		removeReduceEvent(vertex.reducePoint);
		
		removeReduceEvent(nextVertex.reducePoint);
		p = evalPointCost(nextVertex);
		nextVertex.reducePoint = p;
		addReduceEvent(p);
		
		
		//edges
		if(edgeReducible){
			removeReduceEvent(prevVertex.reduceEdgeAsEnd);
			ReduceEdge e = evalEdgeCost(prevVertex.prev);//edge pre.pre -> pre
			prevVertex.prev.reduceEdgeAsStart = e;
			prevVertex.reduceEdgeAsEnd = e;
			addReduceEvent(e);

			removeReduceEvent(vertex.reduceEdgeAsEnd);
			removeReduceEvent(vertex.reduceEdgeAsStart);
			e = evalEdgeCost(prevVertex);//edge pre->next
			prevVertex.reduceEdgeAsStart  = e;
			nextVertex.reduceEdgeAsEnd = e;
			addReduceEvent(e);

			removeReduceEvent(nextVertex.reduceEdgeAsStart);
			e = evalEdgeCost(nextVertex);//edge next->next.next
			nextVertex.reduceEdgeAsStart = e;
			nextVertex.next.reduceEdgeAsEnd = e;
			addReduceEvent(e);
		}
		
		return true;
	}
	
	boolean reduceEdge(ReduceEdge reduceEdge){
		if(!edgeReducible)
			throw new RuntimeException("Algorithm error, try to reduce non-reducible edge.");
		
		if(reduceEdge.cost()==Double.POSITIVE_INFINITY)
			throw new GeomException("Can't remove an convex point which will reduce the area");
		if(!(reduceEdge.intersectObj instanceof Point))
			throw new GeomException("Can't remove an edge of which intersection is not a point");
		//Point newPoint = (Point)reduceEdge.intersectObj;
		ReduceVertex newVertex = (ReduceVertex)reduceEdge.intersectObj;
		
		//update polygon
		degree--;
		area += reduceEdge.area();
		
		//delete from polygon and add new intersection
		ReduceVertex start = reduceEdge.start;
		ReduceVertex end = reduceEdge.end;
		ReduceVertex prevVertex = start.prev;
		ReduceVertex nextVertex = end.next;
		//ReduceVertex newVertex = new ReduceVertex(newPoint,prevVertex,nextVertex);
		newVertex.prev = prevVertex;
		newVertex.next = nextVertex;
		prevVertex.next = newVertex;
		nextVertex.prev = newVertex;
		//FIXED: change head if it's removed
		if(start==head || end==head)
			head = newVertex;

		//update reduce events
		//points
		if(pointReducible){//for convex polygon, don't need to update point
			removeReduceEvent(prevVertex.reducePoint);
			ReducePoint p  = evalPointCost(prevVertex);
			prevVertex.reducePoint = p;
			addReduceEvent(p);

			removeReduceEvent(start.reducePoint);
			removeReduceEvent(end.reducePoint);
			p = evalPointCost(newVertex);
			newVertex.reducePoint = p;
			addReduceEvent(p);

			removeReduceEvent(nextVertex.reducePoint);
			p = evalPointCost(nextVertex);
			nextVertex.reducePoint = p;
			addReduceEvent(p);
		}

		//edges
		removeReduceEvent(start.reduceEdgeAsEnd);
		ReduceEdge e = evalEdgeCost(prevVertex);
		prevVertex.reduceEdgeAsStart = e;
		newVertex.reduceEdgeAsEnd = e;
		addReduceEvent(e);
		
		removeReduceEvent(start.reduceEdgeAsStart);
		
		removeReduceEvent(end.reduceEdgeAsStart);
		e = evalEdgeCost(newVertex);
		newVertex.reduceEdgeAsStart = e;
		nextVertex.reduceEdgeAsEnd = e;
		addReduceEvent(e);

		return true;
	}	

	boolean reduce(ReduceEvent event){
		if(event instanceof ReducePoint){
			return reducePoint((ReducePoint)event);
		}if(event instanceof ReduceEdge){
			return reduceEdge((ReduceEdge)event);
		}
		throw new GeomException("Unsupported reduce event");
	}
	
	public SimplePolygon reduce(SimplePolygon.EndCondition ec){
		//reduceWithNoCost();
		double area = area();
		reduceWithSlightCost();
		while(!costQueue.isEmpty()&&degree>3){
			ReduceEvent event = getMinCostEvent();
			double areaNow = area();
			double areaNext = areaNow+event.area();//relative cost
			if(ec.cond(areaNow/area-1,degree, areaNext/area-1, degree-1))
				break;
			reduce(event);	
		}
		return getPolygon();//NOTE: it might be non simple polygon
	}

	
//	public SimplePolygon canon(){
//		reduceWithSlightCost();
//		return getPolygon();//NOTE: it is simple till now.
//	}
	//reduce colinear point
	void reduceWithNoCost(){
		while(!costQueue.isEmpty()&&degree>3){
			ReduceEvent event = getMinCostEvent();
			if(event.cost()!=0)
				break;
			reduce(event);
		}
	}
	void reduceWithSlightCost(){
		double area = area();//polyArea+HullWeight*hullArea;
		while(!costQueue.isEmpty()&&degree>3){
			ReduceEvent event = getMinCostEvent();
			if(event.cost()/area>GeomObj2.eps)
				break;
			//System.out.println("reducing "+event);
			reduce(event);
		}		
	}
	SimplePolygon getPolygon(){
		Point[] points = new Point[degree];
		ReduceVertex current = head;
		for(int i=0; i<degree; i++){
			points[i] = current;
			current = current.next;
		}
		return new SimplePolygon(points);//BUG: duplicated point
	}
	public static void main(String[] args){
//		Point[] pts = new Point[]{
//				Point.create(0,0),
//				Point.create(1,0),
//				Point.create(2,0),
//				Point.create(1.99,1),
//				Point.create(2,2),
//				Point.create(0.01,2),
//				Point.create(0,1.99),				
//		};
//		Point[] pts = new Point[]{
//				Point.create(0,0),
//				Point.create(0,1),
//				Point.create(2,1),
//				Point.create(2,2),
//				Point.create(-1,2),
//				Point.create(-1,-1),
//		};
//		SimplePolygon poly = new SimplePolygon(pts);
//		System.out.println(poly);
//		Reduce t = new Reduce(poly);
//		Reduce tt = new Reduce(poly);
//		System.out.println(t.equals(tt));
//		System.out.println(t+" "+tt);
		
		// check the forward dead loop
		Point[] pts = new Point[]{
				Point.create(1.7356512784315434, -0.0031799670366427354),
				Point.create(1.7688040444860935, -0.0032604835573700267),
				Point.create(1.8026224036997327, -0.003342616572036363),
				Point.create(1.802657696990022, 9.01477127263211E-5),
				Point.create(1.8029930758408814, 0.03271041438828898),
				Point.create(1.8033637479820304, 0.06876344534861431),
				Point.create(1.803734420123179, 0.10481647630893967),
				Point.create(1.8036292165641157, 0.10534807846143085),
				Point.create(1.7681621144092907, 0.10645451691122067),
				Point.create(1.7367880497723278, 0.10738688650968083)
		};
		SimplePolygon poly = new SimplePolygon(pts);
//		System.out.println(poly);
//		System.out.println(poly.convexHull());
		OutwardReduce t = new OutwardReduce(poly);
		//System.out.println(t.canon());
//		t.test();
//		SimplePolygon.EndCondition ec = new SimplePolygon.DegreeEndCondition(5);
//		poly = t.reduce(ec);
//		System.out.println(poly);
	}
}
