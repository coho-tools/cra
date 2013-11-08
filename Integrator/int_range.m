function range = int_range(ldi,lp)
% range = int_range(ldi,lp)
% compute the lower/upper bound of linear differential inclusion in a region bdyLP 
%
% max/min xdot = Ax+b+/-u 
% 		x \in lp

[vs,xs,flags] = lp_opt(lp,[ldi.A',-ldi.A']); % min,max
if(any(flags~=0))
	error('problem evaluating minimum/maximum for model in integrator');
end
n = size(ldi.A,2);
range = [vs(1:n),-vs(n+1:2*n)];
range = range+[-ldi.u,ldi.u]+repmat(ldi.b,1,2);
