package coho.common.matrix;

// RowReduce:  apply the function to entire rows.
public interface ReduceRow extends Reduce {
	BasicMatrix create(int size, BasicMatrix[] x);
}
