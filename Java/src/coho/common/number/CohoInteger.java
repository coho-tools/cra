package coho.common.number;

public class CohoInteger extends BasicScale {
	/***************************************
	 * Members and constructors
	 ***********************************/
	final private int v;
	private CohoInteger(int v){
		this.v = v;
	}
	public CohoInteger(CohoNumber v){
		this(v.intValue());
	}
	public CohoInteger(Number v){
		this(v.intValue());
	}
	public static CohoInteger create(CohoNumber v){
		return new CohoInteger(v);
	}
	public static CohoInteger create(Number v){
		return new CohoInteger(v);
	}
	public CohoInteger convert(CohoNumber v){
		if(v instanceof CohoInteger)
			return (CohoInteger)v;
		return new CohoInteger(v);
	}
	public CohoInteger convert(Number v){
		return new CohoInteger(v);
	}
	public CohoInteger[][] createArray(int nrows, int ncols){
		return new CohoInteger[nrows][ncols];
	}
	public CohoInteger[] createVector(int length){
		return new CohoInteger[length];
	}
	public static final CohoInteger one = new CohoInteger(1);
	public static final CohoInteger zero = new CohoInteger(0);
	public CohoInteger zero(){
		return zero;
	}
	public CohoInteger one(){
		return one;
	}
	public CohoInteger random(){
        double random =(2*(Math.random()-0.5))*Integer.MAX_VALUE;
		return create(random);
	}
	/************************************
	 * Operations for CohoNumber interface
	 ***********************************/
	public CohoInteger abs(){
		return v>0?this:negate();
	}
	public CohoInteger negate(){
		return create(-v);
	}
	public CohoInteger recip(){
		return create(1/v);
	}
	public CohoInteger sqrt(){
		return create(Math.sqrt(v));
	}
	
	public CohoInteger add(CohoInteger x){
		return create(v+x.v);
	}
	public CohoInteger sub(CohoInteger x){
		return create(v-x.v);
	}
	public CohoInteger mult(CohoInteger x){
		return create(v*x.v);
	}
	public CohoInteger div(CohoInteger x){
		return create(v/x.v);
	}
	public CohoInteger max(CohoInteger x){
		return v>x.v?this:x;
	}
	public CohoInteger min(CohoInteger x){
		return v<x.v?this:x;
	}
	public int compareTo(CohoInteger x){
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
		return (double)v;
	}
	
	public static final ScaleType type = new ScaleType(CohoInteger.class, zero, one);
	public ScaleType type(){
		return type;
	}
	public String toString(){
		return ""+v;
	}
}
