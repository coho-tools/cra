#include <vector>
#include <cstdio>
#include <cstdlib>
#include <cstring>

#include "mex.h"

using namespace std;

void mexFunction ( int nlhs, mxArray *plhs[], int nrhs, const mxArray *prhs[]) {
	const mxArray* hexStr = prhs[0];
	char* res = (char*) mxArrayToString(hexStr);
	union {
		unsigned long long i;
		double    d;
	} value;
	
	char* token, hstr[50];
	vector<vector<double> > mat;
	mat.push_back(vector<double>());
	token = strtok(res, ",]");
	while (token) {	
		if (token[0] == ';') mat.push_back(vector<double>());
		while (*token  && *token != '$') token++;
		sscanf(token, "$%s", hstr);
	
		int len = strlen(hstr);
		if (len < 16) {
			while (len < 16) hstr[len++] = '0';
			hstr[len] = '\0';
		}	
		if (*token) {
			value.i = strtoull(hstr, NULL, 16);
			mat.back().push_back(value.d);
		}

		token = strtok(NULL, ",]");
	}
	while (mat.back().empty()) mat.pop_back();
	
	plhs[0] = mxCreateNumericMatrix(mat.size(), mat[0].size(), mxDOUBLE_CLASS, mxREAL);
	double* pointer = mxGetPr(plhs[0]);

	for (int c = 0, j = 0; j < mat[0].size(); j++) {
		for (int i = 0; i < mat.size(); i++) {
			pointer[c++] = mat[i][j];
		}
	}
}
