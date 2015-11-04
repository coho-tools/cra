function [v,x,status] = lp_solve(lp,f,solver)
% [v,x,status] = lp_solve(lp,f,solver)
% Find a minimizing point that satisfies lp for each linear objective in f.  
%
% Input:
% 	lp: a LP from lp_create
% 	f: 	optimal direction
% 	solver: a string 
% 		'cplex': cplex LP solver
% 		'matlab': linprog function by Matlab
% 		'java': coho lp solver
% Output: 
% 	v: v(i) is the optimal value of f(:,i);
% 	x: x(i) is the optimal point of f(:,i);
% 	status: status(i) is the status of f(:,i)
% 		0: optimal
% 		1: unbounded
% 		2: infeasible
%  		3: infeasible or unbounded
% 		4: numerical problems
if(nargin<3||isempty(solver))
	solver='java';
end
switch(lower(solver)) 
	case 'java'
		assert(lp_iscoho(lp));
    [v,x,status] = java_lpSolve(f,lp);
	case 'cplex'  % require cplex liences
		A = lp.A; b = lp.b; 
    if(~isempty(lp.bwd)) 
      A = A*lp.bwd; 
    end
		IndEq = length(lp.b)+1:length(b);
		[v,x,status] = cplex_lp([],f,A,b,IndEq);
		%0: error before getting the result
		%1: optimal 2: unbounded 3: infeasible 4: infeasible or unbounded
		%others, see CPX_STAT 
		if(any(status==[1,2,3,4]))
			status = status - 1;
		else
			status = 4;
		end
	case {'matlab','linprog'}
		[x,v,status] = linprog(f,lp.A,lp.b); 
		map = [4,NaN,2,4,1,2,NaN,4,0]; %
		% 1 Function converged to a solution x.
		% 0 Number of iterations exceeded options.MaxIter.
		%-2 No feasible point was found.
		%-3 Problem is unbounded.
		%-4 NaN value was encountered during execution of the algorithm.
		%-5 Both primal and dual problems are infeasible.
		%-7 Search direction became too small. No further progress could be made.
		status = map(status+8);
	otherwise
		error('do not support');
end
if(status==1) 
	v = -Inf; 
end
