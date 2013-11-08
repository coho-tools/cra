function v= java_getParams(key)
% v= java_getParams(key)
% This function get parameters of java process
% key: 1: maxNewEdgeLen for poly_reduce
%	   2: maxNewPointDist for poly_reduce

java_writeLine( sprintf('v = get(%f);',key) );
java_writeLine( sprintf('println(v, %s);',java_format('read')) );
java_writeDummy;
v = java_readNum;
