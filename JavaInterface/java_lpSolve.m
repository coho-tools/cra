function [v,x,status,optBasis] = java_lpSolve(f, lp) 
% [v,x,status,optBasis] = java_linp(f, lp) 
% This function implements linear programming solver for coho and coho dual lp.
% status
% 0: 	optimal
% 1:	unbounded
% 2:	infeasible
% 3:	infeasible or unbounded
% v: optimal value if status=1, -Inf if status=2; 0 otherwise
% x: optimal point if status =1, zeros(nx1) matrix otherwise;
% optBasis: optimal basis if status =1, zeros(nx1) matrix otherwise.
if(nargin < 2)
    error('java_linp: not enough arguments');
end

% default value 
v = 0; x = zeros(dim,1); optBasis = zeros(dim,1);

A = -lp.A; b = -lp.b; % Java side uses Ax >= b
Aeq = zeros(0,dim); beq = zeros(0,1);
pos = zeros(dim,1);
bwd = lp.bwd; fwd = lp.fwd;

% write the problem to Java
java_writeComment('BEGIN lp_opt'); % comment in matlab2java
java_writeLabel; % comment in java2matlab
java_writeMatrix(A,'A'); % Ax >= b 
java_writeMatrix(b,'b');
java_writeMatrix(Aeq,'Aeq'); % Aeq x = beq
java_writeMatrix(beq,'beq');
java_writeBoolMatrix(pos,'pos'); % x[pos] >= 0?
if(~isempty(bwd))
	java_writeMatrix(bwd,'bwd'); % bwdT
	java_writeMatrix(fwd,'fwd'); % fwdT 
	java_writeLine('lp = lpGeneral(Aeq, beq, A, b, pos,bwd,fwd);'); 
else
	java_writeLine('lp = lpGeneral(Aeq, beq, A, b, pos);'); 
end
java_writeMatrix(f,'f'); % obj
java_writeLine('lpSoln = lp_opt(lp, f);');
java_writeLine('println(lp_status(lpSoln));');
java_writeDummy; % force java to compute 

% read the result from java
status = java_readNum;
if(status>3 || status <0)
	error('java_linp: unknow status in java side, debug it');
end;

if(status==0)
	fmt = java_format('read');
	java_writeLine( sprintf('println(lp_cost(lpSoln),%s);', fmt) );
	java_writeLine( sprintf('println(lp_point(lpSoln),%s);',fmt) );
	java_writeLine( sprintf('println(lp_basis(lpSoln),%s);',fmt) );
	java_writeDummy; 
	v = java_readNum;
	x = java_readMatrix;
	optBasis = java_readMatrix;
	optBasis = optBasis+1; % the index in java is from 0.
end

java_writeComment('END lp_opt');
