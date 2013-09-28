package coho.lp.project;

import java.util.*;

import coho.common.matrix.*;
import coho.common.number.*;
import coho.lp.*;
import coho.lp.solver.*;
import coho.geom.twodim.*;

/**
 * compute the project of a coholp to x-y plane
 * 
 * The user should use the same type of Matrix for lp x, and y. The returned result hull is 
 * of the same type. 
 * 
 * The CohoSolver used is defined by CohoSolverFactory.Solver. 
 * @author chaoyan
 * 
 * deprecated doc
 * Now, lp use Rational computation only after convert(). Because convert() use sqrt which
 * is supported by CohoRational. Therefore, we force to DoubleMatrix at convert() functions.
 * Another solution is that CohoSolver support A*bwd*x>b, we can use it then we don't need 
 * to use convert() function. Do this later.
 * 
 * And because polygonVT still use double value(do we need to change to rational? the reason 
 * for polygon is because of bug). Therefore, we now use double here for the plane.
 * 
 */
//The result is the same type of CohoNumber with x, and y.
//However, the data type used for LP solver is different, provided by the CohoSolverFactory.Solver
//With different CohoSolver, we have different LPProject.
public class LPProject {
	protected final LP lp; // the original lp, in coho form
	protected Matrix x, y; // column vectors:  basis for this projection		
	protected Matrix hull; // Each column of hull is a vertex of the projection.
	public Matrix hull(){return hull;};//TODO change matrix to polygon?
	CohoSolverFactory.Solver solver;
	
	public static boolean projectPoint = false;
	public static boolean useOneHop = true;
	
	//debug counter
//	public static int oneHopLP = 0, directLP = 0, sucessLP = 0, failLP = 0, feasibleLP = 0;
//	public static int apprFail = 0, apprAll = 0, exactAll=0;
	//public static int numLP = 0,numFail=0,numPt=0;
	//public static double t1=0.0,t2=0.0,t3=0.0,t4=0.0,t5=0.0,t6=0.0,t7=0.0;
	//public static int reduce=0, noreduce=0;

	//public static final boolean LPproject_DEBUG = true;
	//public static double errTol = 0;//TODO: to simplify the projectgon, reduce the result by errTol. Provide this interface for matlab?
	//At least, we can check if the two algorithms produce same result.
	//BUG: convert =false has problems, we should change nextNorm function. which introduce larger error.
	//public static boolean Convert = true; //convert lp with fwd or solve it directly
	// assumption:  x and y are orthonormal and LP is a coho LP
	LPProject(LP _lp, Matrix _x, Matrix _y, CohoSolverFactory.Solver _solver) {
		if(_x.ncols()!=1||_y.ncols()!=1)
			throw new LPError("x and y are required as column vectors. Use tranpose instead");
		lp = _lp;
		x = _x;
		y = _y;
		solver = _solver;
		//XXX: call grind() directly. Compare these two algorithms
		//hull = grind();
		if ((lp.fwd() == null && lp.bwd()==null) )
			hull = grind();
		else
			convert();//convert to simple project and call again.
		
	}
	//mark's algorithm. right
	//XXX: now the lp solver support the A*bwd*x>b form, why don't we solve the project directly?
	//I don't know how to deal with the case when some columns of A are all zero.
	/*
	 * Geometry explanation
	 *	Project A*bwd*x >= b along t, the norm of (x,y) plane
	 *	Now we change the coordinate: new_coord=fwd*orig_coord.
	 *	For the same point, let y be the coord in new_coord and x be in the orig_coord
	 *	We have y=bwd*x. Therefore, the polyhedron in the new_coord is A*y >= b
	 *	The norm t in the new coordinate is bwd*t.
	 *	PAY ATTENTION, bwd*t is not the norm of (bwd*x,bwd*y) unless bwd*bwd'=I. It's the norm of (fwd'*x,fwd'*y).
	 *
	 *Algebra explanation
	 *	Now, the lp for projection is 
	 *		min(c'*x)
	 *	s.t	A*bwd*x>=b
	 *	Let y=bwd*x, x=fwd*y, we have
	 *		min((fwd'c)'*y)
	 *	s.t A*y>=b
	 *	We solve it first then get x=fwd*x. The cost vector of new lp is fwd'c. 
	 *  Therefore, the new projection plane is (fwd'*x,fwd'*y).
	 *	 
	 *	Therefore, we first project Ay>=b along bwd*t, get the polygon in new_coord: result.
	 *  Then get the polygon in orig_coord = fwd*result.
	 */
	//Here, we convert the matrix to APRMatrix, because we don't want to introduce
	//too much error. Now, the polyhedra should be described by ScaleMatrix rather than interval matrix.
	//If sometimes interval based polyhedra is used, this will introduce error.
	//The only error is from the norm of x2, y2
	protected void convert(){
		LP newLp = LP.createCoho(lp.neq());
		APRMatrix fwdT = APRMatrix.create(lp.fwd().transpose());
		
		APRMatrix xAxis = APRMatrix.create(x);
		APRMatrix yAxis = APRMatrix.create(y);
		APRMatrix u = APRMatrix.create(fwdT.mult(xAxis));
		APRMatrix v = APRMatrix.create(fwdT.mult(yAxis));
		// Make u and v orthonormal using G-S
		APRMatrix x2 = u;

		x2 = x2.div(DoubleMatrix.create(x2).norm());//FIXED: sqrt not supported for all CohoNumber

		//CohoAPR numer = x2.transpose().mult(v).V(0);//FIXED: v not yAxis
		CohoAPR numer = x2.dotProd(v);
//		CohoAPR denom = x2.transpose().mult(x2).V(0);
//		APRMatrix factor = APRMatrix.create(x2.size()).fill(numer.div(denom));
		APRMatrix factor = APRMatrix.create(x2.size()).fill(numer);
		APRMatrix y2 = v.sub(x2.elMult(factor));
//		x2 = x2.div(DoubleMatrix.create(x2).norm());
		y2 = y2.div(DoubleMatrix.create(y2).norm());

//		change back to the same type of matrix with x-y;
//		Matrix xx = x.convert(x2.size()).assign(x2);
//		Matrix yy = y.convert(y2.size()).assign(y2);
//		LPProject lpp = ProjectFactory.getProject(newLp,xx,yy,solver);
		//CONSIDER: If double is used, the error introduced may introduce large error though APR Solver is used. 
		// One method is not use convert, change nextNorm. The other is to remove nearly identical plane in findPlanes() to avoid introducing large error. 
		// We can also use APR for xx and yy. This will make the result as APR. However, this does not slow down the program. 
		// Because it is only used to convert the data back in restoreVertices() function.
		LPProject lpp = ProjectFactory.getProject(newLp,x2,y2,solver);
		hull = x.convert(lpp.hull());
		hull = (lp.fwd().mult(hull));//promoted to lp?
	}

	/*
	 * NOTE: Use the point project. Because we use APR, therefore, it is not under approximation
	 * If you use the plane project. It works most time. However, if there are duplicated endpoints
	 * with different base, it will jump to next quad incorrectly, which results in large over approximation.
	 */
	protected Matrix grind(){
		/******************
		 * Trim the matrix, remove the all-zero column
		 ******************/
		Matrix A_C_full = lp.neq().a();
		Matrix b_C = lp.neq().b();
		BooleanMatrix bmEmptyCol = A_C_full.eq(A_C_full.zeros()).allInCol();
		BooleanMatrix bmNonEmptyCol =  bmEmptyCol.negate();
		// trimmed. remove the all-zero columns
		Matrix A_C = A_C_full.col(bmNonEmptyCol);
		Matrix x_axis = x.row(bmNonEmptyCol);
		Matrix y_axis = y.row(bmNonEmptyCol);

		Matrix vertices =null;
		/******************
		 * Find each points
		 *****************/
		//Use plane only when allow error
		if(projectPoint || ProjectFactory.errTol <=0 ){
			vertices  = findPoints(A_C,b_C,x_axis,y_axis);
		}
		/******************
		 * Find the norm for each edge
		 *****************/
		else{
			vertices = findByLine(A_C,b_C,x_axis,y_axis);
		}
		//to full dimension
		Matrix full = vertices.convert(x.length(),vertices.ncols()).zeros();
		for(int dim=0,i=0; dim<x.length(); dim++){
			if(bmNonEmptyCol.V(dim).booleanValue()){
				full.assign(vertices.row(i),dim,0);
				i++;
			}
		}
		return full;
	}
	
	
	/***********************
	 * New Algorithm
	 * 1)Use points, not norm
	 * 2)Compute both forward and backward normal. 
	 * 3)alpha is from -1 to 1, not 1 to infinity. But when compute forward/backward, it's all possible value.
	 * avoid the problem that backward alpha is in another quad.
	 ***********************/
	/*
	 * Find the forward normal and backward normal for the current basis.
	 * optDir is the optimal direction of current basis.
	 * A normal is represented as x+alpha*y
	 *  
	 * The result returned is [prevNorm, nextNorm], which is the max/min value that make the current basis non-optimal.
	 * prevNorm or nextNorm could be null, but not both.(?)
	 * If not null, prevNorm \in (-inf,optDir], nextNorm \in [optDir, inf)
	 * It's possible prevNorm==nextNorm when 1) the projection is a line or point 2)there are other optimal basis
	 * which has different prevNorm and nextNorm. Think about a triangular prism.
	 * 2007.12.07. it is possible prevNorm == nextNorm = null. Think of the triangular prism.
	 * 
	 */
	protected class Norm{
		CohoNumber norm;
		int evictIndex;
		public Norm(CohoNumber norm, int evictIndex){
			this.norm = norm;
			this.evictIndex = evictIndex;
		}
	}
	/*
	 * Add evict index to norm, which is used for next lp_opt 
	 */
	protected Norm[] norm(CohoMatrix A_trans_base, Matrix x, Matrix y, CohoNumber optDir){
		try{
			Matrix pi = A_trans_base.getSolution(x);
			Matrix eta = A_trans_base.getSolution(y); 
			CohoNumber nextNorm=null, prevNorm=null;
			int prevEvict = 0, nextEvict = 0;
			for(int dim=0; dim<pi.length(); dim++){
				CohoNumber pi_dim = pi.V(dim);
				CohoNumber eta_dim = eta.V(dim);
				int varSign =pi_dim.add(optDir.mult(eta_dim)).compareTo(0);
				if(varSign<0){
					throw new RuntimeException("LPProject.norm: The basis is not optimal");
				}
				int etaSign = eta_dim.compareTo(0);
				//varSign==0. combine with varSign>0
				//if(varSign==0){
				//if etaSign = 0, nothing. Because can not make the basis infeasible.
				//if etaSign !=0 prevNorm = nextNorm = optDir
				//}
				//varSign>0
				if(etaSign==0){
					assert pi_dim.compareTo(0)>=0:
						"LPProject.norm: pi is negative when eta is zero. " +
						"The basis is not optimal, it should be detected before.";
					//all optimal dir on the line x+alpha*y can not make the varaible of this dim infeasible(negative)
				}else{
					CohoNumber norm = pi_dim.div(eta_dim).negate();
					int normAdvance = norm.compareTo(optDir);
					if(etaSign<0){//forward make infeasible
						assert normAdvance>=0:
							"LPProject.norm: norm is less than optDir." +
							"The basis is not optimal, it should be detected before.";
						if(nextNorm==null || norm.compareTo(nextNorm)<0){
							nextEvict = dim;
							nextNorm = norm;
						}
					}else if(etaSign>0){//backward make infeasible
						assert normAdvance<=0:
							"LPProject.norm: norm is greater than optDir." +
							"The basis is not optimal, it should be detected before.";
						if(prevNorm==null || norm.compareTo(prevNorm)>0){
							prevEvict = dim;
							prevNorm = norm;
						}
						
					}else{
						throw new RuntimeException("LPProject.norm: Impossible.");
					}
				}
			}

			assert nextNorm!=null||prevNorm!=null:
				"LPProject.norm: both nextNorm and currNorm is null. "+pi.toString()+eta.toString()+optDir;
			assert nextNorm==null||nextNorm.compareTo(optDir)>=0:
				"LPProject.norm: nextNorm is less than optDir."+pi.toString()+eta.toString()+optDir;
			assert prevNorm==null||prevNorm.compareTo(optDir)<=0:
				"LPProject.norm: nextNorm is greater than optDir."+pi.toString()+eta.toString()+optDir;
//			//BUG: debug it later. It's possible prevNorm==nextNorm==optDir
//			assert prevNorm==null||nextNorm==null||prevNorm.compareTo(nextNorm)!=0:
//				"LPProject.norm: it is impossible that nextNorm==prevNorm==optDir. The basis is singular";
			return new Norm[]{new Norm(prevNorm,prevEvict), new Norm(nextNorm,nextEvict)};
		}catch(SingularMatrixException e){
			throw new LPError("LPproject.nextNorm(): This should never happen when solve the optimal point.");
		}
	}

	/*
	 * Compute the projection point of A>=b onto x-y.
	 * Each point is a n*1 vector, and the result is a n*m matrix.
	 */
	protected Matrix findPoints(Matrix A, Matrix b, Matrix x_axis, Matrix y_axis){
//		exactAll++;
		HashSet<Matrix> points = new HashSet<Matrix>();
		LP cohoLP = LP.createCoho(new Constraint(A,b));
		CohoSolver lpSolver = CohoSolverFactory.getSolver(cohoLP, solver);

		CohoSolverResult result = lpSolver.opt(x_axis.sub(y_axis));
//		directLP++;
		if(result.status()!=LPResult.ResultStatus.OK){
			// the lp is not feasible
			return x_axis.convert(x_axis.length(),1).zeros();//return zero			
		}
		
		
		points.add(result.optPoint()); //the first point on the direction of x-y
		LPBasis basis = result.optBasis();
		assert basis!=null:"infeasible lp, thus the lpproject is empty";
		
		CohoMatrix cohoA_trans = lpSolver.dataFactory().createCohoMatrix(A.transpose(), true);
		CohoMatrix A_trans_base = null;
		Norm[] norms = null;
		CohoNumber prevNorm=null, currNorm=null,nextNorm=null;
		LPBasis leftPtBasis = basis;
		int leftBasisEvict = 0;
		
		//NOTE A trick: initially, quad is -1 not 0, we set nextNorm =0 to force to find currNorm in quad 0. 
		for(int quad=-1; quad<4; quad++){
//			System.out.println("At quad "+quad);
			QUADLOOP: while(true){	
				//If nextNorm == null, jump to next quad and initialize the currNorm;
				if(nextNorm==null){
					assert result!=null && basis!=null: "LPProject.norm: optimal basis not found in the previous quad";
					if(quad>=0){
						Matrix swap = x_axis;
						x_axis = y_axis;
						y_axis = swap.negate();
					}						
//					System.out.println("go to next quad because next norm is null");
					//basis is also the optimal basis for x-y in the new quad.
					//A_trans_base = lpSolver.dataFactory().createCohoMatrix(A.transpose().col(basis.basis()),true);
					A_trans_base = (CohoMatrix)cohoA_trans.col(basis.basis());
					norms = norm(A_trans_base,x_axis,y_axis,result.optCost().one().negate());
					//prevNorm = norms[0].norm; no use
					nextNorm = norms[1].norm;
					//NOTE we can not assume nextNorm!=null. Because it may just two quad. For example an triangle.
					//assert nextNorm!=null:"LPProject.norm: nextNorm for initialization of quad is null";
					break;
				}

				//If nextNorm >=1, jump to next quad and nextNorm = -1/nextNorm;
				if(nextNorm.compareTo(1)>=0){
//					System.out.println("go to next quad because next norm is greater than 1");
					Matrix swap = x_axis;
					x_axis = y_axis;
					y_axis = swap.negate();
					nextNorm = nextNorm.recip().negate();//\in[-1,0)
					break;//jump to next quad						
				}

				//If nextNorm <1, stay in the same quad.
				currNorm = nextNorm;
				leftPtBasis = basis;
				leftBasisEvict = norms[1].evictIndex;
				assert currNorm.compareTo(1)<0 && currNorm.compareTo(-1)>=0:"currNorm"+currNorm+" is not in the range of [-1,1)";

				//give current norm, find the right point of this edge
				CohoNumber rOptDir = currNorm.add(GeomObj2.eps).min(currNorm.add(1).div(2)); //min(currNorm+delta,(currNorm+1)/2)
				RIGHPOINTLOOP: while(true){
					//rOptDir in [currNorm,1);

					//CODE to find norms						
					/*
					 * NOTE: given the current optimal basis, find the next optimal basis. 
					 * Usually, there is only one hop between these two bases (>80%), try the fast method. 
					 */
					//
					//numLP++;
//					System.out.println("try rOptDir: "+rOptDir);
					if(useOneHop)
						result = lpSolver.opt(x_axis.add(y_axis.mult(rOptDir)),leftPtBasis,leftBasisEvict);
					else
						result = lpSolver.opt(x_axis.add(y_axis.mult(rOptDir)));
					//counter ++;
					points.add(result.optPoint());
//					System.out.println(result.optPoint());
					basis = result.optBasis();
					assert basis!=null:"infeasible lp, thus the lpproject is empty";

					//A_trans_base = lpSolver.dataFactory().createCohoMatrix(A.transpose().col(basis.basis()),true);
					A_trans_base = (CohoMatrix)cohoA_trans.col(basis.basis());
					norms = norm(A_trans_base,x_axis,y_axis,rOptDir);
					prevNorm = norms[0].norm;
					nextNorm = norms[1].norm;

					/*
					 * NOTE This assumption " prevNorm!=null && prevNorm.compareTo(currNorm)>=0 " is not true. 
					 * Consider the case when more than three lines intersect on the same point,
					 * Then any basis on this point is optimal when we find the right endpoint. 
					 * If the basis we use is not the 'best' one, the currNorm could be less than the real value. 
					 *             3  | / 2
					 *        in      |/
					 * -------------------- 1
					 *        out    /|
					 * (1,2)->(1,3)       
					 * If we use the 'incorrect' current norm, we will find the prevNorm of next right endponit is the 'correct' currNorm.
					 * However, we don't know this because it is greater than currNorm. 
					 * Then the rOptDir = ('incorrect currNorm'+'correct currNorm')/2 is less than 'correct currNorm'
					 * The rOptDir will find the correct basis, and the nextNorm of this basis is the 'correct currNorm'.
					 * However, in this case, its prevNorm is not greater than 'incorrect currNorm'.   
					 * So we replace currNorm as nextNorm.     
					 * 
					 * It is also possible the 'corrct currNorm' and 'incorrect currNorm' in different quad.
					 *       out      |/ 
					 * ---------------|- 3
					 *       in    1 /|  2
					 * In this case, prevNorm=null or < 'incorrect currNorm' happens when first try to find right endpoint
					 * And also, we can not assume nextNorm is not null      
					 * 
					 * If the 'incorrect currNorm' is used. The faked norm just recompute the optimal point twice. 
					 * It does not change the reuslt because it only find optimal point. 
					 * Just replace it with 'correct norm' or 'better incorrect norm'. (more than four line intersect on the same point. 
					 * If nextNorm is null, jumpt to next quad.     
					 */
					if(prevNorm==null || prevNorm.compareTo(currNorm)<0){//fake currNorm found
						assert nextNorm==null || nextNorm.compareTo(currNorm)>0:
							"LPProject.findPoints: impossible. Incorrect currNorm found before, but can not find the correct currNorm";
						//System.out.println("fake currNorm is found before, update it");
						//currNorm is a faked norm. Ignore it and use the correct one. 
						//And we use point, it does not change the result. Just find the optPoint twice.
						//if nextNorm is null, jump to next quad.
						break;
					}
					//prevNorm == currNorm
					if(prevNorm.compareTo(currNorm)==0){//backward norm == currNorm ==> this basis is the right point of current edge
//						System.out.println("The right point is found, go to next");
						break;
					}
					//prevNorm > currNorm, then bisect the optimal direction and compute new basis and nextNorm
					rOptDir = currNorm.add(prevNorm).div(2);
				}

			}
		}
		//project points onto plane.
		Point[] points2D = matrix2Points(points,x_axis,y_axis,(ScaleType)x.elementType());//NOTE: convert from lpsolver type to lpproject type
		Polygon poly = points2Polygon(points2D,ProjectFactory.errTol);		
		Matrix vertices = poly2FullMatrix(poly,x_axis,y_axis);//x,y restore to original
		return vertices;
	}

	/*
	 * findPoints can find exactly result, however, it is slow. 
	 * Some time, we can overapproximate the projected polygon by a small ammount.
	 * Therefore, we implement findPlanes again. 
	 * However, we don't depend on finding the correct right endpoint as before  which may introduce large error.
	 * For each basis, we can compute the prevNorm and nextNorm, and each defines a line. 
	 * Although some points are omitted as expected, the lines are exactly (the normal is not exactly before).
	 * Therefore, we hope it is faster and introduce slight error. In fact, we only need to 
	 * find all normal of edges of the reduced projected polygon.
	 */
	//public static double angleEps = 1e-2;
	protected Matrix findByLine(Matrix A, Matrix b, Matrix x_axis, Matrix y_axis){
//		apprAll ++;
		//assert(ProjectFactory.errTol>0):"This method only applied to when the error is allowed";
		LP cohoLP = LP.createCoho(new Constraint(A,b));
		CohoSolver lpSolver = CohoSolverFactory.getSolver(cohoLP, solver);
		CohoSolverResult result = lpSolver.opt(x_axis.sub(y_axis));
		if(result.status()!=LPResult.ResultStatus.OK){
			// the lp is not feasible
			return x_axis.convert(x_axis.length(),1).zeros();//return zero			
		}
//		directLP++;
		LPBasis basis = result.optBasis();
		//numLP ++;
		//assert basis!=null:"infeasible lp, thus the lpproject is empty";
		
		ArrayList<Line> hullAsPlanes = new ArrayList<Line>();	
		HashSet<Matrix> points = new HashSet<Matrix>();
		points.add(DoubleMatrix.create(result.optPoint()));
		
		CohoMatrix cohoA_trans = lpSolver.dataFactory().createCohoMatrix(A.transpose(), true);
		CohoMatrix A_trans_base = null;
		Norm[] norms = null;
		CohoNumber prevNorm=null, currNorm=null,nextNorm=null;
		LPBasis leftPtBasis = basis;
		int leftBasisEvict = 0;
		Line p = null;
		CohoNumber rOptDir = null;
		boolean edgeIgnored =false;


		//NOTE A trick: initially, quad is -1 not 0, we set nextNorm =0 to force to find currNorm in quad 0. 
		for(int quad=-1; quad<4; quad++){
//			System.out.println("At quad "+quad);
			QUADLOOP: while(true){	
				//If nextNorm == null, jump to next quad and initialize the currNorm;
				if(nextNorm==null){
//					System.out.println("go to next quad because next norm is null");
					//assert result!=null && basis!=null: "LPProject.norm: optimal basis not found in the previous quad";
					if(quad>=0){
						Matrix swap = x_axis;
						x_axis = y_axis;
						y_axis = swap.negate();
					}						
					//basis is also the optimal basis for x-y in the new quad.
					//A_trans_base = lpSolver.dataFactory().createCohoMatrix(A.transpose().col(basis.basis()),true);
					A_trans_base = (CohoMatrix)cohoA_trans.col(basis.basis());
					norms = norm(A_trans_base,x_axis,y_axis,result.optCost().one().negate());
					
					//special case of the initial edge whose normal is [0,-1]				
					//NOTE: It's possible that prevNorm==nextNorm==-1. We add an extra line only prevNorm=-1 && nextNorm!=-1
					if(quad==-1 && norms[0].norm!=null && norms[0].norm.compareTo(-1)==0 && norms[1].norm.compareTo(-1)!=0){
						p = new Line(0,(ScaleNumber)result.optCost().one().negate(),(ScaleNumber)result.optCost());
						hullAsPlanes.add(p);
						//System.out.println("special case of initial edge, add current plane "+p);
					}
					//prevNorm = norms[0].norm; no use
					nextNorm = norms[1].norm;
					//NOTE we can not assume nextNorm!=null. Because it may just two quad. For example an triangle.
					//assert nextNorm!=null:"LPProject.norm: nextNorm for initialization of quad is null";
					break;
				}

				//If nextNorm >=1, jump to next quad and nextNorm = -1/nextNorm;
				if(nextNorm.compareTo(1)>=0){
//					System.out.println("go to next quad because next norm is greater than 1 "+nextNorm+result.optCost());
					Matrix swap = x_axis;
					x_axis = y_axis;
					y_axis = swap.negate();
					nextNorm = nextNorm.recip().negate();//\in[-1,0)
					break;//jump to next quad						
				}

				//If nextNorm <1, stay in the same quad.
				currNorm = nextNorm;
				//add the current edge
				//optCost and optDir are the same type. We can convert it to double here because we use approximation?
				//ScaleNumber optValue = (ScaleNumber)x_axis.add(y_axis.mult(currNorm)).transpose().mult(result.optPoint()).V(0);
				ScaleNumber optValue = (ScaleNumber) (x_axis.add(y_axis.mult(currNorm)).dotProd(result.optPoint()));
				p = new Line(quad,(ScaleNumber)currNorm,optValue);//1x+currNorm*y >= optCost
				hullAsPlanes.add(p);
				//System.out.println("add "+p);

				leftPtBasis = basis;
				leftBasisEvict = norms[1].evictIndex;
				//assert currNorm.compareTo(1)<0 && currNorm.compareTo(-1)>=0:"currNorm"+currNorm+" is not in the range of [-1,1)";

				//give current norm, find the right point of this edge
				//double minAmt = angleEps*(1+currNorm.doubleValue()*currNorm.doubleValue())/(1-angleEps*currNorm.doubleValue()); //TODO compute from error tolerence later.
				double angleEps = 2*ProjectFactory.errTol;//approximated. The error introduced is about 0.5*minAmt*L^2
				//System.out.println(minAmt);
				rOptDir = currNorm.add(angleEps).min(currNorm.add(1).div(2)); //min(currNorm+delta,(currNorm+1)/2)
//				System.out.println("currNorm "+currNorm+"roptdir"+rOptDir);
				//numLP ++;
//				RIGHPOINTLOOP: while(true){
					//if(useOneHop && !lastJump){
				    if(useOneHop){
						result = lpSolver.opt(x_axis.add(y_axis.mult(rOptDir)),leftPtBasis,leftBasisEvict);
					}else{
						result = lpSolver.opt(x_axis.add(y_axis.mult(rOptDir)));
					}
					points.add(DoubleMatrix.create(result.optPoint()));
					//counter ++;
					basis = result.optBasis();
					//assert basis!=null:"infeasible lp, thus the lpproject is empty";
					
					//A_trans_base = lpSolver.dataFactory().createCohoMatrix(A.transpose().col(basis.basis()),true);
					A_trans_base = (CohoMatrix)cohoA_trans.col(basis.basis());
					norms = norm(A_trans_base,x_axis,y_axis,rOptDir);
					prevNorm = norms[0].norm;
					nextNorm = norms[1].norm;
					//System.out.println("Using the direction"+rOptDir+" and finding the point "+result.optPoint());
//					System.out.println("prevNorm is "+prevNorm+"Next norm is "+nextNorm);

					//prevNorm < currNorm, fake currNorm used before, remove it.(initial currNorm may not be a normal vector?)
					if(prevNorm==null || prevNorm.compareTo(currNorm)<0){//fake currNorm found
						//assert nextNorm==null || nextNorm.compareTo(currNorm)>0:
						//	"LPProject.findPoints: impossible. Incorrect currNorm found before, but can not find the correct currNorm";
						hullAsPlanes.remove(hullAsPlanes.size()-1);
						//System.out.println("fake currNorm found, remove previous plane");
//						lastJump = false;
					}
					//prevNorm > currNorm,add prevNorm as an edge. Ignore points, do not bisect
					if(prevNorm.compareTo(currNorm)>0){
						//NOTE: prevNorm > currNorm and <= optDir, where optDir-currNorm < minAmt, therefore, prevNorm-currNorm<minAmt
						//We can ignore it. Of course, we can add it because we want to reduce # of lps. We are using APR intersection
						//Therefore, the result is more acccurate.
						//NOTE: it's possible nextNorm==prevNorm, add only if they are not equal
						//NOTE: 2007.12.07, when nextNorm==prevNorm? Yes, it's possible (see norm func)
						//Should we add it if nextNorm is null? Why we can not add the line if nextNorm==prevNorm? (redundant, to maintain the order of lines)
						//I change 
						//if(nextNorm!=null && nextNorm.compareTo(prevNorm)!=0){
						//to below because I found a bug where nextNorm is null and the prevNorm>currNorm, the edge is not added. If this introduce potential bug, debut it later.
						if(nextNorm==null || nextNorm.compareTo(prevNorm)!=0){
							//optValue = (ScaleNumber)x_axis.add(y_axis.mult(prevNorm)).transpose().mult(result.optPoint()).V(0);
							optValue = (ScaleNumber) (x_axis.add(y_axis.mult(prevNorm)).dotProd(result.optPoint()));
							p = new Line(quad,(ScaleNumber)prevNorm,optValue);
							hullAsPlanes.add(p);
							edgeIgnored = true;
							//System.out.println("add "+p);
						}
//						lastJump = true;
					}else{//prevNorm == currNorm, don't add twice
//						lastJump = false;
					}
//					break;//loop is no use
//				}
			}
		}
		
		
		// NOTE: inPoly can be a segment of a point because we did not try all optimal directions. 
		// Therefore, we allow convexHull to bloat this segment by eps, which is reasonable because over-approximation is allowed.
		// Round-off error may also cause this kind of problem, thus we also allow over-approximation in outPoly. 
		Polygon inPoly = ConvexPolygon.convexHull(matrix2Points(points,DoubleMatrix.create(x_axis),DoubleMatrix.create(y_axis),CohoDouble.type),true);
		Polygon poly = null;
		double error = 0;
		if(edgeIgnored){
			Point[] out = line2Points(hullAsPlanes,CohoDouble.type);
			Polygon outPoly = ConvexPolygon.convexHull(out,true);
			poly = outPoly;
			// If the error is too large, use exact computation.
			//NOTE: By probability analysis, only 1/4 of area difference of outPoly and inPoly is the real overestimated.
			//remove 4 if you want to guarantee the overestimated area is bounded by errorTol with performance penalty.
			//We use 4 here to remove overestimated error
			//error = (outPoly.area().doubleValue()/inPoly.area().doubleValue()-1)/4;
			error = (outPoly.area().doubleValue()/inPoly.area().doubleValue()-1);
			if(error>ProjectFactory.errTol){
				//NOTE: here we use cohoA in java to avoid creating coho matrix again. 
				//However c cohosolver does not support APR now. So use slow method.
//				apprFail++;
				if(ProjectFactory.lpSolver==CohoSolverFactory.SOLVER.C){
					return findPoints(A,b,x,y);//recompute
				}else{
					return findPoints(cohoA_trans.transpose(),b,x,y);//recompute
				}				
			}
		}else{
			//error=0
			poly = inPoly;
		}
		if(poly.degree()>3 && ProjectFactory.errTol-error>0)
			poly = poly.reduce(new Polygon.CostEndCondition(ProjectFactory.errTol-error));
		Matrix hull = poly2FullMatrix(poly,x_axis,y_axis);//x,y restore to original
		return hull;
	}
	/*
	 * Given the edge of the polygon, compute the vertices which is converted to the given type
	 */
	protected static Point[] line2Points(ArrayList<Line> hullAsPlanes,ScaleType type){
		Line[] planes = hullAsPlanes.toArray(new Line[hullAsPlanes.size()]);
		Line currP = planes[hullAsPlanes.size()-1];//to find the first point
		final ArrayList<Point> points = new ArrayList<Point>();
		for (int i=0;i<hullAsPlanes.size();i++) {
			Line nextP = planes[i];
			GeomObj2 p = currP.intersect(nextP);
			assert (p instanceof Point) :"Parallel edge found in lp project";
			points.add((Point)p.specifyType(type));	
			currP = nextP;
		}
		return points.toArray(new Point[points.size()]);
	}
	/*
	 * Given the full dimension points, project down onto plane x-y. The points are converted to given type
	 */
	protected static Point[] matrix2Points(Collection<Matrix> points, Matrix x, Matrix y,ScaleType type){
		Matrix fullPts = x.convert(x.length(),points.size());//points and x has the same data type
		Iterator<Matrix> iters = points.iterator();
		for(int i=0; iters.hasNext(); i++){
			fullPts.assign(iters.next(),0,i);//each point is a column vector			
		}
		Matrix coord = x.convert(x.length(),2);
		coord.assign(x,0,0);//on quad5, x,y restore to original
		coord.assign(y,0,1);
		Matrix pts2D = fullPts.transpose().mult(coord);
		Point[] ptsGeom = new Point[pts2D.nrows()];
		for(int row=0;row<pts2D.nrows();row++){
			ptsGeom[row] = new Point((ScaleNumber)type.zero().convert(pts2D.V(row,0)),(ScaleNumber)type.zero().convert(pts2D.V(row,1)));
		}
		return ptsGeom;
	}
	/*
	 * Given the vertices, compute its convex hull and reduce it.
	 */
	protected static Polygon points2Polygon(Point[] ps,double errorTol){
		//TODO: assume it's convex polygon. Test it later.	
		ConvexPolygon poly = SimplePolygon.convexHull(ps,true); // fix it if less than 3 points
		//ConvexPolygon poly = SimplePolygon.convexHull(ps);
		if(errorTol > 0 && poly.degree()>3){//TODO add the degree requirement?
			poly = poly.reduce(new Polygon.CostEndCondition(ProjectFactory.errTol));
		}
		//System.out.println("area\t"+poly.area());
		return poly;		
	}
	/*
	 * Given polygon on plane x-y, compute its full dimension position
	 * The result is of the type of x
	 */
	// BUG, if poly contains almost-identical points, vertices may contain identical points because of round-off error.
	protected static Matrix poly2FullMatrix(Polygon poly, Matrix x,Matrix y){
		//assert(x.length()==y.length()):"The length of x and y are not the same";
		Matrix pts2D = x.convert(2,poly.degree());//2xm matrix
		for(int i=0; i<poly.degree(); i++){
			Point p = poly.point(i);
			pts2D.assign(p.x(),0,i);
			pts2D.assign(p.y(),1,i);
		}

		Matrix coord = x.convert(x.length(),2);//nx2 matrix
		coord.assign(x,0,0);
		coord.assign(y,0,1);
		Matrix vertices = coord.mult(pts2D);
		return vertices;		
	}


	//public static int counter = 0;
	public String optBases2Matlab(LPBasis b){
		String str = "%BASIS\t[";
		IntegerMatrix m = b.basis().transpose();
		for(int i=0; i<m.length(); i++){
			str+=m.V(i);
			if(i!=m.length()-1)
				str+=", ";
		}
		str += "];";
		return str;
	}




	protected CohoNumber nextNorm(CohoMatrix A_trans_base, Matrix x, Matrix y, CohoNumber currNorm){
		try{
			Matrix pi = A_trans_base.getSolution(x);
			Matrix eta = A_trans_base.getSolution(y); 
			CohoNumber newNorm = null;
			for(int i=0;i<pi.length();i++){
				CohoNumber pi_i = pi.V(i);
				CohoNumber eta_i = eta.V(i);
				if(eta_i.compareTo(0.0)<0 && pi_i.compareTo(0.0)>=0){//pi_i must be non-negative here. Otherwise, it's not optimal basis
					CohoNumber Norm_i = pi_i.div(eta_i.negate());//positive
					if(	(newNorm==null ||(newNorm!=null && Norm_i.compareTo(newNorm)<0)) 
							&& Norm_i.compareTo(currNorm)>0){//is it possible norm_i<currNorm
						newNorm = Norm_i;
					}
				}
			}
			return newNorm;//return null if next quad
		}catch(SingularMatrixException e){
			throw new LPError("LPproject.nextNorm(): This should never happen when solve the optimal point.");
		}
	}

	/**
	 * @deprecated
	 */
	protected Matrix findByPlane(Matrix A,Matrix b,Matrix x,Matrix y){
		ArrayList<Line> hullAsPlanes = findPlanes(A,b,x,y);
		Polygon poly =  points2Polygon(line2Points(hullAsPlanes,CohoDouble.type),ProjectFactory.errTol);
		return poly2FullMatrix(poly,x,y);
	}
	/**
	 * @deprecated
	 */
	protected ArrayList<Line> findPlanes(Matrix A, Matrix b,Matrix x, Matrix y){
		// Initialization
		ArrayList<Line> hullAsPlanes = new ArrayList<Line>();
		LP cohoLp = LP.createCoho(new Constraint(A, b));
		CohoSolver lpSolver =CohoSolverFactory.getSolver(cohoLp,solver);
		CohoSolverResult result = lpSolver.opt(x);//the first costVector is the x axis
		LPBasis B = result.optBasis(); //FIXED see CohoSolver.resultStatus()
		//if(DEBUG.debug) DEBUG.println(LPproject_DEBUG,"LPproject.findPlanes(): The inital basis: "+B.toString());
		boolean testedX = false; //for (1,0), the initial direction
		// Loop
		LOOP: for (int quad = 0; quad<4||(quad==4&&!testedX) ; quad++) {//for each quad
			//if(DEBUG.debug) DEBUG.println(LPproject_DEBUG,"LPproject.findPlanes(): Beging the "+quad+"th quadrant");
			if(quad!=0){
				Matrix swap = x;
				x = y;
				y = swap.negate();
			}
			//the norm is (1, currNorm).
//			System.out.println(result.status());
			CohoNumber currNorm = result.optCost().one().negate(); //get the data type from lp solver
			while (true) {//status is defined as (alpha, B).
				/*************************************
				 * find the norm, and add the new edge
				 *************************************/
				CohoMatrix A_trans_base = lpSolver.dataFactory().createCohoMatrix(A.transpose().col(B.basis()), true); 
				//CohoRationalMatrix.create(A.transpose().col(B.basis()),true);
				CohoNumber nextNorm = nextNorm(A_trans_base,x,y,currNorm);

				if(nextNorm!=null){//next edge in the same quad
					currNorm = nextNorm;
					if(quad==0 && currNorm.equals(currNorm.zero())){
						testedX = true;
					}
					//if (1,0) is a norm and not added at the beginnig, we will find it again and add it here. 
					//It's possible this is skipped, but at that case, the area increase is small because the adjacent norm is close enough.
					if (quad==4) {//(1,0) is considered here if not added at the beginning
						testedX = true;
						if(currNorm.compareTo(currNorm.zero())>0)
							break;
					}

					//add a new hyperplane
					result = lpSolver.opt((x.add(y.mult(currNorm))));//direction = x+y*alpha
					//TODO: lost precision here. no problems, will reomove similar planes in Plane
					//here might add very similar planes. In interval project, we force alpha to be a little larger. 
					//but here not. 1)create plane based on Rational all?
					Line p;
					if(currNorm.type() instanceof ScaleType){
						p = new Line(quad,(ScaleNumber)currNorm,(ScaleNumber)result.optCost());
						//p = new Line((ScaleNumber)result.optCost(),quad,(ScaleNumber)currNorm);//double or apr
					}else{//NOTE: for DoubleIntervalCohoSolver(but it's overried), I don't want to support it longer
						p = new Line(quad,((IntervalNumber)currNorm).x(),((IntervalNumber)result.optCost()).x());//over-approx
						//p = new Line(((IntervalNumber)result.optCost()).x(),quad,((IntervalNumber)currNorm).x());//over-approx
					}
					hullAsPlanes.add(p);	
					//if(DEBUG.debug) DEBUG.println(LPproject_DEBUG,"LPproject.findPlanes(): A new face found: "+p.toString()+" with basis "+B);

					//remove the jump two quadrants case. Because we use rational value, which can't omit the correct one.
					//TODO: find optBasis is the left endpoint. find all optimal bases now. or force to the right.
					//2 methods: 1)find all opts and test nextNorm, this case is rare, so performance is ok
					//2) add small amount to currNorm, introduce small error.
					if(result.optBasis().equals(B)){//BUG: should not compare basis, compare point 
						result = lpSolver.opt((x.add(y.mult(currNorm.add(GeomObj2.eps)))));//the result must be different from oldB
					}
					B = result.optBasis();
					//System.out.println("basis"+ B);
					//it might be possible that B = oldB
				}else{//next edge in the next quad. 
					//System.out.println("jump to next quad");
					continue LOOP;
				}						
			} /* end while loop */
		} /* end quadrant for loop */
		return hullAsPlanes;
	}


	/* LPproject */
	public String toString() {
		return ("lpProject(\n  " +  hull + ")\n");
	}

//	private Matrix restoreVertices(ArrayList<Line> hullAsPlanes, Matrix T, BooleanMatrix bmNonEmptyCol){
//	//long startTime = System.nanoTime();
//	Line[] planes = hullAsPlanes.toArray(new Line[hullAsPlanes.size()]);
//	Line currP = planes[hullAsPlanes.size()-1];//to find the first point
//	final ArrayList<Point> ps = new ArrayList<Point>();
//	for (int i=0;i<hullAsPlanes.size();i++) {
//		Line nextP = planes[i];
//		//System.out.println("Plane"+((CohoAPR)nextP.C()).denominator().bitLength()+"\t"+((CohoAPR)nextP.C()).numerator().bitLength());
//		/* NOTE: the plane may be apr, double or interval, what about the result point?
//		 *  convert to the same type as plane. But what if interval. it doesn't work for polygon.
//		 */
//		//GeomObj2 p = currP.specifyType(CohoDouble.type).intersect(nextP.specifyType(CohoDouble.type));
//		GeomObj2 p = currP.intersect(nextP);
//		//GeomObj2 p = currP.specifyType(CohoDouble.type).intersect(nextP.specifyType(CohoDouble.type));
//		//System.out.println(p.specifyType(x.elementType())+"is the intersection of "+currP.specifyType(x.elementType())+" and "+nextP.specifyType(x.elementType()));
//		if(p instanceof Point){
//			//NOTE: The plane are the same type of x.elementType. Which is APR and slow. 
//			//If allow error, use double to speedup the compuation.
//			if(ProjectFactory.errTol>0)
//				ps.add((Point)p.specifyType(CohoDouble.type));
//			else
//				ps.add((Point)p.specifyType(x.elementType()));	
//		}else{
//			throw new RuntimeException("Impossible");
//		}
//		currP = nextP;
//	}
//	//long startTime  = System.nanoTime();
//	ConvexPolygon cp = SimplePolygon.convexHull(ps.toArray(new Point[ps.size()]));//also remove duplicated point
//	//t7 += (double)(System.nanoTime()-startTime);
//	
//	if(ProjectFactory.errTol>0 && cp.degree()>4){//TODO add the degree requirement?
//		cp = cp.reduce(new Polygon.CostEndCondition(ProjectFactory.errTol));
//	}
//	System.out.println("poly"+cp+"\narea\t"+cp.area());		//startTime = System.nanoTime();
//	//t6 += (double)(System.nanoTime()-startTime);
//	
//	//System.out.println("poly"+cp+"\n area\t"+cp.area());
//	//NOTE: change back to double matrix? no. matlab input is always double.the result is also double
//	Matrix vertices = x.convert(x.nrows(),cp.degree());
//	for(int i=0; i<cp.degree(); i++){
//		Point p = cp.point(i);
//		Matrix xevNz = x.convert(2,1);
//		xevNz.assign(p.x(),0);//NOTE: convert from type of LPsolver to type of project
//		xevNz.assign(p.y(),1);
//		xevNz = T.mult(xevNz);
//		Matrix full = x.convert().zeros();//Matrix full = x.convert(x.nrows(),x.ncols()).zeros();
//		full.assign(xevNz,bmNonEmptyCol.find().transpose());//FIXED if not assigned, it's the default value of zero
//		vertices.assign(full,0,i);
//	}
//	//System.out.println(System.nanoTime()-startTime);
//	return vertices;
//}


	public static void main(String[] args){
//		double[][] P = new double[][]{
//		{1,0},{-1,0},{0,1},{0,-1},{-1,1}
//		};
//		double [] b = new double[]{
//		0,-1,0,-1,-1
//		};
//		DoubleMatrix neqA = new DoubleMatrix(P);
//		DoubleMatrix neqb = new DoubleMatrix(b);
//		Constraint neq = new Constraint(neqA,neqb);
//		int vars = neq.ncols();
//		LP lp = LP.create(new Constraint(neq.a().convert(0,vars),neq.b().convert(0,1)),
//		neq,BooleanMatrix.create(vars,1).zeros());
//		DoubleMatrix x = new DoubleMatrix(new double[]{1,0});
//		DoubleMatrix y = new DoubleMatrix(new double[]{0,1});
//		LPProject proj = ProjectFactory.getProject(lp, x, y);
//		Matrix hull = proj.hull();
//		System.out.println(hull);

		double[][] A = new double[][]{
				{      0.2387336254340564,                    -0.0,                    -0.0,                    -0.0,                    -0.0,                    -0.0,                    -0.0 },
				{     0.20229287805692237,                    -0.0,                    -0.0,                    -0.0,  -8.673617379884035E-19,                    -0.0,                    -0.0 },
				{      0.1391570085274647,                    -0.0,                    -0.0,                    -0.0,                    -0.0,                    -0.0,                    -0.0 },
				{     0.12345473960801945,                    -0.0,                    -0.0,                    -0.0,                    -0.0,                    -0.0,                    -0.0 },
				{     0.12262751801102449,                    -0.0,                    -0.0,                    -0.0,                    -0.0,                    -0.0,                    -0.0 },
				{     0.09869080668009504,                    -0.0,                    -0.0,                    -0.0,                    -0.0,                    -0.0,                    -0.0 },
				{    0.049952279224426244,                    -0.0,                    -0.0,    -0.06243134982300004,                    -0.0,                    -0.0,                    -0.0 },
				{     0.04626520338668047,  -0.0024706616797245395,                    -0.0,                    -0.0,                    -0.0,                    -0.0,                    -0.0 },
				{     0.04181145658227183,                    -0.0,                    -0.0,                    -0.0,                    -0.0,                    -0.0,   8.673617379884035E-19 },
				{     0.03857862823949324,                    -0.0,                    -0.0,                    -0.0,                    -0.0,                    -0.0,   1.7976028296104452E-9 },
				{    0.004225745593043673,                    -0.0,    -0.06243134982285742,                    -0.0,                    -0.0,                    -0.0,                    -0.0 },
				{   0.0033658656617006066,                    -0.0,   -0.050083422638104017,                    -0.0,                    -0.0,                    -0.0,                    -0.0 },
				{    0.003288770952068684,     0.06230412607951585,                    -0.0,                    -0.0,                    -0.0,                    -0.0,                    -0.0 },
				{    0.002868980133051674,     0.05021064638144559,                    -0.0,                    -0.0,                    -0.0,                    -0.0,                    -0.0 },
				{   0.0017916060388735305,     0.03135528762865809,                    -0.0,                    -0.0,                    -0.0,                    -0.0,                    -0.0 },
				{    0.001644385476034349,     0.03115206303975819,                    -0.0,                    -0.0,                    -0.0,                    -0.0,                    -0.0 },
				{   0.0016443854760343352,    0.031152063039757656,                    -0.0,                    -0.0,                    -0.0,                    -0.0,                    -0.0 },
				{   0.0010773740941781434,    0.018855358752787495,                    -0.0,                    -0.0,                    -0.0,                    -0.0,                    -0.0 },
				{   1.3637773693409017E-4,                    -0.0,                    -0.0,     0.06243134982300001,                    -0.0,                    -0.0,                    -0.0 },
				{   1.0144749581808554E-4,                    -0.0,                    -0.0,    0.050083422637961436,                    -0.0,                    -0.0,                    -0.0 },
				{    5.570447771946485E-5,                    -0.0,                    -0.0,                    -0.0,  -1.2045711077907084E-9,                    -0.0,                    -0.0 },
				{   1.2025153612871264E-6,                    -0.0,                    -0.0,                    -0.0,                    -0.0,   -0.062431349823000026,                    -0.0 },
				{    7.773597904270346E-7,                    -0.0,                    -0.0,                    -0.0,                    -0.0,                    -0.0,    -0.05021064638144504 },
				{    2.778035685935265E-7,                    -0.0,                    -0.0,                    -0.0,                    -0.0,   -0.050083422637961394,                    -0.0 },
				{   2.5961719662959126E-7,                    -0.0,                    -0.0,                    -0.0,                    -0.0,       0.059932109224858,                    -0.0 },
				{   2.2135228455733014E-7,                    -0.0,                    -0.0,                    -0.0,                    -0.0,                    -0.0,     0.05021064458385668 },
				{    6.400492624791809E-8,                    -0.0,                    -0.0,                    -0.0,                    -0.0,     0.05258266323610344,                    -0.0 },
				{    5.426440899769058E-8,                    -0.0,                    -0.0,                    -0.0,     0.05021064642667135,                    -0.0,                    -0.0 },
				{  -4.440892098500626E-16,                    -0.0,                    -0.0,   -0.050083422637961394,                    -0.0,                    -0.0,                    -0.0 },
				{  -1.1163387500512378E-7,                    -0.0,                    -0.0,                    -0.0,   -0.050210645144017706,                    -0.0,                    -0.0 },
				{  -1.9341625351332326E-7,                    -0.0,                    -0.0,                    -0.0,    -0.06226442742176241,                    -0.0,                    -0.0 },
				{   -6.458868596734589E-7,                    -0.0,                    -0.0,                    -0.0,    0.062304126034290086,                    -0.0,                    -0.0 },
				{    -8.62562131736036E-7,                    -0.0,                    -0.0,                    -0.0,                    -0.0,                    -0.0,     0.06230412607950193 },
				{   -3.179989225221558E-6,                    -0.0,                    -0.0,                    -0.0,                    -0.0,                    -0.0,   -0.062304126079516395 },
				{  -0.0033508114544433254,    -0.06724669163930733,                    -0.0,                    -0.0,                    -0.0,                    -0.0,                    -0.0 },
				{  -0.0034926727155393245,                    -0.0,      0.0526619316822009,                    -0.0,                    -0.0,                    -0.0,                    -0.0 },
				{   -0.003970021404052559,                    -0.0,     0.05985284077876052,                    -0.0,                    -0.0,                    -0.0,                    -0.0 },
				{   -0.006428968159721105,    -0.11251477246096143,                    -0.0,                    -0.0,                    -0.0,                    -0.0,                    -0.0 },
				{    -0.04953285497452964,    -0.04279741914192957,                    -0.0,                    -0.0,                    -0.0,                    -0.0,                    -0.0 },
				{    -0.05049380088957575,                    -0.0,                    -0.0,                    -0.0,  -3.9698690610198706E-5,                    -0.0,                    -0.0 },
				{     -0.0803870409824831,                    -0.0,                    -0.0,                    -0.0,                    -0.0,                    -0.0,                    -0.0 },
				{     -0.0984195896054943,                    -0.0,                    -0.0,                    -0.0,                    -0.0,                    -0.0,                    -0.0 },
				{    -0.12216680605385236,                    -0.0,                    -0.0,                    -0.0,                    -0.0,                    -0.0,                    -0.0 },
				{    -0.13915881246851747,                    -0.0,                    -0.0,                    -0.0,                    -0.0, -1.3877787807814457E-17,                    -0.0 },
				{     -0.1518538849724869,                    -0.0,                    -0.0,                    -0.0, -1.3877787807814457E-17,                    -0.0,                    -0.0 },
				{    -0.17364484406519742,                    -0.0,                    -0.0, -1.3877787807814457E-17,                    -0.0,                    -0.0,                    -0.0 },
				{     -0.2388625425692088,                    -0.0,  1.3877787807814457E-17,                    -0.0,                    -0.0,                    -0.0,                    -0.0 },
		};
		double[][] b = new double[][]{
				{  -0.0017724720056619368 },
				{  -0.0015019185615296608 },
				{  -0.0010331678311261664 },
				{   -9.165867167796242E-4 },
				{   -9.104450301174012E-4 },
				{   -7.327274980163245E-4 },
				{    -0.11668228540200314 },
				{   -8.293933558566027E-4 },
				{   -3.104281442267842E-4 },
				{   -2.864260289025282E-4 },
				{    -0.11226042085127996 },
				{    -0.09005842199383916 },
				{   0.0044764661606497705 },
				{   0.0036169180923512666 },
				{    0.002258674510051312 },
				{   0.0022382330803249035 },
				{   0.0022382330803248666 },
				{   0.0013582435822999542 },
				{     0.10858503600359062 },
				{     0.08710820670294518 },
				{   -4.138201408563627E-7 },
				{    -0.11481898771311157 },
				{   -0.003948558606514657 },
				{    -0.09210965746585134 },
				{     0.10188196491840021 },
				{  -2.6713647363538245E-4 },
				{     0.08938821952107437 },
				{   -3.318627763678779E-6 },
				{    -0.09580865045809604 },
				{   -0.010156815781533908 },
				{   -0.012595106836562888 },
				{   -4.149088771323178E-6 },
				{  -3.3152713913087465E-4 },
				{   -0.004899768719546485 },
				{   -0.016323512786492536 },
				{     0.08211233199868831 },
				{     0.09332462997063265 },
				{   -0.019394024923333707 },
				{   -0.013388335412936199 },
				{   -0.005362454166752169 },
				{   -0.008527530386330412 },
				{   -0.010440439537436208 },
				{    -0.01295956584659176 },
				{   -0.014762093334291605 },
				{   -0.016108798166452987 },
				{    -0.01842040291691104 },
				{    -0.02533875566285521 },
		};
		DoubleMatrix neqA = new DoubleMatrix(A);
		DoubleMatrix neqb = new DoubleMatrix(b);
		Constraint neq = new Constraint(neqA,neqb);
		int vars = neq.ncols();
		LP lp = LP.create(new Constraint(neq.a().convert(0,vars),neq.b().convert(0,1)),
				neq,BooleanMatrix.create(vars,1).zeros());
		DoubleMatrix x = new DoubleMatrix(new double[]{1,0,0,0,0,0,0});
		DoubleMatrix y = new DoubleMatrix(new double[]{0,0.9999113445291045,0.012921118778252682, -4.416589150365119E-9,-3.7865120430657246E-16,  -0.0032167952246010016, -1.387969845126939E-10});
		LPProject proj = ProjectFactory.getProject(lp, x, y);
		Matrix hull = proj.hull();
		System.out.println(hull);
//		Matrix c = new DoubleMatrix(new double[]{3.2256306014114246E13,-0.9999113445291045,-0.012921118778252682,4.416589150365119E-9,3.7865120430657246E-16 , 0.0032167952246010016,1.387969845126939E-10});
////		Matrix c = new DoubleMatrix(new double[]{1,0,0,0,0,0,0});
//		CohoSolver solver = CohoSolverFactory.getSolver(lp);
//		LPResult r = solver.allOpts(c);
//		System.out.println(r);
//		double[][] ne = {{-1,0,0},{1,0,0},{0,-1,0},{0,1,0},{0,0,-1},{0,0,1}};
//		double[] b={-1,0,-1,0,-1,0};
//		DoubleMatrix A = DoubleMatrix.create(ne);
//		DoubleMatrix B = DoubleMatrix.create(b);
//		Constraint neq = new Constraint(A,B);
//		//double[][] bw={{1,2,0},{1,1,0},{1,1,1}};
//		//double[][] fw={{-1,2,0},{1,-1,0},{0,-1,1}};
//		double[][] bw={{2,0,0},{0,2,0},{0,0,2}};
//		double[][] fw={{0.5,0,0},{0,0.5,0},{0,0,0.5}};
//		DoubleMatrix bwd = DoubleMatrix.create(bw);
//		DoubleMatrix fwd = DoubleMatrix.create(fw);
//		LP problem = LP.createCoho(neq,null,fwd,bwd);
//		DoubleMatrix x = DoubleMatrix.create(3,1).zeros().assign(1,0);
//		DoubleMatrix y = DoubleMatrix.create(3,1).zeros().assign(1,1);
//		LPProject prj = ProjectFactory.getProject(problem,x,y, CohoSolverFactory.Solver.DOUBLEINTERVAL);
//		Matrix hull = prj.hull();
//		System.out.println(hull.toString());


//		System.out.println(Math.pow(2,10));
//		System.out.println(1<<10);
//		//test polygon
//		double[][] d = {{1.0166491285238044, -0.0036094790232671235},
//		{1.0169025713017488, -0.04361072085401306},
//		{1.2172225416983482, -0.042341519012982505},
//		{1.2169690989204036, -0.0023402771822365843},
//		{1.2169690989204016, -0.0023402771822365843}};
//		final Point[] pp = new Point[d.length];
//		for(int i=0;i<d.length;i++)
//		pp[i]=new Point(d[i][0],d[i][1]);
//		Enumeration e = new Enumeration<Segment>(){
//		private int i;
//		public boolean hasMoreElements() { return(i < pp.length);}
//		public Segment nextElement(){
//		i++;
//		return new Segment(pp[i%pp.length],pp[(i+1)%pp.length]);
//		}
//		}; 
//		Polygon poly = (new Polygon(e)).convexHull();
//		PolygonVT p = new PolygonVT(poly);
//		Iterator<Point> itr = p.hullVertices();//vertices and hullVertices?
//		while(itr.hasNext()){
//		System.out.println(itr.next());			
//		}		
	}
}

