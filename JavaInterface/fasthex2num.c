#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include "mex.h"

#define min(a, b) (a>b ? b : a)

void mexFunction ( int nlhs, mxArray *plhs[], int nrhs, const mxArray *prhs[]) {
	if (nrhs < 1 || !mxIsChar(prhs[0])) {
		mexErrMsgTxt("Must have at least one input and should be a string.");
	}
	const mwSize* dims = mxGetDimensions(prhs[0]);
    	if (mxGetNumberOfDimensions(prhs[0]) != 2 || min(dims[0], dims[1]) > 1) {
		mexErrMsgTxt("Input must be a string.");
	}

	const mxArray* hexStr = prhs[0];
	char* res = (char*) mxArrayToString(hexStr);
	union {
		unsigned long long i;
		double    d;
	} value;
	value.i = strtoull(res, NULL, 16);
	plhs[0] = mxCreateDoubleScalar(value.d);
}
