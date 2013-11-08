package coho.lp.solver;

import coho.lp.*;
import coho.common.number.*;
import coho.common.matrix.*;


public class APRDoubleHybridCohoSolver extends BasicCohoSolver {
	DoubleIntervalCohoMatrix intervalA;// A copy of dualA used when double interval fails to find a unique pivot 
	DoubleIntervalMatrix intervalB, intervalC;
	DoubleCohoMatrix doubleA;
	DoubleMatrix doubleB,doubleC;
	public APRDoubleHybridCohoSolver(LP lp) throws LPError {
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
		doubleA = DoubleCohoMatrix.create(dualA,true);
		doubleB = DoubleMatrix.create(dualB);
		doubleC = DoubleMatrix.create(dualC);
		return result;
	}
	@Override
	protected void init(Matrix c){
		super.init(c);
		intervalA = DoubleIntervalCohoMatrix.create(dualA, true);//dualA is avaliable now
		intervalB = DoubleIntervalMatrix.create(dualB);
		intervalC = DoubleIntervalMatrix.create(dualC);		
		doubleA = DoubleCohoMatrix.create(dualA,true);
		doubleB = DoubleMatrix.create(dualB);
		doubleC = DoubleMatrix.create(dualC);
	}

	//@Override
	//Find nextBasis using double and then check if it is correct by reduceCost and feasibility. 
	//If failed, use APR. 
	protected LPBasis pivot(LPBasis currBasis) throws UnboundedLPError{
		//try double, copy from CohoSolver
		try{
			BooleanMatrix basis = currBasis.booleanBasis(doubleA.ncols());		
			CohoMatrix B = doubleA.trim(currBasis.basis());
			/*
			 * find new column
			 * A new efficient method to compute relativecost, see pp83-84 @ master thesis.
			 */
			int newCol = 0;
			Matrix cb =doubleC.row(currBasis.basis());
			Matrix d = B.transpose().getSolution(cb).transpose();
			CohoNumber relativeCost = null;
			for(;newCol<ncols;newCol++){//we don't want to introduce the added column
				if(basis.V(newCol).booleanValue())
					continue;	//skip if it is in the basis, it's can't be the new basic column
				
				// compute d*Aj
				CohoNumber ctj = doubleA.col(newCol).dotProd(d); //use cohoMatrix dotProd
				//CohoMatrix Aj = doubleA.convert(doubleA.col(newCol),true);
//				CohoMatrix Aj = (CohoMatrix)doubleA.col(newCol);
//				java.util.ArrayList<Integer> pos = Aj.rowsAtCol()[0];
//				CohoNumber ctj = d.V(pos.get(0)).mult(Aj.V(pos.get(0)));
//				if(pos.size()>1)
//					ctj = ctj.add(  d.V(pos.get(1)).mult(Aj.V(pos.get(1)))  );
				
				relativeCost = doubleC.V(newCol).sub(ctj);
				if(relativeCost.compareTo(0)<0){
					break;
				}//if relativeCost = 0; it is degeneracy, we have get the optimal one. we don't want to continue it anymore
			}			
			if(newCol==ncols){//it may not be optimal
				if(isClearOpt(currBasis)){
					return null;
				}else{
					return super.pivot(currBasis);
				}
				//return super.pivot(currBasis);
			}
				//return null;
			/*
			 * find column to be evicted out
			 */
			Matrix T0 = B.getSolution(doubleB);
			Matrix Tj = B.getSolution(doubleA.col(newCol));
			//argmin(t0i/tji)
			int evictBasis = -1;
			CohoNumber min = null;
			for( int i=0; i<T0.length(); i++){
				CohoNumber tji = Tj.V(i);
				if(tji.compareTo(0)<=0)
					continue;
				CohoNumber r = T0.V(i).div(tji);
				if(min==null||min.compareTo(r)>0){//can't use >=0 anti-cycle algorihtm
					evictBasis = i;
					min = r;
				}
			}
			//This might be or not be unbounded LP
			if(evictBasis<0){//unbounded all tj is nonpositive for cj < 0
				return super.pivot(currBasis);
				//throw new UnboundedLPError("APRDoubleHybridCohoSolver.pivot(): The lp is unbouned using double, try to use apr");
			}
			//cohoDualFeasible is faster than reduceCost
			int removeCol = currBasis.basis().V(evictBasis).intValue();
			LPBasis newBasis = currBasis.replace(removeCol,newCol,LPBasis.BasisStatus.UNKNOWN,LPBasis.BasisStatus.UNKNOWN);			
			//if(cohoDualFeasible(newBasis) && reduceCost(currBasis,newCol,evictBasis).less(0)){
			if(costReduced(currBasis,newCol,evictBasis) && cohoDualFeasible(newBasis)){
				return newBasis;
			}else{
				return super.pivot(currBasis);
			}
			
//			//compute the reduce cost using interval to check. 
//			if(reduceCost(currBasis,newCol,evictBasis).less(0)){
//				int removeCol = currBasis.basis().V(evictBasis).intValue();// get the column number of the i^th of current basis.
//				//The basis could be infeasible or even not inveritable. Therfore, *Feasible function must handle the SingularMatrixException. 
//				LPBasis newBasis = currBasis.replace(removeCol,newCol,LPBasis.BasisStatus.UNKNOWN,LPBasis.BasisStatus.UNKNOWN);
//				if(cohoDualFeasible(newBasis)){
//					return newBasis;
//				}else{
//					return super.pivot(currBasis);
//				}
//			}else{//check fail
//				return super.pivot(currBasis);
//			}
		}catch(SingularMatrixException e){
			//The basis found by double might be invertiable, therefore, it is possible to throw a SingularMatrixException.
			//reduceCost here will not throw SingularMatrixException, because if the basis is not inveritable, the exception is throw before.
			//But it is possible the reduceCost is [-inf,inf]
			return super.pivot(currBasis);
		}
	}
	
	private CohoNumber reduceCost(LPBasis basis, int newCol, int evictBasis) throws SingularMatrixException{
		try{
			CohoMatrix B = intervalA.trim(basis.basis());
			Matrix tj = B.getSolution(intervalA.col(newCol));
			Matrix cb = intervalC.row(basis.basis());
			//CohoNumber relativeCost = intervalC.V(newCol).sub(cb.transpose().mult(tj).V(0));
			CohoNumber relativeCost = intervalC.V(newCol).sub(cb.dotProd(tj));
			Matrix t0 = B.getSolution(intervalB);
			CohoNumber reduceCost = t0.V(evictBasis).div(tj.V(evictBasis)).mult(relativeCost);
			return reduceCost;
		}catch(ArithmeticException e){//divided by zero
			return DoubleInterval.create(-Double.MAX_VALUE,Double.MAX_VALUE);
		}
	}
	
	//Save with APRIntervalHybridCohoSolver TODO: update it later.
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
				boolean result = super.cohoFeasible(basis);//for example, divided by zero causd by interval
				return result;
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
	private boolean costReduced(LPBasis basis, int newCol, int evictBasis)throws SingularMatrixException{
		//use interval
		try{
			CohoNumber reduceCost = reduceCost(basis,newCol,evictBasis);
			if(reduceCost.less(0)){
				return true;
			}
			if(reduceCost.geq(0)){
				return false;
			}
		}catch(ArithmeticException e){//divided by zero
			//use apr
		}catch(SingularMatrixException e){
			//if basis is singular, the apr also throw the exception
			//if caused by double interval, use apr
		}
		try{
			//use apr
			CohoMatrix B = dualA.trim(basis.basis());
			Matrix tj = B.getSolution(dualA.col(newCol));
			Matrix cb = dualC.row(basis.basis());
			CohoNumber relativeCost = dualC.V(newCol).sub(cb.dotProd(tj));
			Matrix t0 = B.getSolution(dualB);
			CohoNumber reduceCost =  t0.V(evictBasis).div(tj.V(evictBasis)).mult(relativeCost);
			return reduceCost.less(0);
		}catch(ArithmeticException e){//BUG: divide by zero
			return false;
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
			boolean isOpt = cohoDualFeasible(basis) &&  cohoFeasible(basis) ; //cohoFeasible is slower than cohoDualFeasible
			return isOpt;
		}catch(Exception e){
			basis.setStatus(LPBasis.BasisStatus.INFEASIBLE, LPBasis.BasisStatus.INFEASIBLE);
			return false;
		}
	}

}
