package coho.lp.solver;

import java.util.ArrayList;

import coho.common.matrix.*;
import coho.common.number.CohoNumber;
import coho.common.number.DoubleInterval;
import coho.lp.*;

public class APRIntervalDoubleHybridCohoSolver extends	APRIntervalHybridCohoSolver {
	DoubleCohoMatrix doubleA;
	DoubleMatrix doubleB,doubleC;
	public APRIntervalDoubleHybridCohoSolver(LP lp)throws LPError{
		super(lp);
	}
	@Override
	protected LPBasis initialBasis(Matrix c){
		LPBasis result = super.initialBasis(c);
		doubleA = DoubleCohoMatrix.create(dualA,true);
		doubleB = DoubleMatrix.create(dualB);
		doubleC = DoubleMatrix.create(dualC);
		return result;
	}
	@Override
	protected void init(Matrix c){
		super.init(c);
		doubleA = DoubleCohoMatrix.create(dualA,true);
		doubleB = DoubleMatrix.create(dualB);
		doubleC = DoubleMatrix.create(dualC);
	}
	
	//@Override
	//Find nextBasis using double and then check if it is correct by reduceCost and feasibility. 
	//If failed, use super (double->interval->APR).	
	//This is not a good strategy. Because once double failed, it's high probable that interval fails too. 
	//See APRDoubleHybridCohoSolver.
	protected LPBasis pivot(LPBasis currBasis) throws UnboundedLPError{
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
//				ArrayList<Integer> pos = Aj.rowsAtCol()[0];
//				CohoNumber ctj = d.V(pos.get(0)).mult(Aj.V(pos.get(0)));
//				if(pos.size()>1)
//					ctj = ctj.add(  d.V(pos.get(1)).mult(Aj.V(pos.get(1)))  );

				relativeCost = doubleC.V(newCol).sub(ctj);
				if(relativeCost.compareTo(0)<0){
					break;
				}//if relativeCost = 0; it is degeneracy, we have get the optimal one. we don't want to continue it anymore
			}			
			if(newCol==ncols){//it may not be optimal
				return super.pivot(currBasis);
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

			//reduceCost here will not throw SingularMatrixException, because if the basis is not inveritable, the exception is throw before.
			//But it is possible the reduceCost is [-inf,inf]
			//if(reduceCost(currBasis,newCol,evictBasis).less(0)){
			if(costReduced(currBasis,newCol,evictBasis)){
				int removeCol = currBasis.basis().V(evictBasis).intValue();// get the column number of the i^th of current basis.
				//The basis could be infeasible or even not inveritable. Therfore, *Feasible function must handle the SingularMatrixException. 
				LPBasis newBasis = currBasis.replace(removeCol,newCol,LPBasis.BasisStatus.UNKNOWN,LPBasis.BasisStatus.UNKNOWN);
				if(cohoDualFeasible(newBasis)){//sucess
					return newBasis;
				}else{
					return super.pivot(currBasis);
				}
			}else{//check fail
				return super.pivot(currBasis);
			}
		}catch(SingularMatrixException e){
			//The basis found by double might be invertiable, therefore, it is possible to throw a SingularMatrixException.
			return super.pivot(currBasis);
		}
	}
	
	//compute the reduce cost using interval to check. 
	private CohoNumber reduceCost(LPBasis basis, int newCol, int evictBasis) throws SingularMatrixException{
		try{
			CohoMatrix B = intervalA.trim(basis.basis());
			Matrix tj = B.getSolution(intervalA.col(newCol));
			Matrix cb = intervalC.row(basis.basis());
			//CohoNumber relativeCost = intervalC.V(newCol).sub(cb.transpose().mult(tj).V(0));
			CohoNumber relativeCost = intervalC.V(newCol).sub(cb.dotProd(tj));
			Matrix t0 = B.getSolution(intervalB);
			return t0.V(evictBasis).div(tj.V(evictBasis)).mult(relativeCost);
		}catch(ArithmeticException e){//divided by zero
			return DoubleInterval.create(-Double.MAX_VALUE,Double.MAX_VALUE);
		}
	}
	/*
	 * Return true if the reduce cost <0
	 * Return false if reduce cost >=0
	 * Throw SingularMatrixException if the basis is not invertiable.
	 */
	private boolean costReduced(LPBasis basis, int newCol, int evictBasis)throws SingularMatrixException{
		//use interval
		try{
//			CohoMatrix B = intervalA.trim(basis.basis());
//			Matrix tj = B.getSolution(intervalA.col(newCol));
//			Matrix cb = intervalC.row(basis.basis());
//			CohoNumber relativeCost = intervalC.V(newCol).sub(cb.dotProd(tj));
//			Matrix t0 = B.getSolution(intervalB);
//			CohoNumber reduceCost =  t0.V(evictBasis).div(tj.V(evictBasis)).mult(relativeCost);
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
		//use apr
		try{
			CohoMatrix B = dualA.trim(basis.basis());
			Matrix tj = B.getSolution(dualA.col(newCol));
			Matrix cb = dualC.row(basis.basis());
			CohoNumber relativeCost = dualC.V(newCol).sub(cb.dotProd(tj));
			Matrix t0 = B.getSolution(dualB);
			CohoNumber reduceCost =  t0.V(evictBasis).div(tj.V(evictBasis)).mult(relativeCost);
			return reduceCost.less(0);
		}catch(ArithmeticException e){//divide by zero
			return false;
		}
	}
	


}
