package coho.interp;

/* functions that we provide in the interpreter:
 *   lpDebug(action, ...)   // no idea what it does
 * 
 *   LP lpGeneral(Matrix Aeq, Matrix beq, Matrix Aineq, Matrix bineq [, Matrix pos [, Matrix T]])
 *     DON"T BELIEVE THIS!!! KAREN MODIFIED THE CODE AND HASN'T UPDATED THIS COMMENT YET.
 *     create an lp with feasible region satisfying:
 *       Aeq*x = beq
 *       Aineq*x >= bineq
 *       ForAll i. pos(i) => x(i) >= 0
 *     default for pos: [false, false, ...]
 *     default for T: I
 *     need to determine how pos and T interact.
 * 
 *   LP lpCanonical(Matrix Aeq, Matrix beq)
 *     create an lp with feasible region satisfying
 *       Aeq*x = b
 *       x >= 0
 * 
 *   Result lp_opt(LP lp, Matrix c)
 *     Find x that in the feasible region of lp that minimizes c'*T*x
 * 
 *   Project lp_project(LP lp, Matrix x, Matrix y)
 *     Return the polygon obtained by projecting the feasible region of lp
 *     onto the plane defined by vectors x and y
 */
import java.io.*;
import java.util.*;

import coho.common.matrix.*;
import coho.common.number.*;
import coho.lp.*;
import coho.lp.project.*;
import coho.lp.solver.*;
//the interface to solve lp project. It links the kernel method of LP to the matlab.
//but since we have changed the LP solver, we should also change this interface.
public class LPstuff {
	public LPstuff() {}	

	public static class LPvalue extends BasicValue {
		private LP lp;
		public String name() { return("LP"); }

		protected LPvalue(LP _lp) { lp = _lp; }

		public void print(Writer w, Value[] options)
		throws EvalException, IOException {
			w.write(lp.toString());
		}
		public LP value() { return(lp); }
	}


	public static class ResultValue extends BasicValue {
		private LPResult r;
		public String name() { return("lp-result"); }

		protected ResultValue(LPResult _r) { r = _r; }

		public void print(Writer w, Value[] options)
		throws EvalException, IOException {
			w.write(r.toString());
		}

		public Value negate() throws EvalException {
			throw new EvalException("lp-results can't be negated");
		}

		public LPResult value() { return(r); }
	}


	public static class ProjectValue extends BasicValue {
		private LPProject p;
		public String name() { return("lp-project"); }

		protected ProjectValue(LPProject _p) { p = _p; }

		public void print(Writer w, Value[] options)
		throws EvalException, IOException {
			w.write(p.toString());
		}
		public LPProject value() { return(p); }
	}


	private static Function[] functions = new Function[] {
		new Function() {
			public String name() { return("lpGeneral"); }
			public Value eval(RCvalue args) throws EvalException {
				if(!(args.size() >= 4 && args.size() <= 7))
					throw new EvalException(
							"usage:  " + name() + "(A_eq, b_eq, A_ineq, b_ineq [, [pos [, fwd,bwd]])");
				Constraint eq = constraint_arg(args, 0, name());
				Constraint ineq = constraint_arg(args, 2, name());

				BooleanMatrix pos = null;
				if (args.size() >= 5)
					pos = MatrixValue.booleanMatrix_arg(args, 4, name());
				else 
					pos = BooleanMatrix.create(eq.a().size(1),1).zeros();

				DoubleMatrix fwd = null;
				DoubleMatrix bwd = null;
				if(args.size() >= 7){
					fwd = MatrixValue.doubleMatrix_arg(args, 5, name());
					bwd = MatrixValue.doubleMatrix_arg(args, 6, name());
				}
				if(eq.a().size(1) != ineq.a().size(1)){
					throw new EvalException(name() +
							":  mismatched dimensions of equality and inequality constraints");
				}

				if(eq.a().size(1) != pos.size(0)){
					throw new EvalException(name() + ":  wrong size for pos matrix");
				}

				if (fwd != null& bwd!=null){//fwd & bwd should be null at the same time
					if (fwd.size(0) != fwd.size(1)||bwd.size(0)!=bwd.size(1)||bwd.size(0)!=fwd.size(0)){
						throw new EvalException(name() +
								":  toActualFromWork matrix is not square");          
					}
					if(eq.a().size(1) != fwd.size(1)) {
						throw new EvalException(name() +
								":  mismatched dimensions of toActualFromWork and of eqs/ineqs of LP");
					}
				}

				LP lp = null;
				try{ 
					lp = LP.create(eq, ineq, pos,null,fwd,bwd); 
				}catch (LPError e){
					throw new EvalException("INTERNAL ERROR in lpGeneral:  " + e);
				}
				return(new LPvalue(lp));
			}
		},

		new Function() {
			public String name() { return("lp_project"); }
			public String longName() { return(name() + "(lp, x, y,[errTol])"); }
			public String usage() { return("usage:  " + longName()); }
			public Value eval(RCvalue args) throws EvalException {
				if((args.size() < 3 || args.size() >4)) throw new EvalException(usage());
				LP lp = lp_arg(args, 0, name());
				DoubleMatrix x = MatrixValue.doubleMatrix_arg(args, 1, name());
				DoubleMatrix y = MatrixValue.doubleMatrix_arg(args, 2, name());
				if(!x.isVector())
					throw new EvalException(longName() + ":  x must be a vector");
				else if(x.length() != lp.nVars())
					throw new EvalException(longName() +
							":  x must be compatible with lp");
				else if(!y.isVector())
					throw new EvalException(longName() + ":  y must be a vector");
				else if(y.length() != lp.nVars())
					throw new EvalException(longName() +
							":  y must be compatible with lp");
				if(args.size()==3){ 
					ProjectFactory.errTol = 0;//no reduce by default
				}else{
					ProjectFactory.errTol = ((DoubleValue)(args.value(3))).value();
				}
				return(new ProjectValue(ProjectFactory.getProject(lp, x, y)));
			}
		},
		// changed for new interface
		// only for coho lp
		new Function() {
			public String name() { return("lp_opt"); }
			public String usage() { return("usage: (lp[,c]) "); }
			public Value eval(RCvalue args) throws EvalException {
				if((args.size() != 1 && args.size()!=2)) throw new EvalException(usage());
				LP lp = lp_arg(args, 0, name());
				LPSolver solver = CohoSolverFactory.getSolver(lp);
				if(args.size()==1)
					return new ResultValue(solver.opt());
				DoubleMatrix c = MatrixValue.doubleMatrix_arg(args, 1, name());
				if(!c.isVector())
					throw new EvalException(name() + ":  c must be a vector");
				if(c.length() != lp.nVars())
					throw new EvalException(name() + ":  c incompatible with lp");
				return new ResultValue(solver.opt(c));
			}
		},

		new Function() {
			public String name() { return("lp_cost"); }
			public String longName() {
				return(name() + "(lp-result)");
			}
			public String usage() { return("usage:  " + longName()); }
			public Value eval(RCvalue args) throws EvalException {
				if((args.size() != 1)) throw new EvalException(usage());
				double cost;
				if(args.value(0) instanceof ResultValue){
					CohoNumber temp = ((ResultValue)(args.value(0))).value().optCost();
					if(temp instanceof IntervalNumber){
						cost = ((IntervalNumber)temp).lo().doubleValue();
					}else{
						cost = temp.doubleValue();//NOTE: convert to double here
					}
				}
				else throw new EvalException(usage());
				return(ValueFactory.create(cost, null));
			}
		},

		new Function() {
			public String name() { 
				return("lp_point"); 
			}
			public String longName() {
				return(name() + "(lp-projection or lp-result)");
			}
			public String usage() { 
				return("usage:  " + longName()); 
			}			
			public Value eval(RCvalue args) throws EvalException {
				if((args.size() != 1)){
					throw new EvalException(usage());
				}				
				DoubleMatrix pt;
				if(args.value(0) instanceof ProjectValue){
					pt = DoubleMatrix.create(((ProjectValue)(args.value(0))).value().hull());//NOTE convert to double
				}else if(args.value(0) instanceof ResultValue){
					pt = DoubleMatrix.create(((ResultValue)(args.value(0))).value().optPoint());
				}else{
					throw new EvalException(usage());
				}
				return(ValueFactory.create(pt, null));
			}
		},
		new Function() {
			public String name() { 
				return("lp_basis"); 
			}
			public String longName() {
				return(name() + "(lp-result)");
			}
			public String usage() { 
				return("usage:  " + longName()); 
			}			
			public Value eval(RCvalue args) throws EvalException {
				if((args.size() != 1)){
					throw new EvalException(usage());
				}				
				IntegerMatrix basis;
				if(args.value(0) instanceof ResultValue){
					basis = IntegerMatrix.create(((ResultValue)(args.value(0))).value().optBasis().basis());
				}else{
					throw new EvalException(usage());
				}
				return(ValueFactory.create(basis, null));
			}
		},
		// extract status info from parameter?
		new Function() {
			public String name() { return("lp_status"); }
			public String longName() {
				return(name() + "(lp-result)");
			}
			public String usage() { return("usage:  " + longName()); }
			public Value eval(RCvalue args) throws EvalException {
				if((args.size() != 1)) throw new EvalException(usage());
				LPResult.ResultStatus status;
				if(args.value(0) instanceof ResultValue)
					status = ((ResultValue)(args.value(0))).value().status();
				else 
					throw new EvalException(usage());
				//return(ValueFactory.create(CohoSolverResult.statusName(status), null));
				return(ValueFactory.create((double)CohoSolverResult.statusID(status), null));//return ID
			}
		}
	};

	public static Enumeration functions() {
		return(new Enumeration() {
			private int i = 0;
			public boolean hasMoreElements() { return(i < functions.length); }
			public Object nextElement() { return(functions[i++]); }});
	}

	public static Constraint constraint_arg(RCvalue args, int i, String who)
	throws EvalException {
		DoubleMatrix a = MatrixValue.doubleMatrix_arg(args, i, who);
		DoubleMatrix b = MatrixValue.doubleMatrix_arg(args, i+1, who);
		if(b.size(1) != 1)
			throw new EvalException(who + ":  (parameter " + (i+1) + ")" +
			" column vector expected.");
		if(a.size(0) != b.size(0))
			throw new EvalException(who + ":  (parameters " + i + " and " +
					(i+1) + ") incompatible matrix and column vector for constraints, "
					+ "(" + a.size(0) + " !=  " + b.size(0) + ")");
		return(new Constraint(a, b));
	}

	public static LP lp_arg(RCvalue args, int i, String who)
	throws EvalException {
		if(i > args.size())
			throw new EvalException(who + ":  not enough parameters.");
		Value v = args.value(i);
		if(!(v instanceof LPvalue))
			throw new EvalException(who + ":  parameter " + i 
					+ " must be a lp -- got a " + v.getClass().getName()); 
		return(((LPvalue)(v)).value());
	}

}
