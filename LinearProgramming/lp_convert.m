function no_eq = lp_convert(lp_eq)
% no_eq = lp_convert(lp_eq)
%   Convert equality constraints to pairs of inequality constraints
%   I.e. constraints of the form
%     Aeq * x == beq
%   are replaced by
%   (Aeq * x <= beq) & (-Aeq * x <= -beq)
if(lp_isempty(lp_eq)), no_eq = []; return; end
A = [lp_eq.A; lp_eq.Aeq; -lp_eq.Aeq]; 
b = [lp_eq.b; lp_eq.beq; -lp_eq.beq];
no_eq = lp_create(A,b,[],[],lp_eq.isnorm);
