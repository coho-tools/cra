package coho.geom.twodim;

import coho.common.number.*;
import coho.geom.*;
/*
 * The class is based on CohoNumber. 
 * CONSIDER: 
 * template? I don't like it because java 5 does not support very well. 
 * But use CohoNumber, we have to remember the datatype and cast the type.
 * And we can't implement a Point using double!
 * Define an interface? Then how to define the return type? What functions should it have?
 * TRY: Point is an interface, which require x() and cohoX(). We implement CohoPoint and DoublePoint
 * We don't consider the efficience now. Even we DoublePoint, other object has to implement individual function for it.
 * Because we don't know to create a CohoPoint or a DoublePoint. Simplify it.
 * 
 * Usually, to represent a GeomObj2, the ScaleNumber(double, apr) is used. If double is used, the result has round off error. 
 * For interval compuation, double interval might be used.For example, we use double interval to find the intersection of polygon. 
 * If interval value is used, the operation may through out exceptions.
 * 
 * If the input is CohoDouble, the output is also CohoDouble. 
 * If the input is CohoAPR, the result is CohoAPR.
 *  
 */
public class Point implements GeomObj2,Comparable<Point> {
	private final CohoNumber x, y;
	private final CohoType type;//the representation of Point
	public double doubleX(){return x.doubleValue();}
	public double doubleY(){return y.doubleValue();}
	public CohoNumber x(){return x;}
	public CohoNumber y(){return y;}
	public CohoType type(){return type;}
	
	/*
	 * The input Point should be ScaleNumber. xx and yy should be the same type.
	 * Howerver, boolean, integer, long is not recommended to represent a point
	 * Usually, double or apr is used by user. Interval is used for internal computation.
	 */
	public Point(ScaleNumber xx, ScaleNumber yy){
		if(xx.type()!=yy.type())
			throw new GeomException("x and y must be the same type");
		x = xx; y = yy;
		type = xx.type();
	}
	public Point(Number xx, Number yy){
		x = CohoDouble.create(xx);
		y = CohoDouble.create(yy);
		type = CohoDouble.type;
	}
	/*
	 * The point is represented by interval for internal computation.
	 */
	protected Point(CohoNumber xx, CohoNumber yy){
		if(xx.type()!=yy.type())
			throw new GeomException("x and y must be the same type");
		x = xx; y = yy;
		type = xx.type();
	}	
	/*
	 * clone
	 */
	public Point(Point p){
		x = p.x();
		y = p.y();
		type = p.type;
	}
	
	public static Point create(ScaleNumber x, ScaleNumber y){
		return new Point(x,y);
	}
	public static Point create(Number x, Number y){
		return new Point(x,y);
	}
	protected static Point create(CohoNumber x, CohoNumber y){
		return new Point(x,y);
	}
	public static Point create(Point p){
		return new Point(p);
	}
	
	/*******************************************
	 * Operations doesn't depend on representation 
	 *******************************************/
	public BoundingBox bbox(){
		return new BoundingBox(this,this);
	}
	public Point negate(){
		return new Point(x.negate(),y.negate());
	}
	public Point translate(Point offset){
		return new Point(x.add(offset.x()), y.add(offset.y()));
	}
	public Point transpose(){
		return new Point(y,x);	
	}
	public Point min(Point that){ 
		return (compareTo(that)<=0)?this:that;
	}
	public Point max(Point that){ 
		return (compareTo(that)<=0)?that:this;
	}

	/*
	 * Compared by the order of x() and y(). 
	 * For internal usuage. NonComparableException might by thrown if DoubleInterval is used.
	 * Exact result for ScalType, exception might be thrown for interval type
	 */
	public int compareTo(Point p){
		int cmp = x.compareTo(p.x());
		//System.out.println(new CohoAPR(y)+" "+p.y()+" "+y.compareTo(p.y()));
		if(cmp!=0) return cmp;
		return y.compareTo(p.y());	
	}	
	@Override
	public boolean equals(Object other){
		try{
			return (other instanceof Point) && (compareTo((Point)other) ==0);
		}catch(ArithmeticException e){
			return false;
		}
	}
	@Override//if a euqals b, their hashCode should be the same
	public int hashCode(){
		Long xl = Double.doubleToLongBits(x.doubleValue());
		Long yl = Double.doubleToLongBits(y.doubleValue());
		return (int)((xl^(xl>>32))^(yl^(yl>>32)));
	}
	public GeomObj2 intersect(GeomObj2 g) {
		Point that;
		if(g instanceof Point) {
			that = (Point)g;
			if(this.equals(that)) 
				return(this);
			else 
				return(Empty.instance());
		}else{
			return g.intersect(this);
		}
	}
	public GeomObj2 union(GeomObj2 g){
		if(g instanceof Point){
			if(this.equals(g))
				return this;
			else
				throw new GeomException("We don't have type to represents two points now");
		}else{
			return g.union(this);
		}
	}
	public boolean contains(GeomObj2 g){
		if(g instanceof Point && this.equals(g))
				return true;
		return false;
	}

	public String toString(){
		return("(" + x + ", " + y + ")"); 
	}

	/***********************************************
	 * Operations depends on the data representation.
	 ***********************************************/	
	public Point specifyType(CohoType type){
		if(this.type == type){
			return this;
		}else
			return new Point(type.zero().convert(x),type.zero().convert(y));
	}
	public double maxError(){
		if(type instanceof ScaleType)
			return 0;
		else
			return x.error().max(y.error()).doubleValue();
	}
//	public Point round(){
//		if(type instanceof ScaleType)
//			return this;
//		else{
//			switch(roundMode){
//			case MIDDLE:
//				return new Point(x().doubleValue(),y().doubleValue());
//			default: 
//				throw new UnsupportedOperationException();	
//			}
//		}
//	}
	private static int baseTurn(Point a, Point b, Point c){
		CohoNumber ax = a.x(), ay = a.y();
		CohoNumber bx = b.x(), by = b.y();
		CohoNumber cx = c.x(), cy = c.y();
		CohoNumber det1 = ax.mult(by).add(bx.mult(cy)).add(cx.mult(ay));//ax*by+bx*cy+cx*ay
		CohoNumber det2 = ax.mult(cy).add(bx.mult(ay)).add(cx.mult(by));//ax*cy+bx*ay+cx*by
		return det1.compareTo(det2);//det1-det2 is the area. left turn if area  is positive. 		
	}
	private static int hybridTurn(Point a, Point b, Point c){
		//For double, we don't want to have incorrect answer because of round off error.
		if( (a.type==CohoDouble.type || b.type==CohoDouble.type || c.type==CohoDouble.type)&&
			((a.type instanceof ScaleType) && (b.type instanceof ScaleType) && (c.type instanceof ScaleType) )   ){
			try{
				return baseTurn(a.specifyType(DoubleInterval.type), b.specifyType(DoubleInterval.type),c.specifyType(DoubleInterval.type));
			}catch(ArithmeticException e){
				return baseTurn(a.specifyType(CohoAPR.type), b.specifyType(CohoAPR.type),c.specifyType(CohoAPR.type));
			}			
		}else{//Other scaleNumber don't have round off error. For Interval, thrown exception.
			return baseTurn(a,b,c);
		}
	}
	/*
	 * For scalePoint only. If it is interval, an exception might be thrown.
	 */
	public static boolean isLeftTurn(Point a, Point b, Point c){
		return hybridTurn(a,b,c)>0;
	}
	public static boolean isRightTurn(Point a, Point b, Point c){
		return hybridTurn(a,b,c)<0;
	}
	public static boolean isStraight(Point a, Point b, Point c){
		return hybridTurn(a,b,c)==0;//CONSIDER: use apr directly. for interval?
		//return baseTurn(a.specifyType(CohoAPR.type),b.specifyType(CohoAPR.type),c.specifyType(CohoAPR.type))==0;
	}
	public static void main(String[] args){
		//test isLeftTurn function
		Point prev = Point.create(0.16371036990305246, 1.7389774209497815);
		Point curr = Point.create(0.0029457080119602882, 1.7389774209497815);
		Point next = Point.create(0.0029457080119602882, 1.7389774209497815);
		System.out.println(Point.isLeftTurn(prev,curr,next));
		prev = Point.create(1.66445161479146, -0.008669116560033668);
		curr = Point.create(1.6644516147914503, -0.008669116560033698);
		next = Point.create(1.66445161479146, -0.008669116560033666);
		System.out.println(Point.isRightTurn(prev,curr,next));
		prev = Point.create(1.6536368679227966, -0.0025646614821700506);
		curr = Point.create(1.7646512836249164, -0.0026380383467429725);
		next = Point.create(1.8016560888589563, -0.002662497301600613);
		System.out.println(Point.isRightTurn(prev,curr,next));
	}
}
