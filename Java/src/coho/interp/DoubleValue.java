package coho.interp;

import java.io.*;
import coho.common.util.*;

public class DoubleValue extends BasicValue {
	private double d;  public double d() { return(d); }
	
	protected DoubleValue(Double _d) { d = _d.doubleValue(); }
	public double value() { return(d); }
	
	public Value negate() throws EvalException 
	{ return(factory.create(new Double(-d), null)); }
	public Value abs() throws EvalException 
	{ return(factory.create(new Double(Math.abs(d)), null)); }
	
	public void print(Writer w, Value[] opt) throws EvalException, IOException {
		boolean hex = false;
		for(int i = 0; i < opt.length; i++) {
			String x = opt[i].toString();
			if(x.compareTo("hex") == 0) hex = true;
			else if(x.compareTo("plain") == 0) hex = false;
		}
		if(hex) {
			w.write('$');
			w.write(MoreLong.toHexString(Double.doubleToLongBits(d)));
		} else  {
			w.write(Double.toString(d));
		}
	}
	
	public Value add(Value v) throws EvalException{
		if(v instanceof DoubleValue)
			return(ValueFactory.create(new Double(d + ((DoubleValue)(v)).d())));
		else return(BasicValue.add(this, v));
	}
	public Value mult(Value v) throws EvalException{
		if(v instanceof DoubleValue)
			return(ValueFactory.create(new Double(d * ((DoubleValue)(v)).d())));
		else if(v instanceof MatrixValue)
			return(v.mult(this));
		else return(BasicValue.mult(this, v));
	}
	
	public Value less(Value v) throws EvalException{
		if(v instanceof DoubleValue)
			return(ValueFactory.create(new Double(
					(d < ((DoubleValue)(v)).d()) ? 1.0 : 0.0)));
		else return(BasicValue.less(this, v));
	}
	public Value leq(Value v) throws EvalException{
		if(v instanceof DoubleValue)
			return(ValueFactory.create(new Double(
					(d <= ((DoubleValue)(v)).d()) ? 1.0 : 0.0)));
		else return(BasicValue.leq(this, v));
	}
	public Value eq(Value v) throws EvalException{
		if(v instanceof DoubleValue)
			return(ValueFactory.create(new Double(
					(d == ((DoubleValue)(v)).d()) ? 1.0 : 0.0)));
		else return(BasicValue.eq(this, v));
	}
	public Value neq(Value v) throws EvalException{
		if(v instanceof DoubleValue)
			return(ValueFactory.create(new Double(
					(d != ((DoubleValue)(v)).d()) ? 1.0 : 0.0)));
		else return(BasicValue.neq(this, v));
	}
	public Value geq(Value v) throws EvalException{
		if(v instanceof DoubleValue)
			return(ValueFactory.create(new Double(
					(d >= ((DoubleValue)(v)).d()) ? 1.0 : 0.0)));
		else return(BasicValue.geq(this, v));
	}
	public Value greater(Value v) throws EvalException{
		if(v instanceof DoubleValue)
			return(ValueFactory.create(new Double(
					(d > ((DoubleValue)(v)).d()) ? 1.0 : 0.0)));
		else return(BasicValue.greater(this, v));
	}
	
	public String typeName() { return("double"); }
	
	protected static class factory implements ValueCreate {
		public Value create(Object val, Object args) {
			return(new DoubleValue((Double)(val)));
		}
		public Object foo() { return((new Object() { public Double x; })); }
	}
	protected static final ValueCreate factory = new factory();
	public static ValueCreate factory() { return(factory); }
}
