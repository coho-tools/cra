package coho.common.number;
/**********************
 * [a, b]
 **********************/
public interface IntervalNumber extends CohoNumber {
	public ScaleNumber hi(); //CONSIDER: do we need to move it to CohoNumber. Is it reasonable?
	public ScaleNumber lo();
	public ScaleNumber e();
	public ScaleNumber x();
	
	public IntervalNumber abs();
	public IntervalNumber negate();
	public IntervalNumber recip();
	public IntervalNumber sqrt();	
	public IntervalNumber add(IntervalNumber x);
	//public IntervalNumber add(ScaleNumber x);//within add(CohoNumber x)
	public IntervalNumber add(Number x);
	public IntervalNumber sub(IntervalNumber x);
	public IntervalNumber sub(Number x);
	public IntervalNumber mult(IntervalNumber x);
	public IntervalNumber mult(Number x);
	public IntervalNumber div(IntervalNumber x);
	public IntervalNumber div(Number x);
	public IntervalNumber max(IntervalNumber x);
	public IntervalNumber max(Number x);
	public IntervalNumber min(IntervalNumber x);
	public IntervalNumber min(Number x);
	
	public IntervalNumber one();
	public IntervalNumber zero();
	public IntervalNumber random();
	public IntervalType type();
	
	public boolean isScale();
}
