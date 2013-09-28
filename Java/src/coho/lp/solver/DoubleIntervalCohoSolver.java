package coho.lp.solver;

import java.util.*;
import coho.common.matrix.*;
import coho.common.number.*;
import coho.lp.*;

/**
 * @deprecated use APRSolver or hybrid solver
 */
// We can't use IntegerInterval or BooleanInterval to produce a simplex solver.
// We don't needd a APRInterval solver. 
public class DoubleIntervalCohoSolver extends BasicCohoSolver {
	DoubleIntervalCohoSolver(LP lp)throws LPError{
		super(lp, doubleIntervalFactory);
	}
	
	/*****************************************
	 * Simplex algorithm to find the optimal result
	 *****************************************/
	/**
	 * Return a CohoResult:
	 * 	return the minimum range for the optimal value
	 * 	return a status which is OK or possibleok.
	 *  return a iterator of possbile optimal basis or one clearly optimal basis.
	 *  	the first one is the best possible optimal basis(minimum cost);
	 *
	 * For Coho Application, the result can't be null, can't be unbounded or infeasible
	 * 
	 * c is the costVector, it can be null if the lp has cost vector already
	 */
	@Override
	public CohoSolverResult opt(Matrix c){
		//create initial feasible basis.
		LPBasis initalBasis = initialBasis(c);	
		//solve it
		CohoSolverResult result = null;
		ArrayList<LPBasis> history = new ArrayList<LPBasis>();
		try{
			result = findOptBySimplex(initalBasis,history);
		}catch(FindClearOptResultException e){
			result = e.getResult();
		}catch(IllCondBasisException e){//ill-condition
			result =findSubOpt(e.getBasis());
		}catch(UnboundedLPError e){//unbounded problems
			if(isDual)
				return new CohoSolverResult(LPResult.ResultStatus.UNBOUNDED,null,(LPBasis)null,(Matrix)null);
			else
				return new CohoSolverResult(LPResult.ResultStatus.INFEASIBLE,null,(LPBasis)null,(Matrix)null);
		}
		if(result==null){//infeasible problem 
			if(isDual)
				return new CohoSolverResult(LPResult.ResultStatus.INFEASIBLE,null,(LPBasis)null,(Matrix)null);
			else
				return new CohoSolverResult(LPResult.ResultStatus.INFEASIBLEORUNBOUNDED,null,(LPBasis)null,(Matrix)null);
		}
		if(!isDual){
			result = cohoResultFromDual(result);//TODO: It's not true if the result if from findSubOpt()
		}
		return result;
	}

	/*
	 * Find the optimal result from the branch rooted at currBasis, null if not.
	 * The cost is the minimun interval contain the true optimal cost.
	 * The basis is the one corresponded with the smallest cost of the possible optimal basis, 
	 * maybe infeasible for coho dual lp. So does the optimal point, may under-approximate for coho, but not far away the optimal one.
	 * Pay Attention: The optimal cost != optimal point * cost vector.
	 */
	//TODO: we can use the coho dual clearly feasible basis to narrow down the high value of optimal value.
	//and the coho clearly feasible basis to narrow the low value of optimal value.
	//However, the interval is not quite large now, and usually the feasible basis are much bigger
	//So we don't implemented it now. If needed later, keep a record of feasible basis.
	protected CohoSolverResult findOptBySimplex(LPBasis startBasis, ArrayList<LPBasis> history) throws UnboundedLPError{
		LPBasis currBasis, nextBasis=startBasis;
		do{
			currBasis = nextBasis;
			history.add(currBasis);
			LPBasis[] posNextBasis = pivots(currBasis);
			if(posNextBasis==null||posNextBasis.length==0){//optimal
				break;
			}else if(posNextBasis.length==1){//a clearly favoriable basis. XXX incorrect, one basis only, but may not be clearly feasible
				nextBasis = posNextBasis[0];
				//check if it's visited
				if(history.contains(nextBasis)){
					while(!history.get(0).equals(nextBasis))
						history.remove(0);
					return cohoDualResultFromHistory(history);//not found the best for this branch, find optimal for the lp				
				}
				//check if it's infeasible, and also set the status for dual.  
				if(!cohoDualFeasible(nextBasis)){//lead to a infeasible basis, drop this branch
					//return the current basis?
					//break;
					//find from history?
					return cohoDualResultFromHistory(history); 
					//return null;
				}
			}else{//try all branches
				ArrayList<CohoSolverResult> posOptResult = new ArrayList<CohoSolverResult>();
				for(int i=0; i<posNextBasis.length;i++){
					LPBasis b = posNextBasis[i];
					if(history.contains(b)){//drop the branch if visited
						continue;
					}
					if(!cohoDualFeasible(b)){//drop the branch if it's infeasible
						continue;
					}
					ArrayList<LPBasis> clone = (ArrayList<LPBasis>)history.clone();
					CohoSolverResult posOptResultFromBranch = findOptBySimplex(b,clone);
					
					if(posOptResultFromBranch!=null)
						posOptResult.add(posOptResultFromBranch);					
				}
				if(posOptResult.size()==0)//no better basis
					break;
				return cohoDualResultFromSet(posOptResult.toArray(new CohoSolverResult[posOptResult.size()]));
			}
		}while(true);
		return cohoDualResult(currBasis);
	}
	
	private LPBasis[] pivots(LPBasis currBasis)throws UnboundedLPError{
		return pivot(currBasis,0,false);
	}
	private LPBasis[] pivot(LPBasis currBasis, int start, boolean findClear) throws UnboundedLPError{
		assert currBasis.dualStatus()==LPBasis.BasisStatus.FEASIBLE: "DoubleIntervalCohoSolver.pivot: The currBasis is not feasible";
		try{
			BooleanMatrix basis = currBasis.booleanBasis(dualA.ncols());		
			CohoMatrix B = dualA.trim(basis);
			Matrix cb =dualC.row(currBasis.basis());
			Matrix d = B.transpose().getSolution(cb).transpose();
			
			//find new column
			ArrayList<Integer> cols = new ArrayList<Integer>();// store possible cols to be evicted
			int col = start;
			for(;col<ncols;col++){//we don't want to introduce the added column
				if(basis.V(col).booleanValue())
					continue;	//skip, it's can't be the new basic column
				//compute d*Aj
				CohoNumber ctj = dualA.col(col).dotProd(d); //use cohoMatrix dotProd
				//CohoMatrix Aj = dualA.convert(dualA.col(col),true);
//				CohoMatrix Aj = (CohoMatrix)dualA.col(col);
//				ArrayList<Integer> pos = Aj.rowsAtCol()[0];
//				CohoNumber ctj = d.V(pos.get(0)).mult(Aj.V(pos.get(0)));
//				if(pos.size()>1)
//					ctj = ctj.add(  d.V(pos.get(1)).mult(Aj.V(pos.get(1)))  );
				CohoNumber relativeCost = dualC.V(col).sub(ctj);
				if(relativeCost.less(0.0)){//find a cleary column .
					/*
					 * Many ill-condition excetions are caused when removing the BIGM varibles. 
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
				}//else relativeCost >= 0.0
			}
			
			if(cols.size()==0)//it's the optimal already.
				return null;
			
			//find evicted column
			Matrix T0 = B.getSolution(dualB);
			ArrayList<LPBasis> result = new ArrayList<LPBasis>();
			for(int i=0;i<cols.size();i++){
				int newCol = cols.get(i);
				Matrix Tj = B.getSolution(dualA.col(newCol));
				// find possible columns which should be evicted(possible).
				int[] evictIndex = argmin((DoubleIntervalMatrix)T0,(DoubleIntervalMatrix)Tj);// here should never throw exception		
				if(evictIndex==null||evictIndex.length==0){// here, evictIndex is not the column to be evicted, it's the index of that column in the current basis
					continue;// if remove this possible column, it's unbounded				
				}
				for(int ii=0;ii<evictIndex.length;ii++){
					int removeCol = currBasis.basis().V(evictIndex[ii]).intValue();// get the column number of the i^th of current basis.
					LPBasis newBasis = currBasis.replace(removeCol,newCol,LPBasis.BasisStatus.UNKNOWN,LPBasis.BasisStatus.UNKNOWN);//status?
					if(findClear){
						//check
						try{
							cohoDualPoint(newBasis);
						}catch(SingularMatrixException e){
							LPBasis[] nextClearBasis = pivot(currBasis,col+1,true);
							if(nextClearBasis!=null){
								return nextClearBasis;
							}
							//let findOpt() throw the exception
						}
					}
					result.add(newBasis);
				}
			}
			if(result.size()==0)//unbounded all tj is nonpositive for cj < 0  
				throw new UnboundedLPError("CohoSolver.pivot():The coho dual is unbounded" +
				" This Should Never Happen for our Coho application, but possible for genereal Ax>b form lp.");
			return result.toArray(new LPBasis[result.size()]);
		}catch(SingularMatrixException e){
			throw new LPError("CohoSolver.pivot(): This should never happen. The basis should be well-conditon.");
		}		
	}
	
	private int[] argmin(DoubleIntervalMatrix T0, DoubleIntervalMatrix Tj){
		if(T0.length()!=Tj.length()){
			throw new LPError("The length of vector T0 and Tj should be the same." +
					" T0: "+T0.length()+" Tj "+Tj.length());
		}
		//if T0_i <0, then the basis is nont feasible, we have dropped this branch
		ArrayList<Integer> posIndex = new ArrayList<Integer>();
		ArrayList<DoubleInterval> posMin = new ArrayList<DoubleInterval>();
		//We should consider all possible one. That is possibly greater than zero. 
		for(int i=0;i<T0.length();i++){
			if(Tj.V(i).leq(0.0))
				continue;
			DoubleInterval d = DoubleInterval.create(-Double.MAX_VALUE,Double.MAX_VALUE);
			if(Tj.V(i).greater(0.0))
				d = T0.V(i).div(Tj.V(i));//XXX what if infinity interval
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
	private CohoInteger[] findPosDropCols(LPBasis illCondBasis){
		DoubleIntervalCohoMatrix Abase = (DoubleIntervalCohoMatrix)dualA.trim(illCondBasis.basis());
		BooleanMatrix posSingularCols = Abase.independentConstraint().negate();
		if(!posSingularCols.any()){
			//There is no dependent vector. So we throw a unwanted ill-condition exception because the condition number is over-estimated
			//So throw any constraint will not lead to another ill-condition exception.
			posSingularCols = posSingularCols.negate();
		}
		return posSingularCols.find().toVector();
	}

	//try all or only singular one. If we remove independent one, another ill-condition may be called.
	private CohoSolverResult findSubOpt(LPBasis illCondBasis){
		int rmVar=-1;
		CohoSolverResult rmResult=null;
		DoubleInterval minOptCost = DoubleInterval.create(Double.MAX_VALUE);
		CohoInteger[] posCols = findPosDropCols(illCondBasis);//it's useless to drop independent constraint
		for(int i=0;i<posCols.length;i++){
			int tmpVar = illCondBasis.basis().V(posCols[i].intValue()).intValue();
			if(tmpVar>=ncols){//it's useless to throw the added constraint
				continue;
			}
			LP helpLP = constructDropColLP(tmpVar);
			CohoSolverResult tmpResult = null;
			try{
				tmpResult = CohoSolverFactory.getSolver(helpLP, CohoSolverFactory.Solver.DOUBLEINTERVAL).opt();
				if(tmpResult==null||(tmpResult.status()!=LPResult.ResultStatus.OK&&tmpResult.status()!=LPResult.ResultStatus.POSSIBLEOK))
					continue;//infeasible or not OK/POSSIBLEOK
			}catch(LPError e){//we may through a wrong constraint and make it unbouned or optimal basis contains added columns, just skip it
				continue;//unbounded
			}
			if(betterCost((DoubleInterval)tmpResult.optCost(),(DoubleInterval)minOptCost)){
				rmVar = tmpVar;
				rmResult = tmpResult;
				minOptCost = (DoubleInterval)tmpResult.optCost();
			}
		}
		if(rmResult==null){
			return null;//possible, if throw constraint twice
		}
		//restore the optimal basis
		Iterator<LPBasis> rmOptBasisIter = rmResult.optBasesIter();//get the best basis
		ArrayList<LPBasis> posOptBasis = new ArrayList<LPBasis>();
		while(rmOptBasisIter.hasNext()){
			LPBasis optBasis = rmOptBasisIter.next();
			IntegerMatrix matrix = optBasis.basis();
			for(int i=matrix.length()-1;i>=0;i--){
				int col = matrix.V(i).intValue();
				if(col>=rmVar)
					optBasis = optBasis.replace(col,col+1,optBasis.primalStatus(),optBasis.dualStatus());
				else
					break;//matrix is sorted
			}
			posOptBasis.add(optBasis);
		}
		return CohoSolverResult.create(rmResult.status(),  rmResult.optCost(),
				posOptBasis.toArray(new LPBasis[posOptBasis.size()]),  rmResult.optPoints());
	}
	
	private LP constructDropColLP(int col){
		BooleanMatrix pos;
		if(!findBasis){//BigM method
			pos = BooleanMatrix.create(nrows+ncols,1).ones();
			pos.assign(BooleanMatrix.create(nrows,1).zeros(),ncols,0);//remove BIGM columns
		}else{//no BigM variables
			pos = BooleanMatrix.create(ncols,1).ones();
		}
		if(col>=ncols)
			throw new LPError("CohoSolver.constructDropColLP: The extra added column introduce a ill-condition basis"+col+dualA.ncols()+dualA.nrows());
		pos.assign(false,col);//remove that column
		Matrix A = dualA.col(pos);
		Matrix b = dualB;
		Matrix c = dualC.row(pos);
		return LP.createCohoDual(new Constraint(A,b),c);
	}
	
	
	/*
	 * LPresult for the coho primary problem from coho dual result.
	 */
	private CohoSolverResult cohoResultFromDual(CohoSolverResult dualResult){
		if(dualResult==null)
			return null;
		LPResult.ResultStatus status;
		switch(dualResult.status()){
		case OK: 
			status=LPResult.ResultStatus.OK;
			break;
		case POSSIBLEOK: 
			status=LPResult.ResultStatus.POSSIBLEOK;
			break;
		case INFEASIBLE:
			status=LPResult.ResultStatus.INFEASIBLEORUNBOUNDED;
			break;
		case UNBOUNDED:
			status=LPResult.ResultStatus.INFEASIBLE;
			break;
		case INFEASIBLEORUNBOUNDED:
			status=LPResult.ResultStatus.INFEASIBLEORUNBOUNDED; 
		default:
			throw new RuntimeException("Not reachable code");	
		}
		if(status==LPResult.ResultStatus.OK||status==LPResult.ResultStatus.POSSIBLEOK){
			Iterator<LPBasis> optBases = dualResult.optBasesIter();
			ArrayList<Matrix> optPoints = new ArrayList<Matrix>();
			while(optBases.hasNext()){
				LPBasis basis = optBases.next();
				try{
					optPoints.add(cohoPoint(basis));
				}catch(SingularMatrixException e){
					//then don't compute the opt point. It's well-condition for dual, ill-condition for primal
					//return new CohoSolverResult(status,dualResult.optCost().negate(),dualResult.optBases(),null);					
					//This should never happen. Because the only place to throw SingularMatrixException is from 
					//checkCondition of DoubleIntervalCohoMatrix. Where the condition numbers for A and A' are the same. 
					//The cohoDual feasibility is checked before, so, there never throw an exception.
					throw new LPError("This should never happen");
				}
			}
			return new CohoSolverResult(status,dualResult.optCost().negate(),
					dualResult.optBases(), optPoints.toArray(new Matrix[optPoints.size()]));
		}
		else
			return new CohoSolverResult(status,(DoubleInterval)null,(LPBasis[])null,null);
	}

	@Override //throw exception when clear feasible basis found.
	public CohoSolverResult cohoDualResult(LPBasis basis){
		CohoSolverResult result = super.cohoDualResult(basis);
		if(result!=null &&result.status()==LPResult.ResultStatus.OK){
			throw new FindClearOptResultException(result);
		}
		return result;
	}
	// return false if it is clearly not feasible
	// return true if it is possible feasible or clearly feasible. 
	// This is the only place that could throw an SingularMatrixException.
	// Because in findOpt, for each new basis, this function is called first. 
	// If the basis is ill-condition, here will catch it and throw the IllCondBasisException
	@Override //throw illcondition when SingularMatrixException throw from getSolution 
	public boolean cohoDualFeasible(LPBasis basis){
		try{
			return super.cohoDualFeasible(basis);
		}catch(LPError e){
			throw new IllCondBasisException(basis,((SingularMatrixException)e.getCause()).getCond());
		}
	}
	/*
	 * Find optimal result from history
	 */
	private CohoSolverResult cohoDualResultFromHistory(ArrayList<LPBasis> basis){
		ArrayList<CohoSolverResult> results = new ArrayList<CohoSolverResult>();
		for(int i=0;i<basis.size();i++){
			LPBasis b = basis.get(i);
			CohoSolverResult result = cohoDualResult(b);
			if(result!=null)
				results.add(result);
		}
		return cohoDualResultFromSet(results.toArray(new CohoSolverResult[results.size()]));
	}
	/*
	 * Find the optimal result from sets. The input results must be OK or POSSIBLE OK
	 */
	private CohoSolverResult cohoDualResultFromSet(CohoSolverResult[] results){
		if(results.length==0)
			return null;
		DoubleInterval optCost=null, minCost = DoubleInterval.create(Double.MAX_VALUE);
		int best = -1;
		for(int i=0; i<results.length; i++){
			CohoSolverResult result = results[i];
			if(result.status()==LPResult.ResultStatus.OK){//this should never happen, an exception thrown
				return result;//find a clearly optimal result
			}
			DoubleInterval currCost = (DoubleInterval)result.optCost();
			if(betterCost(currCost,minCost)){
				minCost = currCost;
				best = i;
			}
			if(optCost==null)
				optCost=currCost;
			else//TODO reduce the interval
				optCost=new DoubleInterval(Math.min(optCost.lo().doubleValue(),currCost.lo().doubleValue()),Math.max(optCost.hi().doubleValue(),currCost.hi().doubleValue()));
		}
		ArrayList<LPBasis> optBases = new ArrayList<LPBasis>();
		ArrayList<Matrix> optPoints = new ArrayList<Matrix>();
		Iterator<LPBasis> bestBases = results[best].optBasesIter();
		Iterator<Matrix> bestPoints = results[best].optPointsIter();
		while(bestBases.hasNext()){//add the best basis in head of list
			optBases.add(bestBases.next());
			optPoints.add(bestPoints.next());
		}
		for(int i=0;i<results.length;i++){//append other basis
			if(i==best)
				continue;
			bestBases=results[i].optBasesIter();
			bestPoints=results[i].optPointsIter();
			while(bestBases.hasNext()){//add the best basis in head of list
				LPBasis basis = bestBases.next();
				Matrix point = bestPoints.next();
				if(!optBases.contains(basis)){
					optBases.add(basis);
					optPoints.add(point);
				}
			}
		}
		return new CohoSolverResult(LPResult.ResultStatus.POSSIBLEOK, optCost, 
				optBases.toArray(new LPBasis[optBases.size()]), optPoints.toArray(new Matrix[optPoints.size()]));
	}

	// how to define better cost for  doubleInterval number
	// small max value is more important or small x()?
	private boolean betterCost(DoubleInterval currCost,DoubleInterval optCost){
		return currCost.x().doubleValue()<optCost.x().doubleValue() ||
		       (  currCost.x().doubleValue()==optCost.x().doubleValue() && 
				  currCost.e().doubleValue()<optCost.e().doubleValue() 
				);
	}
	
	public class IllCondBasisException extends RuntimeException {
		  private static final long serialVersionUID = 65556L;
		  LPBasis basis;
		  double cn;
		  public LPBasis getBasis(){return basis;};
		  public double getCN(){return cn;};
		  public IllCondBasisException(LPBasis b, double cn) {
			  super(); 
			  basis=b; 
			  this.cn=cn;
		  }
		  public IllCondBasisException(LPBasis b, double cn, String msg) { 
			  super(msg);
			  basis=b; 
			  this.cn=cn;
		  }
		  /**
		   * chained exception facitly
		   */
		  public IllCondBasisException(LPBasis b, Throwable cause){
			  super(cause);
			  basis=b;
		  }
		  public IllCondBasisException(LPBasis b, String msg, Throwable cause){
			  super(msg,cause);
			  basis=b;
		  }
	}

	
//	when find the clear optimal result, jump back.
	public class FindClearOptResultException extends RuntimeException {
		private static final long serialVersionUID = 65558L;
		CohoSolverResult r;
		public FindClearOptResultException(CohoSolverResult r){
			this.r=r;		
		}
		public CohoSolverResult getResult(){
			return r;
		}
		public String toString(){
			return "The CohoSolver finds a clear optimal result: "+r.toString();
		}
	}

	
}
