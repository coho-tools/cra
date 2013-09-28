package coho.interp;

/**
 * The interface for evaluation for the PTnode of the grammer tree
 * @author chaoyan
 *
 */
public interface Evaluator {
  /**
   * description for this evaluator
   * indicate how to evaluation the node.
   * @return
   */	
  //the production usually return String or String[],
  //and should have the form defined in PTevalFactory.production().
  public Object production();
  /**
   * Evaluation the specified node
   * @param nd
   * @return
   * @throws EvalException
   */
  public Value eval(PTnode nd) throws EvalException;
}
