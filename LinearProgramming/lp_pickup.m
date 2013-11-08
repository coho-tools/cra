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
if(isfield(lp,'Ta2w'))
	error('do not support forward lp');
end

A = lp.A; b = lp.b;
Aeq = lp.Aeq; beq = lp.beq;

n = size(A,2);
in = false(n,1); in(index) = 1; % useful variables

ind = all(A(:,~in)==0,2); % zero for other variables
projA = A(ind,index); % keep the order
%projA = A(ind,in);
projb = b(ind,1);

if(size(Aeq,1)>0)
 	ind = all(Aeq(:,~in)==0,2);
	projAeq = Aeq(ind,in);
	projbeq = beq(ind,1);
else
	projAeq = []; 
	projbeq = [];
end
lp = lp_create(projA,projb,projAeq,projbeq,lp.isnorm);
