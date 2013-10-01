function ph = ph_create(dim,planes,hulls,polys,type,iscanon)
% ph = ph_create(dim,planes,hulls,polys,type,iscanon)
% Creates a projectahedron
% 
% 	dim:   dimension of projectagon
%   planes:projection planes. plane(i,1/2) is the x/y axis of the i^th
%   		projection plane. slice i is projected onto the plane spanned 
% 			by orthogonal axises specified by planes(i,:) 
%   hulls: hulls{i} is the matrix of 2D hull vertices for slice i
%   polys: polys{i} is the matrix of 2D polygon vertices for slice i
% 	type:  0. non-convex 1. convex 2. hyper-rectangle (0 by default)
%   iscanon: true if polys are feasible to each other (false by default)
%
% ph is a structu with the following fileds
%	dim, planes, ns, hulls, polys, bbox, hullLP, bboxLP, type, iscanon.
% ph = [] if the bounding box is empty
%
% ph may not be canonical, call ph_canon trim non-feasible region 
%
% Examples
% 	dim = 3; planes = [1,2; 2,3]; 
% 	hulls{1} = poly1; hulls{2} = poly2;
% 	ph = ph_create(dim,planes,hulls,[]); % create a convex non-canonical projectagon
% 	ph = ph_create(dim,planes,hulls,polys); % create a non-convex non-canonical projectagon
% 	ph = ph_create(dim,planes,hulls,polys,type,iscanon); 
%
if(nargin<3)
	error('not enough parameters');
end
if(nargin<4)
	polys = [];
end
if(nargin<5)
	if(isempty(polys))
		type = 1; % convex
	else
		type = 0; % non-convex 
	end
end
if(nargin<6||isempty(iscanon))
	iscanon = false;
end

ns = size(planes,1);
if(isempty(polys))
	polys = hulls;
end

% compute LP
m = eye(dim); 
hullLP = lp_create(zeros(0,dim), [],[],[],1);
for i=1:ns
	dims = m(:,planes(i,:));
	hullLP = lp_and(hullLP,lp_createByHull(hulls{i},dims));
end

% compute lp and bbox (for convex, hyper-rectangle) 
bbox = lp_box(hullLP);
if(isempty(bbox))
	ph = []; return;
else
	bboxLP = lp_createByBox(bbox);
end

ph = struct('dim',dim, 'planes',planes, 'type',type, 'iscanon', iscanon, ...
	'ns',ns, 'hulls',{hulls}, 'polys',{polys}, 'bbox',bbox, ...
	'hullLP',hullLP,'bboxLP',bboxLP);
