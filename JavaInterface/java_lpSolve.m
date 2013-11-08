function [v,x,status,optBasis] = java_lpSolve(f, A, b, Aeq, beq,Ta2w)
% [v,x,status,optBasis] = java_linp(f, A, b, Aeq, beq)
% This function implements linear programming solver for coho and coho dual lp.
% status
% 0: 	optimal
% 1:	unbounded
% 2:	infeasible
% 3:	infeasible or unbounded
% v: optimal value if status=1, -Inf if status=2; 0 otherwise
% x: optimal point if status =1, zeros(nx1) matrix otherwise;
% optBasis: optimal basis if status =1, zeros(nx1) matrix otherwise.
if(nargin < 3)
    error('java_linp: not enough arguments');
end
dim = size(A,2);
if(nargin < 4 || isempty(Aeq))
    Aeq = zeros(0,dim);
end
if(nargin < 5 || isempty(beq))
    beq = zeros(0,1);
end
if(nargin < 6)
	Ta2w = [];
end

% default value 
v = 0; x = zeros(dim,1); optBasis = zeros(dim,1);

% write the problem Java
A = -A; b = -b; % Java side uses Ax >= b
pos = zeros(dim,1);
java_writeComment('BEGIN lp_opt'); % comment in matlab2java
java_writeLabel; % comment in java2matlab
java_writeMatrix(A,'A'); % Ax >= b 
java_writeMatrix(b,'b');
java_writeMatrix(Aeq,'Aeq'); % Aeq x = beq
java_writeMatrix(beq,'beq');
java_writeBoolMatrix(pos,'pos'); % x[pos] >= 0?
if(~isempty(Ta2w))
	Tw2a = inv(Ta2w);
	java_writeMatrix(Ta2w,'Ta2w'); % bwdT
	java_writeMatrix(Tw2a,'Tw2a'); % fwdT 
	java_writeLine('lp = lpGeneral(Aeq, beq, A, b, pos,Tw2a,Ta2w);'); 
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
