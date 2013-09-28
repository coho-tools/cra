package coho.common.number;
import coho.common.util.Configure;
public class NotcomparableIntervalException extends ArithmeticException {
    private static final long serialVersionUID = Configure.serialVersionUIDPrefix+1;
    public NotcomparableIntervalException() { super(); }
    public NotcomparableIntervalException(String msg) { super(msg); }
}
