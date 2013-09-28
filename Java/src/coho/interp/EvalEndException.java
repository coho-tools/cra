package coho.interp;
/**
 * Indicate the end of the interactive parse.
 * But do we need it now? We don't need to revoke parse repeatly
 * @author chaoyan
 *
 */
public class EvalEndException extends EvalException {
  EvalEndException() { super(); }
  EvalEndException(String msg) { super(msg); }
  private static final long serialVersionUID = 65557L;
}
