package coho.interp;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;

import coho.common.number.*;
//NOTICE: this should never be used till now.
public class DoubleIntervalValue extends BasicValue {
	private DoubleInterval x;

	public DoubleInterval x() {
		return (x);
	}

	protected DoubleIntervalValue(DoubleInterval _x) {
		x = _x;
	}

	public DoubleInterval value() {
		return (x);
	}

	public Value negate() throws EvalException {
		return (factory.create(x.negate(), null));
	}

	public Value abs() throws EvalException {
		return (factory.create(x.abs(), null));
	}

	public void print(Writer w, Value[] opt) throws EvalException, IOException {
		boolean hex = false;
		for (int i = 0; i < opt.length; i++) {
			String x = opt[i].toString();
			if (x.compareTo("hex") == 0)
				hex = true;
			else if (x.compareTo("plain") == 0)
				hex = false;
		}
		w.write('<');
		if (hex) {
			w.write('$');
			// XXX don't align now. Bugs before x.doubleLo and x.doubleHi
			w.write(Long.toHexString(Double.doubleToLongBits(x.lo()
					.doubleValue())));
			w.write(", ");
			w.write(Long.toHexString(Double.doubleToLongBits(x.hi()
					.doubleValue())));

		} else {
			w.write(Double.toString(x.lo().doubleValue()));
			w.write(", ");
			w.write(Double.toString(x.hi().doubleValue()));
		}
	}

	protected void unsupported(Value v, String what) throws EvalException {
		throw new EvalException("Don't know how to " + what + " a "
				+ typeName() + " and a " + v.typeName());
	}

	protected Value do_it(Value v, String what) throws EvalException {
		DoubleInterval y = null;
		if (v instanceof DoubleValue)
			y = DoubleInterval.create(((DoubleValue) (v)).value());
		else if (v instanceof DoubleIntervalValue) // i think here should be
			// DoubleIntervalValue
			y = ((DoubleIntervalValue) (v)).value();
		else
			unsupported(v, what);
		Method m;
		try {
			m = x.getClass().getMethod(what, new Class[] { y.getClass() });
			return (ValueFactory.create(m.invoke(x, new Object[] { y })));
		} catch (Exception e) {
			unsupported(v, what);
		}
		return (null); // unreached
	}

	public Value add(Value v) throws EvalException {
		return (do_it(v, "add"));
	}

	public Value mult(Value v) throws EvalException {
		return (do_it(v, "mult"));
	}

	public String typeName() {
		return ("doubleInterval");
	}

	protected static class factory implements ValueCreate {
		public Value create(Object val, Object args) {
			// change from double to doubleInterval
			return (new DoubleIntervalValue((DoubleInterval) (val)));
		}

		public Object foo() {
			return ((new Object() {
				public DoubleInterval x;
			}));
		}
	}

	protected static final ValueCreate factory = new factory();

	public static ValueCreate factory() {
		return (factory);
	}
}
