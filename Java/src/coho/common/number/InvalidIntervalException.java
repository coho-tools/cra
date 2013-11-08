package coho.common.number;
import coho.common.util.Configure;
public class InvalidIntervalException extends ArithmeticException{
    private static final long serialVersionUID = Configure.serialVersionUIDPrefix+0;
    public InvalidIntervalException() { super(); }
    public InvalidIntervalException(String msg) { super(msg); }
}
