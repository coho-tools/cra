package coho.lp;
import coho.common.util.Configure;
public class LPError extends RuntimeException {
  private static final long serialVersionUID = Configure.serialVersionUIDPrefix+30;
  public LPError() { super(); }
  public LPError(String msg) { super(msg); }
  /**
   * chained exception facility
   */
  public LPError(Throwable cause){super(cause);}
  public LPError(String msg, Throwable cause){super(msg,cause);}
}
