package coho.common.matrix;

public interface SparseMatrix extends Matrix {
	public BooleanMatrix nonZero();	// BooleanMatrix represents which is non-zero
	public boolean isZero(int row, int col);	// if the specified element is zero.
	public int nonZeroNumOfRow(int row);	// number of non-zero elements of a row.
	public int nonZeroNumOfCol(int col);  // number of non-zero elements of a col
}

