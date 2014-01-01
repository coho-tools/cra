function ph = ph_createByLP(dim,planes,lp)
% ph = ph_createByLP(dim,planes,lp)
% This function create a convex canonical projectagon from a lp.
%
% Input:
%	dim,planes: same with ph_create
%	lp: a linear program from LinearProgramming package
%
% Output:
% 	ph:  a projectagon with convex type, use ph_convert() to convert to other types.

% empty lp OR infeasible OR a single point 
%if(isempty(lp)||~lp_feasible(lp)) % infeasible
if(isempty(lp) || ~lp_feasible(lp) || all(diff(lp_box(lp),[],2)==0))
	ph = []; return; 
end

ns = size(planes,1); hulls = cell(ns,1);
for i=1:ns
	hulls{i} = lp_project(lp,planes(i,:));
	if(isempty(hulls{i})), ph = []; return; end % unbounded
end
type = 1; iscanon =true;
ph = ph_create(dim, planes, hulls, hulls, type, iscanon); 
