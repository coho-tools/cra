package coho.common.number;

public interface ScaleNumber extends CohoNumber {
	public ScaleNumber abs();
	public ScaleNumber negate();
	public ScaleNumber recip();
	public ScaleNumber sqrt();
	
	public ScaleNumber add(ScaleNumber x);
	public ScaleNumber add(Number x);
	public ScaleNumber sub(ScaleNumber x);
	public ScaleNumber sub(Number x);
	public ScaleNumber mult(ScaleNumber x);
	public ScaleNumber mult(Number x);
	public ScaleNumber div(ScaleNumber x);
	public ScaleNumber div(Number x);
	public ScaleNumber max(ScaleNumber x);
	public ScaleNumber max(Number x);
	public ScaleNumber min(ScaleNumber x);
	public ScaleNumber min(Number x);
	
	public ScaleNumber one();
	public ScaleNumber zero();
	public ScaleNumber random();
	public ScaleType type();

}
