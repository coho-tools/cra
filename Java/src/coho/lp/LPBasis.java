package coho.lp;

import coho.common.matrix.*;
import coho.common.number.*;
import java.util.*;
/**
 * The basis of a LP problem is a vector indicate which column/row
 * is in the basis. 
 * The optimal basis for primal & dual problem is the same.
 * For the coho dual lp, the basis is for the coulumn. 
 * For coho lp, the basis indicates the rows in the basic.
 * @author chaoyan
 *
 */
public final class LPBasis implements Comparable<LPBasis>{
	public static enum BasisStatus {FEASIBLE,POSSIBLEFEASIBLE,INFEASIBLE,UNKNOWN};
	public static String statusName(BasisStatus status){
		switch(status){
		case FEASIBLE:
			return "feasible";
		case POSSIBLEFEASIBLE:
			return "possible feasible";
		case INFEASIBLE:
			 return "infeasible";
		default:
			return "unknown";
		}
	}
	public String statusName(){
		return "primal "+statusName(primalStatus)+"; dual "+statusName(dualStatus);
	}
	private BasisStatus primalStatus, dualStatus;
	public BasisStatus primalStatus(){
		return primalStatus;
	}
	public BasisStatus dualStatus(){
		return dualStatus;
	}
	public void setStatus(BasisStatus primalStatus, BasisStatus dualStatus){
		this.primalStatus = primalStatus;
		this.dualStatus = dualStatus;
	}
	public void setPrimalStatus(BasisStatus status){
		primalStatus = status;
	}
	public void setDualStatus(BasisStatus status){
		dualStatus = status;
	}
	final private IntegerMatrix basis;
	public IntegerMatrix basis(){
		return basis;
	}
	//basis must be vector
	public LPBasis(IntegerMatrix basis, BasisStatus primalStatus, BasisStatus dualStatus){
		CohoInteger[] temp = basis.toVector();
		Arrays.sort(temp);//sort it
		this.basis = IntegerMatrix.create(temp);
		this.primalStatus = primalStatus;
		this.dualStatus = dualStatus;
	}
	public LPBasis(IntegerMatrix basis){
		this(basis, BasisStatus.UNKNOWN, BasisStatus.UNKNOWN);
	}
	public LPBasis(BooleanMatrix basis, BasisStatus primalStatus, BasisStatus dualStatus){
		this.basis = basis.find();
		this.primalStatus = primalStatus;
		this.dualStatus = dualStatus;
	}
	public LPBasis(BooleanMatrix basis){
		this(basis,BasisStatus.UNKNOWN, BasisStatus.UNKNOWN);
	}
	public static LPBasis create(IntegerMatrix basis, BasisStatus primalStatus, BasisStatus dualStatus){
		return new LPBasis(basis, primalStatus, dualStatus);
	}
	public static LPBasis create(IntegerMatrix basis){
		return new LPBasis(basis);
	}
	public static LPBasis create(BooleanMatrix basis, BasisStatus primalStatus, BasisStatus dualStatus){
		return new LPBasis(basis,primalStatus,dualStatus);
	}
	public static LPBasis create(BooleanMatrix basis){
		return new LPBasis(basis);
	}
	
	/**
	 * N should be no less than the max value of basis;
	 */
	public BooleanMatrix booleanBasis(int N){
		if(N <= basis.V(basis.length()-1).intValue()){
			throw new LPError("N must be greater than the max value of the basis");
		}
		BooleanMatrix result =  BooleanMatrix.create(N,1).zeros();
		for(int i=0; i<basis.length(); i++){
			result.assign(true,basis.V(i).intValue());
		}
		return result;
	}
	public LPBasis replace(int evictCol, int newCol, BasisStatus primalStatus, BasisStatus dualStatus){
		if(evictCol==newCol)
			return this;//we don't change the status
		int maxCol =basis.V(basis.length()-1).intValue();// assume it's sorted, the max value is in the last.
		boolean[] isBasis = new boolean[Math.max(newCol,maxCol)+1];//the first col is 0
		for(int i=0;i<basis.length();i++){
			if(newCol == basis.V(i).intValue())
				throw new LPError("LPbasis:replace: column "+newCol+" is already in basis");
			isBasis[basis.V(i).intValue()]=true;
		}
		isBasis[evictCol]=false;
		isBasis[newCol]=true;
		return LPBasis.create(BooleanMatrix.create(isBasis),primalStatus,dualStatus);
	}
//	private LPBasis replace(int evictCol, int newCol){
//		return replace(evictCol,newCol,primalStatus,dualStatus);
//	}
	public int compareTo(LPBasis obj){
		IntegerMatrix thatBasis = obj.basis();
		for(int i=0;i<basis.length();i++){
			if(basis.V(i).intValue()>thatBasis.V(i).intValue())
				return 1;
			if(basis.V(i).intValue()<thatBasis.V(i).intValue())
				return -1;
		}
		return 0;
	}	
	public String toString(){
		return "Basis("+ basis.stringify(null)+")\n"+statusName();
	}
	@Override
	public boolean equals(Object obj){
		if(obj instanceof LPBasis){
			return (compareTo((LPBasis)obj)==0);
		}
		return false;
	}
	public int hashCode(){
		int hashCode = 0;
		for(int i=0; i<basis.length(); i++){
			hashCode = hashCode<<1^basis.V(i).intValue();
		}
		return hashCode;
	}
}
