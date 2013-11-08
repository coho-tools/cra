package coho.common.matrix;
import coho.common.util.Configure;
// what's the difference of MatrixError and MatrixException
public class MatrixError extends RuntimeException {
  private static final long serialVersionUID = Configure.serialVersionUIDPrefix+10;
  public MatrixError() { super(); }
  public MatrixError(String msg) { super(msg); }
  /**
   * chained exception facility
   */
  public MatrixError(Throwable cause){super(cause);};
  public MatrixError(String msg,Throwable cause){super(msg,cause);}
}
