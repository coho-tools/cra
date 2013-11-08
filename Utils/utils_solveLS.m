function v = utils_solveLS(As,bs)
% v = utils_solveLS(As,bs)
%	solve a set of linear systems 
%      A1*x = b1; A2*x = b2; ... ; An*x = bn;
%   A = [A1,A2,...,An], b = [b1,b2,...,bn]
%   each column of v is a solution for a linear systems. 

% do not support now. 
%	status: 	0, unique solution
%				1, inifite solutions
%				2, no solution
m = size(As,1); n = size(bs,2); 

% Covert to block diagnal matrix
 N = m*n;
 p = (1:N); 
 i = reshape(repmat(reshape(p,m,n),m,1),[],1);
 j = reshape(repmat(p,m,1),[],1);
 A = sparse(i,j,As(:),N,N);
 b = reshape(bs,[],1);
 v = reshape(A\b,m,n);
