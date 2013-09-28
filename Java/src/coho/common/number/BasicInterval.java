package coho.common.number;

public abstract class BasicInterval extends BasicNumber implements IntervalNumber{
	public abstract ScaleNumber lo();
	public abstract ScaleNumber hi();
	public abstract ScaleNumber x();
	public abstract ScaleNumber e();
	public ScaleNumber scale(){
		return x();
	}
	public ScaleNumber error(){
		return e();
	}
	public boolean isScale(){
		return lo().compareTo(hi())==0;
	}
	public long longValue(){
		return x().longValue();
	}
	public double doubleValue(){
		return doubleValue(ROUNDMODE.NEAR);
	}
	public double doubleValue(ROUNDMODE mode){
		switch(mode){
		case CEIL:
			return hi().doubleValue();
		case FLOOR: 
			return lo().doubleValue();
		case ZERO:
			double lo = lo().doubleValue();
			double hi = hi().doubleValue();
			if(lo>0)
				return lo;
			if(hi<0)
				return hi;
			return 0;
		default://NEAR
			return x().doubleValue();
		}
	}
	public abstract IntervalNumber convert(CohoNumber x);
	public abstract IntervalNumber convert(Number x);
	public abstract IntervalNumber[][] createArray(int nrow, int ncols);
	public abstract IntervalNumber[] createVector(int length);

	public abstract IntervalNumber one();
	public abstract IntervalNumber zero();
	public abstract IntervalNumber random();

	public abstract IntervalNumber abs();
	public abstract IntervalNumber negate();
	public abstract IntervalNumber recip();
	public abstract IntervalNumber sqrt();
	
	public IntervalNumber add(IntervalNumber x) {
		return IntervalType.promoteOp(this, x, CohoNumber.ArithOp.ADD);
	}
	public IntervalNumber sub(IntervalNumber x) {
		return IntervalType.promoteOp(this, x, CohoNumber.ArithOp.SUB);
	}
	public IntervalNumber mult(IntervalNumber x) {
		return IntervalType.promoteOp(this, x, CohoNumber.ArithOp.MULT);
	}
	public IntervalNumber div(IntervalNumber x) {
		return IntervalType.promoteOp(this, x, CohoNumber.ArithOp.DIV);
	}
	public IntervalNumber max(IntervalNumber x) {
		return IntervalType.promoteOp(this, x, CohoNumber.ArithOp.MAX);
	}
	public IntervalNumber min(IntervalNumber x) {
		return IntervalType.promoteOp(this, x, CohoNumber.ArithOp.MIN);
	}
	public int compareTo(IntervalNumber x){
		return IntervalType.promoteOp(this, x, CohoNumber.ArithOp.CMP).intValue();
	}
	
	public IntervalNumber add(Number x) {
		return IntervalType.promoteOp(this, x, CohoNumber.ArithOp.ADD);
	}
	public IntervalNumber sub(Number x) {
		return IntervalType.promoteOp(this, x, CohoNumber.ArithOp.SUB);
	}
	public IntervalNumber mult(Number x) {
		return IntervalType.promoteOp(this, x, CohoNumber.ArithOp.MULT);
	}
	public IntervalNumber div(Number x) {
		return IntervalType.promoteOp(this, x, CohoNumber.ArithOp.DIV);
	}
	public IntervalNumber max(Number x) {
		return IntervalType.promoteOp(this, x, CohoNumber.ArithOp.MAX);
	}
	public IntervalNumber min(Number x) {
		return IntervalType.promoteOp(this, x, CohoNumber.ArithOp.MIN);
	}
	public int compareTo(Number x){
		return IntervalType.promoteOp(this, x, CohoNumber.ArithOp.CMP).intValue();
	}
	
	public abstract IntervalType type();
	public String toString(){
		return "["+lo().toString()+", "+hi().toString()+"]";
	}
}
