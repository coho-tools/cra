package coho.geom.twodim.ops;
import java.util.*;
import coho.common.number.*;
import coho.geom.twodim.*;
import coho.geom.*;

public class PolygonOp {
	/*
	 * This function test if the polygon is simple or not.
	 * TODO: we can split the hull into several increasing hulls. Like in IntersectSimplePolygon.
	 * However, it is not the bottleneck now. 
	 */
	public static boolean isSimple(Polygon poly){
		Segment[] edges = poly.edges();
		for(int i=0;i<edges.length;i++){//length-1
			BoundingBox box1 = edges[i].bbox();
			for(int j=i+2;j<edges.length;j++){
				if(i==0 && j==edges.length-1)
					continue;
				BoundingBox box2 = edges[j].bbox();
				if(!(box1.intersect(box2) instanceof  Empty) && !(edges[i].intersect(edges[j]) instanceof Empty)){
					return false;
				}
			}
		}
		return true;
	}
	

	/*
	 * The function converts a non-simple polygon to simple polygon.
	 * BUG: The method to compute simple polygon is not correct. 
	 * We walk along using the union method. 
	 * An counter example is two triangle connected by a line. 
	 * It will end up with a loop with no return.
	 * TODO: develop a new method to convert any polygon to a simple polygon
	 */ 
	public static SimplePolygon toSimplePolygon(Polygon p) throws GeomException{
		return toSimplePolygon(p,true);
	}
	public static SimplePolygon toSimplePolygon(Polygon p, boolean check) throws GeomException{
		// check if it is simple polygon
		if((check && isSimple(p)) ){
			if(p instanceof SimplePolygon)
				return (SimplePolygon)p;
			else
				return new SimplePolygon(p.points());
		}
		
		/*
		 * Compute the intersection and walk around outside. 
		 */
		int n = p.degree();		
		//compute upper/lower hull;
		int ll=p.llPos(); //left lower point 

		//Create IntersectSegment for each edge.
		IntersectSegment[] edges = new IntersectSegment[n];
		for(int i=0;i<n;i++){
			edges[i] = new IntersectSegment(Segment.create(p.point((n+ll+i)%n), p.point((n+ll+i+1)%n)));
		}
		
		//create IntersectPoint for each point
		IntersectPoint left=null;
		for(int i=0;i<n;i++){
			IntersectSegment pre = edges[(n+i-1)%n];
			IntersectSegment pos = edges[i];
			IntersectPoint pt = new IntersectPoint(p.point((ll+i+n)%n), pre,pos, true);
			if(i==0)
				left = pt;
			pre.add(pt);
			pos.add(pt);
		}
		
		//compute intersection point
		for(int i=0; i<n; i++){
			for(int j=i+2;j<n;j++){
				if(i==0&&j==n-1){//neighbour
					continue;
				}
				IntersectSegment s1 = edges[i];
				IntersectSegment s2 = edges[j];
				GeomObj2 obj = s1.intersectSegment(s2);
				if(obj instanceof Point){
					Point point = (Point)obj;
					//NOTE: if point is appeared before, create method will merge them, s1 and s2 doesn't add an copy because of Set
					IntersectPoint ipt= new IntersectPoint(point, s1, s2, point.type()==CohoAPR.type);
					s1.add(ipt);
					s2.add(ipt);
				}else if(obj instanceof Segment){
					//ASSERT must be APR result. Test
					Segment seg = (Segment)obj;
					IntersectPoint ipt = new IntersectPoint(seg.left(), s1, s2, true);
					s1.add(ipt);
					s2.add(ipt);
					ipt = new IntersectPoint(seg.right(), s1, s2, true);
					s1.add(ipt);
					s2.add(ipt);
				}else{
					//empty
				}				
			}
		}
		
		//walk like union
		List<IntersectPoint> points = new LinkedList<IntersectPoint>(); 
		IntersectPoint point = left;
		Point pre = Point.create((ScaleNumber)point.x().sub(1), (ScaleNumber)point.x());
		Segment currSeg = new Segment(pre,point);//scale type
		IntersectSegment nextSeg ;
		do{
			if(points.contains(point))
				throw new GeomException("Infinite loop found. The polygon has self loop. Debug it. ");
			points.add(point);
			nextSeg = point.advance(currSeg,true);
			point = nextSeg.advance(point);
			currSeg = nextSeg;
		}while(!point.equals(left));		
		return new SimplePolygon(SimplePolygonOp.toScale(points, (ScaleType)p.type()));
	}
	
	/*
	 * During the intersection, interval points are created, convert to scale here.
	 */
	protected static Point[] toScale(IntersectPoint[] points, ScaleType type){
		if(type == CohoAPR.type){
			for(int i=0; i<points.length; i++){
				points[i].eval();
			}
			return points;
		}
		if(type!=CohoDouble.type)
			throw new UnsupportedOperationException();

		
		//step1: If overlap,evaluate exact number. Remove the mis-order caused by interval
		int n = points.length; 
		for(int i=0; i<n; i++){
			for(int j=i+1; j<n; j++){
				if(points[i].isExact() && points[j].isExact())
					continue;//exact for both
				else{
					int cmp = points[i].dynamicCompare(points[j]);//evaluate dynamically.
					if(cmp==0){
						throw new RuntimeException("There are identical points in the input.\n"+
								points[i].toString(true)+" \n"+points[j].toString(true));
					}
				}
			}
		}

		
		//step2: all points comparable now. round off
		Point[] roundedPoints = new Point[n];
		for(int i=0; i<n; i++){
			roundedPoints[i] = points[i].point().specifyType(type);//BUG: change the shape, non-simple polygon
		}

		//step3: deal with the same points caused by round off.
		boolean hasDuplicated = false;
		do{
			hasDuplicated = false;
			for(int i=0; i<n;i++){
				for(int j=i+1; j<n; j++){
					if(roundedPoints[i].equals(roundedPoints[j])){
						//point are the same after round off. evaluate the apr value.
						int cmp = points[i].dynamicCompare(points[j]);
						//re-round off. remove the same points caused by using the middler of interval number
						//NOTE: after step1, all same points are caused by round off.
						//if still equal after evaluating the exact value. Move bigger(make sure no loop).
						//move upward to maintain the order of points. 
						//May cause the case like (0,0)->(0,4)->(0,2)->(0,3)->... But still simplePolygon. 
						//if(roundedPoints[i].equals(roundedPoints[j])){
						if(cmp>0){
							roundedPoints[i] = Point.create(roundedPoints[i].x().doubleValue(),
									roundedPoints[i].y().doubleValue()+DoubleInterval.ulp(roundedPoints[i].y().doubleValue()));
						}else if(cmp<0){
							roundedPoints[j] = Point.create(roundedPoints[j].x().doubleValue(),
									roundedPoints[j].y().doubleValue()+DoubleInterval.ulp(roundedPoints[j].y().doubleValue()));
						}else{
							throw new RuntimeException("Identical points found. Algorithm error somewhere");
						}
						//}
						hasDuplicated = true;//points changed, recheck again
						/*
						 * Deal with only one pair of same point for each loop. 
						 * Make the algorithm easy to understand. The case is rarely. 
						 */
						break;
					}
				}
			}
		}while(hasDuplicated);
		
		//step4 remove very short edge. This may under approximation slightly.(so does step5).
		//But this remove many special case caused by round off. non-simple polygon
		if(n>3){
			ArrayList<Point> r = new ArrayList<Point>(n);
			for(int i=0; i<n; i++){
				Segment temp = new Segment(roundedPoints[(i-1+n)%n], roundedPoints[i]);
				if(temp.len2().doubleValue()> GeomObj2.eps2){
					r.add(roundedPoints[i]);
				}else{
					//combine
				}
			}
			n = r.size();
			roundedPoints = r.toArray(new Point[n]);
			r.clear();
		}

		//step5: remove colinear point
		if(n>2){//2008/04/02, BUG:if it is a segment now, can not reduce to zero points
			ArrayList<Point> r = new ArrayList<Point>(n);
			for(int i=0; i<n; i++){
				if(Point.isStraight(roundedPoints[(n+i-1)%n], roundedPoints[i], roundedPoints[(i+1)%n])){
					//not add
				}else{
					r.add(roundedPoints[i]);
				}			
			}
			n = r.size();
			roundedPoints = r.toArray(new Point[n]);
		}
		
		//step6: special case
		if(n==1){
			Point pt = roundedPoints[0];
			double ulpx = GeomObj2.eps, ulpy = GeomObj2.eps;
			return new Point[]{Point.create((ScaleNumber)pt.x().sub(ulpx), (ScaleNumber)pt.y().sub(ulpy)),
					Point.create((ScaleNumber)pt.x().add(ulpx), (ScaleNumber)pt.y().sub(ulpy)),
					Point.create((ScaleNumber)pt.x().add(ulpx), (ScaleNumber)pt.y().add(ulpy)),
					Point.create((ScaleNumber)pt.x().sub(ulpx), (ScaleNumber)pt.y().add(ulpy))};
		}
		if(n==2){
			Point u0 = roundedPoints[0];
			Point u1 = roundedPoints[1];
			double nx = GeomObj2.eps, ny=GeomObj2.eps;
			return new Point[] {//TODO problem very short edge? misorder?
					new Point((ScaleNumber)u0.x().add(nx), (ScaleNumber)u0.y().add(ny)),
					new Point((ScaleNumber)u0.x().sub(nx), (ScaleNumber)u0.y().sub(ny)),
					new Point((ScaleNumber)u1.x().sub(nx), (ScaleNumber)u1.y().sub(ny)),
					new Point((ScaleNumber)u1.x().add(nx), (ScaleNumber)u1.y().add(ny)) };
		}
		return roundedPoints;		
	}
	protected static Point[] toScale(Collection<IntersectPoint> points, ScaleType t){
		return toScale(points.toArray(new IntersectPoint[points.size()]),t);
	}
	
	/*
	 * Round the point outside
	 */
	private static Point round(Point prev, Point curr, Point next){
		double x = curr.x().doubleValue();
		double y = curr.y().doubleValue();
		for(int hop=0; ;hop++){//BUG: too long time when two segments are nearly parallel
			//System.out.println(hop);
			for(int row=-hop; row<=hop; row++){
				for(int col=-hop; col<=hop; col++){
					if(Math.abs(row)==hop || Math.abs(col)==hop){
						Point p = new Point(x+DoubleInterval.ulp(x)*row,y+DoubleInterval.ulp(y)*col);
						try{
							if(!Point.isRightTurn(prev, p, curr) && !Point.isRightTurn(curr, p, next)){
								return p;
							}
						}catch(Exception e){
							//try next;
						}							
					}
				}
			}
		}
	}
	
}
