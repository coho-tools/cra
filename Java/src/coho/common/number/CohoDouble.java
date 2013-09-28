package coho.common.number;

public class CohoDouble extends BasicScale {
	final private double v;
	private CohoDouble(double v){
		this.v  = v; 
	}
	public CohoDouble(CohoNumber v){
		this(v.doubleValue());
	}	
	public CohoDouble(Number v){
		this(v.doubleValue());
	}
	public static CohoDouble create(CohoNumber v){
		return new CohoDouble(v);
	}
	public static CohoDouble create(Number v){
		return new CohoDouble(v);
	}
	public CohoDouble convert(CohoNumber v){
		if(v instanceof CohoDouble)
			return (CohoDouble)v;
		return new CohoDouble(v);
	}
	public CohoDouble convert(Number v){
		return new CohoDouble(v);
	}
	public CohoDouble[][] createArray(int nrows, int ncols){
		return new CohoDouble[nrows][ncols];
	}
	public CohoDouble[] createVector(int length){
		return new CohoDouble[length];
	}
	public static final CohoDouble zero = new CohoDouble(0);
	public static final CohoDouble one = new CohoDouble(1);
	public CohoDouble zero(){
		return zero;
	}
	public CohoDouble one(){
		return one;//FIXED one() -> one;
	}
	/*
	 * random value between -1 and 1;(non-Javadoc)
	 * @see coho.common.number.BasicScale#random()
	 */
	public CohoDouble random(){
        //double random =(2*(Math.random()-0.5))*Double.MAX_VALUE; 
		double random =(2*(Math.random()-0.5));
        return create(random);
	}

	public CohoDouble abs(){
		return v>=0?this:negate();
	}
	public CohoDouble negate(){
		return create(-v);
	}
	public CohoDouble recip(){
		return create(1/v);
	}
	public CohoDouble sqrt(){
		return create(Math.sqrt(v));
	}
	public CohoDouble add(CohoDouble x){
		return create(v+x.v);
	}
	public CohoDouble sub(CohoDouble x){
		return create(v-x.v);
	}
	public CohoDouble mult(CohoDouble x){
		return create(v*x.v);
	}
	public CohoDouble div(CohoDouble x){
		return create(v/x.v);
	}
	public CohoDouble max(CohoDouble x){
		return v>x.v?this:x;
	}
	public CohoDouble min(CohoDouble x){
		return v<x.v?this:x;
	}
	public int compareTo(CohoDouble x){
		if(v==x.v)
			return 0;
		if(v>x.v)
			return 1;
		return -1;
	}
	
	public long longValue(){
		return (long)v;
	}
	public double doubleValue(){
		return v;
	}

	public static final ScaleType type = new ScaleType(CohoDouble.class, zero, one);
	public ScaleType type(){
		return type;
	}
	public String toString(){
		return ""+v;
	}
}
