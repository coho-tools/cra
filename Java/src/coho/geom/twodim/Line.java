package coho.geom.twodim;

import coho.common.number.*;
import coho.geom.*;

/*
 * A line represented as Ax+By+C=0;
 * It can be used to represents a hyper-plane used by lp-project. Then (A,B) is the norm, -C is the value.
 * 
 * A hyper-plane or a 2d-line. It is represented as the norm and the value. 
 * Of course, slope and a point can be used. However, it's for lp-project now.
 * Ax + By = value  
 */
public class Line implements GeomObj2 {
	private final CohoNumber A, B, C;
	private final CohoType type;
	public CohoNumber A(){
		return A;
	}
	public CohoNumber B(){
		return B;
	}
	public CohoNumber C(){
		return C;
	}
	public CohoType type(){
		return type;
	}
	public Line(CohoNumber A, CohoNumber B, CohoNumber C){
		if(A.type()!=B.type()||A.type()!=C.type())
			throw new GeomException("Data type used for A,B,C of Line are not the same");
		this.A = A;
		this.B = B;
		this.C = C;
		this.type = A.type();		
	}
	public Line(ScaleNumber A, ScaleNumber B, ScaleNumber C){
		this((CohoNumber)A,(CohoNumber)B,(CohoNumber)C);
	}
	public Line(double A, double B, double C){
		this(CohoDouble.create(A),CohoDouble.create(B),CohoDouble.create(C));
	}
	//used for lp-project: a plane is represented by norm and value
	//alpha should be non-negative 
	public Line(int quad, ScaleNumber alpha, ScaleNumber value){
		if(alpha.type()!=value.type())
			throw new GeomException("Type for norm and value of Line must be the same");
		if(quad<0)
			throw new GeomException("quad must be non-negative");
		ScaleNumber x=null, y=null;
		switch(quad%4){
		case 0: 
			x = alpha.one();
			y = alpha;
			break;
		case 1: 
			x = alpha.negate();
			y = alpha.one();
			break;
		case 2:
			x = alpha.one().negate();
			y = alpha.negate();
			break;
		case 3:
			x = alpha;
			y = alpha.one().negate();
			break;
		default:
			throw new GeomException("quad must be non-negative");	
		}
		A = x;
		B = y;
		C = value.negate();
		type = value.type();
	}
	public Line(int quad, double alpha, double C){
		this(quad, CohoDouble.create(alpha), CohoDouble.create(C));
	}

	//for interval lp-project
	public boolean similar(Line p){
		double theta = Math.atan2(B.doubleValue(),A.doubleValue());
		double angle = theta>=0?theta:2*Math.PI+theta;
		double ptheta = Math.atan2(p.B.doubleValue(),p.A.doubleValue());
		double pangle =  ptheta>=0?ptheta:2*Math.PI+ptheta;

		double norm = Math.sqrt( A.mult(A).add(B.mult(B)).doubleValue() );
		double pnorm = Math.sqrt( p.A.mult(p.A).add(p.B.mult(p.B)).doubleValue() );	
		
		return (Math.abs(angle-pangle)<eps) && 
		       (Math.abs(C.doubleValue()/norm - p.C.doubleValue()/pnorm)<eps); 
	}
	
	public String toString(){
		return  "The line is "+A()+"x+"+B()+"y+"+C()+"=0";
//		str = "The norm of the face is ["+A+","+B+"]";
//		str += "the value is "+C;
	}
	
	public Line specifyType(CohoType type){
		return new Line(type.zero().convert(A),type.zero().convert(B),type.zero().convert(C));
	}
	
	public double maxError(){
		if(type instanceof ScaleType)
			return 0;
		else
			return Math.max(A.error().max(B.error()).doubleValue(),C.error().doubleValue());
	}
	//this function is not useful
	public BoundingBox bbox() {
		return new BoundingBox(Point.create(Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY),
				Point.create(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
	}

	public boolean contains(GeomObj2 g) {
		throw new UnsupportedOperationException();
	}

	/**
	 * return the intersection of current Line and g. The result is represented as the same type of this line. 
	 * @see coho.geom.twodim.GeomObj2#intersect(coho.geom.twodim.GeomObj2)
	 */
	public GeomObj2 intersect(GeomObj2 g) {
		if(g instanceof Line){
			//return the same type as input plane
			return intersectPlane((Line)g).specifyType(type);
		}
		throw new UnsupportedOperationException();
	}
	//The intersection of line not plane
	//same with segment intersection
	private GeomObj2 baseIntersectPlane(Line that){
		CohoNumber A1 = A();//Ax+By=C
		CohoNumber B1 = B();
		CohoNumber C1 = C();

		CohoNumber A2 = that.A();
		CohoNumber B2 = that.B();
		CohoNumber C2 = that.C();
		
		CohoNumber denom = A1.mult(B2).sub(B1.mult(A2));
//		System.out.println(denom);
//		System.out.println(A1.sub(A2).doubleValue());
//		System.out.println(A1.doubleValue()-(A2.doubleValue()));
		CohoNumber num1 = B1.mult(C2).sub(B2.mult(C1));
		CohoNumber num2 = C1.mult(A2).sub(C2.mult(A1));
		if(denom.compareTo(denom.zero())==0){
			if(num1.compareTo(num1.zero())!=0 || num2.compareTo(num2.zero())!=0){//no intersection
				return Empty.instance();
			}else{//intersection is segment
				return this;//same line 
			}
		}else{
			Point pt = Point.create(num1.div(denom),num2.div(denom));
			//System.out.println(pt+""+type);
			return pt;
		}
	}
	//TODO Unify the result to same type for lpProject
	protected GeomObj2 intersectPlane(Line that){
		if( (type==CohoDouble.type || that.type()==CohoDouble.type) && (type instanceof ScaleType && that.type() instanceof ScaleType) ){
			try{
				GeomObj2 obj = this.specifyType(DoubleInterval.type).baseIntersectPlane(that.specifyType(DoubleInterval.type));
				//make sure the error is not large when conver to double
				//System.out.println(obj.maxError());
				if(obj.maxError()>eps)
					throw new ArithmeticException();
				return obj;
			}catch(ArithmeticException e){
				//System.out.println("here");
				return this.specifyType(CohoAPR.type).baseIntersectPlane(that.specifyType(CohoAPR.type));
			}
		}else{//NOTICE: don't use double and interval. the result may contain error. 
			return baseIntersectPlane(that);
		}
	}

	public GeomObj2 negate() {
		return new Line(A.negate(),B.negate(),C.negate());
	}

	public GeomObj2 translate(Point offset) {
		throw new UnsupportedOperationException();
	}

	public GeomObj2 transpose() {
		throw new UnsupportedOperationException();
	}

	public GeomObj2 union(GeomObj2 g) {
		throw new UnsupportedOperationException();
	}

}
