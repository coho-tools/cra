function isf = lp_feasible(lp)
% isf = lp_feasible(lp)
% The function compute the feasibility of a linear program
%
% Inputs:
% lp: 	feasible region of standard lp. Ax<=b. 
% isf: 	1 if lp is feasible (optimal result found or unbounded)
%      	2 infeasilbe or unbounded
%      	0 infeasible

% Algorithm: We use one optimal direction and try to solve the lp. 
% If the lp is infeasible, the solver return infeasible
% If the lp is feasible, the solver must return feasible
% If the lp is unbounded, the solver may return unbouned or feasible
% If the dual LP is infeasible, the solver return "infeasible or
% unbounded", we return -1 here. 
if(lp_isempty(lp)),isf=false;return; end
n = size(lp.A,2);
optDir = ones(n,1);
[~,~,status] = lp_opt(lp,optDir);
% NOTE -1 is a problem. We do not know if it is feasible or not
mem=[1,1,0,-1,0]; %optimal,unbouned,infeasible,infeasible or unboned, numerical problem.
isf = mem(status+1);
