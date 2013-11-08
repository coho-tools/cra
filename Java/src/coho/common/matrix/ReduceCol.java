package coho.common.matrix;

// ColReduce:  apply the function to entire columns.
public interface ReduceCol extends Reduce {
	BasicMatrix create(int size, BasicMatrix[] x);
}
