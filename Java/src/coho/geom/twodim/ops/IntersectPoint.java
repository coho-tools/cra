package coho.geom.twodim.ops;

import java.util.*;
import coho.geom.*;
import coho.geom.twodim.*;
import coho.common.number.*;

/**
 * A dynamic data structure to represents intersection point of two segments.
 * 
 * The initial value is usually DoubleInterval,(except the vertex and if the intersection is a segment.)
 * This value is stored in the super Point class. It's not changed. EVERY FUNCTION ON POINT IS WORKING ON THE INITIAL VALUE.
 * After the intersection, IntersectPoint may be sorted or compared  to find the union/intersect polygon. 
 * Use dyanmaicCompare() function which will compute the exact value (stored in aprPoint) if needed.
 * 
 * This class is only for this package used by ConvexPolygonOp. It's not recommended to use it out of this package
 * unless you know the details. Therefore I remove the "PUBLIC" identifier from the class.    
 */

/*
 * At first, (see polygonops package if it is not removed); the intersection point is sorted and merged if
 * dyanmaicCompare() return zero. However, there are many difficuties 
 * 
 * 1) To merge the points, we have to find the points with the same coordinate. 
 * If we use list to store all points created, the running time is O(n) which is large.
 * If we use HashSet, we have the hashCode() problem, how to define the hashCode()? Because requires 1)a.compareTo(b)==0 <==> a.equals(b) 
 * ==> a.hashCode()==b.hashCode(). However, here the value is dyanmic, how to define the hashCode()?
 * Mark suggested that we use a static variable for hashCode, but to create a new point, we can't use 
 * assign the hashCode before we search it in the hash, but here hashCode is not avaliable, O(n) is also needed.
 * If we use TreeSet, we can specify Comparator, the search time is O(log(n)). (see polygonops package). Then hashCode is useless. 
 * 
 * Therefore we just throw hashCode. Why sort or merge the intersection point on the intersect step?
 * We only compute the intersection points, store them on the corresponding IntersectSegment, don't sort, don't merge. 
 * To find the union/intersect, we sort/merge as needed. Because the number of points on each segment is expected to small, the 
 * performance is not bad, even better. 
 * 
 * So we extends Point class and used the equals(), hashCode() and compareTo() functions from it. They are
 * consistent with each other and useless(Then why extends it:) 1)more reasonable 2)easy for some small functions). 
 * For intersction, we use dyanamicCompare().
 * 
 */
class IntersectPoint extends Point{
	/**********************
	 * Each intersection point is represented by its initial value (stored in super, double interval usually)
	 * and an exact value aprPoint if needed. The point() function will return the best result. 
	 * The intersection Segments s1 and s2 are stored to compute the aprPoint when needed. 
	 * Points with the same coordinate are not mereged because of 1)It's not easy to find the points 
	 * with same coordinate because of the hash problems.
	 * Our solution is that on the intersection step, we only store all intersection points; on the tranverse
	 * step, the intersect points are sorted and virtually merged by the GetAllSegments() functions. 
	 **********************/
	private Point aprPoint = null;
	private boolean exact=false;
	private final IntersectSegment s1, s2;//point is the intersection of s1 and s2;
	// Return the most exact value of point. 
	public Point point(){
		if(exact)
			return aprPoint;
		else
			return Point.create(x(),y());
	}
	public boolean isExact(){
		return exact;
	}
	public IntersectSegment segment(int i){
		if(i==0)
			return s1;
		else 
			return s2;
	}
	public IntersectPoint(Point p, IntersectSegment s1, IntersectSegment s2, boolean exact){
		super(p.x(),p.y());
		if(exact){//Make sure the representation is APR. Consider the operation result of two exact points with double representation.
			aprPoint = p.specifyType(CohoAPR.type);
			exact = true;
		}
		this.s1 = s1;
		this.s2 = s2;
		//System.out.println("create "+toString()+type());
	}
	public IntersectPoint(Point p, IntersectSegment s1, IntersectSegment s2){
		this(p,s1,s2,false);
	}	
	/*
	 * Because we don't merge points with same coordinate during the intersection step. 
	 * To find the edge for union polygon, we must find all segements through the current point. 
	 * For each IntersectSegment through this point, it must stores all points with the same coordinate.
	 * Because during the intersection step, we intersect each pair of segments and store the intersection
	 * point on each IntersectSegment. Therefore, we can find all points with same coordinate of s1/s2 and 
	 * then combine the set of IntersectSegment, that's the all segments throught this point. See 
	 * IntersectSegment.getAllSegmentsOfPoints.  
	 */
	private Collection<IntersectSegment> getAllSegments(){
		return s1.getAllSegmentsOfPoint(this);		
	}	
	
	
	/*********************************************************
	 * Functions to advance points for tranverse step.
	 *********************************************************/
	/**
	 * return the segment determined by this point and pt.
	 */
	public IntersectSegment getSegment(IntersectPoint pt){
		if(this.dynamicCompare(pt)==0)//can't decide.
			throw new GeomException("IntersectPoint.getSegment: require different points");
		Iterator<IntersectSegment> iter = getAllSegments().iterator();
		while(iter.hasNext()){
			IntersectSegment seg = iter.next();
			if(seg.hasPoint(pt)){
				return seg;
			}
		}
		return null;//did not find
	}
	/**
	 * Find the next points. 
	 * For union, we find the right most segment and advance one point; 
	 * For intersection, find the left most points and advance one point.  
	 */
	public IntersectSegment advance(Segment currSeg, boolean union){
		IntersectSegment nextSeg = null;
		if(union){
			nextSeg = rightMost(currSeg);
		}else{
			nextSeg = leftMost(currSeg);
		}
		return nextSeg;
	}
	// Find the leftmost Segment throught the current point.
	// The segment can't end up with the current point, otherwise it can go further.
	private IntersectSegment leftMost(Segment fwdDir){
		Iterator<IntersectSegment> iter = getAllSegments().iterator();
		IntersectSegment result = null;
		while(iter.hasNext()){
			IntersectSegment candidate = iter.next();
			//System.out.println("consider "+candidate);
			//don't consider segment that point is the end(no next)
			if(isFwdEndPoint(candidate)){//end of segment
				continue;
			}
			if(result == null ){
				IntersectSegment.Side side = candidate.whichSideOf(fwdDir);
				if(side!=IntersectSegment.Side.OPPOSITE)//FIXED: can not go backward, throw exception next
					result = candidate;//or replace with new candidate if OPPOSITE? can't find the empty intersection case
			}else{
				IntersectSegment.Side side = result.whichSideOf(fwdDir);
				IntersectSegment.Side side1, side2;
				switch(side){
				case LEFT:
					side1 = candidate.whichSideOf(result);
					side2 = candidate.whichSideOf(fwdDir);
					if(side1==IntersectSegment.Side.LEFT && side2==IntersectSegment.Side.LEFT)
						result = candidate;
					break;
				case RIGHT:
					side1 = candidate.whichSideOf(result);
					side2 = candidate.whichSideOf(fwdDir);
					if(side1==IntersectSegment.Side.LEFT || side2==IntersectSegment.Side.LEFT)
						result = candidate;
					break;
				case SAME:
					side1 = candidate.whichSideOf(result);
					if(side1==IntersectSegment.Side.LEFT)
						result = candidate;
					break;
				case OPPOSITE:
					throw new RuntimeException("Empty intersection. Can't go backward");
				}
			}
		}
		if(result==null){
			throw new RuntimeException("IntersectPoint: should never happen. " +
					"The point is the intersection of only two segments with opposite direction.");
		}
		return result;
	}
	// Find the rightmost Segment throught the current point.
	// The segment can't end up with the current point, otherwise it can go further.	
	private IntersectSegment rightMost(Segment fwdDir){
//		System.out.println("\nFind the righy most edge of currenpt point "+this.point());
		Iterator<IntersectSegment> iter = getAllSegments().iterator();
		IntersectSegment result = null;
		while(iter.hasNext()){
			IntersectSegment candidate = iter.next();
			//System.out.println("consider "+candidate);
			//don't consider segment that point is the end(no next)
			if(isFwdEndPoint(candidate)){//end of segment
				//System.out.println("This point is the end of "+candidate);
				continue;
			}
			if(result == null){
				//System.out.println(candidate+" is the first try");
				IntersectSegment.Side side = candidate.whichSideOf(fwdDir);
				if(side!=IntersectSegment.Side.OPPOSITE)//FIXED: see leftMost
					result = candidate;
			}else{
				IntersectSegment.Side side = result.whichSideOf(fwdDir);
				//System.out.println(result+" is on the side of "+fwdDir);
				IntersectSegment.Side side1, side2;
				switch(side){
				case LEFT:
					side1 = candidate.whichSideOf(result);
					side2 = candidate.whichSideOf(fwdDir);
					if(side1==IntersectSegment.Side.RIGHT||side2==IntersectSegment.Side.RIGHT){
//						System.out.println(candidate+" replaces ");
						result = candidate;
					}
					break;
				case RIGHT:
					side1 = candidate.whichSideOf(result);
					side2 = candidate.whichSideOf(fwdDir);
					if(side1==IntersectSegment.Side.RIGHT&&side2==IntersectSegment.Side.RIGHT){
//						System.out.println(candidate+" replaces ");
						result = candidate;
					}						
					break;
				case SAME:
					side1 = candidate.whichSideOf(result);
					if(side1==IntersectSegment.Side.RIGHT){
//						System.out.println(candidate+" replaces ");
						result = candidate;
					}
					break;
				case OPPOSITE:
					throw new RuntimeException("Empty intersection. Can't go backward");
				}
			}
		}
		if(result==null){
			throw new RuntimeException("IntersectPoint: should never happen. " +
					"The point is the intersection of only two segments with opposite direction.");
		}
		return result;
	}
	

	// Return true if it's intersection points of different polygons; return false if it's vertex of one polygon
	// This function is used by intersection to find the first intersection point and then begin to tranverse.
	// For a vertex point of only one polygon, it must 1) has only to IntersectSegment 2)the point must be the endpoint
	// of these two IntersectSegments.
	public boolean isIntersect(){
		//If it is not the endpoint of s1 or s2, must be intersection point.
		if(!isEndPoint(s1)||!isEndPoint(s2)){
			return true;
		}
		//If it is the endpoint of s1 and s2, but it has more than 2 intersection segements, it's intersection point
		//System.out.println(s1.getSegmentsNumberOfPoint(this));
		if(s1.getSegmentsNumberOfPoint(this)>2)
			return true;
		//If it is the endpoint of s1 and s2, and has only to intersectio segments, it must be a vertex on only one polgon.
		else
			return  false;
	}
	// forward endpoint, can't go further.
	public boolean isFwdEndPoint(Segment seg){
		return dynamicCompare(seg.p(1))==0;
	}
	public boolean isEndPoint(Segment seg){
		return ((dynamicCompare(seg.p(0))==0) || (dynamicCompare(seg.p(1))==0));
	}
	
	
	/******************************
	 * Compare functions. In the intersection step, we don't care the order of points, but at the tranverse
	 * step, the point should be ordered by their coordinate to find the union/intersect. The points with 
	 * same coordinates are "virtually combined".  
	 ******************************/
	/*
	 * Compare the intersection point by its x-y coordinate. If it's not comparable, re-evaluation
	 * is performed to get the exact value. 
	 */
	public int dynamicCompare(Point that){
		if(that instanceof IntersectPoint){
			return dynamicCompare((IntersectPoint)that);
		}
		try{
			return point().compareTo(that);
		}catch(ArithmeticException e){
			//CONSIDER: avoid loop, Consider divide-by-zero error for apr representation.
			//DONE: But there is no divide computation for compareTo function. Other exception?
//			if(exact)
//				throw e;
			eval();
			return aprPoint.compareTo(that);
		}
	}
	/*
	 * Compare the intersection point by its x-y coordinate. If it's not comparable, re-evaluation
	 * is performed to get the exact value. 
	 */
	public int dynamicCompare(IntersectPoint that){
		int result ;
		try{
			//System.out.println("compare "+this+ "and" + that.point()+" type "+this.type()+that.type());
			result = point().compareTo(that.point());//CONSIDER: Can we merge here? Highly possible, do it later.
			//System.out.println("1 "+result+this.type()+that.type());
		}catch(ArithmeticException e){
			//CONSIDER: avoid loop, Consider divide-by-zero error for apr representation.
			//DONE: But there is no divide computation for compareTo function. Other exception?
//			if(exact && that.exact)
//				throw e;
			eval();
			that.eval();
			result = aprPoint.compareTo(that.aprPoint);
		}
		//System.out.println("result2 "+result);
		return result;
	}
	/*
	 * Recompute the intersection point using APR. This should be called rarely. 
	 */
	public void eval(){
		if(exact)
			return;
		//NOTE: the intersection is impossible to be a segment. 
		//This is because if intersection is segment, APR must be used before.
		//aprPoint = (Point)s1.intersect(s2,true);//compute using APR.
		try{
			aprPoint = (Point)s1.specifyType(CohoAPR.type).intersectSegment(s2.specifyType(CohoAPR.type));//using apr
			exact = true;
		}catch(ClassCastException e){
			throw new RuntimeException("IntersectPoint.eval: it is impossible to happen"+e);
		}
		
	}
	/*
	 * Compare intersection points by their coordinates
	 */
	public static Comparator<IntersectPoint> ascendComparator = new Comparator<IntersectPoint>(){
		public int compare(IntersectPoint p1, IntersectPoint p2){
			return p1.dynamicCompare(p2);
		}
	};
	public static Comparator<IntersectPoint> descendComparator = new Comparator<IntersectPoint>(){
		public int compare(IntersectPoint p1, IntersectPoint p2){
			return p2.dynamicCompare(p1);
		}
	};
	
	/*************************************************************************************
	// NOTE: We use the compareTo() equals() and hashCode() from Point. They satisifies 
	// 1)a.compareTo(b)==0 <==> a.equals(b) and 2) a.equals(b) ==> a.hashCode() = b.hashCode().
	//
	// However, these function are not what we want to use to compute the intersection. 
	// For example, compareTo may throw NotComparableIntervalException if the initial value are 
	// double interval and equals() returns false even their apr value might be the same.
	// Keep in mind, these functions are for the initial value when your create this intersection point
	// it's not for the dynamic value. Use dynamicCompare! 
	//
	// When you use Set, you should notice the definiation of equals. For Point a.equals(b) means
	// a and b has the same coordination with scale number representation(double apr). However,
	// for IntersectPoint, we don't think a.equals(b) if the segments produce it are different even
	// thought a and b have same coordinate. a.equals(b) <==> their exact value are the same && intersect 
	// segments are the same(same coordinate, same polygon). Usually we can use a==b because during the 
	// intersection, there is no "duplicated" IntersectPoint produced. In conclusion, DON'T USE SET
	// during the intersection step, which will lost IntersectionPoint. 
	//
	// When you want to sort them, be clear which compare function you want to use. For intersection compuation,
	// we want to use dynamicCompare(). For example, if you use TreeSet, pass ascendComparator or descendComparator to it. 
	// However, DONT'T USE SET operation because the order is not consistent with the equals(). The equals() may returns
	// false even though their a.dynamicCompare(b)==0; and verse visa. 
	//
	// Similar problem for IntersectSegment. The good news is 1)initial value for segment endpoints
	// are from input and thus exact, which are not changed dynamically; 2)for each intersection point, there are only two 
	// IntersectSegment s1 and s2, we don't need to sort them or compare them. What we need is 1)compareByY() used in IntersectPolygon 
	// to find segments for intersection; 2) compareByAngle() by right()/left() to find the union edge at the last. 
    *************************************************************************************/
	
	
	
	
	public String toString(){
		return point().toString();
	}
	public String toString(boolean printSegment){
		String result = toString()+"\n";
		if(printSegment)
			result += "\t" + ((Segment)s1).toString() + "\t" + ((Segment)s2).toString();
		return result;
	}	
}
