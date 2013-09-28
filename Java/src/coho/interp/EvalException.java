package coho.interp;

public class EvalException extends Exception {
  private static final long serialVersionUID = 65538L;
  public EvalException() { super(); }
  public EvalException(String msg) { super(msg); }
}
