function lp = lp_and(lp1, lp2)
% lp = lp_and(lp1, lp2) 
% The function concatenate the constraintsof lp1 and lp2.  
% 	lp.A = [lp1.A; lp2.A];
% 	lp.b = [lp1.b; lp2.b];
% 	lp.Aeq = [lp1.Aeq; lp2.Aeq];
% 	lp.beq = [lp1.beq; lp2.beq];
% 	lp.isnorm = lp1.isnorm & lp2.isnorm;
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
Aeq = [lp1.Aeq; lp2.Aeq];
beq = [lp1.beq; lp2.beq];
isnorm = lp1.isnorm & lp2.isnorm;
lp = lp_create(A,b,Aeq,beq,isnorm);
