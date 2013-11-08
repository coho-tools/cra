function p = java_polySimplify(p,tol,reduceEdge)
% p = java_polySimplify(p,tol,reduceEdge)
% This function reduce the number of polygon with little increase of area. 
% Parameters
%	p:	polygon to reduce. p(1,:) is x value and p(2,:) is y value.
%	tol:	The maximum percent of area increase(0-1). The default value is 0.02.
%			if tol is greater than 3, than it is the maximum points allowed.
%	reduceEdge:	If it is true, can reduce convex points, otherwise, only reduce concave points.
%	You can set the maximum length of the new edge or maximum distance of new points 
% 	by java_setParams function.

java_writeComment('BEGIN poly_reduce'); 
java_writeLabel;
java_writePoly(p,'p');
java_writeLine(sprintf('r = reduce(p, %f,%i);',tol,reduceEdge));
java_writeLine(sprintf('println(r, %s);', java_format('read')));
java_writeDummy;
p = java_readPoly;
java_writeComment('END poly_reduce'); 

