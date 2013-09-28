package coho.interp;
import coho.common.number.*;

//modified by chaoyan. We have change the intersect/union of polygons, change the interface.
import java.io.*;
import java.util.*;

import coho.geom.*;
import coho.geom.twodim.*;


public class PolygonValue extends BasicValue {
	public final static double defaultErrtol = 0.01;

	private Polygon poly; //polygon
	private RCvalue rc;  //rc value for polygon

	protected PolygonValue(Polygon p){
		poly = p;
	}
	/** 
	 * Create a polygon from a RCvalue(matrix) 
	 * NOTE: the connection from matlab to java
	 * TODO: what if there are less than 3 points 
	 */
	protected PolygonValue(RCvalue u) throws EvalException{//NOTE: matlab to java
		Point[] points;
		if(u.isRow()){
			//each column should have two double values
			points = new Point[u.size()];
			for(int i=0; i<u.size(); i++){
				Value v = u.value(i);
				if(!(v instanceof RCvalue))
					rc_bad();
				RCvalue c = (RCvalue)v;
				if(c.size()!=2)
					rc_bad();
				Value x = c.value(0), y = c.value(1);
				if(!(x instanceof DoubleValue) || !(y instanceof DoubleValue))
					rc_bad();
				points[i] = Point.create(((DoubleValue)x).value(), ((DoubleValue)y).value());
			}
		}else{
			if( !(u.value(0) instanceof RCvalue) || !(u.value(1) instanceof RCvalue) ) 
				rc_bad();
			RCvalue xMatrix = (RCvalue)(u.value(0)), yMatrix = (RCvalue)u.value(1);
			if(xMatrix.size()!=yMatrix.size())
				rc_bad();
			points = new Point[xMatrix.size()];
			for(int i=0; i<xMatrix.size(); i++){
				Value x = xMatrix.value(i), y = yMatrix.value(i);
				if(!(x instanceof DoubleValue) || !(y instanceof DoubleValue))
					rc_bad();
				points[i] = Point.create(((DoubleValue)x).value(), ((DoubleValue)y).value());
			}
		}
		poly = new SimplePolygon(points);
		if(ConvexPolygon.isConvex((SimplePolygon)poly))//create convexPolygon if it is, simplify the computation later
			//BUGS, SimplePolygon removes duplicated points. Therefore, it is incorrect to construct a convex polygon
			// using points and do not check. We should use the points from poly.
			// poly = new ConvexPolygon(points,false);
			poly = new ConvexPolygon(poly.points(),false);
		if(poly.degree()==u.size())//SimplePolygon may remove redundant points
			rc = u;
		else
			rc = null;
	}	

	/**
	 * NOTE: the connection from java to matlab
	 */
	public RCvalue java2Matlab() throws EvalException {
		if(rc == null) {
			if(poly!=null){
				Value[] v = new Value[poly.degree()];
				for(int i = 0; i < v.length; i++) {
					Point pt = poly.point(i);
					assert pt.type()==CohoDouble.type: "The result type is not CohoDouble, it is "+pt.type();
//					if(pt.type()!=CohoDouble.type){
//					throw new RuntimeException("The result type is not CohoDouble, it is "+pt.type() );
//					}
					v[i] = RCvalue.factory().create(new Value[] {
							DoubleValue.factory().create(new Double(((CohoDouble)pt.x()).doubleValue()), null),
							DoubleValue.factory().create(new Double(((CohoDouble)pt.y()).doubleValue()), null)
					}, false);
				}
				rc = (RCvalue)(RCvalue.factory().create(v, true));
			}else{//empty polygon
				rc = (RCvalue)(RCvalue.factory().create(new Value[0], true));
			}
		}
		return(rc);
	}

	protected void rc_bad() throws EvalException {
		throw new EvalException(
				"polygon(u): u must be either a row of columns of two doubles" +
				" or a column of two rows of the same number of doubles."
		);
	}

	public boolean has_rc(){
		return rc != null;
	}
	public Polygon polygon(){
		return poly;
	}

	public Value negate() throws EvalException{
		return new PolygonValue((RCvalue)(java2Matlab().negate()));
	}
	public void print(Writer w, Value[] opt) throws EvalException, IOException {
		Writer true_w = w;
		w = new StringWriter();
		
		w.write("polygon(\n");
		java2Matlab().print(w, opt);
		w.write(")\n\n");

		String s = w.toString();
		//make it readable.
		s = s.replace("\n,", ",\n");
		s = s.replace("\n[ [", "\n[\n [");
		s = s.replace("\n ]\n", "\n]\n");
		true_w.write(s);
	}

	public String toString() { return(BasicValue.toString(this)); }

	protected static class factory_pgon implements ValueCreate {
		public Value create(Object val, Object args) {
			return(new PolygonValue((Polygon)(val)));
		}
		public Object foo() { return((new Object() { public Polygon x; })); }
	}
	protected static class factory_rc implements ValueCreate {
		public Value create(Object val, Object args) throws EvalException {
			return(new PolygonValue((RCvalue)(val)));
		}
		public Object foo() { return((new Object() { public RCvalue x; })); }
	}
	protected static final ValueCreate[] factories = new ValueCreate[] {
		new factory_pgon(),
		new factory_rc()
	};
	public static ValueCreate[] factory() { return(factories); }

	protected interface Operation {
		public String name();
		public PolygonValue op(PolygonValue a, PolygonValue b)
		throws EvalException;
	}
	protected static Value grind(RCvalue args, Operation op)throws EvalException {
		for(int i = 0; i < args.size(); i++)
			if(!(args.value(i) instanceof PolygonValue))
				throw new EvalException(op.name() + "(): all operands must polgons." +
						"Operand " + (i+1) + "is not.");
		return(grind(args, op, 0, args.size()));
	}
	//value(lo) to value(hi-1)
	protected static Value grind(RCvalue args, Operation op,int lo, int hi) throws EvalException {
		switch(hi - lo) {
		case 0: 
			return new PolygonValue(
					(RCvalue)(RCvalue.factory().create(new Value[] {}, true))
			);//empty
		case 1: 
			return args.value(lo);//one
		case 2:  
			Value result = op.op( (PolygonValue)(args.value(lo)), (PolygonValue)(args.value(hi-1)) );
			return result;//two
		default:
			int mid = (lo+hi+1)/2;//split
		Value v0 = grind(args, op, lo, mid);
		Value v1 = grind(args, op, mid, hi);
		result = op.op((PolygonValue)(v0), (PolygonValue)(v1));
		return result;
		}
	}

	private static Function[] functions = new Function[] {
		new Function() {//create a new polygon
			public String name() { return "polygon"; }
			public Value eval(RCvalue args) throws EvalException {
				if(args.size() != 1)
					throw new EvalException("usage: polygon(row/col)");
				if(!(args.value(0) instanceof RCvalue))
					throw new EvalException("polygon: argument must be a row/col");
				return new PolygonValue( (RCvalue)(args.value(0)) );
			}
		},
		new Function() {//compute the convex hull of polygon
			public String name() { return "hull"; }
			public Value eval(RCvalue args) throws EvalException {
				if(args.size() != 1)
					throw new EvalException("usage: hull(polygon)");
				if(!(args.value(0) instanceof PolygonValue))
					throw new EvalException("hull: argument must be a polygon");
				PolygonValue pv = (PolygonValue)(args.value(0));
				Polygon poly = pv.polygon();
				return new PolygonValue(poly.convexHull());	//NOTE: connection for convex hull	
			}
		},
		new Function() {//reduce the number of polygons
			public String name() { return "reduce"; }
			public Value eval(RCvalue args) throws EvalException {
				if((args.size() < 0) || (3 < args.size()))
					throw new EvalException("usage: reduce(polygon, [errtol,[edgeReducible] ] ])");
				if(!(args.value(0) instanceof PolygonValue))
					throw new EvalException("reduce: first operand must be a polygon");
				
				double errtol = defaultErrtol;
				if(args.size() >= 2) {  // get error tolerance
					if(!(args.value(1) instanceof DoubleValue)) 
						throw new EvalException("reduce: second operand must be an integer");
					errtol = ((DoubleValue)(args.value(1))).value();
				}
				boolean edgeReducible = true;
				if(args.size()>=3){
					double temp = ((DoubleValue)(args.value(2))).value();
					edgeReducible = (temp>0); //temp = 1, edge reducible, otherwise, not
				}
				
				Polygon.EndCondition ec = new Polygon.CostEndCondition(errtol);
				if(errtol>=3){
					int maxV = (int)Math.round(errtol);
					ec = new Polygon.DegreeEndCondition(maxV);
				}
				PolygonValue p = (PolygonValue)(args.value(0));
				Polygon poly = p.polygon();
				if(poly instanceof ConvexPolygon){//NOTE: connection for reduce
					if(edgeReducible)
						return new PolygonValue(((ConvexPolygon)poly).reduce(ec));
					else
						return p;//can't reduce because convex polygon can only reduce edge
				}else{
					return new PolygonValue(poly.reduce(ec,true,edgeReducible));
				}
			}
		},
//		new Function() {//intersection of two polygons
//			class Op implements Operation {
//				public String name() { return "intersect"; }
//				public PolygonValue op(PolygonValue a, PolygonValue b)throws EvalException {
//					Polygon poly1 = a.polygon(), poly2 = b.polygon();
//					try { 
//						//TODO: what if the intersection is not a polygon?
//						//Convert to polygon directly or return GeomObj2
//						return new PolygonValue(poly1.intersect(poly2));
//					} catch (GeomException e) {
//						throw e;
//					}
//				}
//				public Op() {};
//			}
//			public String name() { return "intersect"; }
//			public Value eval(RCvalue args) throws EvalException {
//				if(args.size() == 0) 
//					throw new EvalException("intersect(): at least one operand required");
//				return(grind(args, new Op()));
//			}
//		},
		new Function() {//intersection of two polygons
			public String name() { return "intersect"; }
			public Value eval(RCvalue args) throws EvalException {
				if(args.size() != 2) 
					throw new EvalException("usage: intersect(poly1,poly2)");
				Polygon p1 = ((PolygonValue)args.value(0)).polygon();
				Polygon p2 = ((PolygonValue)args.value(1)).polygon();
				return new PolygonValue(p1.intersect(p2));
			}
		},
		new Function() {//union of a set of polygons
			public String name() { return "union"; }
			public Value eval(RCvalue args) throws EvalException{
				Polygon[] polys = new Polygon[args.size()-1];
				for(int i=0; i<args.size()-1; i++){
					PolygonValue pv = (PolygonValue)args.value(i);
					polys[i] = pv.polygon();
				}
				PolygonValue pv = (PolygonValue)args.value(args.size()-1);//the last polygon
				return new PolygonValue(pv.polygon().union(polys));
			}
		},
		//TODO: add BooleanValue ? or use boolean+"" to conver  to string
		new Function() {//union of a set of polygons
			public String name() { return "contain"; }
			public Value eval(RCvalue args) throws EvalException{
				if(args.size() != 2)
					throw new EvalException("usage: reduce(polygon, errtol])");
				PolygonValue pv1 = (PolygonValue)args.value(0);
				PolygonValue pv2 = (PolygonValue)args.value(1);
				boolean isContain = pv1.polygon().contains(pv2.polygon());				
				double contain = 0;
				if(isContain){
					contain = 1;//we don't have a boolean factory now. 
				}
				return ValueFactory.create(contain);
			}
		},
	};

	public static Enumeration pgonFns(){
		return(
				new Enumeration(){
					private int i = 0;
					public boolean hasMoreElements() { return(i < functions.length); }
					public Object nextElement() { return(functions[i++]); }
				}
		);
	}
}
