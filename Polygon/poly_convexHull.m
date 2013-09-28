function hull = poly_convexHull(p)
% hull = poly_convexHull(p)
% This function compute the convex hull of a polygon (or a set of points)
% It calls convhull function (see http://www.qhull.org/)
% Pp: do not report precision problems
% QJ: joggles each input coordinate by adding a random number if a precision error occurs.

if(size(p,2)<3)
	hull = zeros(2,0);
	return;
end

% 2012-08-11: CONVHULL doesn't support Qhull-specific options anymore
%index = convhull(p(1,:),p(2,:),{'Pp','QJ'});
index = convhull(p(1,:),p(2,:));
hull = poly_create(p(:,index(1:end-1)));
