function ph = ph_createByBox(dim,planes,bbox)
% ph = ph_createByBox(dim,planes,bbox)
% This function create a hypre-rectangle projectagon from a bounding box
%
% Input:
%	dim,planes: same with ph_create
%	bbox: dimx2 matrix, bbox(:,1/2) is the lower/upper bound.
%
% Output:
% 	ph:  a projectagon with bounding box type, use ph_convert() to convert to 
% 	other types.

if(isempty(bbox)||any(bbox(:,2)<bbox(:,1)))
	ph = []; return;
end

ns = size(planes,1); hulls = cell(ns,1);
for i=1:ns
	rec = bbox(planes(i,:),:); 
	hulls{i} = poly_createByBox(rec);
end

type = 2; iscanon = true;
ph = ph_create(dim,planes,hulls,hulls,type,iscanon);
