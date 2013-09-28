package coho.common.number;

public class CohoLong extends BasicScale {
	final private long v;
	private CohoLong(long v){
		this.v = v;
	}
	public CohoLong(CohoNumber v){
		this(v.longValue());
	}
	public CohoLong(Number v){//if V is NaN, return 0;	
		this(v.longValue());
	}
	public static CohoLong create(CohoNumber v){
		return new CohoLong(v);
	}
	public static CohoLong create(Number v){
		return new CohoLong(v);
	}
	public CohoLong convert(CohoNumber v){
		if(v instanceof CohoLong)
			return (CohoLong)v;
		return new CohoLong(v);
	}
	public CohoLong convert(Number v){
		return new CohoLong(v);
	}
	public CohoLong[][] createArray(int nrows, int ncols){
		return new CohoLong[nrows][ncols];
	}
	public CohoLong[] createVector(int length){
		return new CohoLong[length];
	}
	public final static CohoLong one = new CohoLong(1);
	public final static CohoLong zero = new CohoLong(0);
	public CohoLong zero(){
		return zero;
	}
	public CohoLong one(){
		return one;
	}
	public CohoLong random(){
        double random =(2*(Math.random()-0.5))*Long.MAX_VALUE; 
        return create(random);
	}
	
	public CohoLong abs(){
		return v>=0?this:negate();
	}
	public CohoLong negate(){
		return create(-v);
	}
	public CohoLong recip(){
		return create(1/v);
	}
	public CohoLong sqrt(){
		return create(Math.sqrt(v));
	}
	public CohoLong add(CohoLong x){
		return create(v+x.v);
	}
	public CohoLong sub(CohoLong x){
		return create(v-x.v);
	}
	public CohoLong mult(CohoLong x){
		return create(v*x.v);
	}
	public CohoLong div(CohoLong x){
		return create(v/x.v);
	}
	public CohoLong max(CohoLong x){
		return v>x.v?this:x;
	}
	public CohoLong min(CohoLong x){
		return v<x.v?this:x;
	}
	public int compareTo(CohoLong x){
		if(v==x.v)
			return 0;
		if(v>x.v)
			return 1;
		return -1;
	}
	
	public long longValue(){
		return v;
	}
	public double doubleValue(){
		return (double)v;
	}
	
	public static final ScaleType type = new ScaleType(CohoLong.class,zero,one);
	public ScaleType type(){
		return type;
	}
	public String toString(){
		return ""+v;
	}
}
