function lp = lp_and(lp1, lp2)
% lp = lp_and(lp1, lp2) 
% The function concatenate the constraintsof lp1 and lp2.  
% 	lp.A = [lp1.A; lp2.A];
% 	lp.b = [lp1.b; lp2.b];
if(lp_isempty(lp2)) 
	lp = lp1;
  return;
end;
if(lp_isempty(lp1))
	lp = lp2;
  return;
end;

A = [lp1.A; lp2.A];
b = [lp1.b; lp2.b];

assert(all(lp1.bwd(:)==lp2.bwd(:)))
assert(all(lp1.fwd(:)==lp2.fwd(:)))
lp = lp_create(A,b,lp1.bwd,lp1.fwd); 
