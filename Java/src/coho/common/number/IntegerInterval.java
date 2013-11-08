package coho.common.number;

public class IntegerInterval extends BasicInterval {
	final private int lo, hi;
	public CohoInteger lo(){
		return CohoInteger.create(lo);
	}
	public CohoInteger hi(){
		return CohoInteger.create(hi);
	}
	public CohoInteger x(){
		return CohoInteger.create(lo/2.0+hi/2.0);
	}
	public CohoInteger e(){
		return CohoInteger.create(Math.max(hi-x().intValue(),x().intValue()-lo));
	}
	private IntegerInterval(int lo,int hi){
		if(lo>hi){
			int tmp = lo;
			lo = hi;
			hi = tmp;
		}
		this.lo = lo;
		this.hi = hi;
	}
	public IntegerInterval(CohoNumber v){
		if(v instanceof IntervalNumber){
			lo = ((IntervalNumber)v).lo().intValue();
			hi = ((IntervalNumber)v).hi().intValue();
		}else{
			lo = hi = v.intValue();
		}
	}
	public IntegerInterval(Number v){
		this(v.intValue(),v.intValue());
	}
	public IntegerInterval(CohoNumber lo, CohoNumber hi){
		this(lo.intValue(),hi.intValue());
	}
	public IntegerInterval(Number lo, Number hi){
		this(lo.intValue(),hi.intValue());
	}
	public static IntegerInterval create(CohoNumber v){
		return new IntegerInterval(v);
	}	
	public static IntegerInterval create(Number v){
		return new IntegerInterval(v);
	}
	public static IntegerInterval create(CohoNumber lo, CohoNumber hi){
		return new IntegerInterval(lo,hi);
	}
	public static IntegerInterval create(Number lo, Number hi){
		return new IntegerInterval(lo,hi);
	}
	public IntegerInterval convert(CohoNumber v){
		if(v instanceof IntegerInterval)
			return (IntegerInterval)v;
		return new IntegerInterval(v);
	}
	public IntegerInterval convert(Number v){
		return new IntegerInterval(v);
	}
	public IntegerInterval convert(CohoNumber lo, CohoNumber hi){
		return new IntegerInterval(lo,hi);
	}
	public IntegerInterval convert(Number lo, Number hi){
		return new IntegerInterval(lo,hi);
	}
	public IntegerInterval[][] createArray(int nrows, int ncols){
		return new IntegerInterval[nrows][ncols];
	}
	public IntegerInterval[] createVector(int length){
		return new IntegerInterval[length];
	}
	public static final IntegerInterval zero = new IntegerInterval(0);
	public static final IntegerInterval one = new IntegerInterval(1);
	public IntegerInterval zero(){
		return zero;
	}
	public IntegerInterval one(){
		return one;
	}
	public IntegerInterval random(){
		int lo = CohoInteger.zero.random().intValue();
		int hi = CohoInteger.zero.random().intValue();
		return new IntegerInterval(lo,hi);
	}
	
	public IntegerInterval abs(){
		if(lo>=0){
			return this;
		}else if(hi<=0){
			return create(-hi,-lo);
		}else{
			return create(0,Math.max(-lo, hi));
		}	
	}
	public IntegerInterval negate(){
		return create(-hi,-lo);
	}
	public IntegerInterval recip(){
		return one.div(this);
	}
	public IntegerInterval sqrt(){
        if(this.lo<0){
            throw new ArithmeticException("NaN: the sqrt of negative value is NaN");
        }
        double l = Math.sqrt(lo);
        double h = Math.sqrt(hi);  
        return create(l,h);
	}
	
	public IntegerInterval add(IntegerInterval x){
		return create(lo+x.lo,hi+x.hi);
	}
	public IntegerInterval sub(IntegerInterval x){
		return add(x.negate());
	}
	public IntegerInterval mult(IntegerInterval x){
		double ll = lo*x.lo;
		double lh = lo*x.hi;
		double hl = hi*x.lo;
		double hh = hi*x.hi;
        double l = Math.min(Math.min(ll,lh),Math.min(hl,hh));
        double h = Math.max(Math.max(ll,lh),Math.max(hl,hh));
        return create(l,h);
	}
	public IntegerInterval div(IntegerInterval x){
		if(x.lo<=0&&x.hi>=0)
			throw new ArithmeticException("divide by zero");
        double ll = lo/x.lo;
        double lh = lo/x.hi;
        double hl = hi/x.lo;
        double hh = hi/x.hi;
        
        double l = Math.min(Math.min(ll,lh),Math.min(hl,hh));
        double h = Math.max(Math.max(ll,lh),Math.max(hl,hh));
        return create(l,h);
	}
	public IntegerInterval max(IntegerInterval x){
		return create(Math.max(lo, x.lo),Math.max(hi, x.hi));
	}
	public IntegerInterval min(IntegerInterval x){
		return create(Math.min(lo, x.lo),Math.max(hi, x.hi));
	}
	public int compareTo(IntegerInterval x){
		if(isScale()&&x.isScale()&&lo==x.lo)
			return 0;
		if(lo>=x.hi)
			return 1;
		if(hi<=x.lo)
			return -1;
		throw new NotcomparableIntervalException();
	}
	
	public static final IntervalType type = new IntervalType(IntegerInterval.class,zero,one);
	public IntervalType type(){
		return type;
	}
}
