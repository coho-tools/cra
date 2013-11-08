function lp = lp_create(A, b, Aeq, beq, isnorm)
% lp = lp_create(A, b, Aeq, beq, isnorm) 
%   Create a linear program structure.
%   Fields:
%		A * x <= b
%		Aeq * x = beq
%		isnorm == 1, indicates that each row of A and Aeq has unit L2 norm.
%
%	Require:
%		size(A,1)==size(b,1) 
%		size(Aeq,1)==size(beq,1)
%		size(A,2)==size(Aeq,2)	
%
%   Default parameter values:
%		A and b are mandatory.
%		If Aeq/beq are omitted or empty, zeros(0, size(A,2)) and zeros(0,1) are used.
%		If A/b are or empty, zeros(0, size(Aeq,2)) and zeros(0,1) are used.
%		If A/b and Aeq/beq are empty, A/Aeq = zeros(0,0), b/beq = zeros(0,1).
%		If isnorm is omitted, false is used.
%
%   NOTE
%       Duplicate constraints are not removed.

if(~any(nargin == [2, 4, 5]))
    error('bad argument count for lp_create()');
end
if(nargin < 3)
    Aeq = []; beq = [];
end
if (nargin < 5||isempty(isnorm))
    isnorm = false;
end

dim = max([size(A,2), size(Aeq,2)]); % A or Aeq might be []

if(isempty(A)) 
	A = zeros(0, dim); b = zeros(0, 1);
end

if(isempty(Aeq))
    Aeq = zeros(0, dim); beq = zeros(0, 1);
end

lp = struct('A',A,'b',b,'Aeq',Aeq,'beq',beq,'isnorm',isnorm);
