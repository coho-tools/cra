package coho.interp;

import java.io.IOException;
import java.io.Writer;

public class VoidValue extends BasicValue {
	protected VoidValue() {}
	public Value negate() { return(instance()); }
	public void print(Writer w, Value[] opt) throws EvalException, IOException {
		w.write("void");
	}
	public String toString() { return(BasicValue.toString(this)); }
	
	private static VoidValue instance = new VoidValue();
	public static VoidValue instance() { return(instance); }
	
	protected static class factory implements ValueCreate {
		public Value create(Object val, Object args) {
			return(instance());
		}
		public Object foo() { return((new Object() { public Void x; })); }
	}
	protected static final ValueCreate factory = new factory();
	public static ValueCreate factory() { return(factory); }
}
