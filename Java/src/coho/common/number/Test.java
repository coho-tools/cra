package coho.common.number;

public class Test {
	interface NumberTest{
		public boolean test();
	}
	
	public static NumberTest promoteTest = new NumberTest(){
		public boolean test(){
			System.out.println("--------------------------");
			CohoAPR n1 = CohoAPR.one.random();
			CohoAPR n2 = CohoAPR.one.random();
			compute(n1,n2,CohoNumber.ArithOp.DIV);
			CohoDouble n3 = CohoDouble.zero.random();
			compute(n1,n3,CohoNumber.ArithOp.MULT);
			APRInterval n4 = APRInterval.zero.random();
			compute(n2,n4,CohoNumber.ArithOp.ADD);
			double n5 = 2*(Math.random()-0.5)*Double.MAX_VALUE;
			//double n5 = 1.5646263405606628E308;
			compute(n1,n5,CohoNumber.ArithOp.CMP);
			compute(n3,n5,CohoNumber.ArithOp.SUB);
			compute(n4,n5,CohoNumber.ArithOp.MAX);
			
			System.out.println("..........");
			compute(n1,n5,CohoNumber.ArithOp.ADD);
			compute(n1,n5,CohoNumber.ArithOp.SUB);
			compute(n1,n5,CohoNumber.ArithOp.MULT);
			compute(n1,n5,CohoNumber.ArithOp.DIV);
			compute(n1,n5,CohoNumber.ArithOp.MAX);
			compute(n1,n5,CohoNumber.ArithOp.MIN);
			try{
				compute(n1,n5,CohoNumber.ArithOp.CMP);
			}catch(NotcomparableIntervalException e){
				System.out.println("not comparable");
			}

			System.out.println("..........");
			compute(n3,n4,CohoNumber.ArithOp.ADD);
			compute(n3,n4,CohoNumber.ArithOp.SUB);
			compute(n3,n4,CohoNumber.ArithOp.MULT);
			compute(n3,n4,CohoNumber.ArithOp.DIV);
			compute(n3,n4,CohoNumber.ArithOp.MAX);
			compute(n3,n4,CohoNumber.ArithOp.MIN);
			try{
				compute(n3,n4,CohoNumber.ArithOp.CMP);
			}catch(NotcomparableIntervalException e){
				System.out.println("not comparable");
			}
			
			System.out.println("..........");
			compute(n4,n5,CohoNumber.ArithOp.ADD);
			compute(n4,n5,CohoNumber.ArithOp.SUB);
			compute(n4,n5,CohoNumber.ArithOp.MULT);
			compute(n4,n5,CohoNumber.ArithOp.DIV);
			compute(n4,n5,CohoNumber.ArithOp.MAX);
			compute(n4,n5,CohoNumber.ArithOp.MIN);
			try{
				compute(n4,n5,CohoNumber.ArithOp.CMP);
			}catch(NotcomparableIntervalException e){
				System.out.println("not comparable");
			}

			
			return true;
		}
	};
	public static NumberTest scaleTest = new NumberTest(){
		public boolean test(){
			System.out.println("--------------------------");
			CohoBoolean n1 = CohoBoolean.zero.random();
			CohoInteger n2 = CohoInteger.zero.random();
			CohoLong n3 = CohoLong.zero.random();
			CohoDouble n4 = CohoDouble.zero.random();
			CohoAPR n5 = CohoAPR.zero.random();
			CohoDouble n6 = CohoDouble.zero.random();
			CohoAPR n7 = CohoAPR.zero.random();
			CohoLong n8 = CohoLong.zero.random();
			
			compute(n1,n2,CohoNumber.ArithOp.ADD);
			compute(n2,n3,CohoNumber.ArithOp.SUB);
			compute(n3,n4,CohoNumber.ArithOp.MULT);
			compute(n4,n5,CohoNumber.ArithOp.DIV);
			compute(n4,n5,CohoNumber.ArithOp.MAX);
			compute(n4,n5,CohoNumber.ArithOp.MIN);
			compute(n4,n5,CohoNumber.ArithOp.CMP);
			
			System.out.println(".......");
			compute(n4,n6,CohoNumber.ArithOp.ADD);
			compute(n4,n6,CohoNumber.ArithOp.SUB);
			compute(n4,n6,CohoNumber.ArithOp.MULT);
			compute(n4,n6,CohoNumber.ArithOp.DIV);
			compute(n4,n6,CohoNumber.ArithOp.MAX);
			compute(n4,n6,CohoNumber.ArithOp.MIN);			
			compute(n4,n6,CohoNumber.ArithOp.CMP);
			compute(n4,CohoNumber.ArithOp.ABS);
			compute(n4,CohoNumber.ArithOp.NEGATE);
			compute(n4,CohoNumber.ArithOp.RECIP);
			compute(n4,CohoNumber.ArithOp.SQRT);
			
			System.out.println(".......");
			compute(n5,n7,CohoNumber.ArithOp.ADD);
			compute(n5,n7,CohoNumber.ArithOp.SUB);
			compute(n5,n7,CohoNumber.ArithOp.MULT);
			compute(n5,n7,CohoNumber.ArithOp.DIV);
			compute(n5,n7,CohoNumber.ArithOp.MAX);
			compute(n5,n7,CohoNumber.ArithOp.MIN);			
			compute(n5,n7,CohoNumber.ArithOp.CMP);			
			compute(n5,CohoNumber.ArithOp.ABS);
			compute(n5,CohoNumber.ArithOp.NEGATE);
			compute(n5,CohoNumber.ArithOp.RECIP);
			
			System.out.println(".......");
			compute(n3,n8,CohoNumber.ArithOp.ADD);
			compute(n3,n8,CohoNumber.ArithOp.SUB);
			compute(n3,n8,CohoNumber.ArithOp.MULT);
			compute(n3,n8,CohoNumber.ArithOp.DIV);
			compute(n3,n8,CohoNumber.ArithOp.MAX);
			compute(n3,n8,CohoNumber.ArithOp.MIN);			
			compute(n3,n8,CohoNumber.ArithOp.CMP);
			compute(n3,CohoNumber.ArithOp.ABS);
			compute(n3,CohoNumber.ArithOp.NEGATE);
			compute(n3,CohoNumber.ArithOp.RECIP);
			compute(n3,CohoNumber.ArithOp.SQRT);
			return true;
			
		}
	};
	public static NumberTest intervalTest = new NumberTest(){
		public boolean test(){
			System.out.println("--------------------------");
			IntegerInterval n1 = IntegerInterval.zero.random();
			IntegerInterval n2 = IntegerInterval.zero.random();
			LongInterval n3 = LongInterval.zero.random();
			DoubleInterval n4 = DoubleInterval.zero.random();
			APRInterval n5 = APRInterval.zero.random();
			DoubleInterval n6 = DoubleInterval.zero.random();
			APRInterval n7 = APRInterval.zero.random();
			LongInterval n8 = LongInterval.zero.random();
			
			compute(n1,n2,CohoNumber.ArithOp.ADD);
			compute(n2,n3,CohoNumber.ArithOp.SUB);
			compute(n3,n4,CohoNumber.ArithOp.MULT);
			try{
				compute(n4,n5,CohoNumber.ArithOp.DIV);
			}catch(ArithmeticException e){
				System.out.println("divided by zero");
			}
			compute(n3,n4,CohoNumber.ArithOp.MAX);
			compute(n3,n4,CohoNumber.ArithOp.MIN);
			try{
				compute(n3,n4,CohoNumber.ArithOp.CMP);
			}catch(NotcomparableIntervalException e){
				System.out.println("not comparable");
			}
			
			System.out.println(".......");
			compute(n4,n6,CohoNumber.ArithOp.ADD);
			compute(n4,n6,CohoNumber.ArithOp.SUB);
			compute(n4,n6,CohoNumber.ArithOp.MULT);
			try{
				compute(n4,n6,CohoNumber.ArithOp.DIV);
			}catch(ArithmeticException e){
				System.out.println("divided by zero");
			}
			compute(n4,n6,CohoNumber.ArithOp.MAX);
			compute(n4,n6,CohoNumber.ArithOp.MIN);			
			try{
				compute(n4,n6,CohoNumber.ArithOp.CMP);
			}catch(NotcomparableIntervalException e){
				System.out.println("not comparable");
			}

			compute(n4,CohoNumber.ArithOp.ABS);
			compute(n4,CohoNumber.ArithOp.NEGATE);
			try{
				compute(n4,CohoNumber.ArithOp.RECIP);
			}catch(ArithmeticException e){
				System.out.println("divided by zero");
			}
			try{
				compute(n4,CohoNumber.ArithOp.SQRT);
			}catch(ArithmeticException e){
				System.out.println("divided by zero");
			}
			
			System.out.println(".......");
			compute(n5,n7,CohoNumber.ArithOp.ADD);
			compute(n5,n7,CohoNumber.ArithOp.SUB);
			compute(n5,n7,CohoNumber.ArithOp.MULT);
			try{
				compute(n5,n7,CohoNumber.ArithOp.DIV);
			}catch(ArithmeticException e){
				System.out.println("divided by zero");
			}
			compute(n5,n7,CohoNumber.ArithOp.MAX);
			compute(n5,n7,CohoNumber.ArithOp.MIN);
			try{
				compute(n5,n7,CohoNumber.ArithOp.CMP);
			}catch(NotcomparableIntervalException e){
				System.out.println("not comparable");
			}
			compute(n5,CohoNumber.ArithOp.ABS);
			compute(n5,CohoNumber.ArithOp.NEGATE);
			try{
				compute(n5,CohoNumber.ArithOp.RECIP);
			}catch(ArithmeticException e){
				System.out.println("divided by zero");
			}
			
			System.out.println(".......");
			compute(n3,n8,CohoNumber.ArithOp.ADD);
			compute(n3,n8,CohoNumber.ArithOp.SUB);
			compute(n3,n8,CohoNumber.ArithOp.MULT);
			try{
				compute(n3,n8,CohoNumber.ArithOp.DIV);
			}catch(ArithmeticException e){
				System.out.println("divided by zero");
			}
			compute(n3,n8,CohoNumber.ArithOp.MAX);
			compute(n3,n8,CohoNumber.ArithOp.MIN);
			try{
				compute(n3,n8,CohoNumber.ArithOp.CMP);
			}catch(NotcomparableIntervalException e){
				System.out.println("not comparable");
			}

			compute(n3,CohoNumber.ArithOp.ABS);
			compute(n3,CohoNumber.ArithOp.NEGATE);
			try{
				compute(n3,CohoNumber.ArithOp.RECIP);
			}catch(ArithmeticException e){
				System.out.println("divided by zero");
			}
			try{
				compute(n3,CohoNumber.ArithOp.SQRT);
			}catch(ArithmeticException e){
				System.out.println("divided by zero");
			}
			return true;
			
		}
	};
	public static void main(String[] args) {
		promoteTest.test();
		scaleTest.test();
		intervalTest.test();
	}
	private static void compute(CohoNumber n1, CohoNumber n2, CohoNumber.ArithOp op){
		String result = null;
		switch(op){
		case ADD:
			result = n1+"+"+n2+"=: "+n1.add(n2)+"\t"+(n1.doubleValue()+n2.doubleValue());
			break;
		case SUB:
			result = n1+"-"+n2+"=: "+n1.sub(n2)+"\t"+(n1.doubleValue()-n2.doubleValue());
			break;
		case MULT:
			result = n1+"*"+n2+"=: "+n1.mult(n2)+"\t"+(n1.doubleValue()*n2.doubleValue());
			break;
		case DIV:
			result = n1+"/"+n2+"=: "+n1.div(n2)+"\t"+(n1.doubleValue()/n2.doubleValue());
			break;
		case MAX:
			result = "max("+n1+","+n2+")=: "+n1.max(n2)+"\t"+Math.max(n1.doubleValue(),n2.doubleValue());
			break;
		case MIN:
			result = "min("+n1+","+n2+")=: "+n1.min(n2)+"\t"+Math.min(n1.doubleValue(),n2.doubleValue());
			break;
		case CMP:
			result = n1+"compareTo("+n2+")=: "+n1.compareTo(n2)+"\t"+Math.signum(n1.doubleValue()-n2.doubleValue());
			break;			
		}
		System.out.println(result);
	}
	private static void compute(CohoNumber n1, Number n2, CohoNumber.ArithOp op){
		String result = null;
		switch(op){
		case ADD:
			result = n1+"+"+n2+"=: "+n1.add(n2)+"\t"+(n1.doubleValue()+n2.doubleValue());
			break;
		case SUB:
			result = n1+"-"+n2+"=: "+n1.sub(n2)+"\t"+(n1.doubleValue()-n2.doubleValue());
			break;
		case MULT:
			result = n1+"*"+n2+"=: "+n1.mult(n2)+"\t"+(n1.doubleValue()*n2.doubleValue());
			break;
		case DIV:
			result = n1+"/"+n2+"=: "+n1.div(n2)+"\t"+(n1.doubleValue()/n2.doubleValue());
			break;
		case MAX:
			result = "max("+n1+","+n2+")=: "+n1.max(n2)+"\t"+Math.max(n1.doubleValue(),n2.doubleValue());
			break;
		case MIN:
			result = "min("+n1+","+n2+")=: "+n1.min(n2)+"\t"+Math.min(n1.doubleValue(),n2.doubleValue());
			break;
		case CMP:
			result =n1+"compareTo("+n2+")=: "+ n1.compareTo(n2)+"\t"+Math.signum(n1.doubleValue()-n2.doubleValue());
			break;			
		}
		System.out.println(result);
	}
	private static void compute(CohoNumber n1, CohoNumber.ArithOp op){
		String result = null;
		switch(op){
		case ABS:
			result = "|"+n1+"|= "+n1.abs()+"\t"+Math.abs(n1.doubleValue());
			break;
		case NEGATE:
			result = "-"+n1+"= "+n1.negate()+"\t"+(-n1.doubleValue());
			break;
		case RECIP:
			result = "1/"+n1+"= "+n1.recip()+"\t"+(1/n1.doubleValue());
			break;
		case SQRT:
			result = "sqrt("+n1+")= "+n1.sqrt()+"\t"+Math.sqrt(n1.doubleValue());
			break;
		}
		System.out.println(result);
	}

}
