package coho.common.number;

public class LongInterval extends BasicInterval {
	final private long lo, hi;
	public CohoLong lo(){
		return CohoLong.create(lo);
	}
	public CohoLong hi(){
		return CohoLong.create(hi);
	}
	public CohoLong x(){
		return CohoLong.create(lo/2.0+hi/2.0);
	}
	public CohoLong e(){
		return CohoLong.create(Math.max(hi-x().longValue(),x().longValue()-lo));
	}
	private LongInterval(long lo, long hi){
		if(lo>hi){
			long tmp = lo;
			lo = hi;
			hi = tmp;
		}
		this.lo = lo;
		this.hi = hi;
	}
	public LongInterval(CohoNumber v){
		if(v instanceof IntervalNumber){
			lo = ((IntervalNumber)v).lo().longValue();
			hi = ((IntervalNumber)v).hi().longValue();
		}else{
			lo = hi = v.longValue();
		}
	}
	public LongInterval(Number v){
		this(v.longValue(),v.longValue());
	}
	public LongInterval(CohoNumber lo, CohoNumber hi){
		this(lo.longValue(),hi.longValue());
	}
	public LongInterval(Number lo, Number hi){
		this(lo.longValue(),hi.longValue());
	}
	public static LongInterval create(CohoNumber v){
		return new LongInterval(v);
	}	
	public static LongInterval create(Number v){
		return new LongInterval(v);
	}
	public static LongInterval create(CohoNumber lo, CohoNumber hi){
		return new LongInterval(lo,hi);
	}
	public static LongInterval create(Number lo, Number hi){
		return new LongInterval(lo,hi);
	}
	public LongInterval convert(CohoNumber v){
		if(v instanceof LongInterval)
			return (LongInterval)v;
		return new LongInterval(v);
	}
	public LongInterval convert(Number v){
		return new LongInterval(v);
	}
	public LongInterval convert(CohoNumber lo, CohoNumber hi){
		return new LongInterval(lo,hi);
	}
	public LongInterval convert(Number lo, Number hi){
		return new LongInterval(lo,hi);
	}
	public LongInterval[][] createArray(int nrows,int ncols){
		return new LongInterval[nrows][ncols];
	}
	public LongInterval[] createVector(int length){
		return new LongInterval[length];
	}
	public static final LongInterval zero = new LongInterval(0);
	public static final LongInterval one = new LongInterval(1);
	public LongInterval zero(){
		return zero;
	}
	public LongInterval one(){
		return one;
	}
	public LongInterval random(){
		long lo = CohoLong.zero.random().longValue();
		long hi = CohoLong.zero.random().longValue();
		return new LongInterval(lo,hi);
	}

	public LongInterval abs(){
		if(lo>=0){
			return this;
		}else if(hi<=0){
			return create(-hi,-lo);
		}else{
			return create(0,Math.max(-lo, hi));
		}	
	}
	public LongInterval negate(){
		return create(-hi,-lo);
	}
	public LongInterval recip(){
		return one.div(this);
	}
	public LongInterval sqrt(){
		if(this.lo<0){
			throw new ArithmeticException("NaN: the sqrt of negative value is NaN");
		}
		double l = Math.sqrt(lo);
		double h = Math.sqrt(hi);  
		return create(l,h);
	}

	public LongInterval add(LongInterval x){
		return create(lo+x.lo,hi+x.hi);
	}
	public LongInterval sub(LongInterval x){
		return add(x.negate());
	}
	public LongInterval mult(LongInterval x){
		double ll = lo*x.lo;
		double lh = lo*x.hi;
		double hl = hi*x.lo;
		double hh = hi*x.hi;
		double l = Math.min(Math.min(ll,lh),Math.min(hl,hh));
		double h = Math.max(Math.max(ll,lh),Math.max(hl,hh));
		return create(l,h);
	}
	public LongInterval div(LongInterval x){
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
	public LongInterval max(LongInterval x){
		return create(Math.max(lo, x.lo),Math.max(hi, x.hi));
	}
	public LongInterval min(LongInterval x){
		return create(Math.min(lo, x.lo),Math.max(hi, x.hi));
	}
	public int compareTo(LongInterval x){
		if(isScale()&&x.isScale()&&lo==x.lo)
			return 0;
		if(lo>=x.hi)
			return 1;
		if(hi<=x.lo)
			return -1;

		throw new NotcomparableIntervalException();
	}

	public static final IntervalType type = new IntervalType(LongInterval.class,zero,one);
	public IntervalType type(){
		return type;
	}
}
