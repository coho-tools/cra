function p = java_polyIntersect(p1,p2)
% This function compute the intersection of two pgons p1 and p2 using java engine.
%	p1, p2:	Two simple polygons. 
%	p:	The intersection of p1 and p2. return [] if the intersection are empty
% This function my throw exception 


java_writeComment('BEGIN poly_intersect'); 
java_writeLabel;
java_writePoly(p1,'p1');
java_writePoly(p2,'p2');
java_writeLine('p = intersect(p1, p2);');
java_writeLine(sprintf('println(p, %s);',java_format('read')));
java_writeDummy;
p = java_readPoly;
java_writeComment('END poly_intersect'); 
