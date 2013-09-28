function [hulls,polys] = ph_project(ph,planes)
% [hulls,polys] = ph_project(ph,planes)
% This function projects a projectagon onto two-dimensional faces

if(nargin<2), error('not enough parameters'); end;

ns = size(planes,1); 
hulls = cell(ns,1); polys = cell(ns,1); 

% empty projectagon
if(ph_isempty(ph)), return; end

for i=1:ns
	plane = planes(i,:);
	ind = find(all(ph.planes==repmat(plane,ph.ns,1),2));
	if(~isempty(ind))
		polys{i} = ph.polys{ind};
		hulls{i} = ph.hulls{ind};
		continue;
	end
	ind = find(all(ph.planes==repmat(plane([2,1]),ph.ns,1),2));
	if(~isempty(ind))
		poly = ph.polys{ind};
		hull = ph.hulls{ind};
		polys{i} = poly([2,1],end:-1:1); 
		hulls{i} = hull([2,1],end:-1:1);
		continue;
	end
	% compute by project 
	if(ph.type==2) % bbox 
		hull = poly_createByBox(ph.bbox(plane,:)); 
	else 
		hull = lp_project(ph.hullLP,plane); 
	end 
	hulls{i} = hull; polys{i} = hull;
end
