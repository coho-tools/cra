package coho.lp.solver;

import coho.common.matrix.*;
import coho.lp.*;

public interface CohoSolver extends LPSolver {
	public CohoSolverResult opt();	
	public CohoSolverResult opt(Matrix c);
	public CohoSolverResult opt(Matrix c, LPBasis cohoFeasibleBasis, int evict);
	public DataFactory dataFactory();
	public interface DataFactory{
		public CohoMatrix createCohoMatrix(Matrix m, boolean isDual);
		public Matrix createMatrix(Matrix m);
	}	
	static final DataFactory doubleFactory = new DataFactory(){
		public DoubleCohoMatrix createCohoMatrix(Matrix m, boolean isDual){
			if(m instanceof DoubleCohoMatrix)
				return (DoubleCohoMatrix)m;
			return DoubleCohoMatrix.create(m, isDual);
		}
		public DoubleMatrix createMatrix(Matrix m){
			if(m instanceof DoubleMatrix)
				return (DoubleMatrix)m;
			return DoubleMatrix.create(m);
		}
	};
	static final DataFactory aprFactory = new DataFactory(){
		public APRCohoMatrix createCohoMatrix(Matrix m, boolean isDual){
			if(m instanceof APRCohoMatrix)
				return (APRCohoMatrix)m;
			return APRCohoMatrix.create(m, isDual);
		}
		public APRMatrix createMatrix(Matrix m){
			if(m instanceof APRMatrix)
				return (APRMatrix)m;
			return APRMatrix.create(m);
		}
	};
	static final DataFactory doubleIntervalFactory = new DataFactory(){
		public DoubleIntervalCohoMatrix createCohoMatrix(Matrix m, boolean isDual){
			if(m instanceof DoubleIntervalCohoMatrix)
				return (DoubleIntervalCohoMatrix)m;
			return DoubleIntervalCohoMatrix.create(m, isDual);
		}
		public DoubleIntervalMatrix createMatrix(Matrix m){
			if(m instanceof DoubleIntervalMatrix)
				return (DoubleIntervalMatrix)m;
			return DoubleIntervalMatrix.create(m);
		}
	};
}
