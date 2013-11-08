package coho.common.number;

/**
 * CohoType is defined for each CohoNumber class for promtion. 
 * @author chaoyan
 *
 */
public interface CohoType {
	public CohoType promote(CohoType that);//default type for this op that
	public Class classType();
	public String name();
	public CohoNumber zero();
	public CohoNumber one();
}
