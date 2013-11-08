function p = java_polyUnion(projs)
% p = java_polyUnion(projs)
% The function computes the union of a set of polygons using the java engine
%   projs: a cell of polygons
%   p: the union of these polygons
% This function my throw exception 

fmt = java_format('read');% read
java_writeComment('BEGIN poly_union'); 
java_writeLabel;
unionString = 'u = union(';
for i = 1:length(projs)
	java_writePoly(projs{i},['p',num2str(i)]);
    unionString = sprintf('%s p%d,', unionString, i);
end
unionString = [unionString(1:end-1),');'];
java_writeLine(unionString);
java_writeLine( sprintf('println(u, %s);', fmt) );
java_writeDummy;
p = java_readPoly;
java_writeComment('END poly_union'); 
