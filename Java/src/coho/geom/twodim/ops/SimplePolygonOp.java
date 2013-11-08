package coho.geom.twodim.ops;
import java.util.*;
import coho.common.number.*;
import coho.geom.twodim.*;
import coho.geom.*;
/**
 * Operations for SimplePolygon. 
 * We don't want to the result depends on the representation of polygon. 
 * Therefore, we provide the function in ops package, which use interval for performance and apr for accuracy
 * 
 * NOTE: I change the method to advance. 
 * At fist, we record the previous and current point to create a segment, and based on this segment
 * we find the rightmost/leftmost point to advance. This method introduce new segments, 
 * which has the problem that the segment might not be scaleType.
 * Now, we don't create new segemnt. We use the rightmost/leftmost segment instead, they are in the same line
 * 
 * NOTE: There are two place generated the new polygon. 
 * First is the lp_project. There we use convexHull to remove many special cases because we know the result 
 * is always a convex polygon. Then reduce is called to remove points with slight cost.
 *  
 * The second is the union/intersect. After computing the points, we can't convert the points to double directly.
 * We use toScale to deal with these problem. If two points are the same after round off, it will move one point
 * and maintain the order. It also removes short edge which may introduce non simple polygon. Then canon is called.   
 */
public class SimplePolygonOp extends PolygonOp{
	public static boolean stupid = false;
	static class PolygonPair{
		IntersectSimplePolygon p1, p2;//the intersection of p1 and p2 are computed already
		public PolygonPair(IntersectSimplePolygon p1, IntersectSimplePolygon p2){
			this.p1 = p1;
			this.p2 = p2;
		}
		public boolean equals(Object obj){
			if(obj instanceof PolygonPair){
				PolygonPair that = (PolygonPair)obj;
				if((p1==that.p1 && p2==that.p2)||(p1==that.p2 && p2 == that.p1)){
					return true;
				}
			}
			return false;
		}
		//(p1,p2)=(p2,p1);
		public int hashCode(){
			return p1.hashCode()^p2.hashCode();
		}
		public String toString(){
			return p1.toString()+"\t"+p2.toString();
		}
	}
	
	/*
	 * Compute the union of a set of polygons
	 */
	public static SimplePolygon union(SimplePolygon[] polys){
		if(!(polys[0].type() instanceof ScaleType))
			throw new GeomException("Input polygons must be exact");
		ScaleType type = (ScaleType)polys[0].type();
		int n = polys.length;
		ArrayList<IntersectSimplePolygon> pList = new ArrayList<IntersectSimplePolygon>();
		for(int i=0; i<n; i++){
			if(!(polys[i].type() instanceof ScaleType)){
				throw new GeomException("Input polygons must be the same type");
			}
			//System.out.println(polys[i].toMatlab());
			pList.add(new IntersectSimplePolygon(polys[i]));
		}
		//find intersection points; stupid method
		if(stupid){//but if number of polygons is not quite large, stupid is good. O(n^2)
			for(int i=0; i<n; i++){//NOTE pList.length-1 is ok
				for(int j=i+1;j<pList.size(); j++){
					pList.get(i).intersect(pList.get(j));
				}
			}
		}else{//>o(nlog(n))
			List<Collection<IntersectSimplePolygon>> sets = split(pList);
			Iterator<Collection<IntersectSimplePolygon>> setIters = sets.iterator();
			//Cache the result we have computed before, don't recompute and introduce duplicate intersection point
			HashSet<PolygonPair> history = new HashSet<PolygonPair>();
			while(setIters.hasNext()){
				Collection<IntersectSimplePolygon> set = setIters.next();
				IntersectSimplePolygon[] ps = set.toArray(new IntersectSimplePolygon[set.size()]);
				for(int i=0; i<ps.length; i++){
					for(int j=i+1; j<ps.length; j++){
						PolygonPair pair = new PolygonPair(ps[i],ps[j]);
						if(!history.contains(pair)){
							ps[i].intersect(ps[j]);
							history.add(pair);
						}else{
						}
						
					}
				}
			}
		}
		
		//find left most
		IntersectPoint left = pList.get(0).left();
		for(int i=1; i<pList.size(); i++){
			IntersectPoint curr = pList.get(i).left();
			if(curr.compareTo(left)<0)
				left = curr;
		}

		//find union polygon
		List<IntersectPoint> points = new LinkedList<IntersectPoint>();
		IntersectPoint point = left;
		Point pre = Point.create(point.doubleX()-1,point.doubleY());
		Segment currSeg = new Segment(pre,point);
		IntersectSegment nextSeg;
		do{
			if(points.contains(point)){
				throw new NonSimplePolygonException("Infinite loop found, the input or the output polygon is not simple\n");
			}
			points.add(point);
			nextSeg = point.advance(currSeg, true);
			point = nextSeg.advance(point);
			currSeg = nextSeg;
		}while(!point.equals(left));
		return new SimplePolygon(toScale(points,type));
	}

	//Divide-and-Conquer. We don't need to test the intersection of all polygons. 
	//TODO how to remove duplicated pair? Use a hash to record it?
	private static final int minSize = 4;
	private static final double maxFactor = 0.75;
	private static List<Collection<IntersectSimplePolygon>> split(Collection<IntersectSimplePolygon> polys){
		List<Collection<IntersectSimplePolygon>> result = new LinkedList<Collection<IntersectSimplePolygon>>();
		if(polys.size()<=minSize){//NOTE Must have: consider if there is only one polygon, we can't split it and call. This will never return
			result.add(polys);
			return result;
		}
		//split the bounding box
		Iterator<IntersectSimplePolygon> iter = polys.iterator();
		BoundingBox bbox = iter.next().bbox();
		while(iter.hasNext()){
			bbox = bbox.union(iter.next().bbox());
		}
		BoundingBox[] bboxes = bbox.split();
		
		//split polygons
		iter = polys.iterator();
		Set<IntersectSimplePolygon>[] subs = new HashSet[4];//NOTE: for IntersectPolygon it use Object's equals() function. No problem
		for(int i=0; i<4; i++){
			subs[i] = new HashSet<IntersectSimplePolygon>();
		}
		while(iter.hasNext()){
			IntersectSimplePolygon poly = iter.next();
			BoundingBox b = poly.bbox();
			for(int i=0; i<4; i++){
				if(!(bboxes[i].intersect(b)instanceof Empty))
					subs[i].add(poly);
			}
		}
		
		int maxSize = (int)Math.ceil(polys.size()*maxFactor);//NOTE: this factor must be less than 1. Think of identical polygons
		if(subs[0].size()>maxSize || subs[1].size()>maxSize  || subs[2].size()>maxSize || subs[3].size()>maxSize){
			//don't split it
			result.add(polys);
			return result;
		}			
		for(int i=0; i<4; i++){
			if(subs[i].size()<minSize){//NOTE: don't call to save time
				result.add(subs[i]);
			}else{
				result.addAll(split(subs[i]));
			}
		}
		return result;
	}
	
	
	/*
	 * Compute the union of p1 and p2.
	 * Return a new polygon with same vertices if p1 == p2
	 * Return a new polygon with vertices same with p1 if p1 contains p2;
	 * Return a new polygon with vertices same with p2 if p2 contains p1;
	 * Return the leftmost polygon if p1 and p2 disjoint.
	 */
	public static SimplePolygon union(SimplePolygon p1, SimplePolygon p2){
		if(!(p1.type() instanceof ScaleType) || p1.type()!=p2.type() )
			throw new GeomException("Input polygons must be exact and same type");
		ScaleType type = (ScaleType)p1.type();
		//find intersection points
		IntersectSimplePolygon pi1 = new IntersectSimplePolygon(p1);
		IntersectSimplePolygon pi2 = new IntersectSimplePolygon(p2);
		pi1.intersect(pi2);
		
		//find left most point
		IntersectPoint left = pi1.left();
		if(pi2.left().compareTo(left)<0)
			left = pi2.left();
		
		//take a tour and find all points		
		List<IntersectPoint> points = new LinkedList<IntersectPoint>(); 
		IntersectPoint point = left;
		Point pre = Point.create((ScaleNumber)point.x().sub(1), (ScaleNumber)point.x());
		Segment currSeg = new Segment(pre,point);//scale type
		IntersectSegment nextSeg ;
		do{
			if(points.contains(point)){
				throw new NonSimplePolygonException("Infinite loop, the input or the output is not simple polygon\n");
			}
			points.add(point);
			nextSeg = point.advance(currSeg,true);
			point = nextSeg.advance(point);
			currSeg = nextSeg;
		}while(!point.equals(left));		
		return new SimplePolygon(toScale(points,type));
	}
	

	/*
	 * Compute the intersection of p1 and p2.
	 * Return null if intersection is empty, point or segment
	 * The result may not be correct if the intersection is non-simple polygon.
	 */
	public static SimplePolygon intersect(SimplePolygon p1, SimplePolygon p2){
		if(!(p1.type() instanceof ScaleType) || p1.type()!=p2.type() )
			throw new GeomException("Input polygons must be exact and same type");
		ScaleType type = (ScaleType)p1.type();
		
		// Compute intersection points of all segments
		IntersectSimplePolygon pi1 = new IntersectSimplePolygon(p1);
		IntersectSimplePolygon pi2 = new IntersectSimplePolygon(p2);
		pi1.intersect(pi2);		
		List<IntersectPoint> points = new LinkedList<IntersectPoint>();
		
		
		/*
		 * 1. Find an intersection points as the start point. 
		 *   (If can not find, break)
		 * 2. Find a polygon by left-most walking until reach the start point
		 *   (If a loop is found, reset start point and return the loop) 
		 */ 
		IntersectPoint point = pi1.left();
		IntersectPoint start = point;
		Point pre = Point.create((ScaleNumber)point.x().sub(1),(ScaleNumber)point.y());
		IntersectSegment currSeg = new IntersectSegment(new Segment(pre,point));
		List<IntersectPoint> nipts = new LinkedList<IntersectPoint>(); //no-intersect points		
		do{		
			if(!start.isIntersect()){// find an intersection point
				if(point.isIntersect()){
					start = point; 
					points.clear();
					nipts.clear();
				}
				if(!points.isEmpty() && point.equals(start)){// no intersection point found
					break; 
				}
			}else{
				if(points.contains(point)){ // find a loop, reset start
					start = point;
					points.clear();
					nipts.clear();
				}
			}
			points.add(point);
			if(!point.isIntersect()){
				nipts.add(point);
			}
			currSeg = point.advance(currSeg, false);
			point = currSeg.advance(point);	
		}while( !(start.isIntersect()&&point.equals(start)) );
		
		/*
		 * If no intersection point found, it is only possible 
		 *   1) intersect(p1,p2) = null, 2) p1 contains p2 3) p2 contains p1
		 * Otherwise, intersection polygon can be 1) polygon or 2) intersection is point or segments
		 *   2) <==> either p1 or p2 does not contain any point of nipts
		 */
		if(start.isIntersect()){
			if(!nipts.isEmpty()){
				if( !p1.contains(nipts.get(0)) || !p2.contains(nipts.get(0)) )
					return null;
			}
		}else{
			if(p2.contains(p1.point(0)))
				return p1;
			if(p1.contains(p2.point(0)))
				return p2;
			return null;
		}
		if(points.size()<3) // is it possible?
			return null;
		return new SimplePolygon(toScale(points,type));
	}
	

	public static ConvexPolygon intersect(ConvexPolygon p1, ConvexPolygon p2){
		return intersect((SimplePolygon)p1,(SimplePolygon)p2).convexHull();//the result may not be convex because of round off
	}
	
	/*
	 * Reduce the degree of polygon
	 * The reduce function may return polygon with different type  (double)
	 */
	public static SimplePolygon reduce(SimplePolygon p, SimplePolygon.EndCondition ec){
		if(ConvexPolygon.isConvex(p)){
			return reduce((ConvexPolygon)p,ec);
		}else{
			return reduce(p,ec,false,true);
		}
	}
	
	/*
	 * Reduce simple polygon. 
	 */
	public static SimplePolygon reduce(SimplePolygon p, SimplePolygon.EndCondition ec,boolean pointReducible,boolean edgeReducible){
		//try to use double first, if fail, use original type
		SimplePolygon poly;
		try{
			OutwardReduce t = new OutwardReduce(p.specifyType(CohoDouble.type),pointReducible,edgeReducible);
			poly = t.reduce(ec);
		}catch(Exception e){
			OutwardReduce t = new OutwardReduce(p,pointReducible,edgeReducible);
			poly = t.reduce(ec);
		}
		// We can't guarantee the result is simple polygon when edgeReducible=true.
		// TODO: However, it happens rarely, do we need to improve its performance? Coho do not call it frequently.
		// The polygon might be non-simple even if only concave points are removed. Thus we have to check it all the time.
		// I am afraid this make the program slow
		//return	poly = toSimplePolygon(poly);//check and convert to simple polygon
		return poly;
	}
	
	/*
	 * Reduce edge only for convex polygon
	 */
	public static ConvexPolygon reduce(ConvexPolygon p, SimplePolygon.EndCondition ec){
		//try to use double first, if fail, use original type
		try{
			OutwardReduce t = new OutwardReduce(p.specifyType(CohoDouble.type));
			System.out.println(p);
			return new ConvexPolygon(t.reduce(ec),false);//NOTE: don't need to check, reduce will not make a convex polygon to non-convex
		}catch(Exception e){
			OutwardReduce t = new OutwardReduce(p);
			return new ConvexPolygon(t.reduce(ec),false);			
		}
	}
	
	
	public static void main(String[] args){
		Polygon p = new SimplePolygon(
			new Point[]{
					Point.create(0,0),
					Point.create(1,0),
					Point.create(1,1),
					Point.create(0.5,-0.5),
					Point.create(0,1),
			}
		);
		System.out.println(isSimple(p));
//		Polygon p = new SimplePolygon( 
//				 new Point[]{
//					 Point.create(-0.023982335276425463,1.7674655059018052),
//					 Point.create(-0.018420357424696773,1.7674654395643523),
//					 Point.create(-0.018420357424696773,1.7324857044829909),
//					 Point.create(-0.023982323055977358,1.73248570994799),
//					 Point.create(-0.023982323055977358,1.6999542709436428),
//					 Point.create(0.04878131537017482,1.699954185042175),
//					 Point.create(0.048781315370114854,1.699954124011999),
//					 Point.create(0.18317467923187652,1.6999538704031227),
//					 Point.create(0.18317467923187652,1.7324855064025368),
//					 Point.create(0.18317467923187647,1.7674630351496678),
//					 Point.create(0.1831746792318765,1.7999795384209742),
//					 Point.create(0.18317467923187647,1.7999807693018397),
//					 Point.create(0.13008350768146995,1.799981497673149),
//					 Point.create(0.13008350768146995,1.7999802216432979),
//					 Point.create(0.04878131533220974,1.799981267909017),
//					 Point.create(0.048781315324984385,1.7999806621087322),
//					 Point.create(0.03252087692036749,1.7999808535887463),
//					 Point.create(0.03252087692036749,1.799981951888314),
//					 Point.create(-0.018420357424696773,1.7999825212008826),
//					 Point.create(-0.018420357424696773,1.7999814534636098),
//					 Point.create(-0.023982335276425463,1.7999815189604662),
//				}
//				);
//		System.out.println(SimplePolygonOp.canon((SimplePolygon)p));
//		Polygon p = new SimplePolygon( 
//				new Point[]{
//						Point.create(-0.01684758835037611,-0.0038515091496964023),
//						Point.create(0.007202889470751731,-0.0038514709647416855),
//						Point.create(0.0290700237146366,-0.0038514362462824647),
//						Point.create(0.1211637930333708,-0.003851048038039747),
//						Point.create(0.14232313098302543,-0.003850827519256529),
//						Point.create(0.1626523581639976,-0.003850615651737197),
//						Point.create(0.16265235816399762,0.045572070970201224),
//						Point.create(0.0290700237146366,0.0455716777854025),
//						Point.create(0.012499848404113965,0.045571558296088104),
//						Point.create(-0.0014898458213371043,0.04557144583626336),
//						Point.create(-0.01673226911216081,0.04557131331894764),
//						Point.create(-0.01684758835037611,0.025477339274190925),
//				}
//		);
//		p  = p.reduce(new Polygon.CostEndCondition(0.02));
//		System.out.println(p);
		//test union function for lines
//		Point[] points = new Point[]{
//			Point.create(0,0),
//			Point.create(0,1),
//			Point.create(0,2)
//		};
//		SimplePolygon poly1 = new SimplePolygon(points);
//		points = new Point[]{
//				Point.create(0,1),
//				Point.create(0,2),
//				Point.create(0,3)
//		};
//		SimplePolygon poly2 = new SimplePolygon(points);
//		System.out.println(poly1.union(poly2));
		
		
//		ArrayList<SimplePolygon> ps = new ArrayList<SimplePolygon>();
//		int n=3;
//		for(int i=0; i<n; i++){
//			Point[] points = new Point[]{
//					Point.create(coho.common.number.CohoDouble.one.random(),coho.common.number.CohoDouble.one.random()),
//					Point.create(coho.common.number.CohoDouble.one.random(),coho.common.number.CohoDouble.one.random()),
//					Point.create(coho.common.number.CohoDouble.one.random(),coho.common.number.CohoDouble.one.random()),
//					Point.create(coho.common.number.CohoDouble.one.random(),coho.common.number.CohoDouble.one.random()),
//					Point.create(coho.common.number.CohoDouble.one.random(),coho.common.number.CohoDouble.one.random()),
//					Point.create(coho.common.number.CohoDouble.one.random(),coho.common.number.CohoDouble.one.random()),
//					Point.create(coho.common.number.CohoDouble.one.random(),coho.common.number.CohoDouble.one.random()),
//					Point.create(coho.common.number.CohoDouble.one.random(),coho.common.number.CohoDouble.one.random()),
//					Point.create(coho.common.number.CohoDouble.one.random(),coho.common.number.CohoDouble.one.random()),
//					Point.create(coho.common.number.CohoDouble.one.random(),coho.common.number.CohoDouble.one.random())
//			};
//			ps.add(new SimplePolygon(points));
//		}
//		System.out.println(SimplePolygonOp.union(ps));
		

//		Point[] points = new Point[]{
//				Point.create(0,0),
//				Point.create(1,0),
//				Point.create(1,1),
//				Point.create(0,1.1)
//		};
//		SimplePolygon poly1 = new SimplePolygon(points);
//		points = new Point[]{
//				Point.create(0,0),
//				Point.create(1,0),
//				Point.create(1,1),
//				Point.create(0,1)
//		};
//		SimplePolygon poly2 = new SimplePolygon(points);
//		SimplePolygon poly = SimplePolygonOp.union(poly1, poly2);
//		System.out.println(poly);
//		poly = SimplePolygonOp.intersect(poly1, poly2);
//		System.out.println(poly);
//		LinkedList<SimplePolygon> list = new LinkedList<SimplePolygon>();
//		list.add(poly1);
//		list.add(poly2);
//		list.add((SimplePolygon)poly);
//		poly = SimplePolygonOp.union(list.toArray(new SimplePolygon[list.size()]));
//		System.out.println(poly);
//		
//		ArrayList<ConvexPolygon> polys = new ArrayList<ConvexPolygon>();
//		int n = 3;
//		for(int i=0; i<n; i++){
//			BoundingBox box;
//			//bottom
//			box = BoundingBox.create(Point.create(i,0), Point.create(i+1,1));
//			polys.add(new ConvexPolygon(new Point[]{box.ll(),box.lr(), box.ur(), box.ul()}));
//			
//			//right
//			box = BoundingBox.create(Point.create(n-1,i), Point.create(n,i+1));
//			polys.add(new ConvexPolygon(new Point[]{box.ll(),box.lr(), box.ur(), box.ul()}));
//			
//			//top
//			box = BoundingBox.create(Point.create(i,n-1), Point.create(i+1,n));
//			polys.add(new ConvexPolygon(new Point[]{box.ll(),box.lr(), box.ur(), box.ul()}));
//
//			//left
//			box = BoundingBox.create(Point.create(0,i), Point.create(1,i+1));
//			polys.add(new ConvexPolygon(new Point[]{box.ll(),box.lr(), box.ur(), box.ul()}));
//		}
//		System.out.println("begin");
//		SimplePolygon polym = SimplePolygonOp.union(polys.toArray(new SimplePolygon[polys.size()]));
//		System.out.println(polym);
	}

}
