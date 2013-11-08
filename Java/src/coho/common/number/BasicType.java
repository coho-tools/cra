package coho.common.number;

/*
 * If you want to add a subclass of CohoNumber, change this class
 */
public abstract class BasicType implements CohoType {
	protected Class classType;
	protected CohoNumber one;
	protected CohoNumber zero;
	protected BasicType(Class classType, CohoNumber zero, CohoNumber one){
		this.classType = classType;
		this.zero = zero;
		this.one = one;
	}
	public Class classType(){
		return classType;
	}
	public String name(){
		return classType.getName();
	}
	public CohoNumber zero(){
		return zero;
	}
	public CohoNumber one(){
		return one;
	}
	
	private static int typeKey(CohoType t){
		if(t instanceof ScaleType)
			return 0;
		if(t instanceof IntervalType)
			return 1;
		throw new UnsupportedOperationException("We only support Scale Number and Interval Number now");
	}

	public BasicType promote(CohoType that) {
		int p1 = typeKey(this);
		int p2 = typeKey(that);
		if(p1==0&&p2==0)
			return ((ScaleType)this).promote((ScaleType)that);//this will not be called
		else if(p1==1&&p2==1)
			return ((IntervalType)this).promote((IntervalType)that);//this will not be called
		else if(p1==1)
			return this.promote(IntervalType.promote((ScaleType)that));
		else// if(p2==1)
			return IntervalType.promote((ScaleType)this).promote(that);
	}
	
	public static CohoNumber[] promote(CohoNumber a, CohoNumber b){
		int p1 = typeKey(a.type());
		int p2 = typeKey(b.type());
		if(p1==0&&p2==0)
			return ScaleType.promote((ScaleNumber)a, (ScaleNumber)b);
		else if(p1==1&&p2==1)
			return IntervalType.promote((IntervalNumber)a,(IntervalNumber)b);
		else if(p1==1)
			return IntervalType.promote((IntervalNumber)a, IntervalType.promote((ScaleNumber)b));
		else
			return IntervalType.promote(IntervalType.promote((ScaleNumber)a),(IntervalNumber)b);
			
	}
	public static CohoNumber[] promote(CohoNumber a, Number b){
		int p = typeKey(a.type());
		if(p==0)
			return ScaleType.promote((ScaleNumber)a, b);
		else 
			return IntervalType.promote((IntervalNumber)a, b);
	}
	
	public static CohoNumber promoteOp(CohoNumber n1, CohoNumber n2, CohoNumber.ArithOp op){
		CohoNumber[] operands = promote(n1,n2);
		int t = typeKey(operands[0].type());
		if(t==0){
			ScaleNumber op1 = (ScaleNumber)operands[0];
			ScaleNumber op2 = (ScaleNumber)operands[1];
			return ScaleType.promoteOp(op1,op2,op);
		}else{
			IntervalNumber op1 = (IntervalNumber)operands[0];
			IntervalNumber op2 = (IntervalNumber)operands[1];
			return IntervalType.promoteOp(op1,op2,op);			
		}		
	}
	public static CohoNumber promoteOp(CohoNumber n1, Number n2, CohoNumber.ArithOp op){
		CohoNumber[] operands = promote(n1,n2);
		int t = typeKey(operands[0].type());
		if(t==0){
			ScaleNumber op1 = (ScaleNumber)operands[0];
			ScaleNumber op2 = (ScaleNumber)operands[1];
			return ScaleType.promoteOp(op1,op2,op);
		}else{
			IntervalNumber op1 = (IntervalNumber)operands[0];
			IntervalNumber op2 = (IntervalNumber)operands[1];
			return IntervalType.promoteOp(op1,op2,op);			
		}		
	}
	public String toString(){
		return name();
	}
}
