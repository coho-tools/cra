package coho.common.number;

public class APRInterval extends BasicInterval {
	final private CohoAPR lo, hi;
	public CohoAPR lo(){
		return lo;
	}
	public CohoAPR hi(){
		return hi;
	}
	public CohoAPR x(){
		return lo.add(hi).div(CohoAPR.create(2));
	}
	public CohoAPR e(){
		return hi.sub(lo).div(CohoAPR.create(2));
	}
	private APRInterval(CohoAPR lo,CohoAPR hi){
		if(lo.compareTo(hi)>0){
			CohoAPR tmp = lo;
			lo = hi;
			hi = tmp;
		}
		this.lo = lo;
		this.hi = hi;
	}
	private APRInterval(CohoAPR v){
		lo = v;
		hi = v;
	}
	public APRInterval(CohoNumber v){
		if(v instanceof IntervalNumber){
			lo = CohoAPR.create(((IntervalNumber)v).lo());
			hi = CohoAPR.create(((IntervalNumber)v).hi());
		}else{
			lo = hi = CohoAPR.create(v);
		}
	}
	public APRInterval(Number v){
		this(CohoAPR.create(v));
	}
	public APRInterval(CohoNumber lo, CohoNumber hi){
		this(CohoAPR.create(lo),CohoAPR.create(hi));
	}
	public APRInterval(Number lo, Number hi){
		this(CohoAPR.create(lo),CohoAPR.create(hi));
	}
	public static APRInterval create(CohoNumber v){
		return new APRInterval(v);
	}	
	public static APRInterval create(Number v){
		return new APRInterval(v);
	}
	public static APRInterval create(CohoNumber lo, CohoNumber hi){
		return new APRInterval(lo,hi);
	}
	public static APRInterval create(Number lo, Number hi){
		return new APRInterval(lo,hi);
	}
	public APRInterval convert(CohoNumber v){
		if(v instanceof APRInterval)
			return (APRInterval)v;
		return new APRInterval(v);
	}
	public APRInterval convert(Number v){
		return new APRInterval(v);
	}
	public APRInterval convert(CohoNumber lo, CohoNumber hi){
		return new APRInterval(lo,hi);
	}
	public APRInterval convert(Number lo, Number hi){
		return new APRInterval(lo,hi);
	}
	public APRInterval[][] createArray(int nrows, int ncols){
		return new APRInterval[nrows][ncols];
	}
	public APRInterval[] createVector(int length){
		return new APRInterval[length];
	}
	public static final APRInterval zero = new APRInterval(CohoAPR.zero);
	public static final APRInterval one = new APRInterval(CohoAPR.one);
	public APRInterval zero(){
		return zero;
	}
	public APRInterval one(){
		return one;
	}
	public APRInterval random(){
		CohoAPR lo = CohoAPR.zero.random();
		CohoAPR hi = CohoAPR.zero.random();
		return new APRInterval(lo,hi);
	}

	public APRInterval abs(){
		if(lo.compareTo(CohoAPR.zero)>=0){
			return this;
		}else if(hi.compareTo(CohoAPR.zero)<=0){
			return create(hi.negate(),lo.negate());
		}else{
			return create(CohoAPR.zero, hi.max(lo.negate()));
		}	
	}
	public APRInterval negate(){
		return create(hi.negate(),lo.negate());
	}
	public APRInterval recip(){
		return one.div(this);
	}
	public APRInterval sqrt(){
		throw new UnsupportedOperationException();
	}

	public APRInterval add(APRInterval x){
		return create(lo.add(x.lo),hi.add(x.hi));
	}
	public APRInterval sub(APRInterval x){
		return add(x.negate());
	}
	public APRInterval mult(APRInterval x){
		CohoAPR ll = lo.mult(x.lo);
		CohoAPR lh = lo.mult(x.hi);
		CohoAPR hl = hi.mult(x.lo);
		CohoAPR hh = hi.mult(x.hi);
		CohoAPR l = (ll.min(lh)).min(hl.min(hh));
		CohoAPR h = (ll.max(lh)).max(hl.max(hh));
		return create(l,h);
	}
	public APRInterval div(APRInterval x){
		if(x.lo.compareTo(CohoAPR.zero)<=0&&x.hi.compareTo(CohoAPR.zero)>=0)
			throw new ArithmeticException("divide by zero");
		CohoAPR ll = lo.div(x.lo);
		CohoAPR lh = lo.div(x.hi);
		CohoAPR hl = hi.div(x.lo);
		CohoAPR hh = hi.div(x.hi);
		CohoAPR l = (ll.min(lh)).min(hl.min(hh));
		CohoAPR h = (ll.max(lh)).max(hl.max(hh));
		return create(l,h);
	}
	public APRInterval max(APRInterval x){
		return create(lo.max(x.lo),hi.max(x.hi));
	}
	public APRInterval min(APRInterval x){
		return create(lo.min(x.lo), hi.min(x.hi));
	}
	public int compareTo(APRInterval x){
		if(isScale()&&x.isScale()&&lo.compareTo(x.lo)==0)
			return 0;
		if(lo.compareTo(x.hi)>=0)
			return 1;
		if(hi.compareTo(x.lo)<=0)
			return -1;
		throw new NotcomparableIntervalException();
	}

	public static final IntervalType type = new IntervalType(APRInterval.class,zero,one);
	public IntervalType type(){
		return type;
	}

}
