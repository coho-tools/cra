package coho.common.number;
import java.math.BigInteger;
import java.util.*;

public class CohoAPR extends BasicScale implements Round {
	final BigInteger numerator, denominator;//they are normalized, and denominator are positive
	public BigInteger numerator(){return numerator;}
	public BigInteger denominator(){return denominator;}
	/*
	 * Convert a double to a APR
	 */
	public static final int BIAS=1023;
	private static BigInteger[] fraction(double val){
		long bits = Double.doubleToLongBits(val);
		int sign = ((bits >> 63) == 0) ? 1 : -1;
		int exp = (int)((bits >> 52) & 0x7ffL);
		if(exp == 2047){
			throw new ArithmeticException("The double value "+val+" is Infinity or NaN, can't be represented as rational number");
		}
		
		long frac = bits & 0x000fffffffffffffL ; 
		if(exp!=0){//exp==0 for denormalized number
			frac = frac | 0x0010000000000000L ;//add 1 here. 1.Signficant for normalized number
		}		
		int numOfZeros = Math.min(Long.numberOfTrailingZeros(frac),52);//if significant are all zeros, 64 returned for numberofTrailingZeros() function.
		frac = frac >>> numOfZeros;//remove zeros, unsigned shift
		
		exp = Math.max(exp-BIAS,-BIAS+1);//biased exponent, -1022 for denormalized number
		
		int numOfShifts = 52 - numOfZeros;//move the decimal point to make the maginitude a integer
		int numerExp = exp-numOfShifts;//numerator is frac*2^numerExp
		int denomExp = 0;//denominator is 1*2^0. 
		if(numerExp<0){
			denomExp = - numerExp;//numerator is frac and denominator is 2^(-numerExp)
			numerExp = 0;
		}

		//numerator is frac*2^numerExp
		int n = numOfShifts+numerExp+1;//number of bits to store numerator		
		byte[] numer = new byte[n/8+(n%8==0?0:1)];
		frac = frac<<numerExp%8; //numerator is frac*2^(numerExp%8)*2^(numerExp/8)
		for(int i = numerExp/8; i<numer.length;i++){//big-endian
			numer[numer.length-1-i] = (byte)(frac%256);
			frac = frac>>8;
		}
		BigInteger numerator = new BigInteger(sign,numer);
		
		//denominator is 2^denomExp
		n = denomExp+1;//number of bits to store denominator
		byte[] denom = new byte[n/8+(n%8==0?0:1)];
		denom[0] |= 1<<(denomExp%8);//big-endian, 2^((denomExp%8)+(8*(denom.length-1)))=2^(deomExp/8+deomExp%8)
		BigInteger denominator = new BigInteger(1,denom);
		
		return new BigInteger[]{numerator,denominator};
	}
//	private static BigInteger[] fraction(double val){
//		long bits = Double.doubleToLongBits(val);
//		//int exp  = (int)((bits & 0x7ff0000000000000L)>>>52)-1023;
//		int exp = (int)((bits >> 52) & 0x7ffL)-1023;
//		if(exp == 1024){
//			throw new ArithmeticException("The double value "+val+" is Infinity or NaN, can't be represented as rational number");
//		}
//		//int sign = (int)((bits & 0x8000000000000000L)>>>63);
//		int sign = ((bits >> 63) == 0) ? 1 : -1;
//		long frac = bits & 0x000fffffffffffffL ; 
//		int num = 52 - Math.min(Long.numberOfTrailingZeros(frac),52);
//		
//		if(exp!=-1023)	frac = frac | 0x0010000000000000L ;//fraction of denormalized and normalized number
//		frac = frac >>> (52-num);//remove zeros
//		int numerExp = exp-num;
//		int denomExp = 0;
//		if(numerExp<0){
//			denomExp = - numerExp;
//			numerExp = 0;
//		}
//		
//		int n = num+1+numerExp;		
//		byte[] numer = new byte[n/8+(n%8==0?0:1)];
//		frac = frac<<numerExp%8;
//		for(int i = numerExp/8; i<numer.length;i++){//big-endian
//			numer[numer.length-1-i] = (byte)(frac%256);
//			frac = frac>>8;
//		}
//		//BigInteger numerator = new BigInteger(sign==1?-1:1,numer);
//		BigInteger numerator = new BigInteger(sign,numer);
//		
//		n = (denomExp+1);
//		byte[] denom = new byte[n/8+(n%8==0?0:1)];
//		denom[denom.length-1-denomExp/8] |= 1<<(denomExp%8);
//		BigInteger denominator = new BigInteger(1,denom);
//		return new BigInteger[]{numerator,denominator};
//	}
	
	private BigInteger[] normalize(BigInteger numerator, BigInteger denominator){
		if(denominator.equals(BigInteger.ZERO))
			throw new ArithmeticException("Zero divisor");
			//return this; //_numerator.compareTo(BigInteger.ZERO);//throw new ArithmeticException("Zero divisor");
		if(denominator.compareTo(BigInteger.ZERO)<0){
			numerator = numerator.negate();
			denominator = denominator.negate();			
		}
		BigInteger gcd = numerator.gcd(denominator);
		if(!gcd.equals(BigInteger.ONE)){
			numerator = numerator.divide(gcd);
			denominator = denominator.divide(gcd);
		}
		return new BigInteger[]{numerator,denominator};	
	}
	public CohoAPR(CohoNumber x){
		if(x instanceof CohoAPR){ 
			this.numerator = ((CohoAPR)x).numerator();
			this.denominator = ((CohoAPR)x).denominator();
		}else{
			BigInteger[] temp = fraction(x.doubleValue());
			this.numerator = temp[0];
			this.denominator = temp[1];
		}
    }
	public CohoAPR(Number x){
		BigInteger[] temp = fraction(x.doubleValue());
		this.numerator = temp[0];
		this.denominator = temp[1];
	}
	public CohoAPR(CohoNumber numerator, CohoNumber denominator){
		this(BigInteger.valueOf(numerator.longValue()), BigInteger.valueOf(denominator.longValue()));
	}
	public CohoAPR(Number numerator, Number denominator){
		this(BigInteger.valueOf(numerator.longValue()), BigInteger.valueOf(denominator.longValue()));
	}
	public CohoAPR(BigInteger numerator, BigInteger denominator){
		BigInteger[] temp = normalize(numerator, denominator);
		numerator = temp[0];
		denominator = temp[1];
		this.numerator = numerator;
		this.denominator = denominator;
	}
	
	public static CohoAPR create(CohoNumber x){
		return new CohoAPR(x);
	}
	public static CohoAPR create(Number x){
		return new CohoAPR(x);
	}
	public static CohoAPR create(CohoNumber numerator, CohoNumber denominator){
		return new CohoAPR(numerator,denominator);
	}
	public static CohoAPR create(Number numerator, Number denominator){
		return new CohoAPR(numerator,denominator);
	}
	public static CohoAPR create(BigInteger numerator,BigInteger denominator){
		return new CohoAPR(numerator,denominator);
	}
	
	public CohoAPR convert(CohoNumber x){
		if(x instanceof CohoAPR)
			return (CohoAPR)x;
		return new CohoAPR(x);
	}
	public CohoAPR convert(Number x){
		return new CohoAPR(x);
	}
	public CohoAPR convert(CohoNumber numerator,CohoNumber denominator){
		return new CohoAPR(numerator,denominator);
	}
	public CohoAPR convert(Number numerator, Number denominator){
		return new CohoAPR(numerator,denominator);
	}
	public CohoAPR convert(BigInteger numerator, BigInteger denominator){
		return new CohoAPR(numerator,denominator);
	}
	public CohoAPR[][] createArray(int nrows,int ncols){
		return new CohoAPR[nrows][ncols];
	}
	public CohoAPR[] createVector(int length){
		return new CohoAPR[length];
	}

	public static final CohoAPR zero = new CohoAPR(0,1);
	public static final CohoAPR one = new CohoAPR(1,1);
	public CohoAPR zero(){
		return zero;
	}
	public CohoAPR one(){
		return one;
	}
	public CohoAPR random(){//XXX or use double?
		BigInteger numerator = new BigInteger(32,new Random(CohoLong.zero.random().longValue()));
		BigInteger denominator = new BigInteger(32,new Random(CohoLong.zero.random().longValue()));
		return new CohoAPR(numerator,denominator);
	}
	
	public CohoAPR abs(){
		return numerator.signum()>=0?this:create(numerator.abs(),denominator);
	}
	public CohoAPR negate(){
		return create(numerator.negate(),denominator);
	}
	public CohoAPR recip(){
		return create(denominator,numerator);
	}
	/*
	 * This is not supported, because the square root of APR is no long a APR. 
	 * To get the approximation, convert it to CohoDouble the compute the sqrt of it.
	 * @see coho.common.number.BasicScale#sqrt()
	 */
	public CohoAPR sqrt(){
		throw new UnsupportedOperationException();
	}
	/*
	 * b/a + d/c = (bc+ad)/ac
	 */
	public CohoAPR add(CohoAPR x){
		BigInteger a = denominator;
		BigInteger b = numerator;
		BigInteger c = x.denominator;
		BigInteger d = x.numerator;		
		BigInteger denominator = a.multiply(c);
		BigInteger numerator = b.multiply(c).add((a.multiply(d)));
		return create(numerator,denominator);	
	}
	public CohoAPR sub(CohoAPR x){
		return add(x.negate());
	}
	/*
	 * b/a * d/c = (bd)/(ac)
	 */
	public CohoAPR mult(CohoAPR x){
		BigInteger a = denominator;
		BigInteger b = numerator;
		BigInteger c = x.denominator;
		BigInteger d = x.numerator;		
		BigInteger denominator = a.multiply(c);
		BigInteger numerator = b.multiply(d);
		return create(numerator,denominator);
	}
	public CohoAPR div(CohoAPR x){
		return mult(x.recip());
	}
	public CohoAPR max(CohoAPR x){
		return compareTo(x)>0?this:x;
	}
	public CohoAPR min(CohoAPR x){
		return compareTo(x)<0?this:x;
	}
	public int compareTo(CohoAPR x){
    	BigInteger bc = this.numerator.multiply(x.denominator);
    	BigInteger ad = this.denominator.multiply(x.numerator);
    	return bc.compareTo(ad);
	}
	
	public long longValue(){
		return numerator.divide(denominator).longValue();
	}
	public double doubleValue(){
		return doubleValue(ROUNDMODE.NEAR);
	}	


	/*
	 * Add operations for BigInteger.
	 */
	static BigInteger floor(BigInteger numerator, BigInteger denominator){
		return numerator.divide(denominator);
	}
	static BigInteger ceil(BigInteger numerator, BigInteger denominator){
		BigInteger[] temp = numerator.divideAndRemainder(denominator);
		if(temp[1].compareTo(BigInteger.ZERO)==0){
			//System.out.println(temp[0].bitLength());
			return temp[0];
		}else{
			return temp[0].add(BigInteger.ONE);
		}
	}
	static BigInteger round(BigInteger numerator, BigInteger denominator){
		BigInteger[] temp = numerator.divideAndRemainder(denominator);
		if(leftShift(temp[1],1).compareTo(denominator)>=0)
			return temp[0].add(BigInteger.ONE);
		else
			return temp[0];
	}
	static int floorLog2(BigInteger num){
		return num.bitLength()-1;
	}
	static int ceilLog2(BigInteger num){
		return num.subtract(BigInteger.ONE).bitLength();
	}
	static BigInteger pow(int n){
		byte[] temp = new byte[((n+1)/8)+((n+1)%8==0?0:1)];
		temp[0] |= 1<<(n%8);
		return new BigInteger(1,temp);//2^n
	}
	static BigInteger leftShift(BigInteger num, int n){
		return num.multiply(pow(n));
	}
	static BigInteger rightShift(BigInteger num, int n){
		return num.divide(pow(n));
	}	
	/*
	 * Round a APR to the double 
	 * 
	 * shift the numerator and denominator to get the 53 bits significant
	 * 
	 * if(n<d) return -(round(-n/d))
	 * if(n==0) return 0;
	 * if(n==d) return 1;
	 * if(n>d)
	 *     exp = floor(log2(floor(n/d)))//estimate exp
	 *     m = floor(n*2^52/(d*2^exp))  //floor/round/ceil
	 * if(n<d)
	 *     exp = -(ceil(log2(ceil(d/n))))
	 *     m = floor(n*2^(52-exp)/d)
	 * return m*2^exp;//x = m&0x000fffffffffffffL
	 * 
	 * for denomalized case (exp<-1022)
	 *     exp = -1022;     
	 *     m = floor(n*2^(1022+52)/d)
	 *     exp == -1023;// to set 0 on the exp bits
	 *     
	 * number | bitlength | floor | ceil
	 *    0   |     0     |       |
	 *    1   |     1     |   0   |   0
	 *    2   |     2     |   1   |   1
	 *    3   |     2     |   1   |   2
	 *    4   |     3     |   2   |   2
	 *    5-7 |     3     |   2   |   3
	 *    8   |     4     |   3   |   3
	 *  
	 * floor(log2(x)) = x.bitLength-1;
	 * ceil(log2(x)) = (x-1).bitLength;       
	 */
	@Override
	public double doubleValue(ROUNDMODE mode){
		int cmp = numerator.compareTo(BigInteger.ZERO);
		if(cmp==0){
			return 0;
		}
		if(cmp<0){//negative
			ROUNDMODE negMode ;
			switch(mode){
			case CEIL:
				negMode = ROUNDMODE.FLOOR;
				break;
			case FLOOR:
				negMode = ROUNDMODE.CEIL;
				break;
			case ZERO:
				negMode = ROUNDMODE.ZERO;
				break;
			default:
				negMode = ROUNDMODE.NEAR;
			}
			return -negate().doubleValue(negMode);
		}
		
		cmp = numerator.compareTo(denominator);
		if(cmp ==0){
			return 1;
		}
		
		//estimate the exp
		int exp;
		if(cmp>0){//n>d normalized number
			//estimate exp;
			BigInteger div = floor(numerator,denominator);//estimate exp by floor.
			exp = floorLog2(div);//floor of log2(div)
		}else{
			BigInteger div = ceil(denominator, numerator);
			exp = ceilLog2(div);
			exp = -exp;
		}

		BigInteger M = null;
		boolean denomalized = false;
		for(boolean round=true; round; ){
			if(exp<-1022){//denomalized number 0.f*2^-1022
				denomalized = true;
				exp = -1022;
			}
			switch(mode){
			case CEIL:
				if(exp>0){
					M = ceil(leftShift(numerator,52),leftShift(denominator,exp));
				}else{
					M = ceil(leftShift(numerator,52-exp),denominator);	
				}
				break;
			case FLOOR:
			case ZERO:
				if(exp>0){
					M = floor(leftShift(numerator,52),leftShift(denominator,exp));
				}else{
					M = floor(leftShift(numerator,52-exp),denominator);				
				}
				break;
			default:
				if(exp>0){
					M = round(leftShift(numerator,52),leftShift(denominator,exp));
				}else{
					M = round(leftShift(numerator,52-exp),denominator);
				}
			}
			if(exp>=1024){
				//or return infinity?
				throw new ArithmeticException("Out of range of double value"+exp+"\t"+numerator.divide(denominator).doubleValue());
			}
			if(M.bitLength()==54){
				exp = exp+1;//reround use the new exp
			}else{
				round = false;
				if(M.bitLength()>54)
					throw new RuntimeException("CohoAPR.round(): Algorithm error "+M.bitLength());
				if(M.bitLength()!=53 && !denomalized)
					throw new RuntimeException("CohoAPR.round(): The significant should be 53 bits "+M.bitLength());
			}
		}

		//use m and exp to construct the double
		long m = M.longValue();
		long l= ( (m&0x000fffffffffffffL) | ((long)(exp+BIAS))<<52 );
		if(denomalized)
			l = (m&0x000fffffffffffffL);//exp should be zero not 1
		return Double.longBitsToDouble(l);
	}
	public static boolean test(){
		boolean result = true;
		int n = 100;
		for(int i=0; i<n; i++){//test n>d
			CohoDouble d = CohoDouble.zero.random();
			double dd = d.doubleValue()*Double.MAX_VALUE;
			CohoAPR apr = CohoAPR.create(d).mult(CohoAPR.create(Double.MAX_VALUE));
			if(dd!=apr.doubleValue(ROUNDMODE.NEAR)){
				System.out.println("original "+dd+" vs. "+apr.doubleValue(ROUNDMODE.NEAR));
				result = false;
			}
		}
		for(int i=0; i<n; i++){//test n<d
			CohoDouble d = CohoDouble.zero.random();
			double dd = d.doubleValue();
			CohoAPR apr = CohoAPR.create(d);
			if(dd!=apr.doubleValue(ROUNDMODE.NEAR)){
				System.out.println("original "+dd+" vs. "+apr.doubleValue(ROUNDMODE.NEAR));
				result = false;
			}
		}
		for(int i=0; i<n; i++){//test denomalized 
			CohoDouble d = CohoDouble.zero.random();
			double dd = d.doubleValue()*Double.MIN_VALUE;
			CohoAPR apr = CohoAPR.create(d).mult(CohoAPR.create(Double.MIN_VALUE));
			if(dd!=apr.doubleValue(ROUNDMODE.NEAR)){
				System.out.println("original "+dd+" vs. "+apr.doubleValue(ROUNDMODE.NEAR));
				result = false;
			}else{
				//System.out.println("pass"+dd);
			}
		}
		//test carry in of ceil and near
		byte[] b = new byte[7];
		for(int i = 0; i<7; i++){
			b[i] = (byte)0xff;
		}
		BigInteger bi = new BigInteger(1,b);
		CohoAPR apr = new CohoAPR(bi,BigInteger.ONE);
		System.out.println(apr.doubleValue(ROUNDMODE.NEAR));
		System.out.println(apr.doubleValue(ROUNDMODE.CEIL));
		System.out.println(apr.doubleValue(ROUNDMODE.FLOOR));
		System.out.println(apr.doubleValue(ROUNDMODE.ZERO));
		
		//test round with different mode
		for(int i=0; i<n; i++){
			CohoDouble d = CohoDouble.zero.random();
			apr = CohoAPR.create(d).mult(CohoAPR.create(Double.MAX_VALUE));
			if(apr.doubleValue(ROUNDMODE.NEAR)!=apr.doubleValue(ROUNDMODE.CEIL)&&apr.doubleValue(ROUNDMODE.NEAR)!=apr.doubleValue(ROUNDMODE.FLOOR)){
				System.out.println("round to near is incorrect");
			}
			if(apr.doubleValue(ROUNDMODE.ZERO)!=
				(apr.compareTo(CohoAPR.zero)>0?apr.doubleValue(ROUNDMODE.FLOOR):apr.doubleValue(ROUNDMODE.CEIL)) ){
				System.out.println("round to zero is incorrect");
				System.out.println(apr.doubleValue(ROUNDMODE.ZERO));
				System.out.println(apr.doubleValue(ROUNDMODE.FLOOR));
				System.out.println(apr.doubleValue(ROUNDMODE.CEIL));
			}
		}
		
		//test doubleValue
		for(int i=0; i<n; i++){
			CohoDouble d = CohoDouble.zero.random();
			double dd = d.doubleValue()*Double.MIN_VALUE;
			apr = CohoAPR.create(dd);
			if(dd!=apr.doubleValue()){
				System.out.println("doubleValue incorrect"+dd+"\t"+apr.doubleValue());
			}
		}

		return result;
	}
	public static ScaleType type = new ScaleType(CohoAPR.class,zero,one);
	public ScaleType type(){
		return type;
	}
	public static boolean printDouble = true;
	public String toString(){
		return toString(printDouble);
	}
	public String toString(boolean printDouble){
		if(printDouble){
			return ""+doubleValue();
		}else{
			return numerator.toString()+"/"+denominator.toString();
		}		
	}
	public static void main(String[] args){
		System.out.println(test());
		//System.out.println(BigInteger.ZERO.bitLength());
//		for (int i=0; i<1000000; i++){
//			CohoDouble d = CohoDouble.zero.random();
//			double dd = d.doubleValue()*Double.MAX_VALUE;
//			BigInteger[] b1 = fraction(dd);
//			BigInteger[] b2 = fraction2(dd);
//			if(b1[0].compareTo(b2[0])!=0){
//				System.out.println("numerator not the same "+b1[0]+" vs. "+b2[0]);
//			}
//			if(b1[1].compareTo(b2[1])!=0){
//				System.out.println("denominator not the same "+b1[1]+" vs. "+b2[1]);
//			}
//		}
//		System.out.println("pass fraction");
	}
}
