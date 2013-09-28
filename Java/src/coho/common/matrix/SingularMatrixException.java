package coho.common.matrix;
import coho.common.util.Configure;
public class SingularMatrixException extends Exception {
	  private static final long serialVersionUID = Configure.serialVersionUIDPrefix+11 ;  
	  double cond;
	  public double getCond(){return cond;}
	  public SingularMatrixException(double cn) { super(); this.cond=cn;}  
	  public SingularMatrixException(double cn, String msg) { super(msg); this.cond=cn;}
	  /**
	   * chained exception facility.
	   */
	  public SingularMatrixException(Throwable cause){super(cause);}
	  public SingularMatrixException(String msg, Throwable cause){super(msg,cause);}

}
