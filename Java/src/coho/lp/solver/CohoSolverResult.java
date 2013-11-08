package coho.lp.solver;

import coho.lp.*;
import coho.common.matrix.*;
import coho.common.number.*;
//nothing new here
public class CohoSolverResult extends LPBasicResult {
	public CohoSolverResult( 	LPResult.ResultStatus status, CohoNumber optCost,
								LPBasis[] optBases, Matrix[] optPoints){
		super(status, optCost, optBases, optPoints);
	}
	public CohoSolverResult(	LPResult.ResultStatus status, CohoNumber optcost,
								LPBasis optBasis, Matrix optPoint){
		super(status, optcost, optBasis, optPoint);
	}
	public static CohoSolverResult create(LPResult.ResultStatus status, CohoNumber optCost,
								LPBasis[] optBases, Matrix[] optPoints){
		return new CohoSolverResult(status, optCost, optBases, optPoints);
	}
	public static CohoSolverResult create(LPResult.ResultStatus status, CohoNumber optcost,
								LPBasis optBasis, Matrix optPoint){
		return new CohoSolverResult(status, optcost, optBasis, optPoint);
	}
}
