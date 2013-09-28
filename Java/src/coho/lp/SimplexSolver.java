package coho.lp;
import coho.common.matrix.*;
public interface SimplexSolver extends LPSolver{
	LPResult opt(LPBasis proposedBasis);
	LPResult opt(Matrix c, LPBasis proposedBasis);
}
