package coho.lp.solver;

import coho.common.number.*;
import coho.common.matrix.*;

public class DoubleIntervalCohoMatrix extends BasicCohoMatrix<DoubleInterval> {
	protected DoubleIntervalCohoMatrix(DoubleInterval[][] data, int[][] pos, int nrows, int ncols, boolean isDual){
		super(DoubleInterval.zero,data,pos,nrows,ncols,isDual);
	}
	public DoubleIntervalCohoMatrix(Matrix m, boolean isDual){
		super(DoubleInterval.zero,m,isDual);
	}
	public static DoubleIntervalCohoMatrix create(Matrix m, boolean isDual){
		if(m instanceof DoubleIntervalCohoMatrix && ((DoubleIntervalCohoMatrix)m).isDual == isDual)
			return (DoubleIntervalCohoMatrix)m;
		return new DoubleIntervalCohoMatrix(m,isDual);
	}
	public static DoubleIntervalCohoMatrix typeCast(BasicCohoMatrix<DoubleInterval> m){
		return new DoubleIntervalCohoMatrix(m.data(),m.pos,m.nrows(),m.ncols(),m.isDual);
	}
	public DoubleIntervalCohoMatrix convert(Matrix m, boolean isDual){
		if(m instanceof DoubleIntervalCohoMatrix && ((DoubleIntervalCohoMatrix)m).isDual == isDual)
			return (DoubleIntervalCohoMatrix)m;
		return new DoubleIntervalCohoMatrix(m,isDual);
	}
	
	public DoubleIntervalCohoMatrix trim(IntegerMatrix basis){
		return typeCast(super.trim(basis));
	}
	public DoubleIntervalCohoMatrix trim(BooleanMatrix basis){
		return typeCast(super.trim(basis));
	}
	public DoubleIntervalCohoMatrix transpose(){
		return typeCast(super.transpose());
	}
	public DoubleIntervalCohoMatrix abs(){
		return typeCast(super.abs());
	}
	public DoubleIntervalCohoMatrix negate(){
		return typeCast(super.negate());
	}
	
	/*************************************************************
	 * Solve Ax=b
	 * Support both CohoMatrix and Coho Transpose Matrix
	 * Replace the old version once complete
	 *************************************************************/
	/**
	 * Solve Ax=b for both CohoMatrix and Coho Transpose Matrix.
	 */
	/*
	 * There are three steps to solve Ax=b:
	 * 	1)Solve the row with only one non-zero variable directly. And remove that variable(col) from the matrix.
	 * 	2)Find the variable which column has only one non-zero elements and move its column and row. Solve it after the 3rd step
	 * 	3)Now the matrix is cycles matrix. Sovle it by solveCycles function
	 * 	4)Apply the resolved variable to the row of 2) and solve others
	 * 	For the Coho Transpose Matrix, only 1) and 2) are required. 
	 */
	@Override
	public DoubleIntervalMatrix getSolution(Matrix m)throws SingularMatrixException{
		return DoubleIntervalMatrix.typeCast(super.getSolution(m));
	}
	/**
	 * If the interval is large for some variable, calcualte it directly. Otherwise, from x1
	 */
	public static final double MAXINTERVAL=1e-3;
	public static final double MINXMIDDLE=1e-8;
	@Override
	protected DoubleIntervalMatrix solveCycle(BasicMatrix<DoubleInterval> alpha, BasicMatrix<DoubleInterval> y) throws SingularMatrixException{
		int n=y.length();
		// cal P from alpha recursively
		// omit P0, it's 1. P_1=P[0],P_2=P[1] and so on
		DoubleInterval[] P = new DoubleInterval[n];
		P[0] = alpha.V(0);
		for(int i=1;i<n;i++){// cal for P1 to Pn
			P[i] = alpha.V(i).mult(P[i-1]);
		}
		//check the condition
		checkCondition(P[n-1],n);

		// cal parital sum
		// similarly, partialSum_i = partialSum[i-1]
		DoubleInterval[] partialSum = new DoubleInterval[n];
		partialSum[0] = y.V(0);
		for(int i=1;i<n;i++){
			partialSum[i] = y.V(i).mult(P[i-1]).add(partialSum[i-1]);
		}
		// cal the result
		DoubleIntervalMatrix  result = DoubleIntervalMatrix.create(1,n).zeros();
		result.assign(partialSum[n-1].div(DoubleInterval.one.sub(P[n-1])),0);
		for(int i=1;i<n;i++){
			DoubleInterval x  = result.V(0).sub(partialSum[i-1]).div(P[i-1]);
			double relativeError = (x.hi().doubleValue()-x.lo().doubleValue())/Math.max(x.x().doubleValue(),MINXMIDDLE);
			if(relativeError>MAXINTERVAL){//calculate it directly
				x=solveVarDirect(alpha,y,i);
			}
			result.assign(x,i);
		}
		return result;
	}
	
	private static DoubleInterval solveVarDirect(BasicMatrix<DoubleInterval> alpha, 
			BasicMatrix<DoubleInterval> y, int shift){
		int n = y.length();
		DoubleInterval[] rotateAlpha = new DoubleInterval[n];
		DoubleInterval[] rotateY = new DoubleInterval[n];
		DoubleInterval[] P = new DoubleInterval[n];
		DoubleInterval[] partialSum = new DoubleInterval[n];

		//rotate it
		for(int i=0;i<n;i++){
			rotateAlpha[i]=alpha.V((i+shift)%n);
			rotateY[i]=y.V((i+shift)%n);
		}
		// cal P from alpha recursively
		// omit P0, it's 1. P_1=P[0],P_2=P[1] and so on
		P[0] = rotateAlpha[0];
		for(int i=1;i<n;i++){// cal for P1 to Pn-1, we don't need to calcualate Pn
			P[i] = rotateAlpha[i].mult(P[i-1]);
		}
		
		// cal parital sum
		// similarly, partialSum_i = partialSum[i-1]
		partialSum[0] = rotateY[0];
		for(int i=1;i<n;i++){
			partialSum[i] = rotateY[i].mult(P[i-1]).add(partialSum[i-1]);
		}
		
		return partialSum[n-1].div(DoubleInterval.one.sub(P[n-1]));
	}

	/*
	 * estimation of condition number for cycle matrix.
	 * see page44 from paper
	 * We have a better method to estimate the condition number
	 */
	//TODO this number is fixed now. It should be parameterized later.
	public static boolean checkException=true;
	public static double MAXCONDNUMBER = 1E+5;//test 1e5, 1e4 seems a little small
	private static void checkCondition(DoubleInterval Pn, final int n)throws SingularMatrixException{
		if(!checkException)
			return;
		DoubleInterval cn = conditionNumber(Pn, n);
		if(cn.geq(MAXCONDNUMBER)){
			throw new SingularMatrixException(cn.x().doubleValue());
		}

	}
	/*
	 * This method is incorrect. The condition number is not bounded by the ratio of max eigenvalue and min eigenvalue 
	 */
	private static DoubleInterval conditionNumber(DoubleInterval Pn, int n){
		//DoubleInterval beta = Pn.abs().nrt(n); 	
		DoubleInterval beta = Pn.abs();
		double lo = Math.pow(beta.lo().doubleValue(),1.0/n);
		double hi = Math.pow(beta.hi().doubleValue(),1.0/n);
		beta = DoubleInterval.create(lo-DoubleInterval.ulp(lo),hi+DoubleInterval.ulp(hi));

		DoubleInterval divisor = (DoubleInterval.one.sub(beta)).abs();
		DoubleInterval dividend;
		if(n%2==0)
			dividend = DoubleInterval.one.add(beta);
		else{
			int middle = Math.round(n/2);
			double cos = Math.cos(2*Math.PI*middle/n);
			dividend = (DoubleInterval)((beta.mult(beta).add(1.0)).sub(beta.mult(2*cos))).sqrt();
		}			
		if(divisor.lo().doubleValue()<=0&&divisor.hi().doubleValue()>=0)
			return DoubleInterval.create(Double.POSITIVE_INFINITY);
		return dividend.div(divisor);//return infinity if divisor contains zero.
	}
	
	
	//if the n vectors is k-dimension. 
	//B[k]=true if it's (k-1)-dimension after remove the n's row/col
	public static double DEPENDENT=1E-3;
	public BooleanMatrix independentConstraint(){
		if(!isSquare()){
			throw new MatrixError("CohoMatrix.independentConstraint(): " +
					"We only support square matrix now. rows: "+nrows+" cols: "+ncols);
		}
		if(!isDual)
			return this.transpose().independentConstraint();
		
		BooleanMatrix result = BooleanMatrix.create(ncols,1).ones();
		DoubleIntervalMatrix orth;
		DoubleInterval[] lengths = new DoubleInterval[ncols];
		for(int shift=0;shift<ncols;shift++){
			orth = DoubleIntervalMatrix.create(ncols,ncols).zeros();
			for(int col=0;col<ncols;col++){//orthogonalize each row
				int shiftCol = (shift+col)%ncols;
				DoubleIntervalMatrix thisCol = DoubleIntervalMatrix.typeCast(col(shiftCol));
				thisCol = thisCol.div(thisCol.norm());
				for(int prj=0;prj<col;prj++){
					orth.col((prj+shift)%ncols);
					DoubleInterval p = thisCol.dotProd(orth.col((prj+shift)%ncols));
					thisCol = thisCol.sub(orth.col((prj+shift)%ncols).mult(p));
				}
				if(thisCol.x().norm().doubleValue()<DEPENDENT){//We don't need exact value here
					result.assign(false,shiftCol);// if the col is singular enough, just it.
					break;
				}
				DoubleInterval length = thisCol.norm();
				lengths[shiftCol] = length;
				thisCol = thisCol.div(length);
				orth.assign(thisCol,0,shiftCol);
			}
		}
		return result;
	}
	public static void main(String args[]){
		try{
			double[][] d = {
					{1,1e100},
					{2e-100,1}};
			double[] b={1e100,1};
			DoubleIntervalCohoMatrix A = DoubleIntervalCohoMatrix.create(DoubleIntervalMatrix.create(d),false);
			DoubleIntervalMatrix B = DoubleIntervalMatrix.create(b);
			System.out.println("test");
			System.out.println(A.getSolution(B));
		}catch(SingularMatrixException e){
			System.out.println(e.toString());
		}
	}

}
