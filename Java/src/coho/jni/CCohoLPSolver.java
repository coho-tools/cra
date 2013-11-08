package coho.jni;

import coho.common.number.*;
import coho.common.matrix.*;
import coho.lp.*;
import coho.lp.solver.*;

public class CCohoLPSolver implements CohoSolver {
	private final LP lp;
	private CohoSolverResult result;
	public DataFactory dataFactory(){
		return CohoSolver.aprFactory;
	}
	//interface
	public CCohoLPSolver(LP lp){
		if(lp.isCoho()){//only support coho lp
			this.lp = lp;
		}else{
			throw new UnsupportedOperationException("We only support coho lp");
		}
		if(lp.fwd()!=null||lp.bwd()!=null){
			throw new UnsupportedOperationException("fwd must be null");
		}
		//from Ax>=b(java) to -Ax<=-b(c)
		Matrix A = lp.neq().a().negate();
		Matrix b = lp.neq().b().negate();
		if(!(b instanceof DoubleMatrix)){
			throw new UnsupportedOperationException("b should be double matrix");
		}
		if(A instanceof DoubleMatrix){
			A = DoubleCohoMatrix.create(A, false);//not dual
		}
		if(!(A instanceof DoubleCohoMatrix)){
			throw new UnsupportedOperationException("A should be double coho matrix");
		}
		createCSolver((DoubleCohoMatrix)A,(DoubleMatrix)b);
	}
	public LP lp() {
		return lp;
	}

	public CohoSolverResult opt() {
		throw new UnsupportedOperationException("It's not used now, implement later");
	}
	public CohoSolverResult opt(Matrix c) {
		if(c.elementType()==CohoAPR.type){
			cOpt(new APRMatrix(c));
		}else if(c.elementType()==CohoDouble.type){
			cOpt(new DoubleMatrix(c));
		}else{
			throw new UnsupportedOperationException();
		}
		return result;
	}
	public CohoSolverResult opt(Matrix c, LPBasis basis, int evict){
//		System.out.println("Direction:"+c);
//		System.out.println("Try with java");
//		CohoSolver solver = new APRDoubleHybridCohoSolver(lp);
//		solver.opt(c, basis, evict);
		if(c.elementType()==CohoAPR.type){
			cOpt(new APRMatrix(c),basis.basis(),evict);
		}else if(c.elementType()==CohoDouble.type){
			cOpt(new DoubleMatrix(c),basis.basis(),evict);
		}else{
			throw new UnsupportedOperationException();
		}
//		System.out.println("Optimal Basis:"+result.optBasis().toString());
		return result;
	}

	//called by c
	private void setOptResult(double v, IntegerMatrix basis, DoubleMatrix point){
		LPBasis b = new LPBasis(basis,LPBasis.BasisStatus.FEASIBLE,LPBasis.BasisStatus.FEASIBLE);
		result = new CohoSolverResult(LPResult.ResultStatus.OK,CohoDouble.create(v),b,point);
	}
	private void setOptResult(double v, int[] basis, double[] point){
		int[][] bm = new int[basis.length][1];
		for(int i=0; i<basis.length; i++){
			bm[i][0] = basis[i];
		}
		LPBasis b = new LPBasis(IntegerMatrix.create(bm),LPBasis.BasisStatus.FEASIBLE,LPBasis.BasisStatus.FEASIBLE);
		result = new CohoSolverResult(LPResult.ResultStatus.OK,CohoDouble.create(v),b,DoubleMatrix.create(point));
	}
	//gni
	static {
		try{
			System.loadLibrary("CCohoLPSolver");
			initCSolver();
		}catch(UnsatisfiedLinkError e){
			System.err.println("Can not find the c++ coho lp solver");
		}
	}
	private native static void initCSolver();
	private native static void createCSolver(final DoubleCohoMatrix A, final DoubleMatrix b);
	private native void cOpt(final DoubleMatrix c);
	private native void cOpt(final DoubleMatrix c, final IntegerMatrix basis, int evict);
	private native void cOpt(final APRMatrix c);
	private native void cOpt(final APRMatrix c, final IntegerMatrix basis, int evict);
	//private native static void testRational(final APRMatrix c);
	
	public static void main(String[] args){
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
		
//		DoubleMatrix Mc = DoubleMatrix.create(c);
//		APRMatrix Rc = APRMatrix.create(Mc);
//		System.out.println(Rc);
//		CCohoLPSolver.testRational(Rc);
		DoubleMatrix MA = DoubleMatrix.create(A);
		DoubleMatrix Mb = DoubleMatrix.create(b);
		DoubleMatrix Mc = DoubleMatrix.create(c);
		LP lp = LP.createCoho(new Constraint(MA,Mb));
		CCohoLPSolver solver = new CCohoLPSolver(lp);
		LPResult result = solver.opt(Mc);
		System.out.println(result.toString(true));
	}
}
