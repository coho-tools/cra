package coho.geom.twodim;
import coho.common.number.*;
public interface Polygon extends GeomObj2 {
	public int degree();
	public CohoNumber area();
	public CohoNumber perimeter();
	public Point[] points();
	public Point point(int i);
	public Segment[] edges();
	public Segment edge(int i);
	public int llPos();//left lower point index;
	public GeomObj2 intersect(GeomObj2 obj);
	public Polygon intersect(Polygon p);
	public GeomObj2 union(GeomObj2 obj);
	public Polygon union(Polygon p);
	public Polygon union(Polygon[] p);
	public ConvexPolygon convexHull();
	public Polygon specifyType(CohoType type);
	public Polygon reduce(EndCondition ec);//reduce points
	public Polygon reduce(EndCondition ec, boolean pointReducible, boolean edgeReducible);
//	public Polygon reduce(EndCondition ec, double maxDist);
//	public Polygon canon();//remove points with little cost
	public static interface EndCondition{
		public boolean cond(double costNow, int degreeNow, double costNext, int degreeNext);
	}
	public static class CostEndCondition implements EndCondition{
		private double errtol;
		public CostEndCondition(){
			errtol = 0.01;// 1 percent
		}
		public CostEndCondition(double errtol){
			this.errtol = errtol;
		}
		public boolean cond(double costNow, int degreeNow, double costNext, int degreeNext){
			return (costNext > errtol); //TODO what's costNext? 			
		}
		public double errtol(){
			return errtol;
		}
	}
	public static class DegreeEndCondition implements EndCondition{
		private int maxV;
		public DegreeEndCondition(int maxV){
			this.maxV = maxV;
		}
		public boolean cond(double costNow, int degreeNow, double costNext, int degreeNext){
			return (degreeNow <= maxV);
		}
		public int degree(){
			return maxV;
		}
	};
}
