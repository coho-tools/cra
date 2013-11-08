package coho.lp.solver;
import java.util.*;
import coho.lp.*;
/*
 * NOTE Don't use it. I can not find all optimal results
 * Given and LP and at least one optimal result, find all optimal results
 */
public class AllOptCohoSolver {
	public static CohoSolverResult findAllOptResult(LP lp, CohoSolverResult optResult){
		if(optResult.status()!=LPResult.ResultStatus.OK){
			throw new RuntimeException("AllOptCohoSolver.findAllOptResult: an optimal result for the LP is required");
		}
		
		//initialization
		LinkedList<LPBasis> optBases = new LinkedList<LPBasis>();
		LPBasis[] currOpts = optResult.optBases();
		if(currOpts.length<1)
			throw new RuntimeException("AllOptCohoSolver.findAllOptResult: an optimal result for the LP is required");
		for(int i=0; i<currOpts.length; i++){
			LPBasis basis = currOpts[i];
			if( basis.primalStatus()!=LPBasis.BasisStatus.FEASIBLE || basis.dualStatus()!=LPBasis.BasisStatus.FEASIBLE ){
				throw new RuntimeException("AllOptCohoSolver.findAllOptResult: an optimal result for the LP is required");
			}
			optBases.offer(basis);
		}
		
		//bread first search
		ArrayList<LPBasis> resultBases = new ArrayList<LPBasis>();
		while(!optBases.isEmpty()){
			LPBasis basis = optBases.poll();
			resultBases.add(basis);
			LPBasis[] newBases = null;//TODO
			for(int i=0; i<newBases.length; i++){
				LPBasis newBasis = newBases[i];
				if(resultBases.contains(newBasis) || optBases.contains(newBasis)){
					//not add
				}else{
					optBases.offer(newBasis);
				}
			}
		}
		//we don't provide optimal points now. 
		return new CohoSolverResult(LPResult.ResultStatus.OK,  optResult.optCost(), 
				resultBases.toArray(new LPBasis[resultBases.size()]), null);
	}
}
