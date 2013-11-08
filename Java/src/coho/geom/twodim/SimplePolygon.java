package coho.geom.twodim;

import java.util.*;

import coho.geom.twodim.ops.*;
import coho.common.number.*;
import coho.geom.*;
/**
 * Simple Polygons, can be represented as points.
 * The polygon can't be modified. See PolygonBuffer to add points
 * The input polygon is exact, even if the Point is represented as double. 
 * The output polygon point might be apr or interval. 
 * The interval computation will use interval for performance and apr for accuracy. 
 * See SimplePolyogonOp in ops package for details. 
 *  
 * The input should not be interval, otherwise, exception might be thrown. 
 * The interval representation is used only for internal computation. 
 */
public class SimplePolygon implements Polygon {
	protected final Point[] points;
	protected final CohoType type;
	public CohoType type(){
		return type;
	}
	//CONSIDER: Is it safe? It might be changed outside 
	public SimplePolygon(Point[] points){
		this(points,true);
	}
	public SimplePolygon(Collection<Point> points){
		this(points.toArray(new Point[points.size()]));
	}
	public SimplePolygon(Collection<Point> points, boolean check){
		this(points.toArray(new Point[points.size()]),check);
	}
	public SimplePolygon(SimplePolygon p){
		this(p.points,false);
	}
	//TODO: not check?
	public SimplePolygon(Iterator<Point> iter){
		ArrayList<Point> p = new ArrayList<Point>();
		type = p.get(0).type();
		while(iter.hasNext()){
			Point pt = iter.next();
			if(type!=pt.type())
				throw new GeomException("Type for polygon are not the same");
			p.add(pt);
		}
		points = p.toArray(new Point[p.size()]);
	}
	//NOTICE: the points should be anti-clock-wise
	//if check=false, we assign points directly, it's much faster and efficient.
	//However, we should make sure 1)points is never changed outside 2)polygon is valid
	protected SimplePolygon(Point[] points, boolean check){
		if(!check){
			this.points = points;//NOTE: points can't be changed outside. 
			this.type = points[0].type();
		}else{
			//if(points==null || points.length==0){
			if(points==null || points.length <1 ){
				//throw new GeomException("null polygon");
				this.points = new Point[0];
				this.type = CohoDouble.type; //NOTE Let matlab handle it
			}else{
				//Use hashset rather than contain() of ArrayList to save time
				HashSet<Point> set = new HashSet<Point>();
				ArrayList<Point> list = new ArrayList<Point>();
				this.type = points[0].type();
				for(int i=0; i<points.length; i++){
					if(set.add(points[i])){//copy and remove duplicated points
						if(type!=points[i].type())
							throw new GeomException("Type for polygon is not the same");
						list.add(points[i]);
					}else{
						//System.out.println("repeated point "+points[i]+"found");
					}
				}
				if(list.size()<3){
					throw new GeomException("A polygon must have at least three vertices");
				}
				this.points = list.toArray(new Point[list.size()]);
			}
		}
	}
	
	//NOTICE: the direction of segment must be anti-clock-wise.
	public SimplePolygon(Segment[] segs){
		Point curr;
		try{
			this.type = segs[0].type();
			Hashtable<Point,Point> map  = new Hashtable<Point,Point>();
			for(int i=0; i<segs.length; i++){
				if(type!=segs[i].type())
					throw new GeomException("type for polygon is not the same");
				map.put(segs[i].p(0), segs[i].p(1));
			}
			points = new Point[segs.length];
			curr = segs[0].p(0);
			for(int i=0; i<segs.length;i++){
				points[i] = curr;
				curr = map.get(curr);
			}
		}catch(Exception e){
			throw new GeomException("The polygon is invalid");
		}
		if(!curr.equals(segs[0].p(0))){
			throw new GeomException("The polygon is not closed up");
		}
	}
	
	public int degree(){
		return points.length;
	}
	public Point[] points(){
		return points.clone();
	}
	public Point point(int i){//i should be 0-degree()-1
		return points[i];
	}
	public Segment[] edges(){
		Segment[] edges = new Segment[points.length];
		for(int i=0; i<points.length; i++){
			edges[i] = Segment.create(points[i], points[(i+1)%points.length]);
		}			
		return edges;
	}
	public Segment edge(int i){
		return Segment.create(points[i], points[(i+1)%points.length]);
	}
	
	/**
	 * Similar with points, but it start from the ll point;
	 * TODO: return ordered points and edges
	 */
	public Point[] vertices(){
		int llIndex = llPos(), n = points.length;
		ArrayList<Point> list = new ArrayList<Point>();
		for(int i=0; i<n; i++){
			list.add(points[(i+llIndex)%n]);
		}
		return list.toArray(new Point[list.size()]);
	}
	
	public int llPos(){
		int llIndex = 0, n = points.length;
		Point ll = points[0];
		for(int i=1; i<n; i++){
			if(points[i].compareTo(ll)<0){
				ll = points[i];
				llIndex = i;
			}
		}
		return llIndex;
	}
	
	public Point ll(){
		Point ll = points[0];
		for(int i=1; i<points.length; i++){
			if(points[i].compareTo(ll)<0)
				ll = points[i];
		}
		return ll;
	}
	public Point ur(){
		Point ur = points[0];
		for(int i=0; i<points.length; i++){
			if(points[i].compareTo(ur)>0)
				ur = points[i];
		}
		return ur;
	}
	/*
	 * NOTE: The result is not exact, because APR doesn't support sqrt
	 * @see coho.geom.twodim.Polygon#perimeter()
	 */
	public CohoNumber perimeter(){
		CohoNumber perimeter = type.zero();
		for(int i=0; i<points.length; i++){
			CohoNumber x_i = points[i].x();
			CohoNumber y_i = points[i].y();
			CohoNumber x_inc = points[(i+1)%points.length].x();
			CohoNumber y_inc = points[(i+1)%points.length].y();
			CohoNumber dx = x_i.sub(x_inc);
			CohoNumber dy = y_i.sub(y_inc);
			CohoNumber dist = type.zero().convert(Math.sqrt(dx.mult(dx).add(dy.mult(dy)).doubleValue()));
			perimeter = perimeter.add(dist);
		}
		return perimeter;
	}
	//TODO: sign of area? polygon area is 0.5* sum(x_i*y_{i+1}-x_{i+1}*y_i)
	public CohoNumber area(){
		CohoNumber result = points[0].x().zero();//get the type
		for (int i=0; i<points.length; i++){
			CohoNumber x_i = points[i].x();
			CohoNumber y_i = points[i].y();
			CohoNumber x_inc = points[(i+1)%points.length].x();
			CohoNumber y_inc = points[(i+1)%points.length].y();
			result = result.add(x_i.mult(y_inc).sub(x_inc.mult(y_i)));
		}
		return result.div(2);
	}
	
	public boolean isConvex( int i){
		int n = degree();
		return Point.isLeftTurn(point((n+i-1)%n),point(i),point((i+1)%n));
	}
	public boolean isConcave( int i){
		int n = degree();
		return Point.isRightTurn(point((n+i-1)%n),point(i),point((i+1)%n));
	}
	public boolean isFlat( int i){
		int n = degree();
		return Point.isStraight(point((n+i-1)%n),point(i),point((i+1)%n));
	}

	public BoundingBox bbox() {
		CohoNumber minX = points[0].x();
		CohoNumber maxX = points[0].x();
		CohoNumber minY = points[0].y();
		CohoNumber maxY = points[0].y();
		for(int i=1; i<points.length; i++){
			CohoNumber x = points[i].x();
			CohoNumber y = points[i].y();
			minX = minX.min(x);
			maxX = maxX.max(x);//FIXED: maxX.max(y) -> maxX.max(x);
			minY = minY.min(y);
			maxY = maxY.max(y);
		}
		return new BoundingBox(Point.create(minX,minY),Point.create(maxX,maxY));
	}

	public GeomObj2 negate() {
		Point[] p = new Point[points.length];
		for(int i=0; i<points.length; i++){
			p[i] = points[i].negate();
		}
		return new SimplePolygon(p,false);
	}

	public GeomObj2 translate(Point offset) {
		Point[] p = new Point[points.length];
		for(int i=0; i<points.length; i++){
			p[i] = points[i].translate(offset);
		}
		return new SimplePolygon(p,false);
	}

	public GeomObj2 transpose() {
		Point[] p = new Point[points.length];
		for(int i=0; i<points.length; i++){
			p[i] = points[i].transpose();
		}
		return new SimplePolygon(p,false);
	}
	public GeomObj2 intersect(GeomObj2 obj) {
		if(obj instanceof SimplePolygon)
			return intersect((SimplePolygon)obj);
		throw new UnsupportedOperationException();
	}
	public Polygon intersect(Polygon p){
		if(p instanceof SimplePolygon)
			return intersect((SimplePolygon)p);
		throw new UnsupportedOperationException();
	}

	public SimplePolygon intersect(SimplePolygon p) {
		return SimplePolygonOp.intersect(this,p);	
	}

	public GeomObj2 union(GeomObj2 obj) {
		if(obj instanceof SimplePolygon)
			return union((SimplePolygon)obj);
		throw new UnsupportedOperationException();	
	}
	public Polygon union(Polygon p){
		if(p instanceof SimplePolygon)
			return union((SimplePolygon)p);
		throw new UnsupportedOperationException();
	}	
	public SimplePolygon union(SimplePolygon p) {
//		try{
			return SimplePolygonOp.union(this, p);
//		}catch(NonSimplePolygonException e){
//			p = PolygonOp.toSimplePolygon(p,false);
//			return SimplePolygonOp.union(this,p);
//		}
	}
	public Polygon union(Polygon[] p){
		SimplePolygon[] ps = new SimplePolygon[p.length];
		for(int i=0; i<p.length; i++){
			if( !(p[i] instanceof SimplePolygon) ) 
				throw new UnsupportedOperationException();
			ps[i] = (SimplePolygon)p[i];
		}
		return union(ps);
	}
	public SimplePolygon union(SimplePolygon[] p){
		SimplePolygon[] ps = new SimplePolygon[p.length+1];
		for(int i=0; i<p.length; i++){
			ps[i]=p[i];
		}
		ps[p.length] = this;
//		try{
			return SimplePolygonOp.union(ps);
//		}catch(NonSimplePolygonException e){ // put this to Matlab side
//			// if the input is not simple, make the input simple and compute union again. 
//			for (int i=0; i<ps.length; i++){
//				ps[i] = PolygonOp.toSimplePolygon(ps[i]);
//			}
//			return SimplePolygonOp.union(ps);
//		}
	}

	public boolean contains(GeomObj2 g) {
		if(g instanceof Point){
			return contains((Point)g);
		}
		if(g instanceof SimplePolygon){
			return contains((SimplePolygon)g);
		}
		throw new UnsupportedOperationException();	
	}
	
	/*
	 * If this contains the poly, the intersection should be the same polygon with poly. 
	 * However, we don't have a compare function now. So we compare their area.
	 * If they are the same, the area should be the same. 
	 * However, during intersection, round off and canon is performed. 
	 * Therefore, we allow the area can be differ with GeomObj2.eps*poly.area().
	 * NOTE: This might give incorrect result. However, it should not be used by Matlab now. 
	 */
	public boolean contains(SimplePolygon poly){
		return Math.abs(intersect(poly).area().doubleValue()-poly.area().doubleValue())<GeomObj2.eps*poly.area().doubleValue();
	}
	private static  boolean testContains(){
		Point[] points = new Point[]{
				new Point(0,0),
				new Point(1,0),
				new Point(1,1),
				new Point(0,1)
		};
		SimplePolygon poly1 = new SimplePolygon(points);
		points = new Point[]{
				new Point(0,0),
				new Point(1,0),
				new Point(1,1),
				new Point(0.5,1.5),
				new Point(0,1)
		};	
		SimplePolygon poly2 = new SimplePolygon(points);
		if(poly1.contains(poly1)!=true){
			System.out.println("poly1.contains(poly1) error");
			return false;
		}
		if(poly2.contains(poly1)!=true){
			System.out.println("poly2.contains(poly1) error");
			return false;
		}
		if(poly1.contains(poly2)==true){
			System.out.println("poly2.contains(poly1) error");
			return false;
		}
		return true;
	}
	/*
	 * Given a point, we find all segments that below it. 
	 * If there are odd number of segments, it's in the polygon. 
	 * Otherwise, it's outside.
	 * NOTE For example, poly = [0,1,2,1;0,-1,0,1] pt = [0,10] 
	 * FIXED: If p.x == pp.x, where pp is one vertex of the polygon.
	 * Only the segment whose right point is pp is counted 
	 */
	public boolean contains(Point p){
		//Find all segment below it.
		int numBelow = 0;
		CohoNumber pos = p.x();
		for(int i=0; i<degree(); i++){
			Segment edge = edge(i);
			int cmp1 = edge.left().x().compareTo(pos);
			int cmp2 = edge.right().x().compareTo(pos);
			
			if(cmp1*cmp2<0 || cmp2==0){//possible has segment below
				if(Point.isRightTurn(edge.left(), p, edge.right())){
					//Point.isRightTurn(edge.left(), p, edge.right());
					numBelow++;
				}else if(Point.isStraight(edge.left(), p, edge.right())){
					//edge.left.x==edge.right.x==pos
					int c1 = edge.left().y().compareTo(p.y());
					int c2 = edge.right().y().compareTo(p.y());
					if(c1*c2<=0){
						return true;
					}else if(c1<0){//c2<0
						//FIXED: consider a point (0,1) and a polygon (-1,1)->(0,0)->(0,-1)->(1,0)
						//should not count the (0,0)->(0,-1) edge in
						//numBelow++;
					}else{
						//edge above
					}
				}else{
					//edge above
				}
			}else{//cmp1==0 or cmp1*cmp2>0. no below segment
				if(cmp1==0 && edge.left().equals(p))
					return true;
				//not related
			}
		}
		return numBelow%2==1;
	}
	public static ConvexPolygon convexHull(Point[] points){
		return convexHull(points,false);
	}
	public static ConvexPolygon convexHull(Point[] points, boolean fix){
		//Graham's Algorithm
		final class thetaComparator implements Comparator<Point>{
			Point p;
			boolean cmpLength =false;//sort by the length if angle is equal;
			public thetaComparator(Point p){
				this.p = p;
			}
			public thetaComparator(Point p, boolean cmpLength){
				this.p = p;
				this.cmpLength = cmpLength; 
			}
			//CONSIDER: If they equals, the near one can be removed.
			//DONE: Put the nearest point before. remove outside;
			public int compare(Point p1, Point p2){
				Segment seg1 = Segment.create(p, p1);
				Segment seg2 = Segment.create(p, p2);
				int result =  seg1.compareByAngle(seg2);
				if(result==0 && cmpLength){
					result = seg1.len2().compareTo(seg2.len2());//longer distance is larger
				}
				return result;
			}
		};
		
		int n = points.length;
		if(n<3){
			throw new GeomException("Convex.Hull requires more than 3 points");
		}
		//step1: Let p0 be the point in poly with the minimum y-coordinate
		//       or the leftmost such point in case of a tie.
		Point p0 = points[0];
		for(int i=1; i<n; i++){
			Point p = points[i];
			int cmp = p.y().compareTo(p0.y());
			if(cmp<0 || ((cmp==0)&&(p.x().compareTo(p0.x())<0)) ){
				p0 = p;
			}
		}

		//step2: sort all points by the angle
		//CONSIDER: what if there are duplicated p0 in the polygon. 
		//The sort will put them in the beginning of the array.
		//DONE: We modify SimplePolygon and make sure there is no repeated point
		//NOTE The sort function sort the points by angle, if tie, sort by the distance to p0;
		Arrays.sort(points, 0, n, new thetaComparator(p0,true));

		//NOTE: sort by angle only and remove corresponding points.
		Comparator<Point> cmp = new thetaComparator(p0);
		ArrayList<Point> ptsList = new ArrayList<Point>(n);
		//ptsList.add(p0);//p0 is the first one
		//FIXED if p0==p1(p0 and p1 are the same points). We should remove p1/p0
		for(int i=0; i<n;i++){//the last one must be kept
			if(cmp.compare(points[i], points[(i+1)%n])!=0){
				ptsList.add(points[i]);
			}else{//larger distance point has larger index
			}
		}
		n = ptsList.size();
		
		points = ptsList.toArray(new Point[n]);
		if(n<3){
			if(fix){
				if(n==1){
					Point pt = points[0];
					double ulpx = GeomObj2.eps, ulpy = GeomObj2.eps;
					return new ConvexPolygon(new Point[]{
							Point.create((ScaleNumber)pt.x().sub(ulpx), (ScaleNumber)pt.y().sub(ulpy)),
							Point.create((ScaleNumber)pt.x().add(ulpx), (ScaleNumber)pt.y().sub(ulpy)),
							Point.create((ScaleNumber)pt.x().add(ulpx), (ScaleNumber)pt.y().add(ulpy)),
							Point.create((ScaleNumber)pt.x().sub(ulpx), (ScaleNumber)pt.y().add(ulpy))},false);
				}
				if(n==2){
					Point u0 = points[0];
					Point u1 = points[1];
					double nx = GeomObj2.eps, ny=GeomObj2.eps;
					return new ConvexPolygon(new Point[] {
							new Point((ScaleNumber)u0.x().add(nx), (ScaleNumber)u0.y().add(ny)),
							new Point((ScaleNumber)u0.x().sub(nx), (ScaleNumber)u0.y().sub(ny)),
							new Point((ScaleNumber)u1.x().sub(nx), (ScaleNumber)u1.y().sub(ny)),
							new Point((ScaleNumber)u1.x().add(nx), (ScaleNumber)u1.y().add(ny)) },false);
				}	
			}else{
				throw new GeomException("Convex.Hull The convex hull is a segment. ");
			}
		}
		
		//step3: start with p0, p1, p2;
		//CONSIDER: what if the first three points are on the same line?
		//because we didn't remove points that has same angle.
		//DONE: find the first three points that are of left turn
		ArrayList<Point> stack = new ArrayList<Point>();
		stack.add(points[0]);
		stack.add(points[1]);
		stack.add(points[2]);
		
		//step4: find left turn point continous
		for(int i=3; i<n; i++){
			Point curr = points[i];
			while(!Point.isLeftTurn(stack.get(stack.size()-2),stack.get(stack.size()-1),curr)){
				stack.remove(stack.size()-1);				
			}
			stack.add(curr);
		}
		return new ConvexPolygon(stack,false);//BUG, duplicated point?
	}
	
	public ConvexPolygon convexHull(){
		return convexHull(points());//FIXED: get a copy of points. ConvexHull will change it
	}
	
	/*
	 *reduce the number of polygon  
	 */

	public SimplePolygon reduce(EndCondition end){
		return SimplePolygonOp.reduce(this, end); 
	}
	public SimplePolygon reduce(EndCondition end, boolean pointReducible, boolean edgeReducible){
		return SimplePolygonOp.reduce(this, end,pointReducible,edgeReducible);
	}


	public SimplePolygon specifyType(CohoType type){
		Point[] p = new Point[points.length];
		for(int i=0; i<points.length; i++){
			p[i] = points[i].specifyType(type);
		}
		return new SimplePolygon(p,false);
	}
	public double maxError(){
		if(type instanceof ScaleType)
			return 0;
		double r = 0;
		for(int i=0; i<points.length; i++)
			r = Math.max(r, points[i].maxError());
		return r;	
	}
	public String toString(){
		String result="polygon(\n";
		for(int i=0; i<degree(); i++){
			result+=points[i].toString()+"\n";
		}
		result+=")";
		return result;
	}
	/*
	 * Output for matlab representation.
	 */
	public String toMatlab(){
		String matlab = "A=[\n";
		for(int i=0; i<degree();i++){
			matlab+="["+points[i].x()+", "+points[i].y()+"];\n";
		}
		matlab+="["+points[0].x()+", "+points[0].y()+"];\n";
		matlab+="];\n";
		matlab+="plot(A(:,1),A(:,2),'r');\n";
		matlab+="hold on;\n";
		matlab+="plot(A(:,1),A(:,2),'ro');\n";
		return matlab;
	}
	/*
	 * output for generate the polygon in java
	 *
	 */
	public String toCode(){
		String code = "Polygon p = new SimplePolygon( \n new Point[]{\n";
		for(int i=0; i<degree();i++){
			code+="\t Point.create("+points[i].x()+","+points[i].y()+"),\n";
		}
		code+="}\n);";
		return code;
	}

	private static void testConvexHull(){
		Point[] points = new Point[]{
				 Point.create(1.6229201851279313,0.14368794220087083),
				 Point.create(1.6236036901988011,0.07972214524996689),
				 Point.create(1.6237012376474962,0.07059317084694108),
				 Point.create(1.6240917639072787,0.03404578516997621),
				 Point.create(1.624482290167061,-0.002501600506988655),
				 Point.create(1.6536368679227966,-0.0025646614821700506),
				 Point.create(1.7646512836249164,-0.0026380383467429725),
				 Point.create(1.773102795477337,-0.0026436245171219215),
				 Point.create(1.8016560888589563,-0.002662497301600613),
				 Point.create(1.801646682173024,0.03809911645212569),
				 Point.create(1.8016372754870917,0.07886073020585199),
				 Point.create(1.8016278688011593,0.11962234395957831),
				 Point.create(1.8016137259743965,0.13804238147823192),
				 Point.create(1.7587434614069706,0.13928081780747692),
				 Point.create(1.7158731968395449,0.1405192541367219),
				 Point.create(1.673002932272119,0.1417576904659669),
				 Point.create(1.6526409326368703,0.14280623623956057),
			};

		Polygon p = new SimplePolygon(points);
		System.out.println(p.convexHull());
		System.out.println(Point.isRightTurn(points[6], points[7], points[8]));
		System.out.println(Point.isRightTurn(points[4], points[7], points[8]));
	}
	public static void main(String[] args) {
		//testConvexHull();
		if(testContains())
			System.out.println("Polygon contains test pass");
		else
			System.out.println("Polygon contains test fail");
		Polygon p = new SimplePolygon( 
				 new Point[]{
					 Point.create(1.8034782608695654,-0.006521739130434764),
					 Point.create(1.8067391304347828,-0.003260869565217382),
					 Point.create(1.81,0.0),
					 Point.create(1.8066666666666666,0.003333333333333333),
					 Point.create(1.8033333333333335,0.006666666666666666),
					 Point.create(1.8000000000000003,0.009999999999999854),
					 Point.create(1.7966666666666669,0.006666666666666541),
					 Point.create(1.7933333333333334,0.0033333333333332273),
					 Point.create(1.79,-8.673617379883982E-17),
					 Point.create(1.7933333333333334,-0.0033333333333333765),
					 Point.create(1.7966666666666666,-0.006666666666666666),
					 Point.create(1.8,-0.01),
				}
				);
		Point pt = Point.create(1.7933333333333334, 0.0022019624834348937);
		System.out.println(p.contains(pt));
		p = new SimplePolygon( 
				 new Point[]{
					 Point.create(0.08011710131256577, 0.04903523390302579),
					 Point.create(0.08011710131256577, 0.009354639933431186),
					 Point.create(0.08011710131256577, 0.009354639933431186),
					 Point.create(0.08011710131256577, 0.04903523390302579),
					 Point.create(0.09284457463997592, 0.04903523390302579),
					 Point.create(0.08011710131256577, 0.04903523390302579),
					 Point.create(0.09284457463997592, 0.04903523390302579),
				}
				);
		System.out.println(p.convexHull());

//		Point[] points = new Point[]{
//				Point.create(0,0),
//				Point.create(0,0),
//				Point.create(1,0),
//				Point.create(2,0),
//				Point.create(2,2),
//				Point.create(1,1),
//				Point.create(0,2)
//		};
//		SimplePolygon poly  = new SimplePolygon(points);
//		System.out.println(poly.area());
//		System.out.println(5%5);
//		System.out.println(poly.degree());
//		System.out.println(points.length);
//		System.out.println(poly.convexHull().toString());
//		
//		points = new Point[]{
//				Point.create(1.859283768874234, 7.407898085843028e-4),
//				Point.create(1.859283768874234, 0.08345953842352824),
//				Point.create(1.6549912648912495, 0.08345953842352823),
//				Point.create(1.6549912648912495, 7.407898085843034E-4)
//		};
//		poly = (new SimplePolygon(points)).convexHull();
//		System.out.println(poly.area());
//		System.out.println(Double.MAX_VALUE);
	}

}
