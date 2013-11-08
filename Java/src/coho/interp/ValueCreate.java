package coho.interp;
/**
 * The interface to creat Value
 * @author chaoyan
 *
 */
public interface ValueCreate {
  public Value create(Object val, Object args) throws EvalException;
  /* This factory is invoked when the val parameter of create is of the
   * same class as foo().x". */
  public Object foo();
}
