package coho.lp.solver;

import java.util.*;

import coho.lp.*;
import coho.common.matrix.*;
import coho.common.number.*;

// support Double and APR based solver
// For interval, see DoubleIntervalCohoSolver

//NOTE: for APR CohoSolver, all bases visited are dual feasible and of course invertable
//For APRIntervalCohoSolver, all bases are dual feasible unless the current basis is optimal. In that case, it will call APR
//For double and APRDouble CohoSolver, it is possible incorrect bases are introduced. Therefore, functions must handle the SingularMatrixException.
//However, we do not consider it in the CohoSolver. Because it is used as APR not double till now. Using Double Only can not implemented a correct solver. Use APRDouble instead.
//And all, findOptByOneHop also introduce all kinds of basis, it calls isClearOpt only now. For that function, if Exception happens. It return false. (In fact, it is non-invertiable basis).
//SingularMatrixException can be cause by two possible 1) basis is singular, 2) interval computation cause arithmetic exception.
public class BasicCohoSolver implements CohoSolver{
	protected final LP lp;
	public LP lp(){return lp;}
	protected CohoMatrix dualA;//copy of lp
	protected Matrix dualB;
	protected Matrix dualC;
    //number of rows/cols for original dualA, we add nrows cols to A.
	//If BIGM method is used to find initial basis, then some columns are added to dualA
	// then ncols!=dualA.ncols. If BIGM is not used, then dualA.ncols = ncols
	protected int nrows, ncols;
	protected boolean isDual;
	private DataFactory dataFactory = aprFactory;//data related operations
	public DataFactory dataFactory(){//TRY Used by LPProject
		return dataFactory;
	}

	protected boolean findBasis=true;	
	public static final double BIGM = 1e100;

	public static boolean CohoSolver_DEBUG =true;
	public static boolean CohoSolver_DEBUG_DETAIL=true;
	
	// can't call the constructor outside of this package. Use CohoSolver Factory instead
	// initilize lp and dataFactory only. 
	// initilize the dualA, dualB, dualC and nrows, ncols when cost vector is avaliable in initBasis and opt(Matrix,LPBasis,int).
	BasicCohoSolver(LP lp, DataFactory factory)throws LPError{
		if(factory!=aprFactory){
			throw new RuntimeException("Only aprFactory is supported now.");
		}
		if(lp.isCoho()){
			isDual = false;
		}else if(lp.isCohoDual()){
			isDual = true;
		}else
			throw new LPError("CohoSolver only supports" +
			" standard coho problme or dual of coho problem");
		this.lp = lp;
		if(factory!=null)
			this.dataFactory = factory;
	}
	public BasicCohoSolver(LP lp)throws LPError{
		this(lp,aprFactory);
	}

//	public interface DataFactory{
//		public CohoMatrix createCohoMatrix(Matrix m, boolean isDual);
//		public Matrix createMatrix(Matrix m);
//	}	
//	static final DataFactory doubleFactory = new DataFactory(){
//		public DoubleCohoMatrix createCohoMatrix(Matrix m, boolean isDual){
//			if(m instanceof DoubleCohoMatrix)
//				return (DoubleCohoMatrix)m;
//			return DoubleCohoMatrix.create(m, isDual);
//		}
//		public DoubleMatrix createMatrix(Matrix m){
//			if(m instanceof DoubleMatrix)
//				return (DoubleMatrix)m;
//			return DoubleMatrix.create(m);
//		}
//	};
//	static final DataFactory aprFactory = new DataFactory(){
//		public APRCohoMatrix createCohoMatrix(Matrix m, boolean isDual){
//			if(m instanceof APRCohoMatrix)
//				return (APRCohoMatrix)m;
//			return APRCohoMatrix.create(m, isDual);
//		}
//		public APRMatrix createMatrix(Matrix m){
//			if(m instanceof APRMatrix)
//				return (APRMatrix)m;
//			return APRMatrix.create(m);
//		}
//	};
//
//	static final DataFactory doubleIntervalFactory = new DataFactory(){
//		public DoubleIntervalCohoMatrix createCohoMatrix(Matrix m, boolean isDual){
//			if(m instanceof DoubleIntervalCohoMatrix)
//				return (DoubleIntervalCohoMatrix)m;
//			return DoubleIntervalCohoMatrix.create(m, isDual);
//		}
//		public DoubleIntervalMatrix createMatrix(Matrix m){
//			if(m instanceof DoubleIntervalMatrix)
//				return (DoubleIntervalMatrix)m;
//			return DoubleIntervalMatrix.create(m);
//		}
//	};
	
	public  CohoSolverResult opt(){
		return opt((BasicMatrix)null);
	}
	public CohoSolverResult opt(Matrix c){
//		System.out.println("Direction:"+c);
		CohoSolverResult result = opt(c,null);
//		System.out.println("Optimal Basis:"+result.optBasis().toString());
		return result;
	}
	public  CohoSolverResult opt(LPBasis initalBasis){
		return opt(null,initalBasis);
	}
	public  CohoSolverResult opt(Matrix c, LPBasis initialBasis){
		if(initialBasis==null)
			initialBasis = initialBasis(c);//find initial feasible basis by bigM method.
		else
			init(c);//use the given feasible basis.
		LPBasis optBasis;
		try{
			optBasis= findOptBySimplex(initialBasis);
		}catch(UnboundedLPError e){//unbounded problem
			if(isDual)
				return new CohoSolverResult(LPResult.ResultStatus.UNBOUNDED,new CohoDouble(Double.MAX_VALUE),(LPBasis)null,(Matrix)null);
			else
				return new CohoSolverResult(LPResult.ResultStatus.INFEASIBLE,null,(LPBasis)null,(Matrix)null);
		}
		if(optBasis==null){//infeasible problem
			if(isDual){
				return new CohoSolverResult(LPResult.ResultStatus.INFEASIBLE,null,(LPBasis)null,(Matrix)null);
			}else{
				// we do not know how to distinct a unbounded lp with a infeasible lp
				return new CohoSolverResult(LPResult.ResultStatus.INFEASIBLEORUNBOUNDED,null,(LPBasis)null,(Matrix)null);
//				if(isCohoUnbounded())
//					return new CohoSolverResult(LPResult.ResultStatus.UNBOUNDED,new CohoDouble(Double.MAX_VALUE),(LPBasis)null,(Matrix)null);
//				else
//					return new CohoSolverResult(LPResult.ResultStatus.INFEASIBLE,null,(LPBasis)null,(Matrix)null);
				
			}
		}

		assert cohoFeasible(optBasis) : "algorithm error. the optimal basis is not feasible for primal\n"+optBasis+"\n"+lp;
		
		if(isDual){			
			return cohoDualResult(optBasis); 
		}else{
			return cohoResult(optBasis);
		}
	}
	
	
	/*
	 * Initialization for not using BIGM method. 
	 */
	protected void init(Matrix c){
		if(c==null && dualA !=null)
			return;
		if(c!=null)
			lp.setC(c);
		LP cohoDualLP = lp;//we will not change the lp. 
		if(!isDual)
			cohoDualLP = lp.dual();
		dualA = dataFactory.createCohoMatrix(cohoDualLP.eq().a(),true);
		dualB = dataFactory.createMatrix(cohoDualLP.eq().b());
		dualC = dataFactory.createMatrix(cohoDualLP.c());
		nrows = dualA.nrows();
		ncols = dualA.ncols();
	}
	
	/**
	 * extract data from lp when necessary
	 * try to find a initial basis from the lp
	 * if not, use the bigM methods
	 */
	protected LPBasis initialBasis(Matrix c){
		if(c!=null)
			lp.setC(c);
		LP cohoDualLP = lp;//we will not change the lp. 
		if(!isDual)
			cohoDualLP = lp.dual();

		CohoMatrix origA = dataFactory.createCohoMatrix(cohoDualLP.eq().a(),true);
		Matrix origB = cohoDualLP.eq().b();
		Matrix origC = cohoDualLP.c();

		nrows = origA.nrows();
		ncols = origA.ncols();

		/*
		 * Try to find a feasible basis directly
		 */
		ArrayList<Integer>[] rowsAtCol = origA.rowsAtCol();
		IntegerMatrix basis = IntegerMatrix.create(nrows,1).zeros();
		boolean[] hasBasis = new boolean[nrows];
		CohoNumber zero = origB.zero();
		for(int col=0;col<ncols;col++){
			if(rowsAtCol[col].size()==1){
				int row = rowsAtCol[col].get(0);
				boolean positive = ((origB.V(row).eq(zero)) ||
						((origA.V(row,col).greater(zero)==(origB.V(row).greater(zero)))));
				if(positive){//this can be in the basis
					hasBasis[row]=true;
					basis.assign(col,row);
				}
			}
		}
		for(int i=0;i<nrows;i++){
			if(!hasBasis[i]){
				findBasis=false;
				break;
			}				
		}

		if(findBasis){
			dualA = origA;
			dualB = dataFactory.createMatrix(origB);
			dualC = dataFactory.createMatrix(origC);
			return new LPBasis(basis,LPBasis.BasisStatus.UNKNOWN,LPBasis.BasisStatus.FEASIBLE);//FIXED: add status
		}else{//else use BigM method
			Matrix helpA = origA.convert(nrows,ncols+nrows).zeros();
			Matrix helpC = origC.convert(ncols+nrows,1).zeros();
			helpA.assign(origA,0,0);
			helpA.assign(helpA.ident(nrows),0,ncols);
			helpC.assign(origC,0,0);
			for(int i=0;i<nrows;i++){//make the initial basis feasible
				if(origB.V(i).less(zero)){
					helpA.assign(helpA.col(ncols+i).negate(),0,ncols+i);
				}
				helpC.assign(BIGM,ncols+i);
			}
			dualA = dataFactory.createCohoMatrix(helpA, true);
			dualB = dataFactory.createMatrix(origB);
			dualC = dataFactory.createMatrix(helpC);
			for(int i=0;i<nrows;i++)
				basis.assign(ncols+i,i);
			return new LPBasis(basis,LPBasis.BasisStatus.UNKNOWN,LPBasis.BasisStatus.FEASIBLE);
		}
	}
	/*
	 * Given a feasible basis, find the optimal one using simplex method.
	 * If the problem is unbounded, throw an UnboundedLPError exception.
	 * If the problem is infeasible, return null;
	 */ 
	protected LPBasis findOptBySimplex(LPBasis startBasis)throws UnboundedLPError{
		LPBasis currBasis, nextBasis=startBasis;
		do{
			currBasis = nextBasis;
			//System.out.println(currBasis);
			nextBasis = pivot(currBasis);
			if(nextBasis == null){//optimal
				//contains extra variables, infeasible for dual, unbounded or infeasible for primal
				if(!findBasis && currBasis.basis().V(currBasis.basis().length()-1).geq(ncols)){
					return null;
				}
				return currBasis;
			}	
		}while(true);
	}
	
	protected LPBasis pivot(LPBasis currBasis)throws UnboundedLPError{
		assert currBasis.dualStatus()==LPBasis.BasisStatus.FEASIBLE: "CohoSolver.pivot: The currBasis is not feasible";
		try{
			BooleanMatrix basis = currBasis.booleanBasis(dualA.ncols());		
			CohoMatrix B = dualA.trim(currBasis.basis());
			/*
			 * find new column
			 * A new efficient method to compute relativecost, see pp83-84 @ master thesis.
			 */
			int newCol = 0;
			Matrix cb =dualC.row(currBasis.basis());
			Matrix d = B.transpose().getSolution(cb).transpose();
			CohoNumber relativeCost = null;
			for(;newCol<ncols;newCol++){//we don't want to introduce the added column
				if(basis.V(newCol).booleanValue())
					continue;	//skip if it is in the basis, it's can't be the new basic column
				
				// compute d*Aj
				CohoNumber ctj = dualA.col(newCol).dotProd(d); //use cohoMatrix dotProd
				//CohoMatrix Aj = dualA.convert(dualA.col(newCol),true);
//				CohoMatrix Aj = (CohoMatrix)dualA.col(newCol);
//				ArrayList<Integer> pos = Aj.rowsAtCol()[0];
//				CohoNumber ctj = d.V(pos.get(0)).mult(Aj.V(pos.get(0)));
//				if(pos.size()>1)
//					ctj = ctj.add(  d.V(pos.get(1)).mult(Aj.V(pos.get(1)))  );
				
				relativeCost = dualC.V(newCol).sub(ctj);
				//System.out.println("newCol"+newCol+"relativeCost"+relativeCost);
				if(relativeCost.compareTo(0)<0){
					break;
				}//if relativeCost = 0; it is degeneracy, we have get the optimal one. we don't want to continue it anymore
			}			
			if(newCol==ncols){//it's the optimal already.
				return null;
			}
			/*
			 * find column to be evicted out
			 */
			Matrix T0 = B.getSolution(dualB);
			Matrix Tj = B.getSolution(dualA.col(newCol));
			//System.out.println("T0"+T0+"Tj"+Tj);
			//argmin(t0i/tji)
			int evictCol = -1;
			CohoNumber min = null;
			for( int i=0; i<T0.length(); i++){
				CohoNumber tji = Tj.V(i);
				if(tji.compareTo(0)<=0)
					continue;
				CohoNumber r = T0.V(i).div(tji);
				//System.out.println("i "+i+" r "+r);
				if(min==null||min.compareTo(r)>0){//can't use >=0 anti-cycle algorihtm
					evictCol = i;
					min = r;
				}
			}
			if(evictCol<0){//unbounded all tj is nonpositive for cj < 0
				throw new UnboundedLPError("CohoSolver.pivot():The coho dual is unbounded. Thus the coho lp is infeasible" +
				" This Should Never Happen for our Coho application, but possible for genereal Ax>b form lp.");
			}
			int removeCol = currBasis.basis().V(evictCol).intValue();// get the column number of the i^th of current basis.
			//The new basis is dual feasible and primal unknown
			return currBasis.replace(removeCol,newCol,LPBasis.BasisStatus.UNKNOWN,LPBasis.BasisStatus.FEASIBLE);
		}catch(SingularMatrixException e){
			//e.printStackTrace();
			throw new LPError("CohoSolver.pivot(): This should never happen. The basis should be well-conditon.",e);
		}
	}

	/*
	 * This function is used in LPProject to speedup computation. 
	 * cohoFeasibleBasis is the optimal basis of previous optimal direction, thus feasible for cohoLP 
	 * and infeasible for cohoDualLP.
	 * evict is the index of infeasible variable of cohoFeasibleBasis.
	 * evict is not the index of cols to remove, it is the index of cohoFeasibleBasis to remove. 
	 */	
	//public static int counter1 = 0, counter2=0, counter3=0;
	public	CohoSolverResult opt(Matrix c, LPBasis cohoFeasibleBasis, int evict){
//		System.out.println("Direction:"+c);
//		coho.lp.project.LPProject.oneHopLP++;
		init(c);
		LPBasis optBasis;
		CohoSolverResult result = null;
		//Try to find optimal basis from cohoFeasibleBasis within one hop
		try{
			optBasis= findOptInOneHop(cohoFeasibleBasis, evict);
		}catch(UnboundedLPError e){//unbounded problem
			if(isDual)
				result = new CohoSolverResult(LPResult.ResultStatus.UNBOUNDED,new CohoDouble(Double.MAX_VALUE),(LPBasis)null,(Matrix)null);
			else
				result = new CohoSolverResult(LPResult.ResultStatus.INFEASIBLE,null,(LPBasis)null,(Matrix)null);
//			System.out.println("Optimal Basis:"+result.optBasis().toString());
			return result;
		}
		//coho.debug.STAT.timers[3] += System.nanoTime()-time;
		//If failed, solve it from scrach
		if(optBasis==null){
			//coho.lp.project.LPProject.failLP++;
			result = opt();
//			System.out.println("Optimal Basis:"+result.optBasis().toString());
			return result;
		}		
        //find a feasible basis
		if(optBasis.primalStatus()!=LPBasis.BasisStatus.FEASIBLE){
			//coho.lp.project.LPProject.feasibleLP++;
			result = opt(optBasis);
//			System.out.println("Optimal Basis:"+result.optBasis().toString());
			return result;
		}
		if(isDual){			
			result = cohoDualResult(optBasis); 
		}else{
			result = cohoResult(optBasis);
		}
		//coho.lp.project.LPProject.sucessLP++;
//		System.out.println("Optimal Basis:"+result.optBasis().toString());
		return result;
	}
	
	/*
	 * Try to find the optimal basis from startBasis within one hop.
	 * return null if not a feasible basis found. 
	 * return the optimal basis if one optimal basis is found.
	 * return the feasible basis if one feasible basis is found.
	 *  
	 */
//	public static int sameCounter = 0,diffCounter1=0;
//	public static int same = 0, diff = 0, fail = 0;
	public static int oneHopMethod = 3;
	protected LPBasis findOptInOneHop(LPBasis startBasis,int evict)throws UnboundedLPError{
		LPBasis dualFeasibleBasis = null;
		BooleanMatrix boolBasis = startBasis.booleanBasis(ncols);
		int evictedCol = startBasis.basis().V(evict).intValue();
		if(oneHopMethod==3){
			//MEATHOD 3: evicted and new column has the same non-zero row
			boolean[] tried = new boolean[ncols];
			for(int i=0; i<ncols; i++){
				tried[i] = boolBasis.V(i).booleanValue();
			}
			//try column with same non-zero elements first, 70%
			ArrayList<Integer>[] rowsAtCol = dualA.rowsAtCol();
			ArrayList<Integer>[] colsAtRow = dualA.colsAtRow();
			for(int i=0; i<rowsAtCol[evictedCol].size(); i++){
				int row = rowsAtCol[evictedCol].get(i);
				for (int j=0;j<colsAtRow[row].size();j++){
					int newCol = colsAtRow[row].get(j);
					if(!tried[newCol]){
						tried[newCol] = true;
						//sameCounter ++;
						LPBasis basis = startBasis.replace(evictedCol, newCol,LPBasis.BasisStatus.UNKNOWN,LPBasis.BasisStatus.UNKNOWN);
//						System.out.println("try "+basis.basis());
						if(isClearOpt(basis)){//the dualStatus is always computed.
							return basis;
						}else if(basis.dualStatus()==LPBasis.BasisStatus.FEASIBLE){
							if(dualFeasibleBasis ==null){
								dualFeasibleBasis = basis;
							}				
						}else{
							//nothing
						}	
//						System.out.println("end try");
					}
				}
			}
//			//try others later or call opt() directly? 20%
//			for(int newCol = 0; newCol < ncols; newCol++){
//				if(!tried[newCol]){
//					tried[newCol] = true;
//					//diffCounter1 ++;
//					LPBasis basis = startBasis.replace(evictedCol, newCol,LPBasis.BasisStatus.UNKNOWN,LPBasis.BasisStatus.UNKNOWN);
//					if(isClearOpt(basis)){//the dualStatus is always computed.
//						//diff ++;
//						return basis;
//					}else if(basis.dualStatus()==LPBasis.BasisStatus.FEASIBLE){
//						if(dualFeasibleBasis ==null){
//							dualFeasibleBasis = basis;
//						}				
//					}else{
//						//nothing
//					}									
//				}
//			}
//			//fail++;
		}

		else if(oneHopMethod ==2){
			//MEATHOD 2: order by distance from evicted column
			//System.out.println("sameCounter: "+sameCounter+" same "+same+" fail "+fail);		
			for(int dist = 1; dist<Math.max(evictedCol+1,ncols-evictedCol); dist++){
				for(int newCol = evictedCol-dist;newCol<=evictedCol+dist;newCol += 2*dist){//try evictedCol+/-dist
					if(newCol<0 || newCol>=ncols || boolBasis.V(newCol).booleanValue()){
						continue;
					}
					/*
					 * NOTE: basis could be infeasible or even non-inveritable. Therefore, isClearOpt must handle the SingularMatrixException. 
					 */
					//sameCounter++;		       
					LPBasis basis = startBasis.replace(evictedCol, newCol,LPBasis.BasisStatus.UNKNOWN,LPBasis.BasisStatus.UNKNOWN);
					if(isClearOpt(basis)){//the dualStatus is always computed.
						//same++;
						return basis;
					}else if(basis.dualStatus()==LPBasis.BasisStatus.FEASIBLE){
						if(dualFeasibleBasis ==null){
							dualFeasibleBasis = basis;
						}				
					}else{
						//nothing
					}
				}
			}
			//fail++;
		}
		
		else{
			//MEATHOD 1: order by column index
			//System.out.println("sameCounter: "+sameCounter+" same "+same+" fail "+fail);
			for(int newCol = 0; newCol<ncols; newCol++){
				if(boolBasis.V(newCol).booleanValue())
					continue;
				/*
				 * NOTE: basis could be infeasible or even non-inveritable. Therefore, isClearOpt must handle the SingularMatrixException. 
				 */
				//sameCounter++;
				LPBasis basis = startBasis.replace(evictedCol, newCol,LPBasis.BasisStatus.UNKNOWN,LPBasis.BasisStatus.UNKNOWN);
				if(isClearOpt(basis)){//the dualStatus is always computed.
					//System.out.println(Math.abs(newCol-evictedCol)/(0.0+ncols));
					//same++;
					return basis;
				}else if(basis.dualStatus()==LPBasis.BasisStatus.FEASIBLE){
					if(dualFeasibleBasis ==null){
						dualFeasibleBasis = basis;
					}				
				}else{
					//nothing
				}
			}
			//fail++;
		}
		return dualFeasibleBasis;
	}
	

	/*
	 * Compute the status of LPResult given the optBasis as the optimal basis. 
	 */
	public LPResult.ResultStatus resultStatus(LPBasis optBasis){
		return resultStatus(optBasis,null,null);
	}
	public LPResult.ResultStatus resultStatus(LPBasis optBasis, Matrix cohoPoint, Matrix cohoDualPoint){
		if(cohoPoint!=null)
			cohoFeasible(optBasis,cohoPoint);
		else
			cohoFeasible(optBasis);
		if(cohoDualPoint!=null)
			cohoDualFeasible(optBasis,cohoDualPoint);
		else
			cohoDualFeasible(optBasis);
		if(optBasis.primalStatus()==LPBasis.BasisStatus.FEASIBLE && optBasis.primalStatus()==LPBasis.BasisStatus.FEASIBLE)
			return LPResult.ResultStatus.OK;//optimal
		if(optBasis.primalStatus()==LPBasis.BasisStatus.INFEASIBLE || optBasis.primalStatus()==LPBasis.BasisStatus.INFEASIBLE)
			return LPResult.ResultStatus.INFEASIBLE;//not optimla
		else 
			return LPResult.ResultStatus.POSSIBLEOK;//possible for doubleIntervalFactory		
	}

	/*
	 * create LPResult for the basis if it is optimal or possible optimal. 
	 * return null otherwise
	 */	
	public CohoSolverResult cohoDualResult(LPBasis optBasis){
		return cohoDualResult(optBasis,false);
	}
	public CohoSolverResult cohoDualResult(LPBasis optBasis, boolean checkStatus){
		try{
			CohoSolverResult result = null;
			Matrix point = cohoDualPoint(optBasis);
			LPResult.ResultStatus status = LPResult.ResultStatus.OK;
			if(checkStatus)
				status = resultStatus(optBasis,null,point);
			switch(status){
			case OK:
			case POSSIBLEOK:
				result = new CohoSolverResult(status, cohoDualCost(point),optBasis, point);
			default:
				//return null;
			}
			return result;
		}catch(SingularMatrixException e){
			throw new LPError("cohoResult: This should never happen. The basis is not invertible",e);
		}		
	}
	/*
	 * Find the point of basis for the coho dual problem
	 * Throw a MatrixException if the basis is ill-condition.
	 */
	public Matrix cohoDualPoint(LPBasis dualFeasibleBasis) throws SingularMatrixException{
		IntegerMatrix basis = dualFeasibleBasis.basis();
//		System.out.println(dualA.trim(basis));
//		System.out.println(dualB);
//		System.out.println(basis);
		Matrix result = dualC.zeros().assign(dualA.trim(basis).getSolution(dualB),basis);
		return result;
	}
	//return cost for the converted coho dual lp. used to find optBasis from set.
	public CohoNumber cohoDualCost(LPBasis dualFeasibleBasis){
		try{
			Matrix point = cohoDualPoint(dualFeasibleBasis);
			//return (dualC.transpose()).mult(point).V(0);
			return dualC.dotProd(point);
		}catch(SingularMatrixException e){
			throw new LPError("CohoSolver.dualCost: This should never happen, the input basis is not invertiable.",e);
		}		
	}	
	public CohoNumber cohoDualCost(Matrix point){
		return dualC.dotProd(point);
	}
	/*
	 * Compute the feasiblity of basis for cohoDualLP and set its dualStatus of basis.
	 * Pay attention, if the dataFactory is doubleIntervalFactory, it only means that the basis 
	 * is possibleFeasible or clearFeasible.  See the status of basis for more details then.
	 */
	public boolean cohoDualFeasible(LPBasis basis){
		if(basis.dualStatus()!=LPBasis.BasisStatus.UNKNOWN){
			return basis.dualStatus()!=LPBasis.BasisStatus.INFEASIBLE;
		}
		try{
			return cohoDualFeasible(basis,cohoDualPoint(basis));
//			Matrix cohoDualPoint = cohoDualPoint(basis);
//			Matrix zero = cohoDualPoint.zeros();
//			if(cohoDualPoint.less(zero).any()){
//				basis.setDualStatus(LPBasis.BasisStatus.INFEASIBLE);
//				return false;
//			}else if(cohoDualPoint.geq(zero).all()){
//				basis.setDualStatus(LPBasis.BasisStatus.FEASIBLE);
//			}else{
//				basis.setDualStatus(LPBasis.BasisStatus.POSSIBLEFEASIBLE);
//			}
//			return true;
		}catch(SingularMatrixException e){
			throw new LPError("cohoDualFeasible: This should never happen. The basis is not invertiable",e);
		}
	}
	public boolean cohoDualFeasible(LPBasis basis, Matrix cohoDualPoint){
		if(basis.dualStatus()!=LPBasis.BasisStatus.UNKNOWN)
			return basis.dualStatus()!=LPBasis.BasisStatus.INFEASIBLE;		
		Matrix zero = cohoDualPoint.zeros();
		if(cohoDualPoint.less(zero).any()){
			basis.setDualStatus(LPBasis.BasisStatus.INFEASIBLE);
			return false;
		}else if(cohoDualPoint.geq(zero).all()){
			basis.setDualStatus(LPBasis.BasisStatus.FEASIBLE);
		}else{
			basis.setDualStatus(LPBasis.BasisStatus.POSSIBLEFEASIBLE);
		}
		return true;		
	}
	/*
	 * create LPResult for the basis if it is optimal or possible optimal. 
	 * return null otherwise
	 */
	public CohoSolverResult cohoResult(LPBasis optBasis){
		return cohoResult(optBasis,false);
	}
	/*
	 * NOTE: Improve performance. Set Status to OK directly. If this function is called by findOptInOneHop,
	 * its feasiablity is checked already. If it's called by findOptBySimplex, dualFeasibility is checked already.
	 * And it must be optimal. Otherwise, use the assert in the function to debug. 
	 */
	public CohoSolverResult cohoResult(LPBasis optBasis, boolean checkStatus){
		try{
			CohoSolverResult result = null;
			Matrix point = cohoPoint(optBasis);
			LPResult.ResultStatus status = LPResult.ResultStatus.OK;
			if(checkStatus)
				status = resultStatus(optBasis,point,null);//TODO: do we need to check the status?
			switch(status){
			case OK:
			case POSSIBLEOK:
				result = new CohoSolverResult(status, cohoCost(point),optBasis,point);
			default:
				//return null;
			}
			return result;
		}catch(SingularMatrixException e){
			throw new LPError("cohoResult: This should never happen. The basis is not invertiable",e);
		}
	}
	/*
	 * Find the point of basis for the coho problem
	 */
	public Matrix cohoPoint(LPBasis primalFeasibleBasis)throws SingularMatrixException{
		IntegerMatrix pos = primalFeasibleBasis.basis();
		//Matrix cohoPoint = dualA.transpose()/*negateh*/.trim(pos).getSolution(dualC.row(pos)/*negate*/);//E^-1*x
		Matrix cohoPoint = dualA.trim(pos).transpose().getSolution(dualC.row(pos)/*negate*/);//E^-1*x
		if(lp().fwd()!=null){//A*bwd*x=b
			// NOTE, I find this bug after several years. I think the result shoud ble x_opt = fwd*y_opt 
			// rather than x_opt = y_opt*fwd. Change it back if it is incorrect.
			//cohoPoint = cohoPoint.mult(cohoPoint.convert(lp.fwd()));// x_opt=fwd*y_opt 
			cohoPoint = cohoPoint.convert(lp.fwd()).mult(cohoPoint);// x_opt=fwd*y_opt
		}
		return cohoPoint;
	}
	public CohoNumber cohoCost(Matrix cohoPoint){
		//NOTE, I find this bug in 2010, change it back if it is incorrect. 
		//  If fwd&bwd are not null, the primal-dual LP relationship is
		// 		-min(c'x)		(Coho)		*		min(-b'y)	(Coho Dual)		
		//   s.t A*bwd*x >= b				* 		y >=0
		//   	x free						*		-(A*bwd)'y=-c  <==> -A'y = -fwd'c
		// The optimal value should be c'*x_opt = (-bwd'*dualB)'*x_opt = -dualB'*bwd*x_opt
 		//return dualB.dotProd(cohoPoint).negate();
		CohoNumber V;
		if(lp().bwd()!=null){
			V = dualB.dotProd(lp.bwd().mult(cohoPoint)).negate();
		}else{
			V = dualB.dotProd(cohoPoint).negate();
		}
		return V;
	}
	public CohoNumber cohoCost(LPBasis basis){
		try{
			//here, we don't call the cohoPoint to avoid the problem of fwd;
			IntegerMatrix pos = basis.basis();
			//Matrix cohoPoint = dualA.transpose()/*negateh*/.trim(pos).getSolution(dualC.row(pos)/*negate*/);
			Matrix cohoPoint = dualA.trim(pos).transpose().getSolution(dualC.row(pos)/*negate*/);
			return dualB.dotProd(cohoPoint).negate();//dualB.transpose().mult(cohoPoint).V(0).negate();
		}catch(SingularMatrixException e){
			throw new LPError("This should never happen. The basis is not invertiable",e);
		}
	}
	
	/*
	 * Compute the status of LPResult given the optBasis as the optimal basis.
	 * Pay attention, if the dataFactory is doubleIntervalFactory, it only means that the basis 
	 * is possibleFeasible or clearFeasible.  See the status of basis for more details then.
	 * Throw LPError is the basis is not invertiable
	 */
	public boolean cohoFeasible(LPBasis basis){
		if(basis.primalStatus()!=LPBasis.BasisStatus.UNKNOWN)
			return basis.primalStatus()!=LPBasis.BasisStatus.INFEASIBLE;
		try{
			return cohoFeasible(basis,cohoPoint(basis));
//			IntegerMatrix pos = basis.basis();
//			//improve performance, about 10%
//			Matrix cohoPoint = dualA.trim(pos).transpose().getSolution(dualC.row(pos));
//			BooleanMatrix care = basis.booleanBasis(dualC.length()).negate();
//			Matrix re = dualC.row(care).sub(dualA.col(care).transpose().mult(cohoPoint));
////			Matrix cohoPoint = dualA.transpose()/*negateh*/.trim(pos).getSolution(dualC.row(pos)/*negate*/);//E^-1*x
////			Matrix re = dualA.transpose().mult(cohoPoint).sub(dualC).negate();//AE^{-1}x-b for primal
////			re = re.row(basis.booleanBasis(dualC.length()).negate());//we don't need to compare rows in the basis\
//			Matrix zero = re.zeros();
//			if(re.less(zero).any()){
//				basis.setPrimalStatus(LPBasis.BasisStatus.INFEASIBLE);
//				return false;
//			}else if(re.geq(zero).all()){
//				basis.setPrimalStatus(LPBasis.BasisStatus.FEASIBLE);
//			}else{
//				basis.setPrimalStatus(LPBasis.BasisStatus.POSSIBLEFEASIBLE);
//			}
//			return true;
		}catch(SingularMatrixException e){
			throw new LPError("CohoSolver.dualCost: This should never happen, the input basis is not invertible.",e);
		}	
	}
	public boolean cohoFeasible(LPBasis basis, Matrix cohoPoint){
		if(basis.primalStatus()!=LPBasis.BasisStatus.UNKNOWN)
			return basis.primalStatus()!=LPBasis.BasisStatus.INFEASIBLE;
		BooleanMatrix care = basis.booleanBasis(dualC.length()).negate();
		Matrix re = dualC.row(care).sub(dualA.col(care).transpose().mult(cohoPoint));
		Matrix zero = re.zeros();
		if(re.less(zero).any()){
			basis.setPrimalStatus(LPBasis.BasisStatus.INFEASIBLE);
			return false;
		}else if(re.geq(zero).all()){
			basis.setPrimalStatus(LPBasis.BasisStatus.FEASIBLE);
		}else{
			basis.setPrimalStatus(LPBasis.BasisStatus.POSSIBLEFEASIBLE);
		}
		return true;		
	}
	
	/*
	 * Return true if it is clearly optimal basis. The status is also assigned.
	 * Return false if it is not clearly optimal basis. It is possible the primalStatus is not assigned. 
	 * But the dualStatus is always computed.
	 * Return false if the basis is not feasible or any exception happens.
	 */
	public boolean isClearOpt(LPBasis basis){
		try{
			cohoDualFeasible(basis);
			if(basis.dualStatus() == LPBasis.BasisStatus.FEASIBLE){
				cohoFeasible(basis);
				if(basis.primalStatus()==LPBasis.BasisStatus.FEASIBLE){//optimal
					return true;
				}
			}
			return false;
		}catch(Exception e){//SingularMatrixException cause by infeasible basis or ArithmeticException cause by doubleIntervalFactory
			basis.setStatus(LPBasis.BasisStatus.INFEASIBLE, LPBasis.BasisStatus.INFEASIBLE);//infact, the basis is not invertiable.
			return false;
		}
	}
	/*
	 * Test if this coho lp is unbounded or not
	 * If it is unbouned along this opt direction, its contribution is zero.
	 * WRONG: it can be unbouned on other direction
	 */
	private boolean isCohoUnbounded(){
		Matrix A = lp.neq().a();
		Matrix c = lp.c();
		Matrix re = A.mult(c);
		if(re.eq(re.zeros()).all())
			return true;
		else 
			return false;			
	}
	
	public static void main(String args[]){
		//example for no optimal result
		double[][] A = {
				{     0.09457215083353103,   0.0021705472053314967,                    -0.0 },
		 {   -2.928183932307364E-5,   0.0019760043425969265,                    -0.0 },
		 {   -0.006158298846188838,      0.1765857200515727,                    -0.0 },
		 {  -1.3100821442772227E-4,                    -0.0,                    -0.0 },
		 {    -0.09847645810706145,  -0.0023185668573137974,                    -0.0 },
		 {   4.8617321212506004E-4,   -0.021948900527354098,                    -0.0 },
		 {    0.008872083192123376,    -0.15646480421483322,                    -0.0 },
		 {    8.646397692216035E-4,                    -0.0,                    -0.0 },
		 {    7.302547685321681E-4,                    -0.0,     0.00873608954503935 },
		 {   -0.018753502161373503,                    -0.0,       0.171996182054462 },
		 {    -0.20588518286825708,                    -0.0,  -1.7982115791070896E-5 },
		 {   5.3737562873079625E-5,                    -0.0,  -0.0033048682620322456 },
		 {    0.037028780061372224,                    -0.0,     -0.1774075918540129 },
		 {      0.1868259126368531,                    -0.0,  -1.8293676651293111E-6 },
		 {    -0.03771949601406854,                    -0.0,     0.18071686247054142 },
		 {    -0.04525905098336591,                    -0.0,   -3.952948357088815E-6 },
		 {   5.3737562873079625E-5,                    -0.0,  -0.0033048682620322456 },
		 {    0.037028780061372224,                    -0.0,     -0.1774075918540129 },
		 {     0.04589602937318915,                    -0.0,  -4.4940613919131067E-7 }
		};
		double[] b = {
				     0.09617745821023485 ,
				     -1.76869158876751E-5 ,
				    -0.005189955384505362 ,
				   -1.5665184475666878E-4 ,
				     -0.11778118051837233 ,
				   -0.0018548180118961623 ,
				    -0.006890714041499105 ,
				     8.773182628415303E-4 ,
				      0.00851991896575631 ,
				      0.13382674628904678 ,
				      -0.2462016176440764 ,
				   -0.0036191888168852953 ,
				      -0.1535782705086978 ,
				      0.18956377233988994 ,
				      0.14809907650681123 ,
				     -0.05412167796587164 ,
				   -0.0036191888168852953 ,
				      -0.1535782705086978 ,
				      0.04656861749320273 
		};
		double[] c = {-0.9999699390142412,0.007753777650580621,-8.731426168613141E-5};

		DoubleMatrix MA = DoubleMatrix.create(A);
		DoubleMatrix Mb = DoubleMatrix.create(b);
		DoubleMatrix Mc = DoubleMatrix.create(c);
		LP lp = LP.createCoho(new Constraint(MA,Mb));
		CohoSolver solver= CohoSolverFactory.getSolver(lp,CohoSolverFactory.Solver.APR);
		//solver = new APRIntervalHybridCohoSolver(lp);
		LPResult result = solver.opt(Mc);
		System.out.println(result.toString(true));
		
//		double[][] AA={
//				{0, 0.13029964537309358},
//				{-0.08099361309186094, 0},
//				{0,-0.13029964537309358},
//				{0.08099361309186094,0},
//				{1,0},
//				{-1,0},
//				{0,1},
//				{0,-1}
//		};
//		double[] bb = {
//				1.3671345425541955E-4 ,
//				-0.1413540484795392 ,
//				-0.010690152517610449 ,
//				0.13080060941618415 ,
//				1.56 ,
//				-1.69 ,
//				-0.04000000000000001 ,
//				-0.04000000000000001
//		};
//		double[] cc={
//				1,0
//		};
//		MA = DoubleMatrix.create(AA);
//		Mb = DoubleMatrix.create(bb);
//		Mc = DoubleMatrix.create(cc);
//		lp = LP.createCoho(new Constraint(MA,Mb));
//		solver= CohoSolverFactory.getSolver(lp,CohoSolverFactory.Solver.APR);
//		//solver = new APRIntervalHybridCohoSolver(lp);
//		result = solver.opt(Mc);
//		System.out.println(result.toString(true));
//		
	}
}
