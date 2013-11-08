package coho.common.number;


public class ScaleType extends BasicType{
	public ScaleType(Class classType, ScaleNumber zero, ScaleNumber one){
		super(classType,zero,one);
	}
	public ScaleNumber zero(){
		return (ScaleNumber)zero;
	}
	public ScaleNumber one(){
		return (ScaleNumber)one;
	}
	
	/*
	 * Promotion data
	 * Promotion rule: 	1)boolean->int->double->apr.
	 * 					2)basic->interval 
	 */
	private static ScaleType[] types = null;
	private static int typeKey(ScaleType x){
		if(types==null){
			types = new ScaleType[] {CohoBoolean.type, 
					CohoInteger.type, CohoLong.type, CohoDouble.type, CohoAPR.type};
		}
		for(int i=0; i<types.length; i++){
			if(x==types[i])
				return i;
		}
		throw new IllegalArgumentException("unknown CohoType:  " + x.name());
	}

	/**
	 * This function depends on the order of types to work correctly;
	 */
	public ScaleType promote(ScaleType that) {
		if(this==that){
			return this;
		}
		int tk1 = typeKey(this), tk2 = typeKey(that);
		int tk = Math.max(tk1,tk2);
		return types[tk];
	}

	public static ScaleNumber[] promote(ScaleNumber a, ScaleNumber b){
		ScaleType t = (a.type()==b.type())?a.type():a.type().promote(b.type());
		return new ScaleNumber[]{promote(a,t),promote(b,t)};
	}
	public static ScaleNumber[] promote(ScaleNumber a, Number b){
		return promote(a,promote(b));
	}


	private static ScaleNumber promote(ScaleNumber b, ScaleType t){
		if(t == b.type()){
			return b;
		}
		if(t == CohoBoolean.type)
			return CohoBoolean.create(b);
		else if(t == CohoInteger.type)
			return CohoInteger.create(b);
		else if(t == CohoLong.type)
			return CohoLong.create(b);
		else if(t == CohoDouble.type)
			return CohoDouble.create(b);
		else if(t == CohoAPR.type)
			return CohoAPR.create(b);
		else
			throw new IllegalArgumentException("Unknown subclass of CohoNumber: " + t);
	}
	public static ScaleNumber promote(Number x){
		if( x instanceof Integer || x instanceof Short || x instanceof Byte){
			return CohoInteger.create(x);
		}else if(x instanceof Long){
			return CohoLong.create(x);
		}else if(x instanceof Float || x instanceof Double){
			return CohoDouble.create(x);
		}else{
			return CohoDouble.create(x.doubleValue());
		}
	}
	
	
	public static ScaleNumber promoteOp(ScaleNumber n1, ScaleNumber n2, CohoNumber.ArithOp op){
		ScaleNumber[] operands = promote(n1,n2);
		ScaleType t = operands[0].type();
		if(t == CohoBoolean.type){
			CohoBoolean op1 = (CohoBoolean)operands[0];
			CohoBoolean op2 = (CohoBoolean)operands[1];
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
				return CohoInteger.create(op1.compareTo(op2));
			default:
				throw new UnsupportedOperationException(op.name());
			}
		}else if(t == CohoInteger.type){
			CohoInteger op1 = (CohoInteger)operands[0];
			CohoInteger op2 = (CohoInteger)operands[1];
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
				return CohoInteger.create(op1.compareTo(op2));
			default:
				throw new UnsupportedOperationException(op.name());
			}			
		}else if(t == CohoLong.type){
			CohoLong op1 = (CohoLong)operands[0];
			CohoLong op2 = (CohoLong)operands[1];
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
				return CohoInteger.create(op1.compareTo(op2));
			default:
				throw new UnsupportedOperationException(op.name());
			}			
		}else if(t == CohoDouble.type){
			CohoDouble op1 = (CohoDouble)operands[0];
			CohoDouble op2 = (CohoDouble)operands[1];
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
				return CohoInteger.create(op1.compareTo(op2));
			default:
				throw new UnsupportedOperationException(op.name());
			}			
		}else if(t == CohoAPR.type){
			CohoAPR op1 = (CohoAPR)operands[0];
			CohoAPR op2 = (CohoAPR)operands[1];
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
				return CohoInteger.create(op1.compareTo(op2));
			default:
				throw new UnsupportedOperationException(op.name());
			}			
		}else
			throw new RuntimeException("Unknow ScaleType"+t);
    	
	}
	public static ScaleNumber promoteOp(ScaleNumber n1, Number n2, CohoNumber.ArithOp op){
		ScaleNumber[] operands = promote(n1,n2);
		ScaleType t = operands[0].type();
		if(t == CohoBoolean.type){
			CohoBoolean op1 = (CohoBoolean)operands[0];
			CohoBoolean op2 = (CohoBoolean)operands[1];
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
				return CohoInteger.create(op1.compareTo(op2));
			default:
				throw new UnsupportedOperationException(op.name());
			}
		}else if(t == CohoInteger.type){
			CohoInteger op1 = (CohoInteger)operands[0];
			CohoInteger op2 = (CohoInteger)operands[1];
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
				return CohoInteger.create(op1.compareTo(op2));
			default:
				throw new UnsupportedOperationException(op.name());
			}			
		}else if(t == CohoLong.type){
			CohoLong op1 = (CohoLong)operands[0];
			CohoLong op2 = (CohoLong)operands[1];
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
				return CohoInteger.create(op1.compareTo(op2));
			default:
				throw new UnsupportedOperationException(op.name());
			}			
		}else if(t == CohoDouble.type){
			CohoDouble op1 = (CohoDouble)operands[0];
			CohoDouble op2 = (CohoDouble)operands[1];
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
				return CohoInteger.create(op1.compareTo(op2));
			default:
				throw new UnsupportedOperationException(op.name());
			}			
		}else if(t == CohoAPR.type){
			CohoAPR op1 = (CohoAPR)operands[0];
			CohoAPR op2 = (CohoAPR)operands[1];
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
				return CohoInteger.create(op1.compareTo(op2));
			default:
				throw new UnsupportedOperationException(op.name());
			}			
		}else
			throw new RuntimeException("Unknow ScaleType"+t);
    	
	}

}
