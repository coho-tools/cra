package coho.geom.twodim.ops;

import java.util.*;
import coho.common.number.*;
import coho.geom.twodim.*;

class IntersectSimplePolygon {
	private ArrayList<IntersectSegment[]> hullEdges = new ArrayList<IntersectSegment[]>();
	private IntersectPoint left;//, right;//the left and right most point
	private BoundingBox bbox;//bounding box for this polgyon
	public IntersectPoint left(){
		return left;
	}
//	public IntersectPoint right(){
//		return right;
//	}
	public BoundingBox bbox(){
		return bbox;
	}
	public int hulls(){
		return hullEdges.size();
	}
	public IntersectSegment[] hull(int i){
		return hullEdges.get(i);
	}
	public IntersectSimplePolygon(ConvexPolygon p){
		this.bbox = p.bbox();
		int n = p.degree();
		
		//compute upper/lower hull;
		int ll=0, ur=0; //left lower, right upper point 
		for(int i=1; i<n; i++){
			if(p.point(i).compareTo(p.point(ll))<0){
				ll = i;
			}
			if(p.point(i).compareTo(p.point(ur))>0){
				ur = i;
			}
		}
		//break into lower and upper half. 
		//Because polygon is anti-clock-wise, lower is ll->ur
        //FIXED: don't use fwd and bwd. Assume polygon is anti-clock-wise. 
		//If support both, use direction of edges to tell which is upper/lower
		int lowerLength = (ur-ll)+1;  
		int upperLength = (n+ll-ur)+1;
		if(ur<ll){
			lowerLength = (n+ur-ll)+1;
			upperLength = (ll-ur)+1;
		}
		Point[] lowerPoints = new Point[lowerLength];
		Point[] upperPoints = new Point[upperLength];
		for(int i=0; i<lowerLength; i++){
			lowerPoints[i] = p.point((ll+i)%n);//fwd
		}
		for(int i=0; i<upperLength; i++){
			upperPoints[i] = p.point((ll-i+n)%n);//bwd
		}
		
		//Create IntersectSegment for each edge. 
		IntersectSegment[] lowerSegs = new IntersectSegment[lowerPoints.length-1];
		for(int i=0; i<lowerSegs.length;i++){
			lowerSegs[i] = new IntersectSegment(Segment.create(lowerPoints[i],lowerPoints[i+1]));//anti-clock wise order
		}
		IntersectSegment[] upperSegs = new IntersectSegment[upperPoints.length-1];
		for(int i=0; i<upperSegs.length; i++){
			upperSegs[i] = new IntersectSegment(Segment.create(upperPoints[i+1], upperPoints[i]));//anti-clock wise order
		}
		
		//Create IntersectPoint for each vertex. Store it in IntersectSegment
		IntersectSegment pre = upperSegs[0];
		IntersectSegment pos = lowerSegs[0];
		left = new IntersectPoint(lowerPoints[0],pre,pos,true);//exact value
		IntersectPoint pi = left;
		pre.add(pi);
		pos.add(pi);
		
		for(int i=1; i<lowerSegs.length;i++){//lower hull
			pre = lowerSegs[i-1];
			pos = lowerSegs[i];
			pi = new IntersectPoint(lowerPoints[i], pre,pos, true);
			pre.add(pi);
			pos.add(pi);
		}
		
		pre = lowerSegs[lowerSegs.length-1];
		pos = upperSegs[upperSegs.length-1];
		IntersectPoint right = new IntersectPoint(lowerPoints[lowerPoints.length-1],pre,pos,true);
		pi = right; 
		pre.add(pi);
		pos.add(pi);
		
		for(int i=1; i<upperSegs.length;i++){// upper hull
			pre = upperSegs[i-1];
			pos = upperSegs[i];
			pi = new IntersectPoint(upperPoints[i], pre,pos, true);
			pre.add(pi);
			pos.add(pi);			
		}		
		hullEdges.add(lowerSegs);
		hullEdges.add(upperSegs);
	}
	
	public IntersectSimplePolygon(SimplePolygon p){
		this.bbox = p.bbox();
		int n = p.degree();
		
		//break polygon into ascending hulls
		//int ll=0, ur=0; //left lower, right upper point
		int ll = 0;
		for(int i=1; i<n; i++){
			if(p.point(i).compareTo(p.point(ll))<0){
				ll = i;
			}
		}

		//split into segments
		Point currPt = p.point(ll);
		Point nextPt = currPt;
		IntersectSegment currEdge = null;
		IntersectSegment preEdge = null;
		IntersectSegment firstEdge = null;
		ArrayList<IntersectSegment> seg = new ArrayList<IntersectSegment>();
		boolean inc=true;
		for(int i=0; i<n; i++){
			currPt = nextPt;
			nextPt = p.point((ll+i+1)%n);
			preEdge = currEdge;
			currEdge = new IntersectSegment(Segment.create(currPt, nextPt));			
			
			int cmp = nextPt.compareTo(currPt);
			if(cmp>0!=inc){//oder Changed
				hullEdges.add(seg.toArray(new IntersectSegment[seg.size()]));
				inc = !inc;
				seg.clear();
			}
			seg.add(inc?seg.size():0, currEdge);
			if(i==n-1){
				hullEdges.add(seg.toArray(new IntersectSegment[seg.size()]));
			}


			//IntersectPoint
			if(preEdge==null){
				firstEdge = currEdge;
			}else{
				IntersectPoint currIntersectPt = new IntersectPoint(currPt,preEdge,currEdge,true);
				preEdge.add(currIntersectPt);
				currEdge.add(currIntersectPt);
			}
			if(i==n-1){// ll again
				left = new IntersectPoint(nextPt,currEdge,firstEdge, true);
				currEdge.add(left);
				firstEdge.add(left);
			}
		}
	}
	
	//Compute the intersection of this and p. Store the intersection point on IntersectSegment
	public void intersect(IntersectSimplePolygon p){
		//TODO: Consider bounding box?
		for(int i=0; i<hulls(); i++){
			for(int j=0; j<p.hulls(); j++){
				intersectHull(hull(i),p.hull(j));
			}
		}
	}
	
	//compute the intersection of hull1 and hull2.
	private static void intersectHull(IntersectSegment[] hull1, IntersectSegment[] hull2){
		IntersectSegment s1 = hull1[0], s2 = hull2[0];
		for(int i1=0, i2=0; i1<hull1.length&&i2<hull2.length; ){
			GeomObj2 obj = s1.intersectSegment(s2);
			//System.out.println(s1+" intersect "+s2+" is "+obj);
			if(obj instanceof Point){
				Point point = (Point)obj;
				//NOTE: if point is appeare before, create method will merge them, s1 and s1 doesn't add an copy because of Set
				IntersectPoint p = new IntersectPoint(point, s1, s2, point.type()==CohoAPR.type);
				s1.add(p);
				s2.add(p);
//				System.out.println("add "+p+" to "+s1+" "+s2 );
			}else if(obj instanceof Segment){
				//ASSERT must be apr result. Test
				Segment seg = (Segment)obj;
				IntersectPoint p = new IntersectPoint(seg.left(), s1, s2, true);
				s1.add(p);
				s2.add(p);
				p = new IntersectPoint(seg.right(), s1, s2, true);
				s1.add(p);
				s2.add(p);
			}
			//empty move forward
			int cmp = s1.compareByRightX(s2);
			if(cmp>0){
				//advance s2
				i2++;
				if(i2==hull2.length)
					break;
				s2 = hull2[i2];
				continue;
			}else if(cmp<0){//If equal, doesn't matter
				//advance s1
				i1++;
				if(i1==hull1.length)
					break;
				s1 = hull1[i1];
				continue;				
			}else{//FIXED: make sure intersection point is added to all segments
				i1++; i2++;
				if(i1!=hull1.length){
					IntersectPoint p = new IntersectPoint(s1.right(),hull1[i1],s2,true);
					hull1[i1].add(p);
					s2.add(p);
				}
				if(i2!=hull2.length){
					IntersectPoint p = new IntersectPoint(s1.right(),s1,hull2[i2],true);
					s1.add(p);
					hull2[i2].add(p);					
				}
				if(i1==hull1.length||i2==hull2.length)//end
					break;
				s1 = hull1[i1];
				s2 = hull2[i2];
				continue;
			}
		}		
		//System.out.println("here");
	}	
	public String toString(){
		return bbox().toString();
	}
}
