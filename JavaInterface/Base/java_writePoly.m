function java_writePoly(p, name)
% java_writePoly(p, polyname)
%
% defines a series of vertices as a polygon for Mark's CG code
%
% each column of p is a 2D vertex
% polyname is a string which the polygon will be assigned to for
%       manipulation by the java interpreter
% fid is an open file identifier to the java interpreter
%
M = p;
mtype = 'polygon';
java_writeMatrix(M,name,mtype);

