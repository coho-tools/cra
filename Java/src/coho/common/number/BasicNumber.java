package coho.common.number;

/*
 * sqrt problems:
 * The return type is CohoNumber, not double. 
 * This might cause problem, because NaN.LongValue=0, POSITIVEINFINITY.longValue
 * 1=0
 * Therefore, sqrt(-1)=0! 
 * This can be avoid by promote to Double type. Caller should pay attention to it.
 */
public abstract class BasicNumber implements CohoNumber {

	public abstract CohoNumber abs(); 
	public abstract CohoNumber negate();
	public abstract CohoNumber recip();
	public abstract CohoNumber sqrt();

	public CohoNumber add(CohoNumber x){
		return BasicType.promoteOp(this, x, CohoNumber.ArithOp.ADD);
	}
	public CohoNumber add(Number x) {
		return BasicType.promoteOp(this, x, CohoNumber.ArithOp.ADD);
	}
	public CohoNumber sub(CohoNumber x){
		return BasicType.promoteOp(this, x, CohoNumber.ArithOp.SUB);
	}
	public CohoNumber sub(Number x) {
		return BasicType.promoteOp(this, x, CohoNumber.ArithOp.SUB);
	}
	public CohoNumber mult(CohoNumber x){
		return BasicType.promoteOp(this, x, CohoNumber.ArithOp.MULT);
	}
	public CohoNumber mult(Number x) {
		return BasicType.promoteOp(this, x, CohoNumber.ArithOp.MULT);
	}
	public CohoNumber div(CohoNumber x){
		return BasicType.promoteOp(this, x, CohoNumber.ArithOp.DIV);
	}
	public CohoNumber div(Number x) {
		return BasicType.promoteOp(this, x, CohoNumber.ArithOp.DIV);
	}
	public CohoNumber max(CohoNumber x){
		return BasicType.promoteOp(this, x, CohoNumber.ArithOp.MAX);
	}
	public CohoNumber max(Number x) {
		return BasicType.promoteOp(this, x, CohoNumber.ArithOp.MAX);
	}
	public CohoNumber min(CohoNumber x){
		return BasicType.promoteOp(this, x, CohoNumber.ArithOp.MIN);
	}
	public CohoNumber min(Number x) {
		return BasicType.promoteOp(this, x, CohoNumber.ArithOp.MIN);
	}
	
	public int compareTo(CohoNumber x) {
		return BasicType.promoteOp(this, x, CohoNumber.ArithOp.CMP).intValue();
	}	
	public int compareTo(Number x) {
		return BasicType.promoteOp(this, x, CohoNumber.ArithOp.CMP).intValue();
	}
	public final boolean eq(CohoNumber x){
		try{
			return compareTo(x)==0;
		}catch(NotcomparableIntervalException e){
			return false;
		}
	}
	public final boolean eq(Number x){
		try{
			return compareTo(x)==0;
		}catch(NotcomparableIntervalException e){
			return false;
		}
	}
	public final boolean neq(CohoNumber x){
		try{
			return compareTo(x)!=0;
		}catch(NotcomparableIntervalException e){
			return true;
		}
	}
	public final boolean neq(Number x){
		try{
			return compareTo(x)!=0;
		}catch(NotcomparableIntervalException e){
			return true;
		}
	}
	public final boolean greater(CohoNumber x){
		try{
			return compareTo(x)>0;
		}catch(NotcomparableIntervalException e){
			return false;
		}
	}
	public final boolean greater(Number x){
		try{
			return compareTo(x)>0;
		}catch(NotcomparableIntervalException e){
			return false;
		}
	}
	public final boolean geq(CohoNumber x){
		try{
			return compareTo(x)>=0;
		}catch(NotcomparableIntervalException e){
			return false;
		}
	}
	public final boolean geq(Number x){
		try{
			return compareTo(x)>=0;
		}catch(NotcomparableIntervalException e){
			return false;
		}
	}
	public final boolean less(CohoNumber x){
		try{
			return compareTo(x)<0;
		}catch(NotcomparableIntervalException e){
			return false;
		}
	}
	public final boolean less(Number x){
		try{
			return compareTo(x)<0;
		}catch(NotcomparableIntervalException e){
			return false;
		}
	}
	public final boolean leq(CohoNumber x){
		try{
			return compareTo(x)<=0;
		}catch(NotcomparableIntervalException e){
			return false;
		}
	}
	public final boolean leq(Number x){
		try{
			return compareTo(x)<=0;
		}catch(NotcomparableIntervalException e){
			return false;
		}
	}
	
	public abstract long longValue();
	public abstract double doubleValue();
	public boolean booleanValue() {
		return longValue()!=0;
	}
	public byte byteValue() {
		return (byte)longValue();
	}
	public short shortValue() {
		return (short)longValue();
	}
	public int intValue() {
		return (int)longValue();
	}
	public float floatValue() {
		return (float)doubleValue();
	}
	

	public abstract CohoNumber one();
	public abstract CohoNumber zero();
	public abstract CohoNumber random(); 
	public abstract CohoNumber convert(CohoNumber x); 
	public abstract CohoNumber convert(Number x);
	public abstract CohoNumber[][] createArray(int nrows, int ncols);
	public abstract CohoNumber[] createVector(int length);
	public abstract CohoType type();
	public abstract String toString();
	
	public final boolean equals(Object o){
		if(o instanceof CohoNumber)
			return compareTo((CohoNumber)o)==0;
		if(o instanceof Number)
			return compareTo((Number)o)==0;
		return false;
	}
	//If two CohoNumbers equal, the hashcode must be the same. 
	//Therefore, we have to provide a final function for all CohoNumbers
	public final int hashCode(){
		return ((Double)doubleValue()).hashCode();
	}
}
