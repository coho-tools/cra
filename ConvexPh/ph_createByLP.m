function ph = ph_createByLP(hullLP, planes) 
if(nargin<1)
	error('not enough parameters');
end
if(nargin<2) 
  planes = zeros(0,2); 
end
% Assume hullLP is COHO LP 
dim = size(hullLP.A,2);  

if(isempty(planes)) % default planes
  X = (1:dim)'; Y = X+1; Y(end) = 1; 
  planes = [X, Y];
end

ns = size(planes,1); 
hulls = cell(ns,1);
for i=1:ns
	hull = lp_project(hullLP,planes(i,:));
	if(isempty(hull)) 
    ph = []; return; 
  end 
  hulls{i} = hull;
end

ph = ph_createByHull(dim,planes,hulls); 
