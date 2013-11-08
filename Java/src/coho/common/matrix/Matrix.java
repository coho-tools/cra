package coho.common.matrix;
import coho.common.number.*;
public interface Matrix {
	public Matrix convert(int nrows, int ncol);//empty
	public Matrix convert(int[] size);
	public Matrix convert();//empty
	public Matrix convert(Matrix m);
	public Matrix convert(CohoNumber[][] data);
	public Matrix convert(CohoNumber[] data);
	public Matrix convert(Number[][] data);
	public Matrix convert(Number[] data);
	
	public int nrows();
	public int ncols();
	public int[] size();
	public int size(int pos);
	public int length();//for vector
	public CohoType elementType();
	
	public boolean isVector();
	public boolean isSquare();
	public CohoNumber[][] toArray();
	public CohoNumber[] toVector();

	public Matrix assign(CohoNumber v, int row, int col);
	public Matrix assign(Number v, int row, int col);
	public Matrix assign(CohoNumber v, int n);//for vector
	public Matrix assign(Number v, int n);//for vector
	public Matrix assign(Matrix m);
	public Matrix assign(CohoNumber[][] v);
	public Matrix assign(Number[][] v);
	public Matrix assign(CohoNumber[] v);
	public Matrix assign(Number[] v);
	public Matrix assign(Matrix m, int v_row, int v_col);
	public Matrix assign(Matrix m, int n);
	public Matrix assign(Matrix m, BooleanMatrix mask);
	public Matrix assign(Matrix m, IntegerMatrix pos);//for vector
	public CohoNumber V(int row, int col);
	public CohoNumber V(int n);//for vector
	public Matrix V(Range row, Range col);
	public Matrix V(Range n);//for vector
	public Matrix V(Range row, int col);
	public Matrix V(int row, Range col);
	public Matrix V(IntegerMatrix pos);//for vector
	public Matrix row(int row);
	public Matrix row(Range row);
	public Matrix row(IntegerMatrix pos);
	public Matrix row(BooleanMatrix mask);
	public Matrix col(int col);
	public Matrix col(Range col);
	public Matrix col(IntegerMatrix pos);
	public Matrix col(BooleanMatrix mask);

	public CohoNumber one();
	public CohoNumber zero();
	public CohoNumber random();
	public Matrix ones();
	public Matrix zeros();
	public Matrix randoms();
	public Matrix ident();
	public Matrix ident(int n);
	public Matrix diag();
	public Matrix fill(CohoNumber v);
	public Matrix fill(Number v);

	public Matrix abs();
	public Matrix negate();
	public Matrix transpose();
	public CohoNumber max();
	public CohoNumber min();	
	public CohoNumber norm();
	public CohoNumber prod();//sum and prod are useless
	public CohoNumber sum();
	public Matrix inv() throws SingularMatrixException;
	
	public Matrix add(Matrix that);//promotion
	public Matrix add(CohoNumber that);//no promotion
	public Matrix add(Number that);
	public Matrix sub(Matrix that);
	public Matrix sub(CohoNumber that);
	public Matrix sub(Number that);
	public Matrix mult(Matrix that);
	public Matrix mult(CohoNumber that);
	public Matrix mult(Number that);
	public Matrix elMult(Matrix that);//this*inv(that);
	public Matrix div(Matrix that)throws SingularMatrixException;
	public Matrix div(CohoNumber that);
	public Matrix div(Number that);
	public Matrix leftDiv(Matrix that)throws SingularMatrixException;//inv(this)*that
	public Matrix elDiv(Matrix that);	
	public CohoNumber dotProd(Matrix that);//A'*B
	
	public BooleanMatrix eq(Matrix that);
	public BooleanMatrix neq(Matrix that);
	public BooleanMatrix greater(Matrix that);
	public BooleanMatrix geq(Matrix that);
	public BooleanMatrix less(Matrix that);
	public BooleanMatrix leq(Matrix that);
	
	public String toString();
	public String toString(Object fmt);
	public String toMatlab();
}
