package coho.interp;

import java.io.IOException;
import java.io.Writer;
/**
 * This class is for the value of each node of grammer tree after evaluation
 * @author chaoyan
 *
 */
public interface Value {
  public void print(Writer w, Value[] options)
      throws EvalException, IOException;
  public Value negate()         throws EvalException;
  public Value abs()            throws EvalException;
  public Value add(Value v)     throws EvalException;
  public Value mult(Value v)    throws EvalException;
  
  public Value less(Value v)    throws EvalException;
  public Value leq(Value v)     throws EvalException;
  public Value eq(Value v)      throws EvalException;
  public Value neq(Value v)     throws EvalException;
  public Value geq(Value v)     throws EvalException;
  public Value greater(Value v) throws EvalException;
  
  public String typeName();
}
