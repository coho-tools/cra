function lp = lp_pickup(lp,index)
% This function finds all conditions which have non-zero
% coefficients only for specified variables and return a 
% lower-dimensional lp.
% This function is 'projecting' a lp onto a subspace. 
% However, we do not compute the exact result but 'pickup'
% conditions on this subspace. 
% This is very useful for coho lp. 
if(isempty(lp))
	return;
end

%do not support the forward lp now. 
assert(isempty(lp.bwd))

A = lp.A; b = lp.b;

n = size(A,2);
in = false(n,1); in(index) = 1; % useful variables

ind = all(A(:,~in)==0,2); % zero for other variables
projA = A(ind,index); % keep the order
projb = b(ind,1);

lp = lp_create(projA,projb);
