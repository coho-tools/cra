/*
 * Created on 28-Jun-2004
 */
package coho.interp;

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;

import coho.common.matrix.*;
import coho.common.number.*;

/**
 * @author Mark Greenstreet (mrg@cs.ubc.ca)
 */
//todo support DoubleIntervalMatrix
public class MatrixValue extends BasicValue {
	private Matrix m;

	public Matrix m() {
		return (m);
	}

	protected MatrixValue(Matrix _m) {
		m = _m;
	}

	protected MatrixValue(RCvalue u, CohoType elementType) throws EvalException {
		if (u.size() == 0){
			m = new BasicMatrix(elementType.zero(), 0, 0).zeros();// XXX
		}else {
			int a = u.size();
			int b = 0;
			boolean a_row = false;
			boolean isVector = false;
			for (int i = 0; i < a; i++) {
				Value uu = u.value(i);
				if (uu instanceof RCvalue) {
					if (isVector)
						mv_bad();
					RCvalue v = (RCvalue) (uu);
					if (i == 0) {
						b = v.size();
						if (a > 1)
							a_row = u.isRow();
						else
							a_row = !v.isRow();
						if ((a > 1) && (b > 1) && (a_row == v.isRow()))
							mv_bad();
					} else if ((v.size() != b)
							|| ((b > 1) && (v.isRow() == a_row)))
						mv_bad();
					for (int j = 0; j < b; j++)
						if (!(v.value(j) instanceof DoubleValue))
							mv_bad();
				} else if (uu instanceof DoubleValue) {
					if (i == 0) {
						b = 1;
						isVector = true;
						a_row = u.isRow();
					} else if (!isVector)
						mv_bad();
				} else
					mv_bad();
			}
			if (a_row)
				// m = BasicMatrix.create(b, a, elementType);
				m = new BasicMatrix(elementType.zero(), b, a).zeros();// XXX
			else
				// m = BasicMatrix.create(a, b, elementType);
				m = new BasicMatrix(elementType.zero(), a, b).zeros();// XXX
			for (int i = 0; i < a; i++) {
				Value uu = u.value(i);
				if (uu instanceof RCvalue) {
					RCvalue v = (RCvalue) (uu);
					for (int j = 0; j < b; j++){
						if (a_row){
							m.assign(((DoubleValue) (v.value(j))).value(), j, i);
//							System.out.println("assign"+((DoubleValue) (v.value(j))).value()+" to "+j+" "+i);
//							System.out.println(m);
						}else{
							m.assign(((DoubleValue) (v.value(j))).value(), i, j);
//							System.out.println("assign"+ ((DoubleValue) (v.value(j))).value()+" to "+i+" "+j);
//							System.out.println(m);
						}
					}
				} else if (a_row){
					m.assign(((DoubleValue) (uu)).value(), 0, i);
				}
				else{
					m.assign(((DoubleValue) (uu)).value(), i, 0);
				}
			}
		}
	}

	protected void mv_bad() throws EvalException {
		throw new EvalException(
				"matrix(u): all rows of u must be of the same size"
				+ " and likewise for columns");
	}

	public void print(Writer w, Value[] options) throws EvalException, IOException {
		boolean hex = false;
		for (int i = 0; i < options.length; i++) {
			String x = options[i].toString();
			if (x.compareTo("hex") == 0)
				hex = true;
			else if (x.compareTo("plain") == 0)
				hex = false;
		}
		w.write(m.toString(hex ? "hex" : "dec"));// XXX
	}

	public Value negate() throws EvalException {
		return (new MatrixValue(m.negate()));
	}

	public Value abs() throws EvalException {
		return (new MatrixValue(m.abs()));
	}

	public Matrix value() {
		return (m);
	}

	public Value add(Value x) throws EvalException {
		if (x instanceof MatrixValue) {
			Matrix xm = ((MatrixValue) (x)).m();
			int[] sz = m.size(), xz = xm.size();
			if ((sz[0] == xz[0]) && (sz[1] == xz[1]))
				return (new MatrixValue(m.add(xm)));
			else
				throw new EvalException(
				"add: matrix arguments must be of the same shape");
		} else
			throw new EvalException("add: can't add a " + x.typeName()
					+ " to a " + typeName());
	}

	public Value mult(Value x) throws EvalException {
		if (x instanceof MatrixValue) {
			Matrix xm = ((MatrixValue) (x)).m;
			int[] sz = m.size(), xz = xm.size();
			if (sz[1] == xz[0])
				return (new MatrixValue(m.mult(xm)));
			else
				throw new EvalException("mult: incompatible matrices");
		} else if (x instanceof DoubleValue)
			return (new MatrixValue(m.mult(((DoubleValue) (x)).value())));
		else
			throw new EvalException("mult: can't multiply a " + typeName()
					+ " by a " + x.typeName());
	}

	public Value less(Value x) throws EvalException {
		if (x instanceof MatrixValue) {
			Matrix xm = ((MatrixValue) (x)).m();
			int[] sz = m.size(), xz = xm.size();
			if ((sz[0] == xz[0]) && (sz[1] == xz[1]))
				return (new MatrixValue(m.less(xm)));
			else
				throw new EvalException(
				"less: matrix arguments must be of the same shape");
		} else
			throw new EvalException("less: can't compare a " + typeName()
					+ " with a " + x.typeName());
	}

	public Value leq(Value x) throws EvalException {
		if (x instanceof MatrixValue) {
			Matrix xm = ((MatrixValue) (x)).m();
			int[] sz = m.size(), xz = xm.size();
			if ((sz[0] == xz[0]) && (sz[1] == xz[1]))
				return (new MatrixValue(m.leq(xm)));
			else
				throw new EvalException(
				"leq: matrix arguments must be of the same shape");
		} else
			throw new EvalException("leq: can't compare a " + typeName()
					+ " with a " + x.typeName());
	}

	public Value eq(Value x) throws EvalException {
		if (x instanceof MatrixValue) {
			Matrix xm = ((MatrixValue) (x)).m();
			int[] sz = m.size(), xz = xm.size();
			if ((sz[0] == xz[0]) && (sz[1] == xz[1]))
				return (new MatrixValue(m.eq(xm)));
			else
				throw new EvalException(
				"eq: matrix arguments must be of the same shape");
		} else
			throw new EvalException("eq: can't compare a " + typeName()
					+ " with a " + x.typeName());
	}

	public Value neq(Value x) throws EvalException {
		if (x instanceof MatrixValue) {
			Matrix xm = ((MatrixValue) (x)).m();
			int[] sz = m.size(), xz = xm.size();
			if ((sz[0] == xz[0]) && (sz[1] == xz[1]))
				return (new MatrixValue(m.neq(xm)));
			else
				throw new EvalException(
				"neq: matrix arguments must be of the same shape");
		} else
			throw new EvalException("neq: can't compare a " + typeName()
					+ " with a " + x.typeName());
	}

	public Value geq(Value x) throws EvalException {
		if (x instanceof MatrixValue) {
			Matrix xm = ((MatrixValue) (x)).m();
			int[] sz = m.size(), xz = xm.size();
			if ((sz[0] == xz[0]) && (sz[1] == xz[1]))
				return (new MatrixValue(m.geq(xm)));
			else
				throw new EvalException(
				"geq: matrix arguments must be of the same shape");
		} else
			throw new EvalException("geq: can't compare a " + typeName()
					+ " with a " + x.typeName());
	}

	public Value greater(Value x) throws EvalException {
		if (x instanceof MatrixValue) {
			Matrix xm = ((MatrixValue) (x)).m();
			int[] sz = m.size(), xz = xm.size();
			if ((sz[0] == xz[0]) && (sz[1] == xz[1]))
				return (new MatrixValue(m.greater(xm)));
			else
				throw new EvalException(
				"greater: matrix arguments must be of the same shape");
		} else
			throw new EvalException("greater: can't compare a " + typeName()
					+ " with a " + x.typeName());
	}

	public Value submatrix(Range r, Range c) throws EvalException {
		if ((r.lo() < 0) || (c.lo() < 0))
			throw new EvalException("submatrix: ranges must be positive");
		else if ((r.hi() > m.size(0)) || (c.hi() > m.size(1)))
			throw new EvalException("submatrix: range bigger than matrix");
		else
			return (new MatrixValue(m.V(r, c)));
	}

	public Value block_copy(Matrix src, int r0, int c0) throws EvalException {
		if (r0 < 0)
			throw new EvalException(
					"block_copy(dst, src, r0, c0):  r0 must be >= 0, got " + r0);
		else if (c0 < 0)
			throw new EvalException(
					"block_copy(dst, src, r0, c0):  c0 must be >= 0, got " + c0);
		else if ((r0 + src.size(0)) > m.size(0))
			throw new EvalException(
					"block_copy(dst, src, r0, c0): row index range too large -- "
					+ "dst.nrows = " + m.size(0)
					+ ", r0 + src.nrows = " + (r0 + src.size(0)));
		else if ((c0 + src.size(1)) > m.size(1))
			throw new EvalException(
					"block_copy(dst, src, r0, c0): column index range too large -- "
					+ "dst.ncols = " + m.size(1)
					+ ", c0 + src.ncols = " + (c0 + src.size(1)));
		Matrix x = m.convert();// BasicMatrix.convert(m); XXX clone
		x.assign(m, 0, 0);
		x.assign(src, r0, c0);
		return (new MatrixValue(x));
	}

	public static MatrixValue matrix(RCvalue args, String who,
			CohoType elementType) throws EvalException {
		if (args.size() != 1)
			throw new EvalException("usage:  " + who + "(row or column)");
		if (!(args.value(0) instanceof RCvalue))
			throw new EvalException(who + ":  argument must be a row or column");
		return (new MatrixValue((RCvalue) (args.value(0)), elementType));
	}

	public static Enumeration matrixFns() {
		final Function[] functions = new Function[] { new Function() {
			public String name() {
				return ("matrix");
			}

			public Value eval(RCvalue args) throws EvalException {
				return (matrix(args, name(), CohoDouble.type));// CohoDouble?
			}
		}, new Function() {
			public String name() {
				return ("boolMatrix");
			}

			public Value eval(RCvalue args) throws EvalException {
				return (matrix(args, name(), CohoBoolean.type));
			}
		}, new Function() {
			public String name() {
				return ("intMatrix");
			}

			public Value eval(RCvalue args) throws EvalException {
				return (matrix(args, name(), CohoInteger.type));
			}
		}, new Function() {
			public String name() {
				return ("size");
			}

			public Value eval(RCvalue args) throws EvalException {
				int[] sz = matrix_arg(args, 0, name()).size();
				Matrix m = IntegerMatrix.create(2, 1);// BasicMatrix.create(2,
				// 1, CohoInteger.type);
				m.assign(sz[0], 0);
				m.assign(sz[1], 1);
				return (new MatrixValue(m));
			}
		}, new Function() {
			public String name() {//XXX
				return ("range");
			}

			public Value eval(RCvalue args) throws EvalException {
				if (args.size() != 2)
					throw new EvalException("usage:  " + name() + "(lo, hi)");
				if (!((args.value(0) instanceof DoubleValue) && (args.value(1) instanceof DoubleValue)))
					throw new EvalException(name()+ ":  both arguments must be doubles");
				return (new RangeValue(
						(int) (Math.round(((DoubleValue) (args.value(0))).value())), 
						(int) (Math.round(((DoubleValue) (args.value(1))).value()))
				));
			}
		}, new Function() {
			public String name() {
				return ("transpose");
			}

			public Value eval(RCvalue args) throws EvalException {
				if (args.size() != 1)
					throw new EvalException("usage:  " + name() + "(matrix)");
				return (new MatrixValue(matrix_arg(args, 0, name()).transpose()));
			}
		}, new Function() {
			public String name() {
				return ("diag");
			}

			public Value eval(RCvalue args) throws EvalException {
				if (args.size() != 1)
					throw new EvalException("usage:  " + name() + "(vector)");
				Matrix m = matrix_arg(args, 0, name());
				if (m.isVector()) {
					try {
						return (new MatrixValue(m.diag()));
					} catch (MatrixError e) {
						throw new EvalException("INTERNAL ERROR in diag:  " + e);
					}
				} else
					throw new EvalException("diag: argument must be a vector");
			}
		}, new Function() {
			public String name() {
				return ("zeros");
			}

			public Value eval(RCvalue args) throws EvalException {
				if (args.size() != 1)
					throw new EvalException("usage:  " + name() + "(matrix)");
				return (new MatrixValue(matrix_arg(args, 0, name()).zeros()));
			}
		}, new Function() {
			public String name() {
				return ("ones");
			}

			public Value eval(RCvalue args) throws EvalException {
				if (args.size() != 1)
					throw new EvalException("usage:  " + name() + "(matrix)");
				return (new MatrixValue(matrix_arg(args, 0, name()).ones()));
			}
		}, new Function() {
			public String name() {
				return ("ident");
			}

			public Value eval(RCvalue args) throws EvalException {
				if (args.size() != 2)
					throw new EvalException("usage:  " + name() + "(matrix, n)");
				return (new MatrixValue(matrix_arg(args, 0, name()).ident(BasicArgs.int_arg(args, 1, name()))));
			}
		}, new Function() {
			public String name() {
				return ("submatrix");
			}

			public Value eval(RCvalue args) throws EvalException {
				if (args.size() != 3)
					throw new EvalException(
					"usage: submatrix(matrix, rowRange, colRange)");
				if (!(args.value(0) instanceof MatrixValue))
					throw new EvalException(
					"submatrix: first argument must be a matrix");
				if (!(args.value(1) instanceof RangeValue))
					throw new EvalException(
					"submatrix: second argument must be a range");
				if (!(args.value(2) instanceof RangeValue))
					throw new EvalException(
					"submatrix: third argument must be a range");
				MatrixValue mv = (MatrixValue) (args.value(0));
				Range r = ((RangeValue) (args.value(1))).value();
				Range c = ((RangeValue) (args.value(2))).value();
				return (mv.submatrix(r, c));
			}
		}, new Function() {
			public String name() {
				return ("block_copy");
			}

			public Value eval(RCvalue args) throws EvalException {
				if (args.size() != 4)
					throw new EvalException(
					"usage: block_copy(dst, src, row0, col0)");
				if (!(args.value(0) instanceof MatrixValue))
					throw new EvalException(
					"block_copy(dst, src, row0, col0):  dst must be a matrix");
				if (!(args.value(1) instanceof MatrixValue))
					throw new EvalException(
					"block_copy(dst, src, row0, col0):  src must be a matrix");
				if (!(args.value(2) instanceof DoubleValue))
					throw new EvalException(
					"block_copy(dst, src, row0, col0):  row0 must be a double");
				if (!(args.value(3) instanceof DoubleValue))
					throw new EvalException(
					"block_copy(dst, src, row0, col0):  col0 must be a double");
				MatrixValue dst = (MatrixValue) (args.value(0));
				Matrix src = ((MatrixValue) (args.value(1))).value();
				int r0 = (int) (Math.round(((DoubleValue) (args.value(2)))
						.value()));
				int c0 = (int) (Math.round(((DoubleValue) (args.value(3)))
						.value()));
				return (dst.block_copy(src, r0, c0));
			}
		}, new Function() {
			public String name() {
				return ("all");
			}

			public Value eval(RCvalue args) throws EvalException {
				if (args.size() != 1)
					throw new EvalException("usage: all(matrix)");
				if (!(args.value(0) instanceof MatrixValue))
					throw new EvalException(
					"all: argument must be a matrix of Booleans");
				Matrix m = ((MatrixValue) (args.value(0))).m();
				if (!(m instanceof BooleanMatrix))
					throw new EvalException(
					"all: argument must be a matrix of Booleans");
				else
					return (ValueFactory.create(m.prod()));
			}
		}, new Function() {
			public String name() {
				return ("any");
			}

			public Value eval(RCvalue args) throws EvalException {
				if (args.size() != 1)
					throw new EvalException("usage: any(matrix)");
				if (!(args.value(0) instanceof MatrixValue))
					throw new EvalException(
					"any: argument must be a matrix of Booleans");
				Matrix m = ((MatrixValue) (args.value(0))).m();
				if (!(m instanceof BooleanMatrix))
					throw new EvalException(
					"any: argument must be a matrix of Booleans");
				else
					return (ValueFactory.create(m.sum()));
			}
		}, new Function() {
			public String name() {
				return ("find");
			}

			public Value eval(RCvalue args) throws EvalException {
				BooleanMatrix b = booleanMatrix_arg(args, 0, name());
				return (new MatrixValue(b.find()));// XXX
			}
		}, new Function() {
			public String name() {
				return ("row");
			}

			public String usage() {
				return ("usage:  " + name() + "(matrix, row-specifier)\n"
						+ "  where row-specifier can be a double, a range,"
						+ " a matrix of integers\n" + "  or a matrix of booleans.");
			}

			public Value eval(RCvalue args) throws EvalException {
				if (args.size() != 2)
					throw new EvalException(usage());
				Matrix m = matrix_arg(args, 0, name());
				Matrix r = null;
				Value v = args.value(1);
				try {
					if (v instanceof DoubleValue)
						r = m.row(checkRowIndex(m, BasicArgs.int_arg(args, 1,
								name()), name()));
					else if (v instanceof RangeValue)
						r = m.row(checkRowIndex(m, range_arg(args, 1, name()),
								name()));
					else if (v instanceof MatrixValue) {
						Matrix x = matrix_arg(args, 1, name());
						if (x instanceof IntegerMatrix) {
							// make sure that all elements of x are in range */
							for (int i = 0; i < x.length(); i++) {
								int k = x.V(i).intValue();
								if ((k < 0) || (m.size(0) <= k))
									throw new EvalException(name()+ ": index out of range");
							}
							r = m.row((IntegerMatrix) x);// XXX
						} else if (x instanceof BooleanMatrix) {
							// make sure that all elements of x are in range */
							if (x.length() != m.size(0))
								throw new EvalException(
										name()
										+ ": row specifier wrong size for matrix");
							r = m.row((BooleanMatrix) x);// XXX
						}
					}
				} catch (MatrixError e) {
					throw new EvalException("INTERNAL ERROR:  " + e.toString());
				}
				if (r != null)
					return (new MatrixValue(r));
				throw new EvalException(
						name()
						+ ": don't know what to do with a second parameter of type "
						+ v.getClass().getName());
			}
		}, new Function() {
			public String name() {
				return ("col");
			}

			public String usage() {
				return ("usage:  " + name() + "(matrix, row-specifier)\n"
						+ "  where row-specifier can be a double, a range,"
						+ " a matrix of integers\n" + "  or a matrix of booleans.");
			}

			public Value eval(RCvalue args) throws EvalException {
				if (args.size() != 2)
					throw new EvalException(usage());
				Matrix m = matrix_arg(args, 0, name());
				Matrix c = null;
				Value v = args.value(1);
				try {
					if (v instanceof DoubleValue)
						c = m.col(checkRowIndex(m, BasicArgs.int_arg(args, 1,
								name()), name()));
					else if (v instanceof RangeValue)
						c = m.col(checkRowIndex(m, range_arg(args, 1, name()),
								name()));
					else if (v instanceof MatrixValue) {
						Matrix x = matrix_arg(args, 1, name());
						if (x instanceof IntegerMatrix) {
							// make sure that all elements of x are in range */
							for (int i = 0; i < x.length(); i++) {
								int k = x.V(i).intValue();
								if ((k < 0) || (m.size(1) <= k))
									throw new EvalException(name()+ ": index out of range");
							}
							c = m.col((IntegerMatrix) x);// XXX
						} else if (x instanceof BooleanMatrix) {
							// make sure that all elements of x are in range */
							if (x.length() != m.size(1))
								throw new EvalException(name()+ ": column specifier wrong size for matrix");
							c = m.col((BooleanMatrix) x);// XXX
						}
					}
				} catch (MatrixError e) {
					throw new EvalException("INTERNAL ERROR:  " + e.toString());
				}
				if (c != null)
					return (new MatrixValue(c));
				throw new EvalException(
						name()
						+ ": don't know what to do with a second parameter of type "
						+ v.getClass().getName());
			}
		}, new Function() {
			public String name() {
				return ("min");
			}

			public String usage() {
				return ("usage:  " + name() + "(matrix)");
			}

			public Value eval(RCvalue args) throws EvalException {
				if (args.size() != 1)
					throw new EvalException(usage());
				return (ValueFactory.create(matrix_arg(args, 0, name()).min()));
			}
		},
		// new Function() {
		// public String name() { return("minEachCol"); }
		// public String usage() {
		// return("usage: " + name() + "(matrix)");
		// }
		// public Value eval(RCvalue args) throws EvalException {
		// if(args.size() != 1) throw new EvalException(usage());
		// return(ValueFactory.convert(matrix_arg(args, 0,
		// name()).minEachCol()));
		// }
		// },
		// new Function() {
		// public String name() { return("minEachRow"); }
		// public String usage() {
		// return("usage: " + name() + "(matrix)");
		// }
		// public Value eval(RCvalue args) throws EvalException {
		// if(args.size() != 1) throw new EvalException(usage());
		// return(ValueFactory.convert(matrix_arg(args, 0,
		// name()).minEachRow()));
		// }
		// },
		new Function() {
			public String name() {
				return ("max");
			}

			public String usage() {
				return ("usage:  " + name() + "(matrix)");
			}

			public Value eval(RCvalue args) throws EvalException {
				if (args.size() != 1)
					throw new EvalException(usage());
				return (ValueFactory.create(matrix_arg(args, 0, name())
						.max()));
			}
		},
		// new Function() {
		// public String name() { return("maxEachCol"); }
		// public String usage() {
		// return("usage: " + name() + "(matrix)");
		// }
		// public Value eval(RCvalue args) throws EvalException {
		// if(args.size() != 1) throw new EvalException(usage());
		// return(ValueFactory.convert(matrix_arg(args, 0,
		// name()).maxEachCol()));
		// }
		// },
		// new Function() {
		// public String name() { return("maxEachRow"); }
		// public String usage() {
		// return("usage: " + name() + "(matrix)");
		// }
		// public Value eval(RCvalue args) throws EvalException {
		// if(args.size() != 1) throw new EvalException(usage());
		// return(ValueFactory.convert(matrix_arg(args, 0,
		// name()).maxEachRow()));
		// }
		// },
		new Function() {
			public String name() {
				return ("sum");
			}

			public String usage() {
				return ("usage:  " + name() + "(matrix)");
			}

			public Value eval(RCvalue args) throws EvalException {
				if (args.size() != 1)
					throw new EvalException(usage());
				return (ValueFactory.create(matrix_arg(args, 0, name())
						.sum()));
			}
		},
		// new Function() {
		// public String name() { return("sumEachCol"); }
		// public String usage() {
		// return("usage: " + name() + "(matrix)");
		// }
		// public Value eval(RCvalue args) throws EvalException {
		// if(args.size() != 1) throw new EvalException(usage());
		// return(ValueFactory.convert(matrix_arg(args, 0,
		// name()).sumEachCol()));
		// }
		// },
		// new Function() {
		// public String name() { return("sumEachRow"); }
		// public String usage() {
		// return("usage: " + name() + "(matrix)");
		// }
		// public Value eval(RCvalue args) throws EvalException {
		// if(args.size() != 1) throw new EvalException(usage());
		// return(ValueFactory.convert(matrix_arg(args, 0,
		// name()).sumEachRow()));
		// }
		// },
		new Function() {
			public String name() {
				return ("prod");
			}

			public String usage() {
				return ("usage:  " + name() + "(matrix)");
			}

			public Value eval(RCvalue args) throws EvalException {
				if (args.size() != 1)
					throw new EvalException(usage());
				return (ValueFactory.create(matrix_arg(args, 0, name())
						.prod()));
			}
		},
		// new Function() {
		// public String name() { return("prodEachCol"); }
		// public String usage() {
		// return("usage: " + name() + "(matrix)");
		// }
		// public Value eval(RCvalue args) throws EvalException {
		// if(args.size() != 1) throw new EvalException(usage());
		// return(ValueFactory.convert(matrix_arg(args, 0,
		// name()).prodEachCol()));
		// }
		// },
		// new Function() {
		// public String name() { return("prodEachRow"); }
		// public String usage() {
		// return("usage: " + name() + "(matrix)");
		// }
		// public Value eval(RCvalue args) throws EvalException {
		// if(args.size() != 1) throw new EvalException(usage());
		// return(ValueFactory.convert(matrix_arg(args, 0,
		// name()).prodEachRow()));
		// }
		// },
		// new Function() {
		// public String name() { return("prodEachRow"); }
		// public String usage() {
		// return("usage: " + name() + "(matrix)");
		// }
		// public Value eval(RCvalue args) throws EvalException {
		// if(args.size() != 1) throw new EvalException(usage());
		// return(ValueFactory.convert(matrix_arg(args, 0,
		// name()).prodEachRow()));
		// }
		// },
		new Function() {
			public String name() {
				return ("elMult");
			}

			public String usage() {
				return ("usage:  " + name() + "(matrix, matrix)");
			}

			public Value eval(RCvalue args) throws EvalException {
				if (args.size() != 2)
					throw new EvalException(usage());
				if ((args.value(0) instanceof MatrixValue)
						&& (args.value(1) instanceof MatrixValue)) {
					Matrix x1 = matrix_arg(args, 0, name());
					Matrix x2 = matrix_arg(args, 1, name());
					int[] x1s = x1.size(), x2s = x2.size();
					if ((x1s[0] == x2s[0]) && (x1s[1] == x2s[1]))
						return (new MatrixValue(x1.elMult(x2)));
					else
						throw new EvalException(
								name()
								+ ": matrix arguments must be of the same shape");
				} else
					throw new EvalException("elMult: can't multiply a "
							+ args.value(0).typeName() + " by a "
							+ args.value(1).typeName());
			}
		}, new Function() {
			public String name() {
				return ("elDiv");
			}

			public String usage() {
				return ("usage:  " + name() + "(matrix, matrix)");
			}

			public Value eval(RCvalue args) throws EvalException {
				if (args.size() != 2)
					throw new EvalException(usage());
				if ((args.value(0) instanceof MatrixValue)
						&& (args.value(1) instanceof MatrixValue)) {
					Matrix x1 = matrix_arg(args, 0, name());
					Matrix x2 = matrix_arg(args, 1, name());
					int[] x1s = x1.size(), x2s = x2.size();
					if ((x1s[0] == x2s[0]) && (x1s[1] == x2s[1]))
						return (new MatrixValue(x1.elDiv(x2)));
					else
						throw new EvalException(name()+ ": matrix arguments must be of the same shape");
				} else
					throw new EvalException("elMult: can't divide a "
							+ args.value(0).typeName() + " by a "+ args.value(1).typeName());
			}
		},
		// new Function() {
		// public String name() { return("sortEachCol"); }
		// public String usage() {
		// return("usage: " + name() + "(matrix)");
		// }
		// public Value eval(RCvalue args) throws EvalException {
		// if(args.size() != 1) throw new EvalException(usage());
		// return(ValueFactory.convert(matrix_arg(args, 0,
		// name()).sortEachCol()));
		// }
		// },
		// new Function() {
		// public String name() { return("sortEachRow"); }
		// public String usage() {
		// return("usage: " + name() + "(matrix)");
		// }
		// public Value eval(RCvalue args) throws EvalException {
		// if(args.size() != 1) throw new EvalException(usage());
		// return(ValueFactory.convert(matrix_arg(args, 0,
		// name()).sortEachRow()));
		// }
		// },
		new Function() {
			public String name() {
				return ("norm");
			}

			public String usage() {
				return ("usage:  " + name() + "(matrix)");
			}

			public Value eval(RCvalue args) throws EvalException {
				if (args.size() != 1)
					throw new EvalException(usage());
				return (ValueFactory.create(matrix_arg(args, 0, name())
						.norm()));
			}
		} };
		return (new Enumeration() {
			private int i = 0;

			public boolean hasMoreElements() {
				return (i < functions.length);
			}

			public Object nextElement() {
				return (functions[i++]);
			}
		});
	}

	public static Matrix matrix_arg(RCvalue args, int i, String who)
	throws EvalException {
		if (i > args.size())
			throw new EvalException(who + ":  not enough parameters.");
		Value v = args.value(i);
		if (!(v instanceof MatrixValue))
			throw new EvalException(who + ":  parameter " + i
					+ " must be a matrix -- got a " + v.getClass().getName());
		return (((MatrixValue) (v)).value());
	}

	public static Matrix matrix_arg(RCvalue args, int i, String who, CohoType t)
	throws EvalException {
		BasicMatrix m = (BasicMatrix) (matrix_arg(args, i, who));
		if (m.elementType() != t)
			throw new EvalException(who + ": parameter " + i
					+ " must be a matrix of " + t + " -- got a "
					+ m.elementType());
		return (m);
	}

	public static BooleanMatrix booleanMatrix_arg(RCvalue args, int i,
			String who) throws EvalException {
		return BooleanMatrix.typeCast((BasicMatrix<CohoBoolean>)matrix_arg(args, i, who, CohoBoolean.type));
	}

	public static DoubleMatrix doubleMatrix_arg(RCvalue args, int i, String who)
	throws EvalException {
		//return ((DoubleMatrix) (matrix_arg(args, i, who, CohoDouble.type)));
		return DoubleMatrix.typeCast((BasicMatrix<CohoDouble>)matrix_arg(args, i, who, CohoDouble.type));
	}

	public static IntegerMatrix intMatrix_arg(RCvalue args, int i, String who)
	throws EvalException {
		//return ((IntegerMatrix) (matrix_arg(args, i, who, CohoInteger.type)));
		return IntegerMatrix.typeCast((BasicMatrix<CohoInteger>)(matrix_arg(args, i, who, CohoInteger.type)));
	}

	public static Range range_arg(RCvalue args, int i, String who)
	throws EvalException {
		if (i > args.size())
			throw new EvalException(who + ":  not enough parameters.");
		Value v = args.value(i);
		if (!(v instanceof RangeValue))
			throw new EvalException(who + ":  parameter " + i
					+ " must be a range -- got a " + v.getClass().getName());
		return (((RangeValue) (v)).value());
	}

	public static int[] checkIndex(Matrix m, int[] i, String who)
	throws EvalException {
		checkRowIndex(m, i[0], who);
		checkColIndex(m, i[1], who);
		return (i);
	}

	public static int[] checkIndex(Matrix m, int r, int c, String who)
	throws EvalException {
		return (checkIndex(m, new int[] { r, c }, who));
	}

	public static int checkRowIndex(Matrix m, int r, String who)
	throws EvalException {
		if ((0 <= r) && (r < m.size(0)))
			return (r);
		throw new EvalException(who + ":  row index out of range");
	}

	public static Range checkRowIndex(Matrix m, Range r, String who)
	throws EvalException {
		checkRowIndex(m, r.lo(), who);
		checkRowIndex(m, r.hi() - 1, who);
		return (r);
	}

	public static int checkColIndex(Matrix m, int c, String who)
	throws EvalException {
		if ((0 <= c) && (c < m.size(1)))
			return (c);
		throw new EvalException(who + ":  column index out of range");
	}

	public static Range checkColIndex(Matrix m, Range r, String who)
	throws EvalException {
		checkColIndex(m, r.lo(), who);
		checkColIndex(m, r.hi() - 1, who);
		return (r);
	}

	public String typeName() {
		return ("Matrix");
	}

	protected static class factory implements ValueCreate {
		public Value create(Object val, Object args) {
			return (new MatrixValue((Matrix) (val)));
		}

		public Object foo() {
			return ((new Object() {
				public Matrix x;
			}));
		}
	}

	protected static final ValueCreate factory = new factory();

	public static ValueCreate factory() {
		return (factory);
	}
}
