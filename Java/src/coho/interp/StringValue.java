package coho.interp;

import java.io.IOException;
import java.io.Writer;

public class StringValue extends BasicValue {
	private String s;
	protected StringValue(String _s) { s = _s; }
	public String value() { return(s); }
	
	public Value negate() throws EvalException {
		throw new EvalException("StringValue.negate: unsupported operation");
	}
	
	public String toString() { return(BasicValue.toString(this)); }
	
	public void print(Writer w, Value[] opt) throws EvalException, IOException {
		w.write(s);
	}
	
	protected static class factory implements ValueCreate {
		public Value create(Object val, Object args) {
			return(new StringValue((String)(val)));
		}
		public Object foo() { return((new Object() { public String x; })); }
	}
	protected static final ValueCreate factory = new factory();
	public static ValueCreate factory() { return(factory); }
}
