package coho.lp;
import coho.common.matrix.*;
import coho.common.number.*;
/**
 * The result of a LP problem, returned by the LP solver.
 * @author chaoyan
 *
 */
/*
 * redefine the LPresult interface. Only status and optimal cost are necessary
 */
public interface LPResult{
	public static enum ResultStatus{OK,POSSIBLEOK,INFEASIBLE,UNBOUNDED,INFEASIBLEORUNBOUNDED,UNKNOWN}; 
    public double optcost();	//return the double value for optCost
    public CohoNumber optCost();	//return the CohoNumber for optCost
    public LPBasis optBasis();//return one optimal basis
    public LPBasis[] optBases();//return all optimal basis;
    public Matrix optPoint();//return one optimal points
    public Matrix[] optPoints();//return all optimal points;
    public ResultStatus status();	//return the status of the result
    public String statusName();
    public String toString();
    public String toString(boolean showAll);
}