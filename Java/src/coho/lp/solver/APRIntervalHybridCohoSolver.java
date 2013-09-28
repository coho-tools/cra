package coho.lp.solver;

import java.util.*;

import coho.lp.*;
import coho.common.number.*;
import coho.common.matrix.*;
import coho.common.util.*;

public class APRIntervalHybridCohoSolver extends BasicCohoSolver {
	DoubleIntervalCohoMatrix intervalA;// A copy of dualA used when double interval fails to find a unique pivot 
	DoubleIntervalMatrix intervalB, intervalC;
	public APRIntervalHybridCohoSolver(LP lp) throws LPError {
		super(lp, aprFactory);
		//BUG: For LPproject, the same lp solver is used for several lps. 
		//It should be cleared for each problem. See initialBasis
		//history = new HashSet<LPBasis>(); 
	}
	
	@Override
	protected LPBasis initialBasis(Matrix c){
		LPBasis result = super.initialBasis(c);
		intervalA = DoubleIntervalCohoMatrix.create(dualA, true);//dualA is avaliable now
		intervalB = DoubleIntervalMatrix.create(dualB);
		intervalC = DoubleIntervalMatrix.create(dualC);
		return result;
	}
	@Override
	protected void init(Matrix c){
		super.init(c);
		intervalA = DoubleIntervalCohoMatrix.create(dualA, true);//dualA is avaliable now
		intervalB = DoubleIntervalMatrix.create(dualB);
		intervalC = DoubleIntervalMatrix.create(dualC);		
	}
	@Override
	protected LPBasis findOptBySimplex(LPBasis startBasis)throws UnboundedLPError{
		//NOTE: When the optimal basis is reached, the interval pivot may also 
		//lead to infeasible or less favorable basis.Thus we should track LPBasis visited to find the optiaml one if loop found. 
		Set<LPBasis> history = new HashSet<LPBasis>();
		
		LPBasis currBasis, nextBasis=startBasis;
		do{
			currBasis = nextBasis;
			history.add(currBasis);
			try{
				nextBasis = pivot(currBasis,history);
			}catch(OptInHistoryException e){
				Iterator<LPBasis> iter = history.iterator();
				while(iter.hasNext()){
					LPBasis basis = iter.next();
					if(isOpt(basis)){
						//FIXED: In the next loop, interval pivot might find the less favorable basis again, then infinity loop.
						//We know basis is CohoFeasible and CohoDualFeasible, then it is optimal, return it directly. 
						//nextBasis = basis;//Change the currBasis as optimal one and return null;
						return basis;
					}else{
						//continue;
					}
				}
				throw new RuntimeException("APRDoubleIntervalHybridSolver: Algorithm Error. " +
						"Pivot reports optimal basis is in history. However, it is not found in the hisotry by findOptBySimplex function");
			}
			if(nextBasis == null){//optimal
				//contains extra variables, infeasible for dual, unbounded or infeasible for primal
				if(!findBasis && currBasis.basis().V(currBasis.basis().length()-1).geq(ncols)){
					return null;
				}
				return currBasis;
			}	
		}while(true);
	}
	static class OptInHistoryException extends LPError{
		private static final long serialVersionUID = Configure.serialVersionUIDPrefix+32;
	}
	//@Override
	//IDEA: APR are mostly used when the basis is close to the optimal basis.
	//We can guess based on some probability model to save the computation time on the fail interval computation.
	//FIXED: If the currBasis is optimal, interval pivot may also introduce some infeasible or less favorable basis.
	//There are two method to avoid this 
	//1)Check the cohoFeasibility of currBasis, if it's feasible for coho, it's optimal. The code is clear, but it use apr computation, might be slow.
	//2)Use history to check the less favorable one. Check cohoDual feasiblity for basis if it's not clearly feasible to remove the infeasible basis.
	protected LPBasis pivot(LPBasis currBasis, Set<LPBasis> history) throws UnboundedLPError, OptInHistoryException{
		try{//try to use interval
			LPBasis nextBasis = pivot(currBasis,0,false);//From the begining, find a possible favorable basis.
			if(nextBasis==null){//optimal
				return null;
			}else{
				//If dualStatus is not clear feasible, it might be the case.(check it) 
				//1)currBasis is optimal 2) nextBasis is infeasible or less favorable.
				//The dualStatus might be not clear feasible if currBasis is not optimal. In this case, the check is false.
				if(nextBasis.dualStatus()!=LPBasis.BasisStatus.FEASIBLE){
					//not a clear favorable basis found, Compute the feasiblity of nextBasis use apr.
					//If currBasis is optimal. Interval might introduce non-invertiable basis thus cause a SingularMatrixException and call apr then.
					//NOTE: I override the cohoDualFeasile, it will not throw LPError if the basis is not invertiable. The status is infeasible now. 
					//In this case, we don't need to try the apr.
					cohoDualFeasible(nextBasis);
					//nextBasis is infeasible, then currBasis must be optimal. Otherwise, it is impossible that there is only one infeasible branch.
					if(nextBasis.dualStatus()==LPBasis.BasisStatus.INFEASIBLE)
						return null;
					
					//nextBasis is feasible, but might be less favorable. Then the optimal basis lead to 
					//some feasible basis which might never shouwn before.
					//But at the end, it will introduce a loop and then we can find the optimal one from history. 
					if(history.contains(nextBasis)){//see the equals function of LPBasis, it's independent of status
						throw new OptInHistoryException();
					}else{
						//the basis is really feasible and not appeared before, try it and continue
					}
				}
				return nextBasis;
			}
		}catch(LPError e){//CONSIDER: change to Exception? We have considered the SingularMatrixException and Divided by zero.
			if(e instanceof UnboundedLPError)
				throw e;
			//throw SingularMatrixException. call apr pivot
		}		
		//use apr
		return super.pivot(currBasis);
	}
	
	//
	//Find the next possible more favorable basis. Return null if there is no better one. Throw LPError if more than one next basis found. 
    //The result might be less favorable or infeasible when the currBasis is optimal. It's indicated by the dual status as POSSIBLEFEASIBLE.
	//
	//What about the Singular basis? 
	//Most singular basis are introduced by BIGM variable. We use the same method (see interval solver) to remove it as possible.
	//If it's optimal, return as result. otherwise, throw Exception and use APR in the next pivot
	//
	private LPBasis pivot(LPBasis currBasis, int start, boolean findClear) throws UnboundedLPError{
		assert currBasis.dualStatus()==LPBasis.BasisStatus.FEASIBLE: "APRDoubleIntervalHybridSolver.pivot: The currBasis is not feasible";
		try{
			BooleanMatrix basis = currBasis.booleanBasis(intervalA.ncols());		
			CohoMatrix B = intervalA.trim(basis);
			Matrix cb =intervalC.row(currBasis.basis());
			Matrix d = B.transpose().getSolution(cb).transpose();
			
			//find new column
			ArrayList<Integer> cols = new ArrayList<Integer>();// store possible cols to be evicted
			int col = start;
			for(;col<ncols;col++){//we don't want to introduce the added column
				if(basis.V(col).booleanValue())
					continue;	//skip, it's can't be the new basic column
				//d*Aj
				CohoNumber ctj = intervalA.col(col).dotProd(d); //use cohoMatrix dotProd
//				//CohoMatrix Aj = intervalA.convert(intervalA.col(col),true);
//				CohoMatrix Aj = (CohoMatrix)intervalA.col(col);
//				ArrayList<Integer> pos = Aj.rowsAtCol()[0];
//				CohoNumber ctj = d.V(pos.get(0)).mult(Aj.V(pos.get(0)));
//				if(pos.size()>1)
//					ctj = ctj.add(  d.V(pos.get(1)).mult(Aj.V(pos.get(1)))  );
				CohoNumber relativeCost = intervalC.V(col).sub(ctj);
				if(relativeCost.less(0.0)){//find a cleary column .
					/*
					 * NOTE: Many ill-condition excetions are caused when removing the BIGM varibles. 
					 * Here, the first clear favoriable column may cause an exception, but others may be well.
					 * We can't remove a basis until we know there is a more favorable basis.
					 * Two methods 1)solve the linear system and if it's ill-conditioned, remove it if there are better basis
					 * 2)use the most favorable basis. 
					 */
					cols.clear(); // remove all possible columns
					cols.add(col);// remember this column
					findClear=true;
					break;					
				}else if(!relativeCost.greater(0)&&!findClear){//possible negative
					//even though there is no cost gain, we want to driven out the added column
					cols.add(col);
				}//else relativeCost > 0.0
			}
			
			if(cols.size()==0)//it's the optimal already.
				return null;
			if(cols.size()>1)
				throw new LPError("find more than one branches, use APR instead");
			
			//find evicted column
			Matrix T0 = B.getSolution(intervalB);
			int newCol = cols.get(0);
			Matrix Tj = B.getSolution(intervalA.col(newCol));
			// find possible columns which should be evicted(possible).
			// here, evictIndex is not the column to be evicted, it's the index of that column in the current basis
			//FIXED: ArithmeticException(divide by zero) may be thrown. Catch and use apr
			int[] evictIndex = argmin((DoubleIntervalMatrix)T0,(DoubleIntervalMatrix)Tj);// here should never throw exception
			if(evictIndex==null||evictIndex.length==0){
				throw new UnboundedLPError("CohoSolver.pivot():The coho dual is unbounded" +
				" This Should Never Happen for our Coho application, but possible for genereal Ax>b form lp.");				
			}else if(evictIndex.length>1){
				throw new LPError("find more than one branches, use APR instead");
			}else{
				int removeCol = currBasis.basis().V(evictIndex[0]).intValue();
				//NOTE: If the relative cost is clear negative and only one evict col found(must be clear), the next basis is feasible for coho dual
				//Otherwise, the relative might be negative, the next basis might be less favorable or infeasible for coho dual. 
				//Consider the case when currBasis is optimal. We must check it outside
				LPBasis newBasis = null;
				if(findClear){
					newBasis = currBasis.replace(removeCol,newCol,LPBasis.BasisStatus.UNKNOWN,LPBasis.BasisStatus.FEASIBLE);
				}if(!findClear){
					newBasis = currBasis.replace(removeCol,newCol,LPBasis.BasisStatus.UNKNOWN, LPBasis.BasisStatus.UNKNOWN);
				}
				//Try to remove the Singular basis caused by BIGM variables.
				if(findClear){
					try{
						cohoDualPoint(newBasis);
					}catch(SingularMatrixException e){
						LPBasis nextClearBasis = pivot(currBasis,col+1,true);
						if(nextClearBasis!=null){
							return nextClearBasis;
						}
						//return newBasis
					}
				}
				return newBasis;
			}
		}catch(SingularMatrixException e){
			//when currBasis is optimal, it may introduce non-invertiable basis which causes the SingularMatrixException.
			throw new LPError("singular basis, use APR instead");
		}catch(ArithmeticException e){//FIXED: use apr instead
			throw new LPError("Divide by zero erro from interval, use APR instead");
		}
	}
	
	private int[] argmin(DoubleIntervalMatrix T0, DoubleIntervalMatrix Tj){
		assert T0.length()==Tj.length() :
			"The length of vector T0 and Tj should be the same." +" T0: "+T0.length()+" Tj "+Tj.length();
		//if T0_i <0, then the basis is nont feasible, we have dropped this branch
		ArrayList<Integer> posIndex = new ArrayList<Integer>();
		ArrayList<DoubleInterval> posMin = new ArrayList<DoubleInterval>();
		//We should consider all possible one. That is possibly greater than zero.
		for(int i=0;i<T0.length();i++){
			if(Tj.V(i).leq(0.0))
				continue;
			DoubleInterval d = DoubleInterval.create(-Double.MAX_VALUE,Double.MAX_VALUE);
			if(Tj.V(i).greater(0.0))
				d = T0.V(i).div(Tj.V(i));//FIXED: divide by zero
			addPosMin(d,posMin,i,posIndex);
		}
		int[] result = new int[posIndex.size()];
		for(int i=0;i<posIndex.size();i++){
			result[i] = posIndex.get(i).intValue();
		}
		return result;
	}
	/*
	 * Store all possible minimum value in posMin and their index 
	 * in posIndex after a newly value d;
	 */
	private void addPosMin(DoubleInterval d,ArrayList<DoubleInterval>posMin,
			int index,ArrayList<Integer> posIndex){
		if(posMin.size()==0){// if empty, add directly
			posMin.add(d);
			posIndex.add(new Integer(index));
			return;
		}
		// if this value is greater than any of the possible minimum value,
		// it's not a possible minimum value
		for(int i=0;i<posMin.size();i++){
			if(d.greater(posMin.get(i)))
				return;
		}
		// if any of the value is greater than it, remove that value
		for(int i=posMin.size()-1;i>=0;i--){
			if(d.less(posMin.get(i))){
				posMin.remove(i);
				posIndex.remove(i);
			}
		}
		// add the new possible minimum value and it's index;
		posMin.add(d);
		posIndex.add(index);
	}
	
//	@Override
//	public boolean isClearOpt(LPBasis basis){
//		return isOpt(basis);
//	}
//
//	/*
//	 * Return true if it is optimal
//	 * Return false if it is not optimal
//	 */
//	protected boolean isOpt(LPBasis basis){
//		try{
//			IntegerMatrix pos = basis.basis();
//			//dual feasible
//			Matrix cohoDualPoint = null;
//			try{
//				cohoDualPoint =  intervalC.zeros().assign(intervalA.trim(pos).getSolution(intervalB),pos);
//			}catch(ArithmeticException e){//caused by double interval
//				cohoDualPoint = dualC.zeros().assign(dualA.trim(pos).getSolution(dualB),pos);
//			}
//			Matrix zero = cohoDualPoint.zeros();
//			if(cohoDualPoint.less(zero).any()){
//				basis.setDualStatus(LPBasis.BasisStatus.INFEASIBLE);
//				return false;
//			}else if(cohoDualPoint.geq(zero).all()){
//				basis.setDualStatus(LPBasis.BasisStatus.FEASIBLE);
//			}else{
//				boolean dualFeasible = super.cohoDualFeasible(basis);
//				if(!dualFeasible)
//					return false;
//			}
//			//primal feasible
//			Matrix re = null;
//			try{
//				Matrix cohoPoint = intervalA.transpose()/*negateh*/.trim(pos).getSolution(intervalC.row(pos)/*negate*/);//E^-1*x
//				re = intervalA.transpose().mult(cohoPoint).sub(intervalC).negate();//AE^{-1}x-b for primal
//				re = re.row(basis.booleanBasis(intervalC.length()).negate());//we don't need to compare rows in the basis\
//			}catch(ArithmeticException e){
//				Matrix cohoPoint = dualA.transpose()/*negateh*/.trim(pos).getSolution(dualC.row(pos)/*negate*/);//E^-1*x
//				re = dualA.transpose().mult(cohoPoint).sub(dualC).negate();//AE^{-1}x-b for primal
//				re = re.row(basis.booleanBasis(dualC.length()).negate());//we don't need to compare rows in the basis\				
//			}
//			zero = re.zeros();
//			if(re.less(zero).any()){
//				basis.setPrimalStatus(LPBasis.BasisStatus.INFEASIBLE);
//				return false;
//			}else if(re.geq(zero).all()){
//				basis.setPrimalStatus(LPBasis.BasisStatus.FEASIBLE);
//			}else{
//				boolean primalFeasible = super.cohoFeasible(basis);
//				if(!primalFeasible)
//					return false;
//			}
//			return true;
//		}catch(SingularMatrixException e){//SingularMatrixException cause by infeasible basis or ArithmeticException cause by doubleIntervalFactory
//			return false;
//		}
//	}
	@Override
	public boolean cohoFeasible(LPBasis basis){		
		if(basis.primalStatus()!=LPBasis.BasisStatus.UNKNOWN)
			return basis.primalStatus()!=LPBasis.BasisStatus.INFEASIBLE;
		try{
			IntegerMatrix pos = basis.basis();
			//improve performance
			Matrix cohoPoint = intervalA.trim(pos).transpose().getSolution(intervalC.row(pos)/*negate*/);//E^-1*x
			BooleanMatrix care = basis.booleanBasis(intervalC.length()).negate();
			Matrix re = intervalC.row(care).sub(intervalA.col(care).transpose().mult(cohoPoint));
//			Matrix cohoPoint = intervalA.transpose()/*negateh*/.trim(pos).getSolution(intervalC.row(pos)/*negate*/);//E^-1*x
//			Matrix re = intervalA.transpose().mult(cohoPoint).sub(intervalC).negate();//AE^{-1}x-b for primal
//			re = re.row(basis.booleanBasis(intervalC.length()).negate());//we don't need to compare rows in the basis\
			Matrix zero = re.zeros();
			boolean result = true;
			if(re.less(zero).any()){
				basis.setPrimalStatus(LPBasis.BasisStatus.INFEASIBLE);
				result = false;
			}else if(re.geq(zero).all()){
				basis.setPrimalStatus(LPBasis.BasisStatus.FEASIBLE);
				result = true;
			}else{//NOTE: why high failure rate? more computation, larger interval?
				result = super.cohoFeasible(basis);//use apr
			}
			return result;
		}catch(SingularMatrixException e){
			try{
				return super.cohoFeasible(basis);//for example, divided by zero causd by interval
			}catch(LPError ee){
				basis.setPrimalStatus(LPBasis.BasisStatus.INFEASIBLE);//really non invertiable
				return false;
			}
		}	
	}

	@Override
	public boolean cohoDualFeasible(LPBasis basis){
		if(basis.dualStatus()!=LPBasis.BasisStatus.UNKNOWN)
			return basis.dualStatus()!=LPBasis.BasisStatus.INFEASIBLE;
		try{
			IntegerMatrix pos = basis.basis();
			//dual feasible
			//Matrix cohoDualPoint =  intervalC.zeros().assign(intervalA.trim(pos).getSolution(intervalB),pos);
			Matrix cohoDualPoint =  intervalA.trim(pos).getSolution(intervalB);//trim for typecast we don't need these zeros here
			Matrix zero = cohoDualPoint.zeros();
			boolean result = true;
			if(cohoDualPoint.less(zero).any()){
				basis.setDualStatus(LPBasis.BasisStatus.INFEASIBLE);
				result = false;
			}else if(cohoDualPoint.geq(zero).all()){
				basis.setDualStatus(LPBasis.BasisStatus.FEASIBLE);
				result = true;
			}else{
				result = super.cohoDualFeasible(basis);
			}
			return result;
		}catch(SingularMatrixException e){
			try{
				return super.cohoDualFeasible(basis);
			}catch(LPError ee){
				basis.setDualStatus(LPBasis.BasisStatus.INFEASIBLE);
				return false;
			}
		}
	}

	//Copy from APRIntervalHybridCohoSolver
	@Override
	public boolean isClearOpt(LPBasis basis){
		return isOpt(basis);
	}

	/*
	 * Return true if it is optimal
	 * Return false if it is not optimal
	 */
	protected boolean isOpt(LPBasis basis){
		try{
			boolean dualFeasible = cohoDualFeasible(basis);
			if(dualFeasible==false){
				return false;
			}
			return cohoFeasible(basis);
		}catch(Exception e){
			basis.setStatus(LPBasis.BasisStatus.INFEASIBLE, LPBasis.BasisStatus.INFEASIBLE);
			return false;
		}
	}
}
