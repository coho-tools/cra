package coho.geom.twodim;

import coho.common.number.*;
import coho.geom.*;

/**
 * A segment described by two points p1 and p2. 
 * Also (x-x1)/(x2-x1)  = (y-y1)/(y2-y1) or Ax+By+C=0; A=y2-y1 B = -(x2-x1) C = -(Ax1+By1).
 * CONSIDER: Is the segment directed or not?
 * For the operations, the result depends on the data type it used. 
 * It might throw exceptions, for example, InvalidIntervalException.
 * NotComparableIntervalException.
 * @author chaoyan
 *
 */
public class Segment implements GeomObj2, Comparable<Segment> {
	private final Point[] p = new Point[2];
	private final CohoType type;

	public Point p(int i){
		switch(i){
		case 0:
			return p[0];
		case 1:
			return p[1];
		default:
			throw new UnsupportedOperationException();
		}
	}
	public Point left(){
		return p[0].min(p[1]);
	}
	public Point right(){
		return p[0].max(p[1]);
	}
	public CohoType type(){
		return type;
	}
	
	public Segment(Point p0, Point p1){
		if(p0.type()!=p1.type())
			throw new GeomException("Segment: The point should have same types:"+p0.type()+", "+p1.type());
		p[0] = p0;
		p[1] = p1;
		type = p0.type();
	}
	/*
	 * The representation for p0 and p1 should be the same.
	 */
	public static Segment create(Point p0, Point p1){
		return new Segment(p0,p1);
	}

	/*******************************************
	 * Operations doesn't depend on representation 
	 *******************************************/
	public boolean isPoint(){
		return p[0].equals(p[1]);
	}
	public CohoNumber len2(){
		CohoNumber dx = p[1].x().sub(p[0].x());
		CohoNumber dy = p[1].y().sub(p[0].y());
		return (dx.mult(dx).add(dy.mult(dy)));		
	}
	//The result can't be exact
	private double len(){
		return (Math.sqrt(len2().doubleValue()));//sqrt is not supported for all CohoNumber
	}
	public boolean isHoriz(){
		return p[0].y().equals(p[1].y());
	}
	public boolean isVert(){
		return p[0].x().equals(p[1].x());
	}
	public BoundingBox bbox() {
		//FIXED see constructor of Bounding box. The point is not ll/ur point of bounding box
		return new BoundingBox(p[0],p[1]);
	}
	public Segment translate(Point offset) {
		return new Segment(p[0].translate(offset),p[1].translate(offset));
	}
	public Segment transpose() {
		return new Segment(p[0].transpose(),p[1].transpose());
	}
	public Segment negate(){
		return new Segment(p[0].negate(),p[1].negate());
	}
	// reverse the order
	public Segment reverse(){
		return new Segment(p[1],p[0]);
	}
	
	/***********************************************
	 * Operations depends on the data representation.
	 ***********************************************/		
	public CohoNumber x(ScaleNumber y){
		return transpose().y(y);
	}	
	/*
	 * interpolation at point x.
	 * for internal use only. exception might be thrown for interval representation.
	 */
	public CohoNumber y(ScaleNumber x){
		if(x.compareTo(left().x())<0 || x.compareTo(right().x())>0)
			throw new GeomException("extrapolation is for fools:  " + this +".y(" + x + ")");
		else if(p[0].y().equals(p[1].y()))
			return p[0].y();
		else if(p[0].x().equals(p[1].x()))
			throw new GeomException("singularity in interpolation");
		else{			
			CohoNumber temp = (p[0].y().mult(p[1].x().sub(x))).add(p[1].y().mult(x.sub(p[0].x())));
			return temp.div(p[1].x().sub(p[0].x()));
		}
	}
	//The line is described by Ax+By+C=0
	public CohoNumber A(){//y2-y1
		return p[1].y().sub(p[0].y());
	}
	public CohoNumber B(){//-(x2-x1)
		return p[0].x().sub(p[1].x());
	}
	public CohoNumber C(){//-(Ax1+By1)==(x2-x1)y1-(y2-y1)x1
		CohoNumber x0 = p[0].x();
		CohoNumber y0 = p[0].y();
		CohoNumber x1 = p[1].x();
		CohoNumber y1 = p[1].y();
		CohoNumber delX = x1.sub(x0);
		CohoNumber delY = y1.sub(y0);
		return delX.mult(y0).sub(delY.mult(x0));
	}
	public Line line(){
		if(type instanceof ScaleType){
			Segment aprSeg = specifyType(CohoAPR.type);//remove compuation error as possible
			Line l = new Line(aprSeg.A(),aprSeg.B(),aprSeg.C());
			return l.specifyType(type);
		}else{
			return new Line(A(),B(),C());
		}
	}
	/*
	 * Return the intersection, the same data as this segment
	 * For internal use only, an exception might be thrown for interval representation.
	 */
	public GeomObj2 intersect(GeomObj2 g) {		
		if(g instanceof BoundingBox)
			return intersectBoundingBox((BoundingBox)g).specifyType(type);
		if(g instanceof Point)
			return intersectPoint((Point) g).specifyType(type);
		if(g instanceof Segment)
			return intersectSegment((Segment)g).specifyType(type);
		return g.intersect(this).specifyType(type);
	}
	
	protected GeomObj2 intersectBoundingBox(BoundingBox that){
		if( (type==CohoDouble.type || that.type()==CohoDouble.type) && (type instanceof ScaleType && that.type() instanceof ScaleType) ){
			try{
				GeomObj2 obj = this.specifyType(DoubleInterval.type).baseIntersectBoundingBox(that.specifyType(DoubleInterval.type));
				if(obj.maxError()> eps)
					throw new ArithmeticException("too large interval");
				return obj;
			}catch(ArithmeticException e){
				return this.specifyType(CohoAPR.type).baseIntersectBoundingBox(that.specifyType(CohoAPR.type));
			}
		}else{//NOTICE: don't use double and interval. the result may contain error. 
			return baseIntersectBoundingBox(that);
		}
	}
	
	private GeomObj2 baseIntersectBoundingBox(BoundingBox b){
		if(isPoint()){//point & (point, segment, bbox)
			return b.intersect(this.left());
		}else if(b.isSegment()){//segment & segment
			return baseIntersectSegment(new Segment(b.ll(),b.ur()));
		}else if(b.isPoint()){//segment & point
			return baseIntersectPoint(b.ll());
		}
		//segment & bbox
		if(bbox().intersect(b) instanceof Empty){
			return Empty.instance();
		}else if(b.contains(this)){
			return this;
		}
		Point[] v = new Point[]{b.ll(),b.lr(),b.ur(),b.ul()};//vertex of bounding box
		Point[] q = new Point[2];//intersection point
		int i = 0;
		GeomObj2 g;
		for(int j=0;j<4;j++){//check for intersection with bbox edges
			g = baseIntersectPoint(v[j]);
			if(!(g instanceof Empty)){//vertex of bbox on segment
				q[i++] = v[j];
				v[j] = null;
			}				
		}
		for(int j=0;j<4;j++){//intersection of segment and edgs of bbox
			if(v[j]!=null && v[(j+1)%4]!=null){
				g = baseIntersectSegment( new Segment(v[j],v[(j+1)%4]));
				if(!(g instanceof Empty))
					q[i++] = (Point)g;
			}
		}
		j_loop: for(int j=0;j<2;i++){//check for endpoints of this segment in b
			for (int jj = 0; jj < i; jj++) // is p(j) already in q?
				if (p[j].compareTo(q[jj]) == 0) // yes
					continue j_loop;
			if (!(b.intersect(p[j]) instanceof Empty))
				q[i++] = p[j];
		}
		if (i == 0)
			return (Empty.instance());
		else if (i == 1)
			return (q[0]);
		else
			return (new Segment(q[0], q[1]));
	}
	/*
	 * The intersection of two segments usually represented by ScaleNumber.
	 * NOTICE: For internal use only
	 * If CohoDouble is used, interval or apr is returned
	 * If CohoAPR is used, exact value returned with more running time.
	 * If CohoDoubleInterval is used, interval representation returned, but may throw ArithmeticException.
	 * It's not recommended to use hybrid representation. If double and interval is used, error might be introduced.
	 */
	public GeomObj2 intersectSegment(Segment that){
		if( (type==CohoDouble.type || that.type==CohoDouble.type) && (type instanceof ScaleType && that.type instanceof ScaleType) ){
			try{
				GeomObj2 obj = this.specifyType(DoubleInterval.type).baseIntersectSegment(that.specifyType(DoubleInterval.type));
				//System.out.println("here"+obj);
				if(obj.maxError()> eps)
					throw new ArithmeticException("too large interval");
				return obj;
			}catch(ArithmeticException e){
				//CohoAPR.printDouble = false;
//				System.out.println("this "+this.specifyType(CohoAPR.type));
//				System.out.println("that "+that.specifyType(CohoAPR.type));
				return this.specifyType(CohoAPR.type).baseIntersectSegment(that.specifyType(CohoAPR.type));
			}
		}else{//NOTICE: don't use double and interval. the result may contain error. 
			return baseIntersectSegment(that);
		}
	}	
	private GeomObj2 baseIntersectSegment(Segment that){
		if(isPoint() && that.isPoint()){// point & point
			return (p[0].compareTo(that.p[0])==0) ? p[0] : Empty.instance();
		}else if(that.isPoint()){// segment & point
			return baseIntersectPoint(that.p[0]);
		}else if(isPoint()){// point & segment
			return that.baseIntersectPoint(p[0]);
		}
		// segment & segment
		int cmp = bbox().compareTo(that.bbox());
		if(cmp >0)
			return that.baseIntersectSegment(this);
		else if(cmp==0){
			//FIXED: this is not true. If seg1.box==seg2.box, we can not conclude seg1 and seg2 on the same line.
			//return this;
			cmp = left().compareTo(that.left());//don't consider the direction of segment
			if(cmp>0)
				return that.baseIntersectSegment(this);
			else if(cmp==0)
				return this;//identical segment
		}
		//this < that
		GeomObj2 ibox = bbox().intersect(that.bbox());
		if(ibox instanceof Empty){
			return Empty.instance();
		}
		//It's the same with the line intersection
		CohoNumber A1 = A(), A2 = that.A();
		CohoNumber B1 = B(), B2 = that.B();
		CohoNumber C1 = C(), C2 = that.C();
		CohoNumber denom = A1.mult(B2).sub(A2.mult(B1));
		CohoNumber num1 = B1.mult(C2).sub(B2.mult(C1));
		CohoNumber num2 = C1.mult(A2).sub(C2.mult(A1));
		if(denom.compareTo(denom.zero())==0){
			if(num1.compareTo(num1.zero())!=0 || num2.compareTo(num2.zero())!=0){//no intersection
				return Empty.instance();
			}else{//intersection is segment
				Point p0 = left().max(that.left());
				Point p1 = right().min(that.right());
				if(p0.equals(p1))//FIXED: a point here
					return p0;
				else
					return new Segment(p0,p1);//NOTE: left->right order
			}			
		}else{
			Point result = Point.create(num1.div(denom),num2.div(denom));
			//NOTE: here result is the intersection of two lines. 
			//If it is not on the segment, it should be out of the bounding box. 
			//The previous method test if the point on the segment. Slower.
			//FIXED: if it is the intersection of segments, it should be contained in both bbox(), Test only one is not enougth
			if(bbox().contains(result) && that.bbox().contains(result)){
				//System.out.println("seg:"+this+" contains "+result);
				return result;				
			}else
				return Empty.instance();			
		}
	}
	
	protected GeomObj2 intersectPoint(Point that){
		if( (type==CohoDouble.type || that.type()==CohoDouble.type) && (type instanceof ScaleType && that.type() instanceof ScaleType) ){
			try{
				GeomObj2 obj = this.specifyType(DoubleInterval.type).baseIntersectPoint(that.specifyType(DoubleInterval.type));
				if(obj.maxError()> eps)
					throw new ArithmeticException("too large interval");
				return obj;
			}catch(ArithmeticException e){
				return this.specifyType(CohoAPR.type).baseIntersectPoint(that.specifyType(CohoAPR.type));
			}
		}else{//NOTICE: don't use double and interval. the result may contain error. 
			return baseIntersectPoint(that);
		}
	}
	//FIXED: The formula tells if the point is on the line. Add bounding box test
	private GeomObj2 baseIntersectPoint(Point pt){
		if(!bbox().contains(pt))
			return Empty.instance();
		CohoNumber A = A(), B=B(), C=C();
		CohoNumber x = pt.x(), y = pt.y();
		CohoNumber eq = A.mult(x).add(B.mult(y)).add(C);
		if( eq.compareTo(eq.zero())==0) 
			return pt;
		else
			return Empty.instance();
	}
	
	/*
	 * Detect if two segments are parallel or not.
	 * The segment should be represented by ScaleNumber. Otherwise, it may throw exception when the result is ambiguous.
	 * If it's represented by double, use interval first then use apr. 
	 */
	/*
	 * Detect if two segments are parallel or not.
	 * The segment should be represented by ScaleNumber. Otherwise, it may throw exception when the result is ambiguous.
	 * If it's represented by double, use interval first then use apr. 
	 */
	public boolean isParallelTo(Segment that){
		if( (type==CohoDouble.type || that.type==CohoDouble.type) && (type instanceof ScaleType && that.type instanceof ScaleType) ){
			try{
				return this.specifyType(DoubleInterval.type).baseIsParallelTo(that.specifyType(DoubleInterval.type));
			}catch(ArithmeticException e){
				return this.specifyType(CohoAPR.type).baseIsParallelTo(that.specifyType(CohoAPR.type));
			}
		}else{ 
			return baseIsParallelTo(that);
		}
	}
	private boolean baseIsParallelTo(Segment that){
		CohoNumber A1 = A(), B1 = B();
		CohoNumber A2 = that.A(), B2 = that.B();
		CohoNumber del = A1.mult(B2).sub(A2.mult(B1));
		return del.compareTo(del.zero())==0;
	}
	
	public boolean contains(GeomObj2 g){
		if(g instanceof Point){
			if(baseIntersectPoint((Point)g) instanceof Point)
				return true;
		}else if(g instanceof Segment){
			if(baseIntersectSegment((Segment)g).equals(g))
				return true;
		}
		return false;
	}
	public GeomObj2 union(GeomObj2 g ){
		throw new UnsupportedOperationException("Doesn't support it now");
	}

	/*
	 * NOTICE: Doesn't consider the direction for compareTo
	 * return exact value for ScaleType
	 * exception might thrown for intervalType.
	 */
	public int compareTo(Segment s){
		int result = left().compareTo(s.left());
		if(result ==0 ){
			result = right().compareTo(s.right());
		}
		return result;
	}
	@Override
	public boolean equals(Object s){
		try{
			return (s instanceof Segment) && (compareTo((Segment)s)==0);
		}catch(ArithmeticException e){
			return false;
		}
	}
	@Override
	public int hashCode(){//the same hashCode for segment and bounding box
		return p[0].hashCode()^(p[1].hashCode()>>16);
	}
	/*
	 * NOTICE: compared by the slope of the segment.
	 */
	public int compareBySlope(Segment that){
		if( (type==CohoDouble.type || that.type==CohoDouble.type) && (type instanceof ScaleType && that.type instanceof ScaleType) ){
			try{
				return this.specifyType(DoubleInterval.type).baseCompareBySlope(that.specifyType(DoubleInterval.type));
			}catch(ArithmeticException e){
				return this.specifyType(CohoAPR.type).baseCompareBySlope(that.specifyType(CohoAPR.type));
			}
		}else{ 
			return baseCompareBySlope(that);
		}
	}
	
	//compare by direction. The segment is from left to right. 
	private int baseCompareBySlope(Segment s){
		if(isPoint() && s.isPoint()){
			return 0;
		}else if(isPoint()){
			return -1;
		}else if(s.isPoint()){
			return 1;
		}
		Segment s0 = this, s1 = s;
		CohoNumber dy0 = s0.right().y().sub(s0.left().y());
		CohoNumber dy1 = s1.right().y().sub(s1.left().y());
		int cmp0 = dy0.compareTo(dy0.zero());
		int cmp1 = dy1.compareTo(dy1.zero());
		int cord0 = 0, cord1 = 0;
		if(cmp0<0)
			cord0 = 1;
		if(cmp1<0)
			cord1 = 1;
		if(cord0!=cord1)
			return -(cord0-cord1);//NOTICE: segment in second cord is less than the one in first cord
		
		//same quad
		//special case
		CohoNumber dx0 = s0.right().x().sub(s0.left().x());//FIXED: dx0 is alway non-negative
		CohoNumber dx1 = s1.right().x().sub(s1.left().x());
		cmp0 = dx0.compareTo(dx0.zero());
		cmp1 = dx1.compareTo(dx1.zero());
		if(cmp0==0 && cmp1==0){//pi/2, dy0, dy1 must be positive
			return 0;
		}else if(cmp0==0){//dy0 must be positive
			return 1;
		}else if(cmp1==0){//dy1 must be positive
			return -1;		
		}
		//by slope
		CohoNumber tan0 = dy0.div(dx0);
		CohoNumber tan1 = dy1.div(dx1);
		return tan0.compareTo(tan1);		
	}
	/*
	 * NOTICE: consider the direction of segement
	 * Sorted by the angle of p0->p1
	 * Point is less than any segment
	 * If input is ScaleType, return the exact value. 
	 * If input is interval, exception may be thrown.
	 */
	public int compareByAngle(Segment that){
		if( (type==CohoDouble.type || that.type==CohoDouble.type) && (type instanceof ScaleType && that.type instanceof ScaleType) ){
			try{
				return this.specifyType(DoubleInterval.type).baseCompareByAngle(that.specifyType(DoubleInterval.type));
			}catch(ArithmeticException e){
				return this.specifyType(CohoAPR.type).baseCompareByAngle(that.specifyType(CohoAPR.type));
			}
		}else{ 
			return baseCompareByAngle(that);
		}
	}
	private int baseCompareByAngle(Segment s){
		if(isPoint()&&s.isPoint()){//point & point
			return 0;
		}else if(isPoint()){//point & segment
			return -1;
		}else if(s.isPoint()){//segment & point
			return 1;
		}
		//segment & segment
		Segment s0 = this, s1 = s;		
		CohoNumber dx0 = s0.p[1].x().sub(s0.p[0].x());//FIXED not use left and right
		CohoNumber dy0 = s0.p[1].y().sub(s0.p[0].y());
		
		CohoNumber dx1 = s1.p[1].x().sub(s1.p[0].x());
		CohoNumber dy1 = s1.p[1].y().sub(s1.p[0].y());
		
		int cord0=0;
		int cmpx = dx0.compareTo(dx0.zero());
		int cmpy = dy0.compareTo(dx0.zero());
		if(cmpx>=0 && cmpy>=0){//pi/2 here
			cord0=0;
		}else if(cmpx<0 && cmpy>=0){
			cord0=1;
		}else if(cmpx<=0 && cmpy<0){//FIXED: special case 3*pi/2 here
			cord0=2;
		}else{//cmpx=0 cmpy<0. 
			cord0=3;
		}

		int cord1=0;
		cmpx = dx1.compareTo(dx1.zero());
		cmpy = dy1.compareTo(dx1.zero());		
		if(cmpx>=0 && cmpy>=0){
			cord1=0;
		}else if(cmpx<0 && cmpy>=0){
			cord1=1;
		}else if(cmpx<=0 && cmpy<0){
			cord1=2;
		}else{//cmpx>0 cmpy<0
			cord1=3;
		}
		
		if(cord0!=cord1)
			return Integer.signum(cord0-cord1);
		
		//same quad
		//FIXED: special case. pi/2 and 3*pi/2
		if(dx0.equals(dx0.zero()) && dx1.equals(dx1.zero())){
			return 0;//because same cord
		}else if(dx0.equals(dx0.zero())){
			return 1;//consider cord1=0 and cord=2;
		}else if(dx1.equals(dx1.zero())){
			return -1;
		}
		
		//dx0 ~= 0, dx1 ~= 0
		CohoNumber tan0 = dy0.div(dx0);
		CohoNumber tan1 = dy1.div(dx1);
		return tan0.compareTo(tan1);
	}
	//compare by the x value of endpoins, no direction considered
	//return exact value for scaleType, exception might be thrown for interval type
	//CONSIDER: move to segemnt?
	public int compareByRightX(Segment that){
		int cmp = right().x().compareTo(that.right().x());
		if(cmp!=0)
			return cmp;
		return right().y().compareTo(that.right().y());
	}

	public Segment specifyType(CohoType type){
		return new Segment(p[0].specifyType(type),p[1].specifyType(type));
	}
	public double maxError(){
		return Math.max(p[0].maxError(), p[1].maxError());
	}
	public String toString(){
		return "[" + p[0] +", "+p[1]+"]";
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//test intersect segement method
		Segment seg1 = Segment.create(new Point(0.4494169588187707, 1.7829963763766523), new Point(0.44941695881877064, 1.8013332866549514));
		Segment seg2 = Segment.create(new Point(0.44941695881877064, 1.7829963763766523), new Point(0.4494169588187707, 1.8013332866549514));
		System.out.println(seg1.compareTo(seg2));
		System.out.println(seg1.intersectSegment(seg2));
		System.out.println(seg2.intersectSegment(seg1));
//		Segment seg1 = Segment.create(new Point(1,1.2), new Point(0,1));
//		Segment seg2 = Segment.create(new Point(1,1), new Point(0.5,1.5));
//		System.out.println(seg1.intersect(seg2));
//		CohoNumber x = (new CohoAPR(-1292301982152799L,1125899906842624L));		
//		CohoNumber y1 = (new CohoAPR(4027404655169457L,4503599627370496L));
//		CohoNumber y2 = (new CohoAPR(3840838409L,4294967296L));
//		CohoNumber y3 = (new CohoAPR(8054809310338929L,9007199254740992L ));
//		Segment s1 = new Segment(new Point(x,y1),new Point(x,y2));
//		Segment s2 = new Segment(new Point(x,y3),new Point(x,y1));
//		System.out.println(s1.isParallelTo(s2));
//		System.out.println(s2.isParallelTo(s1));
//		System.out.println(s1.intersect(s2).toString());
//		System.out.println(s2.intersect(s1).toString());
//		System.out.println(s1.len());
//		seg1 = Segment.create(Point.create(1.7910370337818124, -0.0067491186681235635),Point.create(1.7957607796647022, -0.006745807232175642));
//		seg2 = Segment.create(Point.create(1.8004845255475916, -0.00674249579622772),Point.create(1.8024234269756791, -0.006741136589350886));
//		//System.out.println(seg1.specifyType(CohoAPR.type).line().intersect(seg2.specifyType(CohoAPR.type).line()).specifyType(CohoDouble.type));
//		System.out.println(seg1.line().intersect(seg2.line()));
	}

}
