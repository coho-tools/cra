package coho.common.matrix;
/*
 * support the template of the Matrix. Initialization not required 
 */
public interface ArrayFactory {
	public Object[][] createArray(int nrows, int ncols);
	public Object[] createVector(int length);
}
