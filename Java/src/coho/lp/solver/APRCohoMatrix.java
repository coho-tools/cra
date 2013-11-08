package coho.lp.solver;

import coho.common.matrix.*;
import coho.common.number.*;

public class APRCohoMatrix extends BasicCohoMatrix<CohoAPR> {
	protected APRCohoMatrix(CohoAPR[][] data, int[][] pos, int nrows, int ncols, boolean isDual){
		super(CohoAPR.zero,data,pos,nrows,ncols,isDual);
	}
	public APRCohoMatrix(Matrix m, boolean isDual){
		super(CohoAPR.zero,m,isDual);
	}
	public static APRCohoMatrix create(Matrix m, boolean isDual){
		if(m instanceof APRCohoMatrix && ((APRCohoMatrix)m).isDual == isDual)
			return (APRCohoMatrix)m;
		return new APRCohoMatrix(m,isDual);
	}
	public APRCohoMatrix convert(Matrix m, boolean isDual){
		if(m instanceof APRCohoMatrix && ((APRCohoMatrix)m).isDual == isDual)
			return (APRCohoMatrix)m;
		return new APRCohoMatrix(m,isDual);
	}
	public static APRCohoMatrix typeCast(BasicCohoMatrix<CohoAPR> m){
		return new APRCohoMatrix(m.data(), m.pos, m.nrows(), m.ncols(), m.isDual);
	}
	
	public APRCohoMatrix trim(IntegerMatrix basis){
		return typeCast(super.trim(basis));
	}
	public APRCohoMatrix trim(BooleanMatrix basis){
		return typeCast(super.trim(basis));
	}
	public APRCohoMatrix transpose(){
		return typeCast(super.transpose());
	}
	public APRCohoMatrix abs(){
		return typeCast(super.abs());
	}
	public APRCohoMatrix negate(){
		return typeCast(super.negate());
	}
	
	public APRMatrix getSolution(Matrix m)throws SingularMatrixException {
		return APRMatrix.typeCast(super.getSolution(m));
	}
		public static void main(String args[]){
		try{
			double[][] d = {
					{-1,-1e100},
					{-2e-100,-1}};
			double[] b={1e100,1};
			APRCohoMatrix A = APRCohoMatrix.create(APRMatrix.create(d),false);
			APRMatrix B = APRMatrix.create(b);
			System.out.println(A.getSolution(B));		
			System.out.println(A.max());
			System.out.println(A.min());
		}catch(SingularMatrixException e){
			System.out.println("exception"+e.toString());
		}
	}
}
