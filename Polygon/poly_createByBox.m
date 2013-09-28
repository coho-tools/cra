function p = poly_createByBox(bbox)
% p = poly_createByBox(bbox)
% The function constructs an counter-clock wise 2D polygon given a bounding box.
% bbox: 2x2 matrix, bbox(:,1/2) is the lower/upper bound.
if(isempty(bbox)), p = zeros(2,0); return; end
x = bbox(1,[1,2,2,1]); y = bbox(2,[1,1,2,2]);
p = [x;y];
