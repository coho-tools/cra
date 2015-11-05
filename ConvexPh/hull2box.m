function [bbox,bboxLP] = hull2box(dim,planes,hulls)
% compute bbox 
bbox = repmat([Inf,-Inf],dim,1);
X = 1; Y = 2; LO = 1; HI = 2;
for i=1:size(planes,1)
  hull = hulls{i};  plane = planes(i,:);
  bbox(plane(X),LO) = min(bbox(plane(X),LO),min(hull(X,:)));
  bbox(plane(X),HI) = max(bbox(plane(X),HI),max(hull(X,:)));
  bbox(plane(Y),LO) = min(bbox(plane(Y),LO),min(hull(Y,:)));
  bbox(plane(Y),HI) = max(bbox(plane(Y),HI),max(hull(Y,:)));
end

assert(~any(isinf(bbox(:))));
bboxLP = lp_createByBox(bbox);
