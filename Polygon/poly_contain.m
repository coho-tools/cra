function isc = poly_contain(p1,p2,method)
% isc = poly_contain(p1,p2,method)
% This function determinates if p1 contains p2. 
% There are two methods
%  'points': p1 contains p2 <==> p1 contains all vertices of p2
%  'intersect': p1 contains p2 <==> intersect(p1,p2) == p2 
% 				<==> area(p2) == area(intersect(p1,p2))
% The default points method may not be true for non-convex polygons.
% But ususally it is true for non highly illed polygons.

% special case
if(isempty(p2)), isc = true; return; end
if(isempty(p1)), isc = false; return; end

if(nargin<3||isempty(method))
	method = 'pts';
end

switch(lower(method))
case {'pts','points'}
	isc = poly_containPts(p1,p2);
	isc = all(isc);
case 'intersect'
	p = poly_intersect({p1,p2});
	isc = abs(poly_area(p)-poly_area(p2))<eps;
case 'java'
	% same algorithm with 'intersect'
	isc = java_polyContain(p1,p2);
otherwise
	error('do not support');
end
