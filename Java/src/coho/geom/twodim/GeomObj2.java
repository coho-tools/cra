package coho.geom.twodim;
import coho.geom.*;
/**
 * This is the interface for 2d geometry object. 
 * 
 * For this geometry package, it provide operation for simple polygon 
 * which is represented by anti-clock-wise orderd vertices. 
 * 
 * For each point, it's represented by ScaleNumber. However, round off error of CohoDouble may make 
 * the result incorrect and APR is quite slow. Therefore, during the interval computation, we use DoubleInterval
 * for efficient and APR for correctness. The Point can be represented by IntervalNumber, but it's only for interval computation. 
 * 
 * The result is also represented by ScaleNumber. Usually we use the middle of the DoubleInterval, however, user 
 * can request that the result is apr. But for our application, matlab can't recognize apr. We use double for efficience. Although 
 * some error is introduced.
 * 
 * For each operation, we provide a basic operation without considering the data type. User can call it with ScaleNumber representation. 
 * We provide another function that use hybrid method: use interval first then use apr if it fails. Another function is provide to convert 
 * the interval representation to ScaleNumber for users.   
 * 
 * The representation of Point can be arbitrary CohoNumber. However, to create a geometry object,
 * it should use ScaleNumber because the object must be exact. The interval reprsented
 * Point is used for interval computation for performance. 
 * 
 * For all geometry object except polygon, the result depends on the reprsentation of Point.
 * If the point is double, round off should be consider. 
 * If the point is apr, exact value are given.
 * If the point is interval, exception might be thrown. 
 * For each geometry object, the specifyType() function is used to specify the reprsentation type. 
 * 
 * For the polygon operation, which is also the main function of this package, (CONSIDER: only public polygon outside?)
 * it use interval computation for performance and apr computation for accuracy (see coho.geom.twodim.ops package). 
 * The output point might be represented as double(as input), interval or apr. The use can determine which to use.
 * But usually the doubleValue is used.
 * 
 * @author chaoyan
 *
 */
public interface GeomObj2 extends GeomObj, DataTypeInterface{
	public static enum RoundMode{MIDDLE,LARGE,SMALL,CENTER};
	public static RoundMode roundMode = RoundMode.MIDDLE;
	public static final double eps = 1e-12;
	public static final double eps2 = 1e-24;
	public BoundingBox bbox();//each two dimenstional object should has a bounding box
	public GeomObj2 translate(Point offset);//translate the object
	public GeomObj2 negate();
	public GeomObj2 transpose();
	public GeomObj2 intersect(GeomObj2 g);
	public GeomObj2 union(GeomObj2 g);
	public boolean contains(GeomObj2 g);
}
