package coho.lp.solver;

import java.util.ArrayList;
import coho.common.matrix.*;

public interface CohoMatrix extends SparseMatrix {
	public Matrix getSolution(Matrix b) throws SingularMatrixException;
	public Matrix leftDiv(Matrix m)throws SingularMatrixException;//same with getSolution
	public ArrayList<Integer>[] colsAtRow();
	public ArrayList<Integer>[] rowsAtCol();
	public boolean isDual();
	public Matrix expand();
	public CohoMatrix transpose();
	public CohoMatrix negate();
	public CohoMatrix abs();
	public CohoMatrix trim(IntegerMatrix a);
	public CohoMatrix trim(BooleanMatrix a);
	public CohoMatrix convert(Matrix m, boolean isDual);
}
