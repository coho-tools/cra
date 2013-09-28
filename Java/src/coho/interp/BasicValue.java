package coho.interp;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Enumeration;
/**
 * A basic implementation for Value
 * @author chaoyan
 *
 */
public class BasicValue implements Value {
	public static void print(BasicValue v, Writer w, Value[] options)throws IOException {
		w.write(v.name() + "(" + v + ")");
	}
	public void print(Writer w, Value[] options)throws EvalException, IOException{ 
		print(this, w, options); 
	}

	public static Value noSuchOp(Value v1, Value v2, String op)throws EvalException {
		throw new EvalException(
				op + "(" + v1.typeName() + ", " + v2.typeName() + ") not implemented");
	}
	public static Value noSuchOp(Value v, String op)throws EvalException {
		throw new EvalException(op + "(" + v.typeName() + ") not implemented");
	}
	public static Value negate(BasicValue v) throws EvalException {
		return(noSuchOp(v, "negate"));
	}
	public Value negate() throws EvalException { 
		return(negate(this)); 
	}

	public static Value abs(BasicValue v) throws EvalException {
		return(noSuchOp(v, "negate"));
	}
	public Value abs() throws EvalException { 
		return(negate(this)); 
	}

	public static Value add(Value v1, Value v2) throws EvalException {
		return(noSuchOp(v1, v2, "add"));
	}
	public static Value mult(Value v1, Value v2) throws EvalException {
		return(noSuchOp(v1, v2, "mult"));
	}

	public static Value less(Value v1, Value v2) throws EvalException {
		return(noSuchOp(v1, v2, "less"));
	}
	public static Value leq(Value v1, Value v2) throws EvalException {
		return(noSuchOp(v1, v2, "leq"));
	}
	public static Value eq(Value v1, Value v2) throws EvalException {
		return(noSuchOp(v1, v2, "eq"));
	}
	public static Value neq(Value v1, Value v2) throws EvalException {
		return(noSuchOp(v1, v2, "neq"));
	}
	public static Value geq(Value v1, Value v2) throws EvalException {
		return(noSuchOp(v1, v2, "geq"));
	}
	public static Value greater(Value v1, Value v2) throws EvalException {
		return(noSuchOp(v1, v2, "greater"));
	}

	public Value add(Value v) throws EvalException { return(add(this, v)); }
	public Value mult(Value v) throws EvalException { return(mult(this, v)); }

	public Value less(Value v) throws EvalException { return(less(this, v)); }
	public Value leq(Value v) throws EvalException { return(leq(this, v)); }
	public Value eq(Value v) throws EvalException { return(eq(this, v)); }
	public Value neq(Value v) throws EvalException { return(neq(this, v)); }
	public Value geq(Value v) throws EvalException { return(geq(this, v)); }
	public Value greater(Value v) throws EvalException { return(greater(this, v)); }

	public static abstract class CompareFn implements Function {
		private String name;	public String name() { return(name); }
		public CompareFn(String name) { this.name = name; }
		public Value eval(RCvalue args) throws EvalException {
			if(args.size() != 2)
				throw new EvalException("usage:  " + name() + "(arg1, arg2)");
			return(cmp(args.value(0), args.value(1)));
		}
		public abstract Value cmp(Value v1, Value v2) throws EvalException ;
	}
	public static Enumeration functions() {
		final Function[] functions = new Function[] {
				new Function() {
					public String name() { return("abs"); }
					public Value eval(RCvalue args) throws EvalException {
						if(args.size() != 1)
							throw new EvalException("usage:  " + name() + "(v)");
						else return(args.value(0).abs());
					}
				},
				new Function() {
					public String name() { return("negate"); }
					public Value eval(RCvalue args) throws EvalException {
						if(args.size() != 1)
							throw new EvalException("usage:  " + name() + "(v)");
						else return(args.value(0).negate());
					}
				},
				new Function() {
					public String name() { return("add"); }
					public Value eval(RCvalue args) throws EvalException {
						if(args.size() == 0) return(ValueFactory.create(new Double(0.0)));
						Value v = args.value(0);
						for(int i = 1; i < args.size(); i++)
							v = v.add(args.value(i));
						return(v);
					}
				},
				new Function() {
					public String name() { return("mult"); }
					public Value eval(RCvalue args) throws EvalException {
						if(args.size() == 0) return(ValueFactory.create(new Double(1.0)));
						Value v = args.value(0);
						for(int i = 1; i < args.size(); i++)
							v = v.mult(args.value(i));
						return(v);
					}
				},
				new CompareFn("less") {
					public Value cmp(Value v1, Value v2) throws EvalException { return(v1.less(v2)); }
				},
				new CompareFn("leq") {
					public Value cmp(Value v1, Value v2) throws EvalException { return(v1.leq(v2)); }
				},
				new CompareFn("eq") {
					public Value cmp(Value v1, Value v2) throws EvalException { return(v1.eq(v2)); }
				},
				new CompareFn("neq") {
					public Value cmp(Value v1, Value v2) throws EvalException { return(v1.neq(v2)); }
				},
				new CompareFn("geq") {
					public Value cmp(Value v1, Value v2) throws EvalException { return(v1.geq(v2)); }
				},
				new CompareFn("greater") {
					public Value cmp(Value v1, Value v2) throws EvalException { return(v1.greater(v2)); }
				},
		};

		return(new Enumeration() {
			private int i = 0;
			public boolean hasMoreElements() { return(i < functions.length); }
			public Object nextElement() { return(functions[i++]); }});
	}

	public static String name(BasicValue v) { return(v.getClass().getName()); }
	public String name() { return(name(this)); }

	public static String typeName(BasicValue v) {
		return(v.getClass().toString());
	}
	public String typeName() { return(typeName(this)); }

	public static String toString(Value v) {
		StringWriter w = new StringWriter();
		try { v.print(w, new Value[0]); }
		catch (EvalException x_e){ 
			throw new RuntimeException("OOPS!  " + x_e.getMessage()); 
		}
		catch (IOException x_i){ 
			throw new RuntimeException("OOPS!  " + x_i.getMessage()); 
		}
		return(w.toString());
	}
	public String toString() { return(toString(this)); }
}
