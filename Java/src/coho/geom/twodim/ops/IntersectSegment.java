package coho.geom.twodim.ops;

import java.util.*;
import coho.geom.twodim.*;
import coho.common.number.*;

/**
 * Data structure to store all intersection ponint on a segment. 
 * It is created for each edge of polygon when construct a PolygonIntersect
 * The endpoint is exact value from intput. It stores a list of IntersectPoints.
 * The points are not sorted at first, but might be sorted if it's on the union/intersect polygon.
 * 
 * The compareTo() equals() and hashCode() are the default one from Segment. Therefore a.equals(b) doesn't 
 * imply a and b are the same IntersectSegment, because they may belongs to different polygons. 
 * DON'T USE THE SEGMENT OPERATIONS BECAUSE IT'S FOR SEGMENT UNLESS YOU KNOW THIS. 
 * For ConvexPolygonOps, a IntersectSegment is created per edge per polygon. There is no duplicated,
 * so a==b can be used to test if they are the same IntersectionSegment.
 * 
 * It's not recommended to use this class outside the package. 
 * Unless you know the details or not use Set, Hash or Sorted data structure. 
 * See IntersectPoints for details.       
 */
class IntersectSegment extends Segment{
	/***************************************
	 * Constructor and basic functions
	 ***************************************/
	//NOTE: We don't want to sort the points. Because the number of points is small. And we only need equals 
	//for getAllSegment() function and sort for next() function(usually most twice for each segment). Make it simple.
	private Collection<IntersectPoint> points;//A list of intersection points, don't need to sort it.
	private List<IntersectPoint> sortedPoints=null;//Dont' use *Set, the equals() function is for Point

	public IntersectSegment(Segment seg){
		super(seg.p(0),seg.p(1));
		//FIXED HashSet->LinkedList. Don't use HashSet, we don't want to remove duplicated point which has different segments
		points = new LinkedList<IntersectPoint>();
	}	
	public void add(IntersectPoint p){
		points.add(p);
	}
	
	/***************************************
	 * Functions to find union/intersect polygon
	 * 1) find all segment through the current point
	 * 2) find the left/right most segment and turn to that edge
	 * 3) go to the next point with different coordiante  
	 **************************************/
	
	/***********************************************
	// NOTE: Here, we use Set to removed the duplicated Segment which depends on the hashCode() and equals() functions.
	// The equals function is the default one for Segment. So it return true for segments with same endpoints even if they belongs to different polgyons.
	// This lost intersect segments. However, these segments are on the same position (intersection point are identical), it doesn't matter which to go in the next step. 
	// Every such "equals" segments has intersection points with other "equal" segments. We can go to others as needed.
	// Therefore, we don't need to override equals() function as this==obj. But keep in mind it removes not "identical" segment. 
	// Or we can redefine the interface of the function. 
    ************************************************/
	// Return all segments throught the points. 
	// If segment has same position, they are combined even though they belong to different polygon  
	// It first find all points that have the same coordinate with p. 
	// Then combine the their s1 and s2. 
	public Set<IntersectSegment> getAllSegmentsOfPoint(IntersectPoint p){
		Iterator<IntersectPoint> iter = points.iterator();
		HashSet<IntersectSegment> result = new HashSet<IntersectSegment>();//NOTICE: equals() for Segment 
		while(iter.hasNext()){
			IntersectPoint point = iter.next();
			if(point.dynamicCompare(p)==0){
				//System.out.println("add "+point.segment(0)+" and "+point.segment(1));
				result.add(point.segment(0));
				result.add(point.segment(1));
			}
		}
		return result;
	}
	//Assume there are n segments that intersect on point p. For each segment, it must have n-1
	//intersection points which has the same coordinate.
	//TODO: if it's sorted, n/2 rather than n is required. However, n is small.
	public int getSegmentsNumberOfPoint(IntersectPoint p){
		Iterator<IntersectPoint> iter = points.iterator();
		int result = 0;
		while(iter.hasNext()){
			IntersectPoint point = iter.next();
			if(point.dynamicCompare(p)==0){
				//System.out.println(point+"="+p);
				result ++;
			}
		}
		//System.out.println(result);
		//System.out.println(getAllSegmentsOfPoint(p).size());
		//return result++;
		return result+1;//FIXED: result++ does not change the return value
	}
	/**
	 * return if the segment contains this point
	 */
	public boolean hasPoint(Point pt){
		Iterator<IntersectPoint> iter = points.iterator();
		while(iter.hasNext()){
			IntersectPoint point = iter.next();
			if(point.dynamicCompare(pt)==0)
				return true;
		}
		return false;
	}
	
	public static enum Side{LEFT,RIGHT,SAME,OPPOSITE};
	/*
	 * this is on the left/right/same/opposite side of other
	 * Other segment should be ScaleType; otherwise, exception might be thrown.
	 */
	public Side whichSideOf(Segment other){
		int cmp1 = compareByAngle(other);
		if(cmp1==0)
			return Side.SAME;
		Segment bwd = other.reverse();
		int cmp2 = compareByAngle(bwd);
		if(cmp2==0)
			return Side.OPPOSITE;
		Segment piSeg = Segment.create(Point.create(0,0), Point.create(-1,0));
		int cmp3 = other.compareByAngle(piSeg);
		if(cmp3<0){//other in [0,pi)
			if(cmp1<0 || cmp2>0){
				return Side.RIGHT;
			}else{
				return Side.LEFT;
			}
		}else if(cmp3>0){//other in (pi,2*pi)
			if(cmp1>0 || cmp2<0){
				return Side.LEFT;
			}else{
				return Side.RIGHT;
			}
		}else{//other is pi
			if(cmp1>0){
				return Side.LEFT;
			}else{ 
				return Side.RIGHT;
			}
		}
	}

	// Find the next point with different coordiante on this segment
	// Sort the points first then find it in linear time. 
	public IntersectPoint advance(IntersectPoint here){
		if(here.isFwdEndPoint(this)){
			throw new RuntimeException("can't go further");
		}
		if(sortedPoints==null);
			sort();
		Iterator<IntersectPoint> iter = sortedPoints.iterator();
		IntersectPoint p = null;
//		System.out.println("\n next point of "+here);
		while(iter.hasNext()){
			p = iter.next();
			if(p.compareTo(here)==0){//find the current point
				do{//skip points with idential coordinate
//					System.out.println("skip "+p);
					p = iter.next(); 
				}while(p.compareTo(here)==0);
//				System.out.println("reutrn "+p);
				return p; //return the next point
			}
//			System.out.println("find further "+p);
		}
		throw new RuntimeException("The point:"+here+" is not on the segment:" +toString());
	}

	//Sort the points with anti-clock-wise order.
	//If the segment is -->(lower hull), using ascendComparator; otherwise, use descendComparator.
	//For points with same coordinate, the order doesn't matter.
	private List<IntersectPoint> sort(){
		if(sortedPoints!=null)
			return sortedPoints;
		IntersectPoint[] ps = points.toArray(new IntersectPoint[points.size()]);

		Comparator<IntersectPoint> c ;		
		if(p(1).compareTo(p(0))>0)//--> lower hull, use ascendComparator
			c = IntersectPoint.ascendComparator;
		else//<-- Otherwise, use descendComparator.
			c = IntersectPoint.descendComparator;
		
		Arrays.sort(ps, c);//merge sort
		return sortedPoints = Arrays.asList(ps);
	}
	

	/*****************************
	 * Wrapper function for dynamic intersect and compare operations. 
	 * Use apr if double interval failes.  
	 *****************************/
//	public GeomObj2 intersect(Segment that, boolean exact){
//		if(!exact){
//			try{
//				return specifyType(DoubleInterval.type).intersectSegment(that.specifyType(DoubleInterval.type));
//			}catch(ArithmeticException e){
//				//try apr
//			}
//		}
//		return specifyType(CohoAPR.type).intersectSegment(that.specifyType(CohoAPR.type));
//	}
//	//wrapper of the segment intersection
//	public GeomObj2 intersect(Segment that){
//		return intersect(that,false);
//	}
	//CONSIDER: comment out or not?
//	public int dynamicCompareByAngle(IntersectSegment that){
//		try{
//			return this.compareByAngle(that);
//		}catch(ArithmeticException e){
//						
//		}
//	}
//	public int compareByAngle(Segment that, boolean exact){
//		if(!exact){
//			try{
//				return specifyType(DoubleInterval.type).compareByAngle(that.specifyType(DoubleInterval.type));
//			}catch(ArithmeticException e){
//			}
//		}
//		return specifyType(CohoAPR.type).compareByAngle(that.specifyType(CohoAPR.type));		
//	}
//	@Override
//	public int compareByAngle(Segment that){
//		return compareByAngle(that,false);
//	}

	/*****************************************************
	 * default equals(), hashCode() functions from Segment
	 * See IntersectPoint for more details to use Hash or Sorted data structure  
	 *****************************************************/

	public String toString(){
		return super.toString();
	}
	public String toString(boolean printPoint){
		String result = super.toString();
		if(printPoint){
			Iterator<IntersectPoint> iter = points.iterator();
			while(iter.hasNext()){
				result += "\t "+iter.next().toString();
			}
		}
		return result;	
	}
	public static void main(String[] args){
		Segment seg1 = Segment.create(Point.create(0,0), Point.create(1,-0.000001));
		Segment seg2 = Segment.create(Point.create(0,0), Point.create(-1,0));
		IntersectSegment s1 = new IntersectSegment(seg1);
		IntersectSegment s2 = new IntersectSegment(seg2);
		System.out.println(s1.whichSideOf(s2));
		Segment seg3;
		seg1 = Segment.create(Point.create(1.66445161479146, -0.008669116560033668), Point.create(1.8053715759811138, -0.007013598207290258));
		seg2 = Segment.create(Point.create(1.66445161479146, -0.008669116560033673), Point.create(1.6882645612414269, -0.0067842526641031225));
		seg3 = Segment.create(Point.create(1.66445161479146, -0.008669116560033673), Point.create(1.6658345495217963, 0.019730711944451154));
		Point obj1 = (Point)seg1.specifyType(CohoAPR.type).intersect(seg2.specifyType(CohoAPR.type));
		Point obj2 = (Point)seg1.specifyType(CohoAPR.type).intersect(seg3.specifyType(CohoAPR.type));
		System.out.println(obj1.compareTo(obj2));
		System.out.println(obj1.specifyType(CohoDouble.type).compareTo(obj2.specifyType(CohoDouble.type)));
		System.out.println(obj1);
		System.out.println(obj2);
//		System.out.println(s1.intersect(s2));
//		System.out.println(seg1.baseIntersect(seg2));
//		System.out.println(seg1.specifyType(DoubleInterval.type));
//		System.out.println(seg2.specifyType(DoubleInterval.type));
//		System.out.println(seg1.specifyType(DoubleInterval.type).intersectSegment(seg2.specifyType(DoubleInterval.type)));
	}
}
