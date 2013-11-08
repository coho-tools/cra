package coho.lp;

// if a constraint is an equality constraint, we interpret it as:
//      a*x = b
// if a constraint is an inequality constraint, we interpret it as:
//      a*x >= b
// change a b from DoubleMatrix to BasicMatrix, which allow the constraint more generic. 
// This will allow the input as RationalMatrix also. 2006.09.25
// Redefine the class. Change BasicMatrix to Matrix which allows all kinds of matrices. 
// Do we need to add a filed to distinct the equality and inequality case?

import coho.common.matrix.*;

public final class Constraint{
	private Matrix a; 
	private Matrix b;
	public Matrix a(){return a;};
	public Matrix b(){return b;};
//	private boolean eq;//not used now
//	public boolean isEq(){return eq;}//not used now
	public Constraint(Matrix _a, Matrix _b){
		if(_b.size(1)!=1){
			throw new LPError("'b' of the constraint must be a column vector");
		}
		if(_a.size(0)!=_b.size(0)){
			throw new LPError("'a' and 'b' are not compatible");
		}
		a = _a;
		b = _b;
	}
//	public Constraint(Matrix _a, Matrix _b, boolean _eq){
//		if(_b.size(1)!=1){
//			throw new LPError("'b' of the constraint must be a column vector");
//		}
//		if(_a.size(0)!=_b.size(0)){
//			throw new LPError("'a' and 'b' are not compatible");
//		}
//		a = _a;
//		b = _b;
//		eq = _eq;
//	}
	public int[] size(){return a.size();}
	public int size(int i){return a.size(i);}
	public int nrows(){return a.size(0);}
	public int ncols(){return a.size(1);}
	
	public void drop(int d){
	    BooleanMatrix keep = BooleanMatrix.create(b.size()).ones().assign(false,d);
	    a = a.row(keep);
	    b = b.row(keep);		
	}
	public String toString(){
		return ("constraint(\n+"+a.toString()+",\n"+b.toString()+")");
	}	
}
