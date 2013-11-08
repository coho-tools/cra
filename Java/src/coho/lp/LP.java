package coho.lp;

import coho.common.matrix.*;

/**
 * Class defined for general linear programming LP(A,b,c,M,N).
 * 
 * Our interface is optResult=LP(eq,neq,vars,c,fwd,bwd).
 * 	eq:			equality constraints
 * 	neq:		inequality constraints
 * 	vars:		x[i]>=0 if vars[i] is true; free otherwise
 * 	c:			cost vector or object function of min(c'*x)
 * 	fwd,bwd:	For A*bwd*x>=b, we can let y=bwd*x and solve it through A*y>=b.
 * 				fwd and bwd are inverse matrix for each other, fwd*bwd=I. 
 * 				At present, it's only for Coho LP. 
 * 	optResult:	The optimal result for this LP, including the optimal base, 
 * 				optimal point and optimal value.
 *
 * eq, neq, vars are required, can't be null.
 * c, fwd, bwd are optimal, they can be provied once need. 
 * But fwd and bwd should be provied at the same time.
 * 
 * If fwd/bwd is null, it's a general LP.
 * 		min(c'y) 
 * s.t. A*x  =\>=  b
 * 		x_i free if vars[i]=false
 * 		x_i>=0   if vars[i]=true
 * 
 * If fwd/bwd is not null only for the coho lp: 
 * 		min(c'x)
 * s.t. A*bwd*x >= b
 * 		x free
 * Acturally, it's solved using y=bwd*x, it converts to:
 * 		min((fwd'*c)'y)
 * s.t. A*y >= b
 * 	    y free
 * They have the same optimal value and optimal basis, and x_opt = fwd*y_opt
 *   
 * A Coho Matrix is 
 * 		max(c'x)=-min(-c'x)
 * s.t. Ax >= b
 * 		x free
 * 
 * A Coho Dual Matrix is the dual of Coho Matrix:
 * 		min(-b'x)
 * s.t.	-A'x = c
 * 		x >= 0
 * 
 * @author chaoyan
 */
//Change DoubleIntervalMatrix to DoubleMatrix, I think LP should be described exactly
//Change DoubleMatrix to BasicMatrix, this is more generic and allow input as RationalMatrix.
//For c fwd bwd, they should be the same type of Matrix with constraint.
//Change BasicMatrix to Matrix.

public class LP{
	final private Constraint eq;
	public 	Constraint eq(){return eq;}
	final private Constraint neq;
	public 	Constraint neq(){return neq;}
	final private BooleanMatrix vars;//n*1 vector. vars[i]=true if x_i is positive
	public  BooleanMatrix vars(){return vars;};
	private Matrix c;//cost vector
	public Matrix c(){return c;}
	public  int nConstraints(){return eq.nrows()+neq.nrows();}
	public  int nVars(){return vars.length();}
	public  void setC(Matrix c){
		if(c.ncols()!=1||c.length()!=nVars()){
			throw new LPError("LP.setC: c should be a "+nVars()+" by 1 vector. However, its is "+c.nrows()+" x "+c.ncols());
		}
		this.c=c;
	}

	/*
	 * replace T with fwd, which is more meanful; add bwd then we don't need inverse fwd for LP project 
	 * See page91 for fwd and bwd.
	 * fwd=T=e^{M \delt t}=E^{-1}; bwd=E=e^{-M \delta t}.
	 * After each time step, we need to solve CLP(AE,be,c), but AE is not coho matrix.
	 * Instead, we let y=Ex, the solve LP(A,be,fwd'c). 
	 * They have same opt value, opt basis and x_{opt} = fwd*y_{opt}
	 */
	private Matrix fwd, bwd; 
	public 	Matrix fwd(){return fwd;}
	public 	Matrix bwd(){return bwd;}
	/**
	 * Set fwd and bwd for CohoLP. Assume fwd and bwd are inverse matrix each other.
	 * @param fwd: e^{M \delta t}
	 * @param bwd: e^{-M \delta t}
	 * @throws LPError if it's not a Coho LP.
	 */
	public  void setWd(Matrix fwd, Matrix bwd){
		if(!isCoho()){
			throw new LPError("LP.setWd: unsupported operation\n At present, fwd and bwd are only for a coho problem.");
		}
		if(fwd.nrows()!=nVars()||fwd.ncols()!=nVars()){
			throw new LPError("LP.setT(): fwd should be a "+nVars()+" by "+nVars()+"matrix. But it's "+fwd.nrows()+" by "+fwd.ncols()+" matrix");
		}
		if(bwd.nrows()!=nVars()||bwd.ncols()!=nVars()){
			throw new LPError("LP.setT(): bwd should be a "+nVars()+" by "+nVars()+"matrix. But it's "+bwd.nrows()+" by "+bwd.ncols()+" matrix");
		}
		this.fwd=fwd;
		this.bwd=bwd;
	}
	
	// constructor
	public LP(Constraint eq, Constraint neq, BooleanMatrix vars,
			Matrix c,Matrix fwd, Matrix bwd){
		int nvars = eq.ncols();
		if(vars.ncols()!=1||vars.length()!=nvars)
			throw new LPError("LP:constructor: the vars should be a "+nvars+ "by 1 vector. But it's "+vars.nrows()+" by "+vars.ncols()+" matrix");
		if(neq.ncols()!=nvars)
			throw new LPError("LP:constructor: mismatched sizes for eq and neq"+" eq columns: "+eq.ncols()+" neq columns: "+neq.ncols());
		this.eq=eq;
		this.neq=neq;
		this.vars=vars;
		if(c!=null)
			setC(c);
		if(fwd!=null&&bwd!=null)
			setWd(fwd,bwd);
	}		
	public LP(Constraint eq, Constraint neq, BooleanMatrix vars, Matrix c){
		this(eq,neq,vars,c,null,null);
	}
	public LP(Constraint eq, Constraint neq, BooleanMatrix vars){
		this(eq,neq,vars,null);
	}
	// create General
	public static LP create(Constraint eq, Constraint neq,
			BooleanMatrix vars,	Matrix c,
			Matrix fwd, Matrix bwd){
		return new LP(eq,neq,vars,c,fwd,bwd);
	}
	public static LP create(Constraint eq, Constraint neq,
			BooleanMatrix vars,	Matrix c){
		return new LP(eq,neq,vars,c);
	}	
	public static LP create(Constraint eq, Constraint neq,
			BooleanMatrix vars){
		return new LP(eq, neq, vars);
	}
	// createStandard
	public static LP createStandard(Constraint eq, Matrix c){
		int vars = eq.ncols();
		return create(eq,
				new Constraint(eq.a().convert(0,vars),eq.b().convert(0,1)),
				//new Constraint(c.convert(0,vars),c.convert(0,1)),
				BooleanMatrix.create(vars,1).ones(),c);
	}
	public static LP createStandard(Constraint eq){
		return createStandard(eq,null);
	}
// Canonical requires Ax<=b, however, neq is for Ax>b	
//	// createCanonical BUG: vars = 1 or 0?
//	public static LP createCanonical(Constraint neq,Matrix c){
//		int vars = neq.ncols();
//		return create(
//				//new Constraint(c.convert(0,vars),c.convert(0,1)),
//				new Constraint(neq.a().convert(0,vars),neq.b().convert(0,1)),
//				neq,BooleanMatrix.create(vars,1).ones(),c);
//	}
//	public static LP createCanonical(Constraint neq){
//		return createCanonical(neq,null);
//	}
	// createCoho
	/**
	 * A Coho Matrix is 
	 * 		max(c'x)=-min(-c'x)
	 * s.t. Ax >= b
	 * 		x free
	 * Call createCoho(neq,-c) for this.
	 * We drop the negative sign, so remember to negate the opt value.
	 */
	public static LP createCoho(Constraint neq, Matrix c,
			Matrix fwd, Matrix bwd){
		int vars = neq.ncols();
		//c might be null
		Constraint eq = new Constraint(neq.a().convert(0,vars),neq.b().convert(0,1));
		LP coho = create(eq, neq, BooleanMatrix.create(vars,1).zeros(),c,fwd,bwd);
		if(!coho.isCoho())
			throw new LPError("This is not a Coho LP problem\n"+coho);
		return coho;
	}
	public static LP createCoho(Constraint neq,Matrix c){
		return createCoho(neq,c,null,null);
	}
	public static LP createCoho(Constraint neq){
		return createCoho(neq,null);
	}
	// create CohoDual
	public static LP createCohoDual(Constraint eq, Matrix c){
		LP cohoDual = createStandard(eq,c);
		if(!cohoDual.isCohoDual())
			throw new LPError("This is not a CohoDual LP problem");
		return cohoDual;
	}
	public static LP createCohoDual(Constraint eq){
		return createCohoDual(eq,null);
	}
	
	public boolean isStandard()
	{
		return( neq.nrows()==0// no inequality constraints
				&& vars.all());// all variables positive
	}
//	public boolean isCanonical(){
//		return( eq.nrows()==0// no equality constraints
//				&& vars.all());// all variables positive
//	}
	public boolean isCoho(){
		IntegerMatrix counter = IntegerMatrix.create(neq.a().nrows(),1);
		BooleanMatrix cmp = neq.a().neq(neq.a().zeros());
		for(int i=0; i<neq.a().nrows();i++){
			counter.assign(IntegerMatrix.create(cmp.row(i)).sum(),i);
		}
		return (eq.nrows()==0// no equality constraint
				&& counter.max().intValue()<=2// there are 1 or 2 non-zero for each row
				&& counter.min().intValue()>=1
				&& vars.any()==false);//all vars are unconstrainted
	}
	public boolean isCohoDual(){
		IntegerMatrix counter = IntegerMatrix.create(eq.a().ncols(),1);
		BooleanMatrix cmp = eq.a().neq(eq.a().zeros());
		for(int i=0; i<eq.a().ncols(); i++){
			counter.assign(IntegerMatrix.create(cmp.col(i)).sum(),i);
		}
		return (isStandard()// each col has 1 or 2 non-zero elements
				&& counter.max().intValue()<=2// there are 1 or 2 non-zero for each row
				&& counter.min().intValue()>=1 );
	}
	public LP dual(Matrix c){
		setC(c);
		return dual();
	}
	/**
	 * Find the dual problem.
	 * The primal and dual has the opposite optimal value.
	 * Because we use min(-b'y), actrually it should be -min(-b'y).
	 * 
	 * The primal-dual LP relationship 
	 * 		min(c'x)					*		-min(-b'y)
	 * 	s.t A_eq*x  =  b_eq				*  s.t  y_i free
	 * 		A_neq*x >= b_neq		  <===>		y_i >=0 
	 * 		x_i free if vars[i]=false	*		-A_i'*y =  -c_i
	 * 		x_i >=0 if vars[i]=true		*		-A_i'*y >= -c_i
	 * 
	 * If fwd&bwd are not null, the primal-dual LP relationship is
	 * 		-min(c'x)		(Coho)		*		min(-b'y)	(Coho Dual)		
	 *  s.t A*bwd*x >= b				* 		y >=0
	 *  	x free						*		-(A*bwd)'y=-c  <==> -A'y = -fwd'c
	 *  or
	 * 		min(c'x)		(Coho Dual)	*		-min(-b'y)	(Coho)		
	 *  s.t A*bwd*x = b					* 		y free
	 *  	x free						*		-(A*bwd)'y=-c  <==> -A'y = -fwd'c
	 *  Therefore, if fwd/bwd is not null, we only need to change -c to -fwd'*c.
	 *  	
	 */
	//Asssumption: c, fwd, bwd are the same type of matrix with matrix of constraints
	public LP dual(){
		if(c==null){
			throw new LPError("LP.dual: c should be provided to get the dual matrix.");
		}
		int nVarsDual = nConstraints();
		//c can't be null here. otherwise
		Matrix A = c.convert(nConstraints(),nVars()).zeros();
		A.assign(eq.a(),0,0);
		A.assign(neq.a(),eq.nrows(),0);
		Matrix b = c.convert(nConstraints(),1).zeros();
		b.assign(eq.b(),0,0);
		b.assign(neq.b(),eq.nrows(),0);
		
		Matrix _c = b.negate();
		BooleanMatrix u = vars.negate();
		Matrix A_eq = A.transpose().row(u);
		Matrix A_neq = A.transpose().row(vars);
		Matrix tempC = c;
		if(fwd!=null){
			tempC = fwd.transpose().mult(tempC);
		}
		Matrix b_eq = tempC.row(u);
		Matrix b_neq = tempC.row(vars);
		Constraint _eq = new Constraint(A_eq.negate(),b_eq.negate());
		Constraint _neq = new Constraint(A_neq.negate(),b_neq.negate());
		BooleanMatrix _vars = BooleanMatrix.create(nVarsDual,1).ones().assign(BooleanMatrix.create(eq.nrows(),1).ones(),0,0);
		return new LP(_eq,_neq,_vars,_c);
	}

	public String toString(){
		String result="";
		result+="Minimum Problem\n";
		result+="eqaulity constraint:\n"+eq.toString()+"\n";
		result+="ineqaulity constraint:\n"+neq.toString()+"\n";
		result+="positive vars:\n"+vars.toString()+"\n";
		if(c!=null)
			result+="cost vector\n"+c.toString()+"\n";
		if(fwd!=null&&bwd!=null){
			result+="foward matrix:\n"+fwd.toString()+"\n";
			result+="backward matrix:\n"+bwd.toString()+"\n";
		}	
		return result;
	}
	
	public static void main(String[] args){
//		int nVars = 3;
//		DoubleMatrix A_eq = DoubleMatrix.randoms(2,nVars).toInterval();
//		DoubleMatrix b_eq = DoubleMatrix.randoms(2,1).toInterval();
//		Constraint eq = new Constraint(A_eq,b_eq);
//		DoubleMatrix A_neq = DoubleMatrix.randoms(2,nVars).toInterval();
//		DoubleMatrix b_neq = DoubleMatrix.randoms(2,1).toInterval();
//		Constraint neq = new Constraint(A_neq,b_neq);
//		DoubleMatrix c = DoubleMatrix.randoms(nVars,1).toInterval();
//		BooleanMatrix vars = BooleanMatrix.randoms(nVars,1);
//		LP lp = LP.create(eq,neq,vars,c,null);
//		System.out.println("General LP:");
//		System.out.println(lp.toString());
//		System.out.println("General LP after double dual:");
//		System.out.println(lp.dual().dual().toString());
		
		double[] c={-1,-1,-1,-1};
		double[][] ne={{-1,1,-1,1},{-1,1,1,-1}};
		double[] b={-1,-1};
		DoubleMatrix costVector = DoubleMatrix.create(c);
		DoubleMatrix A = DoubleMatrix.create(ne);
		DoubleMatrix B = DoubleMatrix.create(b);
		Constraint neq = new Constraint(A,B);
		LP lp = LP.createCohoDual(neq,costVector);
		System.out.println("Coho Dual LP:");
		System.out.println(lp.toString());
		System.out.println("Coho Dual LP after double dual:");
		System.out.println(lp.dual().dual().toString());
	}
}
