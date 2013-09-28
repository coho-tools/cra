function isc = java_polyContain(p1,p2)
% isc = java_polyContain(p1,p2)
%	p1,p2:	two pgons with p(1,:) as x and p(2,:) as y.
%   isc:	1 if p1 contains p2 
% 			0 if p1 does not contain p2 
% This function my throw exception 

java_writeComment('BEGIN poly_contain'); 
java_writeLabel;
java_writePoly(p1, 'p1');
java_writePoly(p2, 'p2');
java_writeLine('p = contain(p1, p2);');
java_writeLine(sprintf('println(p, %s);', java_format('read')));
java_writeDummy;
isc = java_readNum;
java_writeComment('END poly_contain'); 
