/**
 * This class defines a wrapper for boolean, int, double, and add an extra
 * class for arbitrary precision number. 
 * 
 * The class is abstract and is in charge of promotion and conversion.
 * Number op BasicNumber->BasicNumber. 
 * BasicNumber op CohoNumber->CohoNumber. BasicNumber leaves this to CohoNumber 
 */
/*
 * BasicNumber does 1)convert within/between Number BasicNumber
 * 
 * For each opeartion, promotion is performed firstly, then the corresponding sublcass
 * (BasicNumber of others) mehthods are called. Each subclass should only implment operation
 * on same type numbers. We define abstract function on BasicNumber, that's because I want to 
 * force subclass to implement operations on their own type. It should just call super if not
 * the same type. BasicNumber do the promotion and call the corresponding sublcass' method.
 * 
 * In addition, each subclass should have a constructor(CohoNumber) and constructor(Number) and 
 * static method create(CohoNumber) create(Number). They should have the same function with convert 
 * functions. It's recommended all functions call constructor(CohoNumber). Number can be 
 * converted to BasicNumber by BasicNumber.create();
 */	
package coho.common.number;

/**
 * @author chaoyan
 *
 */
public abstract class BasicScale extends BasicNumber implements ScaleNumber {
	public abstract BasicScale one();
    public abstract BasicScale zero();
    public abstract BasicScale random();
	/*****************************
	 * Conert Number CohoNumber to BasicNumber 
	 *****************************/
	/*
	 * convert x to the same type. truncated if out of range.
	 */
	public abstract BasicScale convert(CohoNumber x);//convert x to the same type
	public abstract BasicScale convert(Number x);
	public abstract ScaleNumber[][] createArray(int nrow, int ncols);
	public abstract ScaleNumber[] createVector(int length);
	public ScaleNumber scale(){	return this; }
	public ScaleNumber error(){ return this.zero();};
	public double doubleValue(ROUNDMODE mode){//default one for boolean, int, long, and double.
		return doubleValue();
	}


    /**************************************************
	 * opearations depends on the data.
	 * But each subclass should only operate on number of the same type. 
	 * For different type, call super. Which will promote them to same. 
	 **************************************************/
    public abstract ScaleNumber abs();
    public abstract ScaleNumber recip();
    public abstract ScaleNumber negate();
    public abstract ScaleNumber sqrt();
    /*
     * Each subclass should also implement
     * public T add(T)
     * public T sub(T)
     * public T mult(T);
     * public T div(T);
     * public T max(T);
     * public T min(T);
     * public T compareTo(T);
     */

    /*****************************************
	 * Operations Within ScaleNumber. Return ScaleNumber as possible.
	 *****************************************/    
    public ScaleNumber add(ScaleNumber x){
    	return ScaleType.promoteOp(this, x, CohoNumber.ArithOp.ADD);
    }
    public ScaleNumber sub(ScaleNumber x){
    	return ScaleType.promoteOp(this, x, CohoNumber.ArithOp.SUB);
    }
    public ScaleNumber mult(ScaleNumber x){
    	return ScaleType.promoteOp(this, x, CohoNumber.ArithOp.MULT);
    }
    public ScaleNumber div(ScaleNumber x){
    	return ScaleType.promoteOp(this, x, CohoNumber.ArithOp.DIV);
    }
    public ScaleNumber max(ScaleNumber x){
    	return ScaleType.promoteOp(this, x, CohoNumber.ArithOp.MAX);
    }
    public ScaleNumber min(ScaleNumber x){
    	return ScaleType.promoteOp(this, x, CohoNumber.ArithOp.MIN);
    }
    public int compareTo(ScaleNumber x){
    	return ScaleType.promoteOp(this, x, CohoNumber.ArithOp.CMP).intValue();
    }
	/******************************
	 * Operations for Number
	 ******************************/
	public ScaleNumber add(Number x) {
    	return ScaleType.promoteOp(this, x, CohoNumber.ArithOp.ADD);
	}
	public ScaleNumber sub(Number x){
    	return ScaleType.promoteOp(this, x, CohoNumber.ArithOp.SUB);
	}
	public ScaleNumber mult(Number x){
    	return ScaleType.promoteOp(this, x, CohoNumber.ArithOp.MULT);
	}
	public ScaleNumber div(Number x){
    	return ScaleType.promoteOp(this, x, CohoNumber.ArithOp.DIV);
	}
	public ScaleNumber max(Number x){
    	return ScaleType.promoteOp(this, x, CohoNumber.ArithOp.MAX);
	}
	public ScaleNumber min(Number x){
    	return ScaleType.promoteOp(this, x, CohoNumber.ArithOp.MIN);
	}
	public int compareTo(Number x){
    	return ScaleType.promoteOp(this, x, CohoNumber.ArithOp.CMP).intValue();
	}

	/***************
	 * Others
	 ***************/
	public abstract ScaleType type(); 
}
