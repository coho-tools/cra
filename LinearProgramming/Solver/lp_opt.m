function [vs,xs,status] = lp_opt(lp,f,solver)
% [vs,xs,status] = lp_opt(lp,f,solver)
% Find a minimizing point that satisfies lp for each linear objective
%	 in f.  Each column of f specifies a linear objective function.
%	 The status return value can be tested to detect infeasibility.
%
% Input:
% 	lp: a LP from lp_create
% 	f: 	optimal directons
% 	solver: a string 
% 		'cplex': cplex LP solver
% 		'matlab': linprog function by Matlab
% 		'java': coho lp solver
% 		'cplexJava': try cplex first, use java if fail
% 		'matlabJava': try linprog first, use java if fail
% Output: 
% 	vs: vs(i) is the optimal value of f(:,i);
% 	xs: xs(:,i) is the optimal point of f(:,i);
% 	status: status(i) is the status of f(:,i)
% 		0: optimal
% 		1: unbounded
% 		2: infeasible
%  		3: infeasible or unbounded
% 		4: numerical problems
% NOTE: this function assume lp is CohoLP by default and use the default solver 
% 	if not provided. use cra_cfg('set','lpSolver',solver) to change the default solver
% 	However, if the lp is a general LP, please specify the solver and do not use 'java'

if(nargin<3||isempty(solver))
	solver=cra_cfg('get','lpSolver');
end
if(lp_isempty(lp))
	error('empty lp');
end
if(size(f,1)~=size(lp.A,2))
    error('lp_opt: f and lp are incompatible');
end

[dim,nf] = size(f);
vs = zeros(nf,1); 
xs = zeros(dim,nf);
status = zeros(nf,1);

for i=1:nf
	switch(lower(solver))
	case {'matlab','cplex','java'}
		[v,x,s] = lp_solve(lp,f(:,i),solver);
 	case 'matlabjava'
		[v,x,s] = lp_solve(lp,f(:,i),'matlab');
		if(s~=0)
			[v,x,s] = lp_solve(lp,f(:,i),'java');
		end
 	case 'cplexjava'
		[v,x,s] = lp_solve(lp,f(:,i),'cplex');
		if(s~=0)
			[v,x,s] = lp_solve(lp,f(:,i),'java');
		end
	otherwise
		erorr('do not support');
	end
	vs(i) = v; xs(:,i) = x(:); status(i) = s;
end
