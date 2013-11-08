function p = poly_rand(n,convex)
% p = poly_rand(n,convex)
% This function generates a random 2d polygon.
% n: number of random points, n>=3
% convex: true(default) to generate a convex polygon, otherwise, non-convex
% 
% Note: the number of vertices of p may be less than n.
% For convex polygon, the algorithm returns the convex hull of n random points 
% For non-convex polygons, the algorithms generates n random angles and radius.

if(nargin<2||isempty(convex))
	convex = true;
end
if(n<3)
	error('a polygon has at least three points');
end

if(convex)
	points = rand(2,n)*2-1;
	p = poly_convexHull(points);
else
	theta = [];
	while(length(theta)<3) 
		theta = sort(unique(rand(n,1)))*2*pi; %anti-clock order 
	end
	radius = rand(length(theta),1);
	p = poly_create([cos(theta).*radius, sin(theta).*radius]');
end
