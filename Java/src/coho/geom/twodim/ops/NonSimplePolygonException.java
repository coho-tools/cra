package coho.geom.twodim.ops;

import coho.common.util.Configure;
import coho.geom.GeomException;

public class NonSimplePolygonException extends GeomException {
	private static final long serialVersionUID = Configure.serialVersionUIDPrefix+41;
	public NonSimplePolygonException() {
		super();
	}
	public NonSimplePolygonException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}
	public NonSimplePolygonException(String arg0) {
		super(arg0);
	}
	public NonSimplePolygonException(Throwable arg0) {
		super(arg0);
	}
}
