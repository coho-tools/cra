package coho.debug;

import coho.common.number.*;
import coho.lp.solver.*;

public class STAT {
	/*****************************************
	 * functions for debug non-generic bugs
	 * @author chaoyan
	 *
	 ******************************************/
//	public static int[] counters = new int[20];
//	public static double[] timers = new double[20];
	public static final boolean stat =false;
	public static void println(String msg) {
		DEBUG.println(msg,"STAT");
	}
	/*
	 * stastic
	 */
//	public static int lpCounter = 0;
	public static int[] lpResult = new int[12];
	
//	public static int lpPivot = 0;
	public static int[] path = new int[5];
	
	public static int basisCounter=0;
	public static int feasibleBasisCounter=0;
	
//	public static int illCondCounter=0;
	public static int[] condNumber = new int[12];
	public static int[] interval = new int[5];

	public static int[] findBasis = new int[2];
	
//	public static int linearCounter=0;
//	public static int[] condCounter = new int[10];
//	public static int[] interCounter = new int[10];
//	
	public static void outStat(){
		println("%-----------------------------------------%");
		println("Stat data");
		int lpCounter=0;
		for(int i=0;i<lpResult.length;i++)
			lpCounter+=lpResult[i];
		println("There are "+lpCounter+" LP solved Totally.");
		println("%-----------------------------------------%");
		println("Stat initial feasible basis method");
		println("There are "+findBasis[0]+"("+findBasis[0]*100.0/lpCounter+" percent)"+" LP use BigM method to find basis.");
		println("There are "+findBasis[1]+"("+findBasis[1]*100.0/lpCounter+" percent)"+" LP have easy basis.");
		println("%-----------------------------------------%");
		println("Stat interval of result");
		println("There are "+lpResult[0]+"("+lpResult[0]*100.0/lpCounter+" percent)"+" results with relative interval greater than 1e-7");
		println("There are "+lpResult[1]+"("+lpResult[1]*100.0/lpCounter+" percent)"+" results with relative interval greater than 1e-8");
		println("There are "+lpResult[2]+"("+lpResult[2]*100.0/lpCounter+" percent)"+" results with relative interval greater than 1e-9");
		println("There are "+lpResult[3]+"("+lpResult[3]*100.0/lpCounter+" percent)"+" results with relative interval greater than 1e-10");
		println("There are "+lpResult[4]+"("+lpResult[4]*100.0/lpCounter+" percent)"+" results with relative interval greater than 1e-11");
		println("There are "+lpResult[5]+"("+lpResult[5]*100.0/lpCounter+" percent)"+" results with relative interval greater than 1e-12");
		println("There are "+lpResult[6]+"("+lpResult[6]*100.0/lpCounter+" percent)"+" results with relative interval greater than 1e-13");
		println("There are "+lpResult[7]+"("+lpResult[7]*100.0/lpCounter+" percent)"+" results with relative interval greater than 1e-14");
		println("There are "+lpResult[8]+"("+lpResult[8]*100.0/lpCounter+" percent)"+" results with relative interval greater than 1e-15");
		println("There are "+lpResult[9]+"("+lpResult[9]*100.0/lpCounter+" percent)"+" results with relative interval greater than 1e-16");
		println("There are "+lpResult[10]+"("+lpResult[10]*100.0/lpCounter+" percent)"+" results with relative interval greater than 1e-17");
		println("There are "+lpResult[11]+"("+lpResult[11]*100.0/lpCounter+" percent)"+" results with relative interval less(equal) than 1e-17");
		println("%-----------------------------------------%");

		int lpPivot=0;
		for(int i=0;i<path.length;i++)
			lpPivot+=path[i];
		println("Stat pivot");
		println("There are "+lpPivot+" pivots totally. About "+(lpPivot+0.0)/lpCounter+" pivots per LP");
		println("There are "+path[1]+"("+path[1]*100.0/lpPivot+" percent)"+" pivots that has an unique branches.");
		println("There are "+path[2]+"("+path[2]*100.0/lpPivot+" percent)"+" pivots that has 2 branches.");
		println("There are "+path[3]+"("+path[3]*100.0/lpPivot+" percent)"+" pivots that has 3 branches.");
		println("There are "+path[4]+"("+path[4]*100.0/lpPivot+" percent)"+" pivots that has 4 branches.");
		println("There are "+path[0]+"("+path[0]*100.0/lpPivot+" percent)"+" pivots that has more than 4 branches.");
		println("%-----------------------------------------%");
		
		println("Stat basis");
		println("There are "+basisCounter+" bases visited Totally. About "+(basisCounter+0.0)/lpCounter+" baes per LP");
		println("With "+feasibleBasisCounter+"("+feasibleBasisCounter*100.0/basisCounter+" percent)"+" clearly feasible basis");
		println("%-----------------------------------------%");
		
		int cn=0, inter=0;
		for(int i=0;i<condNumber.length;i++)
			cn+=condNumber[i];
		for(int i=0;i<interval.length;i++)
			inter+=interval[i];
		int illCondCounter=cn+inter;
		println("Stat ill-condition exception");
		println("There are "+illCondCounter+" Exceptions Totally. About "+(illCondCounter+0.0)/lpCounter+" ill conditions per LP");
		println("Stat condition number exception");
		println("There are "+cn+"("+cn*100.0/illCondCounter+" percent)"+" Exceptions caught by condition number estination");
		println("There are "+condNumber[0]+"("+condNumber[0]*100.0/cn+" percent)"+" exception that the estimated condition number is less than 1e6(greater than 1e5)");
		println("There are "+condNumber[1]+"("+condNumber[1]*100.0/cn+" percent)"+" exception that the estimated condition number is less than 1e7");
		println("There are "+condNumber[2]+"("+condNumber[2]*100.0/cn+" percent)"+" exception that the estimated condition number is less than 1e8");
		println("There are "+condNumber[3]+"("+condNumber[3]*100.0/cn+" percent)"+" exception that the estimated condition number is less than 1e9");
		println("There are "+condNumber[4]+"("+condNumber[4]*100.0/cn+" percent)"+" exception that the estimated condition number is less than 1e10");
		println("There are "+condNumber[5]+"("+condNumber[5]*100.0/cn+" percent)"+" exception that the estimated condition number is less than 1e11");
		println("There are "+condNumber[6]+"("+condNumber[6]*100.0/cn+" percent)"+" exception that the estimated condition number is less than 1e12");
		println("There are "+condNumber[7]+"("+condNumber[7]*100.0/cn+" percent)"+" exception that the estimated condition number is less than 1e13");
		println("There are "+condNumber[8]+"("+condNumber[8]*100.0/cn+" percent)"+" exception that the estimated condition number is less than 1e14");
		println("There are "+condNumber[9]+"("+condNumber[9]*100.0/cn+" percent)"+" exception that the estimated condition number is less than 1e15");
		println("There are "+condNumber[10]+"("+condNumber[10]*100.0/cn+" percent)"+" exception that the estimated condition number is less than 1e16");
		println("There are "+condNumber[11]+"("+condNumber[11]*100.0/cn+" percent)"+" exception that the estimated condition number is greater than 1e16");
		println("Stat large interval exception");
		println("There are "+inter+"("+inter*100.0/illCondCounter+" percent)"+" Exceptions caught by large interval of solution for linear system");
		println("There are "+interval[0]+"("+interval[0]*100.0/inter+" percent)"+" exception that the interval is less than 1e-2");		
		println("There are "+interval[1]+"("+interval[1]*100.0/inter+" percent)"+" exception that the interval is less than 1e-1");
		println("There are "+interval[2]+"("+interval[2]*100.0/inter+" percent)"+" exception that the interval is less than 1e-0");
		println("There are "+interval[3]+"("+interval[3]*100.0/inter+" percent)"+" exception that the interval is less than 1e1");
		println("There are "+interval[4]+"("+interval[4]*100.0/inter+" percent)"+" exception that the interval is greater than 1e1");
		println("%-----------------------------------------%");
		
		println("Stat linear system solver");
		println("There are "+lsCounter+" linear system solved");
		println("There are "+lsHybridCounter+" hybrid method called");
//		//analysis interval and condition number for all case.
//		int solved = 0;
//		for(int i=0;i<condCounter.length;i++)
//			linearCounter+=condCounter[i];
//		for(int i=0;i<interCounter.length;i++)
//			solved += interCounter[i];
//		println("There are "+linearCounter+" linear systems to solve");
//		println("There are "+solved+"("+solved*100.0/linearCounter+" percent)"+" linear systems solved");
//		println("%-----------------------------------------\n%");
//		println("There are "+condCounter[0]+"("+condCounter[0]*100.0/linearCounter+" percent)"+" linear systems with condition number less than 1e0");
//		println("There are "+condCounter[1]+"("+condCounter[1]*100.0/linearCounter+" percent)"+" linear systems with condition number less than 1e1");
//		println("There are "+condCounter[2]+"("+condCounter[2]*100.0/linearCounter+" percent)"+" linear systems with condition number less than 1e2");
//		println("There are "+condCounter[3]+"("+condCounter[3]*100.0/linearCounter+" percent)"+" linear systems with condition number less than 1e3");
//		println("There are "+condCounter[4]+"("+condCounter[4]*100.0/linearCounter+" percent)"+" linear systems with condition number less than 1e4");
//		println("There are "+condCounter[5]+"("+condCounter[5]*100.0/linearCounter+" percent)"+" linear systems with condition number less than 1e5");
//		println("There are "+condCounter[6]+"("+condCounter[6]*100.0/linearCounter+" percent)"+" linear systems with condition number less than 1e6");
//		println("There are "+condCounter[7]+"("+condCounter[7]*100.0/linearCounter+" percent)"+" linear systems with condition number less than 1e7");
//		println("There are "+condCounter[8]+"("+condCounter[8]*100.0/linearCounter+" percent)"+" linear systems with condition number less than 1e8");
//		println("There are "+condCounter[9]+"("+condCounter[9]*100.0/linearCounter+" percent)"+" linear systems with condition number greater than 1e8");
//		println("%-----------------------------------------\n%");
//		println("There are "+interCounter[0]+"("+interCounter[0]*100.0/solved+" percent)"+" solved linear systems with interval less than 1e-15");
//		println("There are "+interCounter[1]+"("+interCounter[1]*100.0/solved+" percent)"+" solved linear systems with interval less than 1e-14");
//		println("There are "+interCounter[2]+"("+interCounter[2]*100.0/solved+" percent)"+" solved linear systems with interval less than 1e-13");
//		println("There are "+interCounter[3]+"("+interCounter[3]*100.0/solved+" percent)"+" solved linear systems with interval less than 1e-12");
//		println("There are "+interCounter[4]+"("+interCounter[4]*100.0/solved+" percent)"+" solved linear systems with interval less than 1e-11");
//		println("There are "+interCounter[5]+"("+interCounter[5]*100.0/solved+" percent)"+" solved linear systems with interval less than 1e-10");
//		println("There are "+interCounter[6]+"("+interCounter[6]*100.0/solved+" percent)"+" solved linear systems with interval less than 1e-9");
//		println("There are "+interCounter[7]+"("+interCounter[7]*100.0/solved+" percent)"+" solved linear systems with interval less than 1e-8");
//		println("There are "+interCounter[8]+"("+interCounter[8]*100.0/solved+" percent)"+" solved linear systems with interval less than 1e-7");
//		println("There are "+interCounter[9]+"("+interCounter[9]*100.0/solved+" percent)"+" solved linear systems with interval greater than 1e-7");
	}
	
	public static int stepLP = 0;
	public static int stepException=0;
	public static void outStepStat(int step){
		if(step<0){
			outStat();
			return;
		}
		println("%-----------------------------------------%");
		println("Step: "+step);
		int lpCounter=0;
		for(int i=0;i<lpResult.length;i++)
			lpCounter+=lpResult[i];
		int lps = lpCounter-stepLP;
		println("LP: "+lps);
		stepLP = lpCounter;
		int cn=0, inter=0;
		for(int i=0;i<condNumber.length;i++)
			cn+=condNumber[i];
		for(int i=0;i<interval.length;i++)
			inter+=interval[i];
		int illCondCounter=cn+inter;
		int es = illCondCounter-stepException;
		println("Exception: "+es+" Percent:"+(es+0.0)/lps);
		stepException = illCondCounter;
		println("%-----------------------------------------%");
		
	}
	
	public static void statLP(CohoSolverResult r,boolean findBais){
		findBasis[findBais?1:0]++;
		DoubleInterval  i = (DoubleInterval)r.optCost();
		double order = Math.abs((i.hi().doubleValue()-i.lo().doubleValue())/i.x().doubleValue());//stupid!
		if(order>1e-7){
			lpResult[0]++;
		}else if(order>1e-8){
			lpResult[1]++;
		}else if(order>1e-9){
			lpResult[2]++;
		}else if(order>1e-10){
			lpResult[3]++;
		}else if(order>1e-11){
			lpResult[4]++;
		}else if(order>1e-12){
			lpResult[5]++;
		}else if(order>1e-13){
			lpResult[6]++;
		}else if(order>1e-14){
			lpResult[7]++;
		}else if(order>1e-15){
			lpResult[8]++;
		}else if(order>1e-16){
			lpResult[9]++;
		}else if(order>1e-17){
			lpResult[10]++;
		}else{
			lpResult[11]++;
		}
	}
	public static void statLPPath(int n){
		path[n%5]++;
	}
//	public static void statBasis(LPBasis basis){
//		basisCounter++;
//		if(basis.status()==LPbasis.fLPBasis) feasibleBasisCounter++;
//	}
	public static void statIllCond(int pos, double n){
		switch(pos){
		case 0: //cn
			if(n<1e6)
				condNumber[0]++;
			else if(n<1e7)
				condNumber[1]++;
			else if(n<1e8)
				condNumber[2]++;
			else if(n<1e9)
				condNumber[3]++;
			else if(n<1e10)
				condNumber[4]++;
			else if(n<1e11)
				condNumber[5]++;
			else if(n<1e12)
				condNumber[6]++;
			else if(n<1e13)
				condNumber[7]++;
			else if(n<1e14)
				condNumber[8]++;
			else if(n<1e15)
				condNumber[9]++;
			else if(n<1e16)
				condNumber[10]++;
			else
				condNumber[11]++;
			break;
		default://interval
			if(n<1e-2)
				interval[0]++;
			else if(n<1e-1)
				interval[1]++;
			else if(n<1)
				interval[2]++;
			else if(n<1e1)
				interval[3]++;
			else
				interval[4]++;
		}
	}
	public static int lsCounter = 0;
	public static int lsHybridCounter=0;
	public static void statLS(int counter){
		lsCounter++;
		lsHybridCounter+=counter;
	}
//	public static void statLinearSystem(int pos, double n){
//		switch(pos){
//		case 0: //cn
//			if(n<1e0)
//				condCounter[0]++;
//			else if(n<1e1)
//				condCounter[1]++;
//			else if(n<1e2)
//				condCounter[2]++;
//			else if(n<1e3)
//				condCounter[3]++;
//			else if(n<1e4)
//				condCounter[4]++;
//			else if(n<1e5)
//				condCounter[5]++;
//			else if(n<1e6)
//				condCounter[6]++;
//			else if(n<1e7)
//				condCounter[7]++;
//			else if(n<1e8)
//				condCounter[8]++;
//			else
//				condCounter[9]++;
//			break;
//		default:
//			if(n<1e-15)
//				interCounter[0]++;
//			else if(n<1e-14)
//				interCounter[1]++;
//			else if(n<1e-13)
//				interCounter[2]++;
//			else if(n<1e-12)
//				interCounter[3]++;
//			else if(n<1e-11)
//				interCounter[4]++;
//			else if(n<1e-10)
//				interCounter[5]++;
//			else if(n<1e-9)
//				interCounter[6]++;
//			else if(n<1e-8)
//				interCounter[7]++;
//			else if(n<1e-7)
//				interCounter[8]++;
//			else 
//				interCounter[9]++;			
//		}
//	}
//	/**
//	 * To debug ill-condition
//	 */
//	public static void compException(CohoSolverResult orig, CohoSolverResult except, double cn ){
//		println("The condition number is "+cn,"CMP");
//		if(orig==null)
//			println("The LP solver failed to solve the lp if not throw an exception","CMP");
//		else{
//			println("The result if not throw an exception\n"+orig.toString(),"CMP");
//			println("The interval is "+(orig.optCost().hi()-orig.optCost().lo()),"CMP");
//		}
//		println("","CMP");
//		println("The result if throw an exception\n"+except.toString(),"CMP");
//		println("The interval is "+(except.optCost().hi()-except.optCost().lo()),"CMP");
//		println("------------------------------------\n","CMP");
//	}
//	public static void debugIllCond(CohoMatrix A, DoubleIntervalMatrix b, DoubleIntervalMatrix c,
//			IntegerMatrix basis, double n, int rmVar) {
//		println("\n-----------Display an exception------------", "ILL");
//		println(A.transpose().x().toString(),"ILL");
//		println(c.x().toString(),"ILL");
//		println(b.x().toString(),"ILL");
//		println(basis.toString(),"ILL");				
//		println(""+rmVar,"ILL");
//		println("With conditon number or relative interval:"+n,"ILL");
//		println("------------------------------------\n","ILL");
//	}
//	public static void debugIllCond(CohoMatrix A, DoubleIntervalMatrix b, DoubleIntervalMatrix c,
//			IntegerMatrix basis, double n) {
//		println("\n-----------Display an exception------------", "ILL");
//		println(A.transpose().x().toString(),"ILL");
//		println(c.x().toString(),"ILL");
//		println(b.x().toString(),"ILL");
//		println(basis.toString(),"ILL");				
//		println("With conditon number or relative interval:"+n,"ILL");
//		println("------------------------------------\n","ILL");
//	}
}
