package coho.lp.project;

import java.util.*;
import coho.common.matrix.*;
import coho.common.number.*;
import coho.lp.*;
import coho.lp.solver.*;
import coho.geom.twodim.*;
import coho.debug.*;

public class DoubleIntervalLPProject extends LPProject {

	DoubleIntervalLPProject(LP _lp, Matrix _x, Matrix _y) {
		super(_lp, _x, _y, CohoSolverFactory.Solver.DOUBLEINTERVAL);
	}
	
	private DoubleInterval nextNorm(DoubleIntervalCohoMatrix A_trans_base, Matrix x, Matrix y,double currNorm){
		try{
			DoubleIntervalMatrix pi = A_trans_base.getSolution(x);
			DoubleIntervalMatrix eta = A_trans_base.getSolution(y); 
			
			DoubleInterval newNorm = DoubleInterval.create(Double.MAX_VALUE);
			boolean nextQuad = true;
			double minNextNorm = currNorm+planeEps*Math.max(1,currNorm);//sin(planeEps)*sqrt{1+currNorm^2}
			for(int i=0;i<pi.length();i++){
				DoubleInterval pi_i = pi.V(i);
				DoubleInterval eta_i = eta.V(i);
				if(eta_i.less(0.0)){
					DoubleInterval Norm_i = pi_i.div(eta_i.negate());
					double norm_i = Math.max(0,Norm_i.x().doubleValue());//make sure it's non-negative. For the special case (1,0).
					//DOC What if Norm_i has greater interval? We use x() value for alpha.
					if(norm_i >= minNextNorm && norm_i < newNorm.x().doubleValue()){
						nextQuad = false;
						double e = Norm_i.hi().doubleValue()-norm_i;
						newNorm = DoubleInterval.create(norm_i-e,norm_i+e);//ususally it's Norm_i.
					}
				}
			}
			if(nextQuad||newNorm.x().doubleValue()>=1/planeEps)//if the angle is small then eps, turn to next quad.
				return null;
			return newNorm;//else
		}catch(SingularMatrixException e){
			throw new LPError("LPproject.nextNorm(): This should never happen when solve the optimal point.");
		}
	}	
	public static final double planeEps = coho.geom.twodim.GeomObj2.eps;//smallest angle between two plane
	public static final double minLength = 1e-6;//to test next quad. should be relative?
	public static final double maxInc = Math.tan(Math.PI/2-planeEps);//the max value of alpha, otherwise, it should jump to next quad.
	protected ArrayList<Line> findPlanes(Matrix A,Matrix b,Matrix x_axis,Matrix y_axis){
		try{
			// Initialization
			Matrix x = x_axis;
			Matrix y = y_axis;
			ArrayList<Line> hullAsPlanes = new ArrayList<Line>();
			LP cohoLp = LP.createCoho(new Constraint(A, b));
			CohoSolver lpSolver = CohoSolverFactory.getSolver(cohoLp, solver);
			CohoSolverResult result = lpSolver.opt(x);//the first costVector is the x axis
			LPBasis B = result.optBasis();
			//if(DEBUG.debug) DEBUG.println(LPproject_DEBUG,"LPproject.findPlanes(): The inital basis: "+B.toString());
			boolean testedX = false; //for (1,0), the initial direction
			DoubleInterval currNorm = null;
			int jumpQuads = 0;
			// Loop
			LOOP: for (int quad = 0; quad<4||(quad==4&&!testedX) ; quad++) {//for each quad
				//if(DEBUG.debug) DEBUG.println(LPproject_DEBUG,"LPproject.findPlanes(): Beging the "+quad+"th quadrant");
				if(quad!=0){
					Matrix swap = x;
					x = y;
					y = swap.negate();
				}
				double alpha= -1;//we want to find cost vector like (1,0)
				int backtrackTimes=0;
				QUAD: while (true) {//status is defined as (alpha, B).
					/*************************************
					 * find the norm, and add the new edge
					 *************************************/
					DoubleIntervalCohoMatrix A_trans_base = DoubleIntervalCohoMatrix.create(A.transpose().col(B.basis()),true);
					DoubleInterval nextNorm = nextNorm(A_trans_base,x,y,alpha);
					
					if(nextNorm!=null){//next edge in the same quad
						backtrackTimes=0;//reset backtrack time
						currNorm = nextNorm;
						alpha = currNorm.x().doubleValue();
						boolean initial = quad==0&&alpha<planeEps;//(1,0) is added at the beginning, so just test four quad
						if(initial)
							testedX = true;
						//if (1,0) is a norm and not added at the beginnig, we will find it again and add it here. 
						//It's possible this is skipped, but at that case, the area increase is small because the adjacent norm is close enough.
						if (quad==4) {//(1,0) is considered here if not added at the beginning
							testedX = true;
							if(alpha>planeEps)
								break;
						}
						
						//add a new hyperplane
						result = lpSolver.opt((x.add(y.mult(alpha))));//direction = x+y*alpha
						Line p = new Line(((DoubleInterval)result.optCost()).lo().doubleValue(),quad,alpha);//over-approx
						hullAsPlanes.add(p);	
						//if(DEBUG.debug) DEBUG.println(LPproject_DEBUG,"LPproject.findPlanes(): A new face found: "+p.toString()+" with basis "+B);
						
						/****************************
						 * update basis(vertex), move to right endpoint of current point, left endpoint of next edge.
						 ****************************/	
						//IF it's the initial edge, try one step at most. Otherwise, 5 times at most
						//IF we jump two quads, try one step at least, to avoid the special case at the left endpoint.
						result = rightEnd(alpha, currNorm.e().doubleValue(),B,result,lpSolver,A,x,y,0,(jumpQuads==2?1:0),(initial?1:5));
						LPBasis newB = result.optBasis();
						/************************************
						 * Special case for jump two quads. 
						 * 1)doesn't reach the right endpoint, use the left end point instead
						 * 2)the basis jump two quads
						 * We will add constraints and remove it later.
						 ***********************************/
						if(jumpQuads==2){
							DoubleIntervalMatrix point = (DoubleIntervalCohoMatrix.create(A.row(B.basis()),false)).getSolution(b.V(B.basis()));								
							DoubleIntervalMatrix newPoint = (DoubleIntervalCohoMatrix.create(A.row(newB.basis()),false)).getSolution(b.V(newB.basis()));
							double length =newPoint.sub(point).norm().doubleValue();
							if(length<minLength){//danger but rarely
								//1)may be the edge is really short or 2) danger case that is the left endpoint
								//TODO: what to do if caused by case 1)?
								//find the constraint								
								DoubleInterval cost = (DoubleInterval)lpSolver.opt((x.sub(y))).optCost();//a norm in the middle court
								double newCost = cost.hi().doubleValue()+minLength;
								//add the constraint and restart
								Matrix newA = A.convert(A.nrows()+1,A.ncols());
								newA.assign(A,0,0);
								newA.assign(x.sub(y).transpose(),A.nrows(),0);//Bug fixed: x-y rather than y-x
								Matrix newb = A.convert(b.length()+1,1);
								newb.assign(b,0,0);
								newb.assign(newCost,b.length());
								if(DEBUG.debug) DEBUG.println("LPproject.findPlanes(): Insert an constraint "+x.sub(y)+""+newCost);
								hullAsPlanes = findPlanes(newA,newb,x_axis,y_axis);
								//remove the edge from the result and return
								Line delP = new Line(newCost,quad-1,1);
								for(int i=0;i<hullAsPlanes.size();i++){
									Line curr = hullAsPlanes.get(i);
									if(delP.similar(curr)){
										hullAsPlanes.remove(curr);
										if(DEBUG.debug) DEBUG.println("LPproject.findPlanes(): Remove the constraint: "+curr.toString());
										break;
									}
								}
								return hullAsPlanes;
							}
						}
						B = newB;
						jumpQuads = 0;
					}else{//next edge in the next quad. Backtrack once for efficient.
						//if(DEBUG.debug) DEBUG.println(LPproject_DEBUG,"LPproject.findPlanes(): nextNorm() return 'go to next quad', recheck the decision it made");
						// backtrack till find the minimum angle.						
						LPBasis newB = lpSolver.opt((x.add(y.mult(maxInc)))).optBasis();//direction=x+y*tan(maxAngle)
						if(!newB.equals(B)){
							DoubleIntervalMatrix point = (DoubleIntervalCohoMatrix.create(A.row(B.basis()),false)).getSolution(b.V(B.basis()));								
							DoubleIntervalMatrix newPoint = (DoubleIntervalCohoMatrix.create(A.row(newB.basis()),false)).getSolution(b.V(newB.basis()));
							double length =newPoint.sub(point).norm().doubleValue(); 
							//we may skip edge shorter than minLength
							if(length>minLength){//should not jump to next quad
								//if(DEBUG.debug) DEBUG.println(LPproject_DEBUG,"LPproject.findPlanes(): There is a new basis "+newB.toString()+" in the same quadrant.");
								Iterator<LPBasis> iter = result.optBasesIter();
								try{
									while(!B.equals(iter.next())){
									}//until we find the current basis
									if(iter.hasNext()){//if contains the right endpoint
										//backtrackTimes=0;//reset
										B = iter.next();//use the next basis										
										//if(DEBUG.debug) DEBUG.println(LPproject_DEBUG,"LPproject.findPlanes(): Let's try another possible optimal basis "+B.toString());
									}else{//force to the right endpoint
										//XXX: here, we don't update alpha now because we update backtrackTimes
										//result = rightEnd(alpha,currNorm.E(),B,result,solver,A,x,y,false,true,8*backtrackTimes);//last para can be true or false. result.bestOptBasis==B
										//at least once, at most 5 times.
										result = rightEnd(alpha,currNorm.e().doubleValue(),B,result,lpSolver,A,x,y,8*backtrackTimes,1,5);//last para can be true or false. result.bestOptBasis==B
										B = result.optBasis();
										backtrackTimes++;
										//if(DEBUG.debug) DEBUG.println(LPproject_DEBUG,"LPproject.findPlanes(): Let's try an new basis "+B.toString());
									}						
								}catch(NoSuchElementException e){
									throw new RuntimeException("Critical Error: The algorithm is incorrect. We can't find the right end point basis");
								}
								continue QUAD;
							}
							//backtrackTime=0; goto loop
						}
						//if(DEBUG.debug) DEBUG.println(LPproject_DEBUG,"LPproject.findPlanes(): we should go to the next quad");
						jumpQuads ++;
						continue LOOP;
					}
					
				} /* end while loop */
			} /* end quadrant for loop */
			return hullAsPlanes;
		}catch(SingularMatrixException e){
			throw new LPError("LPproject.findPlanes(): This should never happen when solve the optimal point.");
		}

	}
	/**
	 * commented out for LPproject 
	 */
	//find the right end point
	//result maybe changed accordingly.
	//turn the direction at least minTimes and at most maxTimes from the beginTime. Unless it's grater than maxInc.
	//max should be greater than minTimes. If maxTimes less than 1, then no limit. 
	private CohoSolverResult rightEnd(double alpha, double delta,
			LPBasis leftB, CohoSolverResult result, CohoSolver solver,
			Matrix A_C,Matrix x, Matrix y, int beginTime, int minTimes,int maxTimes){
			LPBasis rightB = result.optBasis();
			double stepTurn = Math.max(delta+planeEps*planeEps*Math.max(1,alpha),planeEps*Math.max(1,alpha));
			for(int times = beginTime; (times-beginTime<minTimes)||rightB.equals(leftB)||similarBasis(A_C.row(leftB.basis()),A_C.row(rightB.basis()));times++){
				//if times+1>3=31, overflow
				double tmp = times<30?(1<<(times+1)):Math.pow(2.0,times+1);
				double increase = stepTurn * (tmp-1);
				double dir = alpha + increase;
				if(dir>maxInc){//For backtrack, not too large, infinity problems
					dir = maxInc;
				}
				Matrix direction = (x.add(y.mult(dir)));
				result = solver.opt(direction);
				rightB = result.optBasis();
				if(((maxTimes>0)&&(times-beginTime>=maxTimes))||(dir>=maxInc)){//This is used to ensure not to fall into a loop forever. But it should never used.
					//if(DEBUG.debug) DEBUG.println(LPproject_DEBUG,"Too many times tried, return the best result till now, which might be the same with the leftB. Cause to jump to next quad.");
					break;
				}
			}			
			return result;
	}

	//a.size == b.size = n*n
	// TODO: rotate is not enough. By matching
	// we should compare each pertumation
	private boolean similarBasis(Matrix a, Matrix b){
		int n = a.nrows();
		//normalize each row.
		for(int row=0;row<n;row++){
			Matrix a_norm = (a.row(row)).div(a.row(row).norm());
			a.assign(a_norm,row,0);
			Matrix b_norm = (b.row(row)).div(b.row(row).norm());
			b.assign(b_norm,row,0);			
		}
		boolean[] matched = new boolean[n];
		for(int arow=0;arow<n;arow++){
			Matrix a_row = a.row(arow);
			double min = Double.MAX_VALUE;
			int minRow = 0;
			for(int brow=0;brow<n;brow++){
				if(matched[brow])//matched with other row of a before
					continue;
				Matrix b_row = b.row(brow);
				double diff = a_row.sub(b_row).norm().doubleValue();
				if(diff<min){
					min = diff;
					minRow = brow;
				}
			}
			// for left endpoint and right endpoint, there is at least one pair of edges 
			// that the angle is greater than 2*planeEps, thus the difference of norms are at least 2*sin(planeEps) = 2*planeEps.
			if(min>2*planeEps){
				return false;
			}
			matched[minRow]=true;
		}
		return true;
	}
		
	public static void main(String[] args){
//		System.out.println(Math.pow(2,10));
//		System.out.println(1<<10);
//		double[][] d = {{1.0166491285238044, -0.0036094790232671235},
//				{1.0169025713017488, -0.04361072085401306},
//				{1.2172225416983482, -0.042341519012982505},
//				{1.2169690989204036, -0.0023402771822365843},
//				{1.2169690989204016, -0.0023402771822365843}};
//		final Point[] pp = new Point[d.length];
//		for(int i=0;i<d.length;i++)
//			pp[i]=new Point(d[i][0],d[i][1]);
//		Enumeration e = new Enumeration<Segment>(){
//			private int i;
//			public boolean hasMoreElements() { return(i < pp.length);}
//			public Segment nextElement(){
//				i++;
//				return new Segment(pp[i%pp.length],pp[(i+1)%pp.length]);
//			}
//		}; 
//		Polygon poly = (new Polygon(e)).convexHull();
//		PolygonVT p = new PolygonVT(poly);
//		Iterator<Point> itr = p.hullVertices();//vertices and hullVertices?
//		while(itr.hasNext()){
//			System.out.println(itr.next());			
//		}
		
		double[][] ne = {{-1,0,0},{1,0,0},{0,-1,0},{0,1,0},{0,0,-1},{0,0,1}};
		double[] b={-1,0,-1,0,-1,0};
		DoubleMatrix A = DoubleMatrix.create(ne);
		DoubleMatrix B = DoubleMatrix.create(b);
		Constraint neq = new Constraint(A,B);
		//double[][] bw={{1,2,0},{1,1,0},{1,1,1}};
		//double[][] fw={{-1,2,0},{1,-1,0},{0,-1,1}};
		double[][] bw={{2,0,0},{0,2,0},{0,0,2}};
		double[][] fw={{0.5,0,0},{0,0.5,0},{0,0,0.5}};
		DoubleMatrix bwd = DoubleMatrix.create(bw);
		DoubleMatrix fwd = DoubleMatrix.create(fw);
		LP problem = LP.createCoho(neq,null,fwd,bwd);
		DoubleMatrix x = DoubleMatrix.create(3,1).zeros().assign(1,0);
		DoubleMatrix y = DoubleMatrix.create(3,1).zeros().assign(1,1);
		LPProject prj = ProjectFactory.getProject(problem,x,y, CohoSolverFactory.Solver.DOUBLEINTERVAL);
		Matrix hull = prj.hull();
		System.out.println(hull.toString());			
	}
}
