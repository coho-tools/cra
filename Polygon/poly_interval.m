function bbox = poly_interval(poly,bbox)
% bbox = poly_interval(poly,bbox)
% This function computes the intersection of a polygon and a rectangles
% 	and return the minimum rectangle that contains the intersection region.
% Inputs
% 	poly: a polygon from poly_create
%	bbox: [xl,xh;yl,yh]
% Output
% 	bbox: intersection of poly and bbox. [] if the result is empty.
if(isempty(poly)||isempty(bbox)), bbox = zeros(2,0); return; end

% compute the intersection  points
x0 = bbox(1,1); x1 = bbox(1,2); y0 = bbox(2,1); y1 = bbox(2,2);
% find points in the box 
ind=(poly(1,:)>=x0)&(poly(1,:)<=x1)&(poly(2,:)>=y0)&(poly(2,:)<=y1);
points = poly(:,ind);
% find intersection points of polygon and four segments 
% include endpoint if it is in the polygon
lines = {[x0,x1;y0,y0],[x1,x1;y0,y1],[x1,x0;y1,y1],[x0,x0;y1,y0]};
for i=1:length(lines)
	pts = poly_intersectLine(poly,lines{i},false);
	points=[points,pts];
end 
bbox1 = [min(points,[],2),max(points,[],2)];

% consider computation error
if(~isempty(bbox1))
	tol = eps*10;
	assert(all(bbox1(:,1)+tol>=bbox(:,1))&all(bbox1(:,2)-tol<=bbox(:,2)));
	bbox = [max(bbox1(:,1),bbox(:,1)),min(bbox1(:,2),bbox(:,2))];
else
	bbox = bbox1; % []
end

% NOTE, computing the intersection of poly and bbox is not correct. 
% First, when the result is non-simple, poly_intersect can not produce 
% correct result. Second, when bbox is a segment, we have to use 
% poly_intersectLine to compute the intersection point. 
% Thus, we remove the method which uses poly_intersect. 
