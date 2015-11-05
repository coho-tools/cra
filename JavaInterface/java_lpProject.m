function hull = java_lpProject(lp, x, y,tol)
% hull = java_lpProject(lp, x, y,tol)
%
%  Inputs:
%    lp is a linear program
%    x and y are vectors of the same size
%
%  Outputs:
%    hull: a matrix
%      The columns hull are the vertices of a counterclockwise tour
%      of the projection of the polytope corresponding to lp onto the
%      plane that contains x, y, and the origin.
%		 	 	Return [] if the lp is not feasible.
%
% Uses Java LP engine
n = size(lp.A,2);

% send LP to java process
A = -lp.A; b = -lp.b; % Java side uses Ax >= b
bwd = lp.bwd; fwd = lp.fwd;
Aeq = zeros(0,n); beq = zeros(0,1);
pos = zeros(n,1);
bwd = lp.bwd; fwd = lp.fwd;

java_writeComment('BEGIN lp_project'); % comment in matlab2java
java_writeLabel; % comment in java2matlab
java_writeMatrix(A,'A'); % Ax >= b 
java_writeMatrix(b,'b');
java_writeMatrix(Aeq,'Aeq'); % Aeq x = beq
java_writeMatrix(beq,'beq');
java_writeBoolMatrix(pos,'pos'); % x[pos] >= 0?
if(~isempty(bwd))
	java_writeMatrix(bwd,'bwd'); % bwdT
	java_writeMatrix(fwd,'fwd'); % fwdT 
	java_writeLine('lp = lpGeneral(Aeq, beq, A, b, pos,fwd,bwd);'); 
else
	java_writeLine('lp = lpGeneral(Aeq, beq, A, b, pos);'); 
end
% projection directions
java_writeMatrix(x,'xx');
java_writeMatrix(y,'yy');
% project
java_writeLine(sprintf('lpProj = lp_project(lp, xx, yy, %f);',tol));
java_writeLine(sprintf('println(lp_point(lpProj),%s);',java_format('read')));
java_writeDummy;
java_writeComment('END lp_project'); % comment in matlab2java

% get the result
hull = java_readMatrix; 
if(size(hull,2)<3) 
	hull = zeros(2,[]); 
	return; 
end; 

% convert 2D
hull = [x,y]\hull; 
