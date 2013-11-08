package coho.lp.solver;
import coho.lp.*;
import coho.jni.*;
/**
 * This class is used to find proper solver for a lp problem. 
 * currently, we only support cohoSolver. Later, we will add a 
 * general solver.
 * @author chaoyan
 */
public class CohoSolverFactory {
	public static enum Solver{DOUBLE,APR,DOUBLEINTERVAL,AIHYBRID,ADHYBRID,AIDHYBRID,C};
	//public static Solver SOLVER = Solver.APR; //Solver.ADHYBRID; 
	public static Solver SOLVER = Solver.ADHYBRID;
	//public static Solver SOLVER = Solver.C; C solver does not deal with exception. Therefore, we can not use it now.
	public static CohoSolver getSolver(LP lp){
		return getSolver(lp, SOLVER);
	}
	
	public static CohoSolver getSolver(LP lp, Solver solver){
		if(lp.isCoho()||lp.isCohoDual()){
			switch(solver){
			case DOUBLEINTERVAL:
				return new DoubleIntervalCohoSolver(lp);
			case APR:
				return new BasicCohoSolver(lp,BasicCohoSolver.aprFactory);
			case DOUBLE:
				return new BasicCohoSolver(lp,BasicCohoSolver.doubleFactory);
			case AIHYBRID:
				return new APRIntervalHybridCohoSolver(lp);
			case ADHYBRID:
				return new APRDoubleHybridCohoSolver(lp);
			case AIDHYBRID:
				return new APRIntervalDoubleHybridCohoSolver(lp);
			case C:
				return new CCohoLPSolver(lp);
			}
		}
		throw new LPError("Unsupported CohoSolver type: "+solver);
	}
}
