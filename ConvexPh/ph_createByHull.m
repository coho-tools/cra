function ph = ph_createByHull(dim, planes, hulls) 
if(nargin<3)
	error('not enough parameters');
end

ns = size(planes,1);
assert(ns>=dim);

% compute hullLP
m = eye(dim); 
hullLP = lp_create(zeros(0,dim), [],[],[],1);
for i=1:ns
	dims = m(:,planes(i,:));
	hullLP = lp_and(hullLP,lp_createByHull(hulls{i},dims));
end

% compute bbox 
[bbox,bboxLP] = hull2box(dim,planes,hulls);
%bbox = repmat([Inf,-Inf],dim,1);
%X = 1; Y = 2; LO = 1; HI = 2;
%for i=1:ns
%  hull = hulls{i};  plane = planes(i,:);
%  bbox(plane(X),LO) = min(bbox(plane(X),LO),min(hull(X,:)));
%  bbox(plane(X),HI) = max(bbox(plane(X),HI),max(hull(X,:)));
%  bbox(plane(Y),LO) = min(bbox(plane(Y),LO),min(hull(Y,:)));
%  bbox(plane(Y),HI) = max(bbox(plane(Y),HI),max(hull(Y,:)));
%end
%
%assert(~any(isinf(bbox(:))));
%bboxLP = lp_createByBox(bbox);

type = 1;    % convex
status = 0;  % projected
ph = ph_create(dim, planes, hulls, hullLP, bbox, bboxLP, type, status);
