package coho.geom.twodim;
import coho.common.number.*;
// singleton pattern
public class Empty implements GeomObj2 {
	private final static Empty e = new Empty();
	public CohoType type(){
		throw new UnsupportedOperationException();
	}
	private Empty(){};
	public static Empty instance(){return e;}	
	public BoundingBox bbox() {
		return null;
	}
	public Empty translate(Point offset) {
		return e;
	}	
	public Empty negate(){
		return e;
	}	
	public Empty transpose() {
		return e;
	}
	public Empty intersect(GeomObj2 g) {
		return e;
	}
	public Empty union(GeomObj2 g){
		return e;
	}
	public boolean contains(GeomObj2 g){
		return false;
	}	
	public String toString(){
		return "empty";
	}
	public Empty specifyType(CohoType type){
		return e;
	}
	public double maxError(){
		return 0;
	}
	public static void main(String[] args){
		Empty e = Empty.instance();
		System.out.println("The bounding box of empty is "+e.bbox());
		System.out.println("The translate of empty is "+e.translate(new Point(0,0)));
		System.out.println("The transpose of empty is "+e.transpose());
		System.out.println("The intersection of empty and any object is "+e.intersect(e));
	}
}
