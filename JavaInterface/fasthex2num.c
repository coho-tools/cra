#include <stdio.h>
#include <stdlib.h>
#include "mex.h"

void mexFunction ( int nlhs, mxArray *plhs[], int nrhs, const mxArray *prhs[]) {
	const mxArray* hexStr = prhs[0];
	char* res = (char*) mxArrayToString(hexStr);
	union {
		long long i;
		double    d;
	} value;
	value.i = strtoll(res, NULL, 16);
	plhs[0] = mxCreateDoubleScalar(value.d);
}