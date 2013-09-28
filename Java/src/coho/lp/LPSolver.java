package coho.lp;

import coho.common.matrix.*;

public interface LPSolver{
	LPResult opt();	
//	LPresult opt(LPbasis proposedBasis);
	LPResult opt(Matrix c);
//	LPresult opt(Matrix c, LPbasis proposedBasis);
	LP lp();
}
