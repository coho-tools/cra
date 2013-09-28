package coho.lp.solver;

import coho.common.matrix.*;
import coho.common.number.*;

public class DoubleCohoMatrix extends BasicCohoMatrix<CohoDouble> {
	protected DoubleCohoMatrix(CohoDouble[][] data, int[][] pos, int nrows, int ncols, boolean isDual){
		super(CohoDouble.zero,data,pos,nrows,ncols,isDual);
	}
	public DoubleCohoMatrix(Matrix m, boolean isDual){
		super(CohoDouble.zero,m,isDual);
	}
	public static DoubleCohoMatrix create(Matrix m, boolean isDual){
		if( m instanceof DoubleCohoMatrix && ((DoubleCohoMatrix)m).isDual == isDual)
			return (DoubleCohoMatrix)m;
		return new DoubleCohoMatrix(m,isDual);
	}
	public DoubleCohoMatrix convert(Matrix m, boolean isDual){
		if( m instanceof DoubleCohoMatrix && ((DoubleCohoMatrix)m).isDual == isDual)
			return (DoubleCohoMatrix)m;
		return new DoubleCohoMatrix(m,isDual);
	}
	public static DoubleCohoMatrix typeCast(BasicCohoMatrix<CohoDouble> m){
		return new DoubleCohoMatrix(m.data(), m.pos, m.nrows(), m.ncols(), m.isDual);
	}

	public DoubleCohoMatrix trim(IntegerMatrix basis){
		return typeCast(super.trim(basis));
	}
	public DoubleCohoMatrix trim(BooleanMatrix basis){
		return typeCast(super.trim(basis));
	}
	public DoubleCohoMatrix transpose(){
		return typeCast(super.transpose());
	}
	public DoubleCohoMatrix abs(){
		return typeCast(super.abs());
	}
	public DoubleCohoMatrix negate(){
		return typeCast(super.negate());
	}
	
	/*
	 * Classical implementation. But over-approximation not guranteed. 
	 * Might incorrect because of round off for highly ill-condition problem
	 * @see coho.lp.solver.BasicCohoMatrix#getSolution(coho.common.matrix.Matrix)
	 */
	public DoubleMatrix getSolution(Matrix m)throws SingularMatrixException {
		return DoubleMatrix.typeCast(super.getSolution(m));
	}
	public static void main(String args[]){
		try{
			double[][] d = {
					{-1,-1e100},
					{-2e-100,-1}};
			double[] b={1e100,1};
			DoubleCohoMatrix A = DoubleCohoMatrix.create(APRMatrix.create(d),false);
			APRMatrix B = APRMatrix.create(b);
			System.out.println(A.getSolution(B));		
			System.out.println(A.max());
			System.out.println(A.min());
		}catch(SingularMatrixException e){
			System.out.println("exception"+e.toString());
		}
	}
}
