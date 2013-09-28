package coho.interp;

/**
 * The interface for the evaluation of each function
 * @author chaoyan
 *
 */
public interface Function {
  public String name();
  public Value eval(RCvalue args) throws EvalException;
}
