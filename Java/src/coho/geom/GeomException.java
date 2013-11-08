package coho.geom;
import coho.common.util.Configure;
public class GeomException extends RuntimeException {
	private static final long serialVersionUID = Configure.serialVersionUIDPrefix+40;

	public GeomException() {
		super();
	}
	public GeomException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}
	public GeomException(String arg0) {
		super(arg0);
	}
	public GeomException(Throwable arg0) {
		super(arg0);
	}
	
}
