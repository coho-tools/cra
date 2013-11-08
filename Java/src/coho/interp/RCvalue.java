//RCvalue:  row and column expressions

package coho.interp;

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;

public class RCvalue extends BasicValue {
	private Value[] v;
	private boolean isRow; public boolean isRow() { return(isRow); }


	protected RCvalue(Value[] _v, boolean _row) { v = _v; isRow = _row; }
	public Value negate() throws EvalException {
		Value[] vv = new Value[v.length];
		for(int i = 0; i < v.length; i++) vv[i] = v[i].negate();
		return(factory.create(vv, new Boolean(isRow)));
	}
	public Value value(int i) { return(v[i]); }
	public int size() { return(v.length); }


	public void print(Writer w, Value[] opt) throws EvalException, IOException {
		w.write("[ ");
		String sep = isRow ? ", " : "; ";
		for(int i = 0; i < v.length; i++) {
			if(i > 0) w.write(sep);
			v[i].print(w, opt);
		}
		w.write(" ]\n");
	}


	protected static class TailValue extends BasicValue {
		private Value v;
		private TailValue tail;
		private int size; public int size() { return(size); }
		public TailValue(Value[] u) {
			v = u[0];
			if(u.length == 1) {
				tail = null;
				size = 1;
			} else if(u.length == 2) {
				tail = (TailValue)(u[1]);
				size = tail.size() + 1;
			} else throw new IllegalArgumentException(
					"RCvalue.TailValue: bad length for u -- " + u.length);
		}

		public Enumeration enumer() {
			return(new Enumeration() {
				private TailValue t = TailValue.this;
				public boolean hasMoreElements() { return(t != null); }
				public Object nextElement() {
					Object obj = t.v;
					t = t.tail;
					return(obj);
				}
			});
		}

		public void print(Writer w, Value[] options)
		throws IOException, EvalException {
			Enumeration e = enumer();
			while(e.hasMoreElements()) w.write(" ? " + e.nextElement());
		}
	}


	public String toString() { return(BasicValue.toString(this)); }

	protected static class factory implements ValueCreate {
		public Value create(Object val, Object args) {
			Value[] v = (Value[]) val;
			if(args instanceof Boolean)
				return(new RCvalue(v, ((Boolean)(args)).booleanValue()));
			String key = (String)(args);
			if((key.compareTo("row") == 0) || (key.compareTo("col") == 0)) {
				Value vv[];
				if(v.length < 2) vv = v;
				else {
					TailValue tail = (TailValue)(v[1]);
					vv = new Value[tail.size() + 1];
					vv[0] = v[0];
					Enumeration z = tail.enumer();
					for(int i = 1; z.hasMoreElements(); i++)
						vv[i] = (Value)(z.nextElement());
				}
				return(new RCvalue(vv, key.compareTo("row") == 0));
			} else if(    (key.compareTo("rowTail") == 0)
					|| (key.compareTo("colTail") == 0)) {
				return(new TailValue(v));
			} else throw new IllegalArgumentException(
					"RCvalue.factory: bad key -- " + key);
		}
		public Object foo() { return((new Object() { public Value[] x; })); }
	}
	private static factory factory = new factory();
	public static ValueCreate factory() { return(factory); }
}
