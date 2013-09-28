package coho.common.number;

public class CohoBoolean extends BasicScale {
	/*****************
	 * Members and constructors
	 *****************/
	final private boolean v;
	private CohoBoolean(boolean v){
		this.v = v;
	}
	public CohoBoolean(CohoNumber v){
		this(v.booleanValue());
	}
	public CohoBoolean(Number v){
		this(v.doubleValue()!=0);//FIXED this(!v.equals(0). Double(0.0).equals(0) false 0.0==0 true
		//this(!v.equals(0));
	}
	public CohoBoolean(Boolean v){//Boolean is not Number.Therefore we add it.
		this(v.booleanValue());
	}
	public static CohoBoolean create(CohoNumber v){
		return new CohoBoolean(v);
	}
	public static CohoBoolean create(Number v){
		return new CohoBoolean(v);
	}
	public static CohoBoolean create(Boolean v){
		return new CohoBoolean(v);
	}
	public CohoBoolean convert(CohoNumber v){
		if(v instanceof CohoBoolean)
			return (CohoBoolean)v;
		return new CohoBoolean(v);
	}
	public CohoBoolean convert(Number v){
		return new CohoBoolean(v);
	}
	public CohoBoolean convert(Boolean v){
		return new CohoBoolean(v);
	}
	public CohoBoolean[][] createArray(int nrows, int ncols){
		return new CohoBoolean[nrows][ncols];
	}
	public CohoBoolean[] createVector(int length){
		return new CohoBoolean[length];
	}
	public static final CohoBoolean one = new CohoBoolean(true); 
	public static final CohoBoolean zero = new CohoBoolean(false);
	public CohoBoolean one() {
		return one;
	}
	public CohoBoolean zero() {
		return zero;
	}
	public CohoBoolean random() {
		return create(Math.random()>0.5?true:false);
	}

	/****************
	 * Operation functions for CohoNumber interface
	 ****************/
	/*
	 * return this: |1| = 1, |0| = 0
	 */
	public CohoBoolean abs(){
		return this;
	}
	public CohoBoolean negate(){
		return create(!v);
	}
//	/*
//	 *return this:  -1 = 1; -0 = 0;
//	 */
//	public CohoBoolean negate() {
//		return this;
//	}
	/*
	 * 1/1 = 1; 1/0 throw ArithmeticException
	 */
	public CohoBoolean recip(){
		if(v)
			return this;
		else
			throw new ArithmeticException("divided by zero");
	}
	/*
	 *return this:  sqrt(1) = 1; sqrt(0) = 0;
	 */
	public CohoBoolean sqrt() {
		return this;
	}

	/*
	 * xor: 1+1=1; 1+0=0+1=1 0+0=0;
	 */
	public CohoBoolean add(CohoBoolean x){
		return create(v || x.v);
	}
	/*
	 * don't support it!? It doesn't make sense for boolean sub
	 * recommend using and, or, not
	 */
	public CohoBoolean sub(CohoBoolean x){
		throw new UnsupportedOperationException();
	}
//	/*
//	 * xor: 1+1=1; 1+0=0+1=1 0+0=0;
//	 */
//	public CohoBoolean add(CohoBoolean x){
//		return create(v || x.v);
//	}
//	/*
//	 * xor: 1-1=0-0=0-1=0 1-0=1; //a-b!=a+b.negate()
//	 * 1-1=1+(-1)=1+0=1 1-0=1+(-0)=1 0-0=1 0-1=0
//	 */
//	public CohoBoolean sub(CohoBoolean x){
//		return create(v && !x.v);
//	}
//	/*
//	 * xor: 1+1=0(weird); 1+0=0+1=1 0+0=0;
//	 */
//	public CohoBoolean add(CohoBoolean x){
//		return create(v ^ x.v);
//	}
//	/*
//	 * xor: 1-1=0-0=0 1-0=0-1=1;
//	 */
//	public CohoBoolean sub(CohoBoolean x){
//		return create(v ^ x.v);
//	}
	/*
	 * and: ?*1=? ?*0=0
	 */
	public CohoBoolean mult(CohoBoolean x){
		return create(v && x.v);
	}
	/*
	 * ?/1=? ?/0 throw Arithmetic Exception
	 */
	public CohoBoolean div(CohoBoolean x){
		if(x.v)
			return this;
		throw new ArithmeticException("divided by zero");
	}
	/*
	 * or: max(1,1)=max(1,0)=max(0,1)=1 max(0,0)=0;
	 */
	public CohoBoolean max(CohoBoolean x){
		return create(v || x.v);
	}
	/*
	 * and: min(0,0)=min(1,0)=min(0,1)=0; min(1,1)=1;
	 */
	public CohoBoolean min(CohoBoolean x){
		return create(v && x.v);
	}
	
	public int compareTo(CohoBoolean x){
		if(v == x.v)
			return 0;
		if(v)
			return 1;
		return -1;
	}

	public double doubleValue() {
		return v?1:0;
	}
	public long longValue() {
		return v?1:0;
	}
	

	public static ScaleType type = new ScaleType(CohoBoolean.class, zero, one);
	public ScaleType type() {return type;}
	public String toString(){
		return ""+v;
	}
	
	/*********************************
	 * Additional functions
	 ********************************/
	public CohoBoolean and(CohoBoolean x){
		return create(v && x.v);
	}
	public CohoBoolean or(CohoBoolean x){
		return create(v || x.v);
	}
	public CohoBoolean not(CohoBoolean x){
		return create(!v);
	}
	public CohoBoolean xor(CohoBoolean x){
		return create(v ^ x.v);
	}
	
	public static void main(String[] args){
		CohoBoolean a = CohoBoolean.create(0.0);
		Double b = 0.0;
		System.out.println(b==0);
		System.out.println(a);
	}
}
