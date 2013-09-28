package coho.common.number;

public class DoubleInterval extends BasicInterval {
	final private double lo, hi;
	public CohoDouble lo(){
		return CohoDouble.create(lo);
	}
	public CohoDouble hi(){
		return CohoDouble.create(hi);
	}
	public CohoDouble x(){
		//return CohoDouble.create((lo+hi)/2);//overflow
		return CohoDouble.create(lo/2+hi/2);//XXX computation error?
	}
	public CohoDouble e(){
		//return CohoDouble.create((hi-lo)/2);
		return CohoDouble.create(hi/2-lo/2);
	}
	private void checkValid(double v){
		if(Double.isInfinite(v)||Double.isNaN(v))
			throw new InvalidIntervalException(""+v);
	}
	private DoubleInterval(double lo,double hi){
		checkValid(lo);
		checkValid(hi);
		if(lo>hi){
			double tmp = lo;
			lo = hi;
			hi = tmp;
		}
		this.lo = lo;
		this.hi = hi;
	}
	public DoubleInterval(CohoNumber v){
		if(v instanceof IntervalNumber){
			lo = ((IntervalNumber)v).lo().doubleValue(Round.ROUNDMODE.FLOOR);
			hi = ((IntervalNumber)v).hi().doubleValue(Round.ROUNDMODE.CEIL);
		}else{
			//NOTICE if v is apr, doubleValue lost precission
			lo = v.doubleValue(Round.ROUNDMODE.FLOOR);
			hi = v.doubleValue(Round.ROUNDMODE.CEIL);
		}
		checkValid(lo);
		checkValid(hi);	
	}
	public DoubleInterval(Number v){
		this(v.doubleValue(),v.doubleValue());
	}
	public DoubleInterval(CohoNumber lo, CohoNumber hi){
		this(lo.doubleValue(),hi.doubleValue());
	}
	public DoubleInterval(Number lo, Number hi){
		this(lo.doubleValue(),hi.doubleValue());
	}
	public static DoubleInterval create(CohoNumber v){
		return new DoubleInterval(v);
	}	
	public static DoubleInterval create(Number v){
		return new DoubleInterval(v);
	}
	public static DoubleInterval create(CohoNumber lo, CohoNumber hi){
		return new DoubleInterval(lo,hi);
	}
	public static DoubleInterval create(Number lo, Number hi){
		return new DoubleInterval(lo,hi);
	}
	public DoubleInterval convert(CohoNumber v){
		if(v instanceof DoubleInterval)
			return (DoubleInterval)v;
		return new DoubleInterval(v);
	}
	public DoubleInterval convert(Number v){
		return new DoubleInterval(v);
	}
	public DoubleInterval convert(CohoNumber lo, CohoNumber hi){
		return new DoubleInterval(lo,hi);
	}
	public DoubleInterval convert(Number lo, Number hi){
		return new DoubleInterval(lo,hi);
	}
	public DoubleInterval[][] createArray(int nrows, int ncols){
		return new DoubleInterval[nrows][ncols];
	}
	public DoubleInterval[] createVector(int length){
		return new DoubleInterval[length];
	}
	public static final DoubleInterval zero = new DoubleInterval(0.0, 0.0);
	public static final DoubleInterval one = new DoubleInterval(1.0, 1.0);
	public DoubleInterval zero(){
		return zero;
	}
	public DoubleInterval one(){
		return one;
	}
	public DoubleInterval random(){
		double lo = CohoDouble.zero.random().doubleValue();
		double hi = CohoDouble.zero.random().doubleValue();
		return new DoubleInterval(lo,hi);
	}

	protected static final double ulp = ulp();
    // calculate the ulp, it's 2^-52.
    // From IEEE754, for a double, the exp bits is 11, and 
    // Fracation is 52 bits. ulp is the smallest value of the fraction
    protected static double ulp(){	// calculate ulp
        long l = Double.doubleToLongBits(1.0);
        double d = Double.longBitsToDouble(l+1);        
        return d-1;
    }
    /**
     * The greatest possible error introduce by round off of d. 
     * The error is less than the double ulp(all bits of fraction except the last bit are zero)
     * with the exp of d's.
     * @return the greatest possible error introduce by round off of d
     */
    //FIXED: the result is not correct for unomalized number
    //if d==0, we return 0. 
    //if d is quite small, we return negative value. 
    //set ulp as Double.MIN_VALUE if d is not normalized
    public static double ulp(double d){
    	//if(d==0) return 0;
    	if(d==0) return Double.MIN_VALUE;//ulp;
    	long l = Double.doubleToLongBits(d) & 0x7ff0000000000000l;// use the fact that the frac bits is 52.
    	l = l+Double.doubleToLongBits(ulp)-Double.doubleToLongBits(1.0);// instead of mult
    	double result = Double.longBitsToDouble(l);
    	if(result<=0)
    		result = Double.MIN_VALUE;
    	return result;
    }
    
    public static void main(String[] args){
    	double a = Double.MIN_VALUE;
    	System.out.println(ulp(a*Math.pow(2,102)));
    	//System.out.println(ulp(-Double.MAX_VALUE));
    }
	public DoubleInterval abs(){
		if(lo>=0){
			return this;
		}else if(hi<=0){
			return create(-hi,-lo);
		}else{
			return create(0,Math.max(-lo, hi));
		}	
	}
	public DoubleInterval negate(){
		return create(-hi,-lo);
	}
	public DoubleInterval recip(){
		return one.div(this);
	}
	public DoubleInterval sqrt(){
		if(this.lo<0){
			throw new ArithmeticException("NaN: the sqrt of negative value "+this.lo+" is NaN");
		}
		double l = Math.sqrt(lo);
		double h = Math.sqrt(hi);  
		return create(l-ulp(l),h+ulp(h));
	}

	public DoubleInterval add(DoubleInterval x){
		double l = lo+x.lo;
		double h = hi+x.hi;
		return create(l-ulp(l),h+ulp(h));
	}
	public DoubleInterval sub(DoubleInterval x){
		return add(x.negate());
	}
	public DoubleInterval mult(DoubleInterval x){
		double ll = lo*x.lo;
		double lh = lo*x.hi;
		double hl = hi*x.lo;
		double hh = hi*x.hi;
		double l = Math.min(Math.min(ll,lh),Math.min(hl,hh));
		double h = Math.max(Math.max(ll,lh),Math.max(hl,hh));
		return create(l-ulp(l),h+ulp(h));
	}
	public DoubleInterval div(DoubleInterval x){
		if(x.lo<=0&&x.hi>=0)
			throw new ArithmeticException("divide by zero");
		double ll = lo/x.lo;
		double lh = lo/x.hi;
		double hl = hi/x.lo;
		double hh = hi/x.hi;

		double l = Math.min(Math.min(ll,lh),Math.min(hl,hh));
		double h = Math.max(Math.max(ll,lh),Math.max(hl,hh));
		return create(l-ulp(l),h+ulp(h));
	}
	public DoubleInterval max(DoubleInterval x){
		return create(Math.max(lo, x.lo),Math.max(hi, x.hi));
	}
	public DoubleInterval min(DoubleInterval x){
		return create(Math.min(lo, x.lo),Math.max(hi, x.hi));
	}
	public int compareTo(DoubleInterval x){
		if(isScale()&&x.isScale()&&lo==x.lo)
			return 0;
		if(lo>=x.hi)
			return 1;
		if(hi<=x.lo)
			return -1;
		throw new NotcomparableIntervalException();
	}

	public static final IntervalType type = new IntervalType(DoubleInterval.class,zero,one);
	public IntervalType type(){
		return type;
	}
}
