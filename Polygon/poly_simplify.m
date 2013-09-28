function p = poly_simplify(p,tol,reduceEdge)
% p = poly_simplify(p,tol,reduceEdge)

% special case
if(isempty(p))
	return;
end

if(nargin<2 || isempty(tol))
    tol = 0.02;
end
if( nargin<3 || isempty(reduceEdge) )
    reduceEdge = true;
end
if(tol>0)
	p = java_polySimplify(p,tol,reduceEdge);
	p = poly_create(p);
end
