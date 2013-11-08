package coho.common.number;

public class IntervalType extends BasicType {
	public IntervalType(Class classType, IntervalNumber zero, IntervalNumber one){
		super(classType,zero,one);
	}
	public IntervalNumber zero(){
		return (IntervalNumber)zero;
	}
	public IntervalNumber one(){
		return (IntervalNumber)one;
	}

	
	private static IntervalType[] types = null;
	private static int typeKey(IntervalType x){
		if(types==null){
			types = new IntervalType[]{IntegerInterval.type, LongInterval.type, DoubleInterval.type, APRInterval.type};
		}
		for(int i=0; i<types.length; i++){
			if(x==types[i])
				return i;
		}
		throw new IllegalArgumentException("unknown CohoType:  " + x.name());
	}

	public IntervalType promote(IntervalType that){
		if(this==that)
			return this;
		int tk1 = typeKey(this), tk2 = typeKey(that);
		int tk = Math.max(tk1,tk2);
		return types[tk]; 
	}
	public static IntervalType promote(ScaleType t){
		if(t==CohoBoolean.type || t==CohoInteger.type)
			return IntegerInterval.type;
		else if(t==CohoLong.type)
			return LongInterval.type;
		else if(t==CohoDouble.type)
			return DoubleInterval.type;
		else if(t==CohoAPR.type)
			return APRInterval.type;
		else
			throw new IllegalArgumentException("Unknown subclass of CohoNumber: " + t);
	}
	
	private static IntervalNumber promote(IntervalNumber a, IntervalType t){
		if(t == a.type()){
			return a;
		}
		if(t == IntegerInterval.type)
			return IntegerInterval.create(a);
		else if(t == LongInterval.type)
			return LongInterval.create(a);
		else if(t == DoubleInterval.type)
			return DoubleInterval.create(a);
		else if(t == APRInterval.type)
			return APRInterval.create(a);
		throw new IllegalArgumentException("Unknown subclass of CohoNumber: " + t);
	}	
	public static IntervalNumber promote(ScaleNumber a){
		if(a.type()==CohoBoolean.type || a.type()==CohoInteger.type)
			return IntegerInterval.create(a);
		else if(a.type()==CohoLong.type)
			return LongInterval.create(a);
		else if(a.type()==CohoDouble.type)
			return DoubleInterval.create(a);
		else if(a.type()==CohoAPR.type)
			return APRInterval.create(a);
		else
			throw new IllegalArgumentException("Unknown subclass of CohoNumber: " + a.type());
	}
	public static IntervalNumber promote(Number a){
		if(a instanceof Integer || a instanceof Short || a instanceof Byte)
			return IntegerInterval.create(a);
		else if(a instanceof Long)
			return LongInterval.create(a);
		else if(a instanceof Double || a instanceof Float)
			return DoubleInterval.create(a);
		else 
			return DoubleInterval.create(a);
	}
	

	
	public static IntervalNumber[] promote(IntervalNumber a, IntervalNumber b){
		if(a.type()==b.type())
			return new IntervalNumber[]{a,b};
		IntervalType t = a.type().promote(b.type());
		return new IntervalNumber[]{promote(a,t),promote(b,t)};
	}
	public static IntervalNumber[] promote(IntervalNumber a, ScaleNumber b){
		return promote(a,promote(b));
	}
	public static IntervalNumber[] promote(IntervalNumber a, Number b){
		return promote(a, promote(b));
	}

	public static IntervalNumber promoteOp(IntervalNumber n1, IntervalNumber n2, CohoNumber.ArithOp op){
		IntervalNumber[] operands = promote(n1,n2);
		IntervalType t = operands[0].type();
		if(t == IntegerInterval.type){
			IntegerInterval op1 = (IntegerInterval)operands[0];
			IntegerInterval op2 = (IntegerInterval)operands[1];
			switch(op){
			case ADD:
				return op1.add(op2);
			case SUB:
				return op1.sub(op2);
			case MULT:
				return op1.mult(op2);
			case DIV:
				return op1.div(op2);
			case MAX:
				return op1.max(op2);
			case MIN:
				return op1.min(op2);
			case CMP:
				return IntegerInterval.create(op1.compareTo(op2));
			default:
				throw new UnsupportedOperationException(op.name());
			}
		}else if(t == LongInterval.type){
			LongInterval op1 = (LongInterval)operands[0];
			LongInterval op2 = (LongInterval)operands[1];
			switch(op){
			case ADD:
				return op1.add(op2);
			case SUB:
				return op1.sub(op2);
			case MULT:
				return op1.mult(op2);
			case DIV:
				return op1.div(op2);
			case MAX:
				return op1.max(op2);
			case MIN:
				return op1.min(op2);
			case CMP:
				return IntegerInterval.create(op1.compareTo(op2));
			default:
				throw new UnsupportedOperationException(op.name());
			}			
		}else if(t == DoubleInterval.type){
			DoubleInterval op1 = (DoubleInterval)operands[0];
			DoubleInterval op2 = (DoubleInterval)operands[1];
			switch(op){
			case ADD:
				return op1.add(op2);
			case SUB:
				return op1.sub(op2);
			case MULT:
				return op1.mult(op2);
			case DIV:
				return op1.div(op2);
			case MAX:
				return op1.max(op2);
			case MIN:
				return op1.min(op2);
			case CMP:
				return IntegerInterval.create(op1.compareTo(op2));
			default:
				throw new UnsupportedOperationException(op.name());
			}			
		}else if(t == APRInterval.type){
			APRInterval op1 = (APRInterval)operands[0];
			APRInterval op2 = (APRInterval)operands[1];
			switch(op){
			case ADD:
				return op1.add(op2);
			case SUB:
				return op1.sub(op2);
			case MULT:
				return op1.mult(op2);
			case DIV:
				return op1.div(op2);
			case MAX:
				return op1.max(op2);
			case MIN:
				return op1.min(op2);
			case CMP:
				return IntegerInterval.create(op1.compareTo(op2));
			default:
				throw new UnsupportedOperationException(op.name());
			}			
		}else
			throw new RuntimeException("Unknow IntervalType"+t);
	}
	public static IntervalNumber promoteOp(IntervalNumber n1, Number n2, CohoNumber.ArithOp op){
		IntervalNumber[] operands = promote(n1,n2);
		IntervalType t = operands[0].type();
		if(t == IntegerInterval.type){
			IntegerInterval op1 = (IntegerInterval)operands[0];
			IntegerInterval op2 = (IntegerInterval)operands[1];
			switch(op){
			case ADD:
				return op1.add(op2);
			case SUB:
				return op1.sub(op2);
			case MULT:
				return op1.mult(op2);
			case DIV:
				return op1.div(op2);
			case MAX:
				return op1.max(op2);
			case MIN:
				return op1.min(op2);
			case CMP:
				return IntegerInterval.create(op1.compareTo(op2));
			default:
				throw new UnsupportedOperationException(op.name());
			}
		}else if(t == LongInterval.type){
			LongInterval op1 = (LongInterval)operands[0];
			LongInterval op2 = (LongInterval)operands[1];
			switch(op){
			case ADD:
				return op1.add(op2);
			case SUB:
				return op1.sub(op2);
			case MULT:
				return op1.mult(op2);
			case DIV:
				return op1.div(op2);
			case MAX:
				return op1.max(op2);
			case MIN:
				return op1.min(op2);
			case CMP:
				return IntegerInterval.create(op1.compareTo(op2));
			default:
				throw new UnsupportedOperationException(op.name());
			}			
		}else if(t == DoubleInterval.type){
			DoubleInterval op1 = (DoubleInterval)operands[0];
			DoubleInterval op2 = (DoubleInterval)operands[1];
			switch(op){
			case ADD:
				return op1.add(op2);
			case SUB:
				return op1.sub(op2);
			case MULT:
				return op1.mult(op2);
			case DIV:
				return op1.div(op2);
			case MAX:
				return op1.max(op2);
			case MIN:
				return op1.min(op2);
			case CMP:
				return IntegerInterval.create(op1.compareTo(op2));
			default:
				throw new UnsupportedOperationException(op.name());
			}			
		}else if(t == APRInterval.type){
			APRInterval op1 = (APRInterval)operands[0];
			APRInterval op2 = (APRInterval)operands[1];
			switch(op){
			case ADD:
				return op1.add(op2);
			case SUB:
				return op1.sub(op2);
			case MULT:
				return op1.mult(op2);
			case DIV:
				return op1.div(op2);
			case MAX:
				return op1.max(op2);
			case MIN:
				return op1.min(op2);
			case CMP:
				return IntegerInterval.create(op1.compareTo(op2));
			default:
				throw new UnsupportedOperationException(op.name());
			}			
		}else
			throw new RuntimeException("Unknow IntervalType"+t);
	}

}
