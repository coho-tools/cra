package coho.lp;

import java.util.*;
import coho.common.matrix.*;
import coho.common.number.*;
/**
 * A basis result for LP. include status, cost, point and basis
 */
/*
 * Not used now. replaced by CohoResult.
 */
public class LPBasicResult implements LPResult
{
	final protected ResultStatus status;             	
  	final protected CohoNumber optCost; 				
  	final protected LPBasis[] optBases; 	  	
  	final protected Matrix[] optPoints;
  	
  	public LPBasicResult(ResultStatus status, CohoNumber optCost,
  			LPBasis[] optBases, Matrix[] optPoints){
  		this.status = status;
  		this.optCost = optCost;
  		this.optBases = optBases;
  		this.optPoints = optPoints;  		
  	}
  	public LPBasicResult(ResultStatus status, CohoNumber optCost,
  			 LPBasis optBasis, Matrix optPoint){
  		this(status, optCost, (new LPBasis[]{optBasis}), (new Matrix[]{optPoint}));
  	}
  	public LPBasicResult(ResultStatus status, double optCost,
  			LPBasis optBasis, Matrix optPoint){
  		this(status,CohoDouble.create(optCost),(new LPBasis[]{optBasis}),(new Matrix[]{optPoint}));
  	}

  	public ResultStatus status() {return(status);}
  	public double optcost() 	{return(optCost.doubleValue());}
  	public CohoNumber optCost()	{return optCost;}
  	public LPBasis optBasis() 	{return(optBases[0]);}
  	public LPBasis[] optBases()	{return optBases;}
  	public Matrix optPoint() 	{return(optPoints[0]);}
  	public Matrix[] optPoints() {return optPoints;}
	public Iterator<LPBasis> optBasesIter(){
		return new Iterator<LPBasis>(){
			private int i = 0;
			public boolean hasNext(){
				return (i<optBases.length);
			}
			public LPBasis next(){
				if(!hasNext()){
					throw new NoSuchElementException();
				}
				return optBases[i++]; 
			}
			public void remove(){
				throw new UnsupportedOperationException();
			}
		};
	}
	public Iterator<Matrix> optPointsIter(){
		return new Iterator<Matrix>(){
			private int i=0;
			public boolean hasNext(){
				return (i<optPoints.length);
			}
			public Matrix next(){
				if(!hasNext()){
					throw new NoSuchElementException();
				}
				return optPoints[i++];
			}
			public void remove(){
				throw new UnsupportedOperationException();
			}
		};
	}

	//status id
	public static int statusID(ResultStatus s){
		switch(s){
		case OK:
			return 0;
		case UNBOUNDED:
			return 1;
		case INFEASIBLE:
			return 2;
		case INFEASIBLEORUNBOUNDED:
			return 3;
		case POSSIBLEOK:
			return 4;
		default:
			return -1;
		}
	}
  	
  	//status
  	public static String statusName(ResultStatus s){
  		switch(s){
  		case OK:
  			return "ok";
  		case INFEASIBLE:
  			return "infeasible";
  		case UNBOUNDED:
  			return "unbounded";
  		case INFEASIBLEORUNBOUNDED:
  			return "infeasible or unbounded";
  		case POSSIBLEOK://for interval solver
  			return "possible ok";
  		default:
  			return "unknown";	
  		}
  	}  
  	public String statusName() { return(statusName(status)); }  

  	public String toString(){//printout only one optimal poinst and optimal basis
	  	String s = 	"lpResult(\n  " + statusName() + ", " + 
	  				optcost() + ",\n" +
	  				optBasis().toString()+ ",\n"+ 
	  				optPoint().toString() +"\n)";
	  	return s;
 	}
  	public String toString(boolean all){
  		if(!all){
  			return toString();
  		}else{
  		  	String s = 	"lpResult(\n  " + statusName() + ", " +	optCost() + ",\n" ;
  		  	LPBasis[] bases = optBases();//the length must be the same
  		  	Matrix[] points = optPoints();
  		  	if(bases.length!=points.length)
  		  		throw new LPError("The number of optimal bases should be the same with that of optimal points");
  		  	for(int i=0; i<bases.length; i++){
  		  		s += bases[i].toString()+", "+points[i].toString()+"\n";
  		  	}
  		  	return s;
  		}
  	}
}
