function lp = lp_create(A, b, bwd, fwd) 
% function lp = lp_create(A, b, bwd, fwd) 
%   Create a COHO LP or Advanced COHO LP 
%   Fields:
%		A * x <= b
%   A * bwd * x <= b
%
%	Require:
%		size(A,1)==size(b,1)=size(bwd,1)=size(bwd,2)
%   bwd*fwd = eye(N);
%
%   Default parameter values:
%		A and b are mandatory.
%   bwd/fwd are [] by default
%
if(~any(nargin == [2, 4]))
  error('bad argument count for lp_create()');
end
if(nargin < 3)
  bwd = []; fwd = []; 
end

%assert(~isempty(A) & ~isempty(b))
lp = struct('A',A,'b',b,'bwd',bwd,'fwd',fwd); 
