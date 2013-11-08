/*
 * Created on 28-Jun-2004
 */
package coho.interp;

import java.io.IOException;
import java.io.Writer;

import coho.common.matrix.*;


/**
 * @author Mark Greenstreet (mrg@cs.ubc.ca)
 */
public class RangeValue extends BasicValue {
	private Range r;

	protected RangeValue(int lo, int hi) throws EvalException {
		if(lo > hi) throw new EvalException(
				"range(lo, hi): lo must not be greater than hi");
		else r = new Range(lo, hi);
	}
	protected RangeValue(Range _r) { r = _r; }

	public Range value() { return(r); }

	public void print(Writer w, Value[] options)
	throws EvalException, IOException {
		w.write(r.toString());
	}

	public Value negate() throws EvalException {
		return(new RangeValue(-r.hi(), -r.lo()));
	}

	protected static class factory implements ValueCreate {
		public Value create(Object val, Object args) {
			return(new RangeValue((Range)(val)));
		}
		public Object foo() { return((new Object() { public Range x; })); }
	}
	protected static final ValueCreate factory = new factory();
	public static ValueCreate factory() { return(factory); }
}
