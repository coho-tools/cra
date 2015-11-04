function ph = ph_createByBox(bbox,planes) 
if(nargin<1)
  error('not enough parameter') 
end
if(nargin<2) 
  planes = zeros(0,2); 
end

if(isempty(bbox)||any(bbox(:,2)<bbox(:,1)))
	ph = []; return;
end
dim = size(bbox,1); 

if(isempty(planes)) % default planes
  X = (1:dim)'; Y = X+1; Y(end) = 1; 
  planes = [X, Y];
end

ns = size(planes,1); hulls = cell(ns,1);
for i=1:ns 
	rec = bbox(planes(i,:),:); 
	hulls{i} = poly_createByBox(rec);
end
bboxLP = lp_createByBox(bbox);
hullLP = bboxLP;

type = 2; % bbox
status = 0; % projectd

ph = ph_create(dim, planes, hulls, hullLP, bbox, bboxLP, type, status);
