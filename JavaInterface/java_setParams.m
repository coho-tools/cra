function java_setParams(key,value)
% java_setParams(key,value)
% This function set java process parameters
% key: 1: maxNewEdgeLen for poly_reduce
%	   2: maxNewPointDist for poly_reduce
str = sprintf('set(%f,%f);',key,value);
java_writeLine(str);
