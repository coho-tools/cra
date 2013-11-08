package coho.lp;
import coho.common.util.Configure;
/*
 * Throw it if the LP is unbounded
 */
public class UnboundedLPError extends LPError {
	private static final long serialVersionUID = Configure.serialVersionUIDPrefix+31;
	public UnboundedLPError(){super();}
	public UnboundedLPError(String msg){super(msg);}
	public UnboundedLPError(Throwable cause){super(cause);}
	public UnboundedLPError(String msg, Throwable cause){super(msg,cause);}	
}
