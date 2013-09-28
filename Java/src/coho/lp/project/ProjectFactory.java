package coho.lp.project;

import coho.common.matrix.*;
import coho.lp.*;
import coho.lp.solver.*;
public class ProjectFactory {
	public static double errTol = 0;//TODO: to simplify the projectagon, reduce the result by errTol.
	public static CohoSolverFactory.Solver lpSolver = CohoSolverFactory.Solver.ADHYBRID;
	//public static CohoSolverFactory.Solver lpSolver = CohoSolverFactory.Solver.C;
	//public static CohoSolverFactory.Solver lpSolver = CohoSolverFactory.Solver.APR;
	public static LPProject getProject(LP lp, Matrix x, Matrix y){
		return getProject(lp,x,y,lpSolver);
	}
	public static LPProject getProject(LP lp, Matrix x, Matrix y, CohoSolverFactory.Solver lpSolver){
		switch(lpSolver){
		case DOUBLEINTERVAL:
			return new DoubleIntervalLPProject(lp,x,y);
		case APR:
		case DOUBLE:
		case AIHYBRID:
		case ADHYBRID:
		case AIDHYBRID:
		case C:
			return new LPProject(lp,x,y,lpSolver);
		}
		throw new LPError("Unsupported lp solver type: "+lpSolver);
	}	
}
