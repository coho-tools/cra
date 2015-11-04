function hull = lp_project(lp,plane,tol,opt,method)
% hull = lp_project(lp,plane,tol,opt,method)
% This function project a high-dimensional LP (polytope) onto 
% two dimensional x-y space
% 
% Input:
% 	lp: a coho lp. 'bwd' field is allowed. 
% 	plane: index of orthogonal axis plane, e.g [1,2] 
%	tol: error tolerance (area percent)
% 	opt: a structure with fields (parameters for 'matlab' method);
%		angles: initial optimization angles 
%		niters: maximum allowed number of iterations 
%		method: algorithm to reduce error.  possible values are 
%			'hull','both','regular'. see lp_projectAsympt
% 	method: a string of 
% 		'java': use java LP solver (only Coho LP)
% 		'matlab': use matlab LP solver to find an overapproximated result  
% Output:
% 	hull: projection polygon 
%
% NOTE: 
%	'java' only supports coho lp. 
% 	You can try 'matlab' for general LP. But remember change the default 
% 	lp solver  to 'cplex' or 'matlab'. 
if(nargin<3||isempty(tol))
	tol = 0;
end
if(nargin<4||isempty(opt))
	opt = [];
end
if(nargin<5||isempty(method))
	method = cra_cfg('get','projSolver');
end
if(tol<=eps)
	method = 'java'; % only java routine can compute exact solution
end
if(lp_isempty(lp))
	error('empty lp');
end

switch(lower(method))
	case 'java' 
		%assert(lp_iscoho(lp));
		m = eye(size(lp.A,2));
		x = m(:,plane(1)); y = m(:,plane(2));
		hull = java_lpProject(lp,x,y,tol);
		hull = poly_create(hull);
	case {'matlab'}
		hull = lp_projectAsympt(lp,plane,tol,opt);
	case {'javamatlab'}
		% This may happen because faceLP is infeasible
		% (lp_createByHull may return under-approximated result)
		hull = lp_project(lp,plane,tol,opt,'java');
		if(isempty(hull))
			hull = lp_project(lp,plane,tol,opt,'matlab');
		end
	case {'matlabjava'}
		hull = lp_project(lp,plane,tol,opt,'matlab');
		if(isempty(hull))
			hull = lp_project(lp,plane,tol,opt,'java');
		end
	otherwise
		error('do not support');
end

