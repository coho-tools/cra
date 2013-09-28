package coho.geom.twodim;
import java.util.*;

import coho.common.number.CohoType;
import coho.geom.*;
import coho.geom.twodim.ops.*;
public class ConvexPolygon extends SimplePolygon {
	public ConvexPolygon(Point[] points){
		this(points,true);
	}
	public ConvexPolygon(Point[] points, boolean check){
		super(points,check);
		if(check&&!isConvex(this))
			throw new GeomException("The input "+this+ " is not a convex polygon");			
	}
	public ConvexPolygon(Collection<Point> points){
		this(points.toArray(new Point[points.size()]));
	}
	public ConvexPolygon(Collection<Point> points, boolean check){
		this(points.toArray(new Point[points.size()]),check);
	}
	public ConvexPolygon(SimplePolygon p){
		this(p.points(),true);
	}
	public ConvexPolygon(SimplePolygon p, boolean check){
		this(p.points(),check);
	}
	/*
	 * check if the polygon is convex or not
	 * The input polygon should be represented by scalenumber, double or apr
	 * For internal use, an exception might be throw if interval is used.
	 */
	public static boolean isConvex(SimplePolygon p){
		int n = p.degree();
		for(int i=0;i<n;i++){
			//NOTE: can not be on the same line. Must left turn
			if(!Point.isLeftTurn(p.point((i-1+n)%n), p.point(i), p.point((i+1)%n))){
				return false;
			}
		}
		return true;
	}
	
	/*
	 * Intersection of two convex polygons
	 */
	public ConvexPolygon intersect(ConvexPolygon poly){
		return SimplePolygonOp.intersect(this,poly);
	}
	public ConvexPolygon intersect(ConvexPolygon p1, ConvexPolygon p2){
		return SimplePolygonOp.intersect(p1, p2);
	}
	/*
	 * Union of two convex polygons
	 */
	public SimplePolygon union(ConvexPolygon poly){
		return SimplePolygonOp.union(this,poly);
	}
	public static SimplePolygon union(ConvexPolygon p1, ConvexPolygon p2){
		return SimplePolygonOp.union(p1,p2);
	}
	/*
	 * Union of a set of convex polygons
	 */
	public static SimplePolygon union(Collection<ConvexPolygon> polys){
		return SimplePolygonOp.union(polys.toArray(new ConvexPolygon[polys.size()]));
	}
	public ConvexPolygon reduce(EndCondition ec){
		return SimplePolygonOp.reduce(this,ec);
	}
//	public ConvexPolygon reduce(EndCondition ec, double maxDist){
//		return SimplePolygonOp.reduce(this, ec,maxDist);
//	}
//	public ConvexPolygon canon(){
//		return SimplePolygonOp.canon(this);
//	}
	public ConvexPolygon specifyType(CohoType type){
		return (ConvexPolygon)super.specifyType(type);
	}
	/*
	 * convex polygon contains another polygon <==> convex polygon contains all vertices 
	 */
	public boolean contains(SimplePolygon poly){
		for (int i=0; i<poly.degree(); i++){
			if(!contains(poly.point(i)))
				return false;
		}
		return true;
	}
	private static boolean testContains(){
		Point[] points = new Point[]{
				new Point(0,0),
				new Point(1,0),
				new Point(1,1),
				new Point(0,1)
		};
		ConvexPolygon poly1 = new ConvexPolygon(points);
		points = new Point[]{
				new Point(0,0),
				new Point(1,0),
				new Point(1,1),
				new Point(0.5,1.5),
				new Point(0,1)
		};	
		ConvexPolygon poly2 = new ConvexPolygon(points);
		if(poly1.contains(poly1)!=true){
			System.out.println("poly1.contains(poly1) error");
			return false;
		}
		if(poly2.contains(poly1)!=true){
			System.out.println("poly2.contains(poly1) error");
			return false;
		}
		if(poly1.contains(poly2)==true){
			System.out.println("poly2.contains(poly1) error");
			return false;
		}
		return true;

	}
	public static void main(String[] args){
		if(testContains())
			System.out.println("Polygon contains test pass");
		else
			System.out.println("Polygon contains test fail");

		Point[] points = new Point[10];
		points[0] = Point.create(7.287038260217924E-4, 1.668774380843801);
		points[1] = Point.create(0.00265007125490967, 1.656287470507467);
		points[2] = Point.create(0.05771878432828369, 1.6551712634821094);
		points[3] = Point.create(0.1661861830981782, 1.6545191587119343);
		points[4] = Point.create(0.2153901290319991, 1.6545191587119343);
		points[5] = Point.create(0.2153901290319991, 1.8636590980985783);
		points[6] = Point.create(0.12290737545963663, 1.865073521216698);
		points[7] = Point.create(0.0024334245198491407, 1.865073521216698);
		points[8] = Point.create(0.0017096425548213258, 1.8648640604403135);
		points[9] = Point.create(0.0017098673236341532, 1.6687650052007073);
		SimplePolygon p = new SimplePolygon(points);
		System.out.println(ConvexPolygon.isConvex(p));
	}
}	

