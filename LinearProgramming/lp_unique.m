function [lp,I,Ieq] = lp_unique(lp)
% [lp,I,Ieq] = lp_unique(lp)
%  The function remove duplicated constraints from lp. 
%  I and Ieq are the index of the constraints
if(lp_isempty(lp)), I = []; Ieq = []; return; end
[A,b,I] = do_unique(lp.A,lp.b);
[Aeq,beq,Ieq] = do_unique(lp.Aeq,lp.beq);

lp = lp_create(A,b,Aeq,beq,lp.isnorm);

function [A,b,I] = do_unique(A,b)
	nc = size(A,2);
	M = [A,b];
	[M,I] = unique(M,'rows');
	A = M(:,1:nc); b = M(:,nc+1:end);
%end
