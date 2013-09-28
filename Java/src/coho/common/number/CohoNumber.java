package coho.common.number;
import coho.common.matrix.*;
/**
 * 
 * This class provides default implementations of CohoNumber. 
 * It includes all functions of Number, and also many arithemetic operation.
 * 
 * For CohoNumber, we requires 
 * 1) a-b = a+(-b)
 * 2) a/b = a*(1/b)
 * 3) max(a,b) = a.compareTo(b)?:a:b, similar with min;
 * 4) a+b=b+a, a+b+c=a+(b+c), similar with mult
 * 5) abs(a) = a.compareTo(0)?a:-a
 * @author chaoyan chaoyan@cs.ubc.ca
 * @see Number
 *
 */
/*
 * Design principe
 * 1)Each type of Number only deal with its own type.
 * 2)Each type provide create function for CohoNumber, possible with precision lost
 * 3)For promotion, CohoType deal with it.
 * 4)Return the best type after operation.
 * 5)Once create a new type, it should provide operation functions with all existed type 
 * to avoid loop of function call
 * 6)Subtype should know little of other type. SuperType deal with it.
 */
public interface CohoNumber extends Comparable<CohoNumber>, ErrorAnalysis, ArrayFactory, Round{
	/*
	 * Number interface
	 */
	public boolean booleanValue();
	public byte byteValue();
	public int intValue();
	public short shortValue();
	public long longValue();
	public float floatValue();
	public double doubleValue();
	

	/*
	 * Conversion 1)static create function 2)constructor  3)convert
	 * alias for static method creat();
	 * We have not considered Boolean because it is rarely used. But it add lots of functions 
	 * It can be converted to Byte or Int by user 
	 */
	public CohoNumber convert(CohoNumber x);
	public CohoNumber convert(Number x);
	
	/*
	 * Operations
	 * Using the new feature of Java5.0, autobox and autounbox, 
	 * We don't need to declare function for each primitive value
	 */
	public static enum ArithOp {ABS, NEGATE, RECIP, SQRT, ADD, SUB, MULT, DIV, MAX, MIN, CMP};
	public CohoNumber abs();
	public CohoNumber negate();
	public CohoNumber recip();
	public CohoNumber sqrt();
	
	public CohoNumber add(CohoNumber x);
	public CohoNumber add(Number x);
	public CohoNumber sub(CohoNumber x);
	public CohoNumber sub(Number x);
	public CohoNumber mult(CohoNumber x);
	public CohoNumber mult(Number x);
	public CohoNumber div(CohoNumber x);
	public CohoNumber div(Number x);
	public CohoNumber max(CohoNumber x);
	public CohoNumber max(Number x);
	public CohoNumber min(CohoNumber x);
	public CohoNumber min(Number x);
	
	/*
	 * compareTo() function required by Comparable interface
	 * toString() equal() and hashcode() function required by Object 
	 */
	//public int compareTo(CohoNumber x);
	public int compareTo(Number x);
	
	public boolean eq(CohoNumber x);
	public boolean eq(Number x);
	public boolean neq(CohoNumber x);
	public boolean neq(Number x);
	public boolean greater(CohoNumber x);
	public boolean greater(Number x);
	public boolean geq(CohoNumber x);
	public boolean geq(Number x);
	public boolean less(CohoNumber x);
	public boolean less(Number x);
	public boolean leq(CohoNumber x);
	public boolean leq(Number x);
	
	/*
	 * special number. We can't define static function in class
	 */
	public CohoNumber one();
	public CohoNumber zero();
	public CohoNumber random();
	
	/*
	 * For promotion
	 */
	public CohoType type();
}
