function ph = ph_intersectLine(ph,line,eps)
% ph = ph_intersectLine(ph,line,eps)
% This function computes the intersection of a projectagon
% 	and a line Ax=b.
% The result is dim-1 dimensional, not a projectagon. 
% 	we approximate it by bloating it by eps. 
if(nargin<3||isempty(eps))
	eps = 1e-6;
end

Aeq = line.Aeq; beq = line.beq;
lp = lp_create([],[],Aeq,beq);
lp = lp_bloat(lp,eps/2);
ph = ph_intersectLP(ph,lp);
