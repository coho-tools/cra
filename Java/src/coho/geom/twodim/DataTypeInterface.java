package coho.geom.twodim;

import coho.common.number.*;

interface DataTypeInterface {
	public GeomObj2 specifyType(CohoType type);//represents the obj using specified data type
	public CohoType type();//return the type of reprsentation
	public double maxError();//max error of the point of interval reprsentation.

}
