function p = poly_create(points)
% p = poly_create(points,isccw)
% This function create a 2D polygon.  
% Input: 
% 	points: a 2xn matrix, point(1/2,:) is the x/y value.  n>=3
% Output: 
% 	p: currently, p == points if it is a simple polygon
%				  	== [] otherwise (point, segment, non-simple)

p = points;
% check isnan and isinf
if(isempty(p) || any(isnan(p(:))) || any(isinf(p(:))) )
	p = zeros(2,0);
	return;
end

eps = 1e-12; % the default value is about 2e-16

% reduce duplicate points
diffs = p - p(:,[2:end,1]);
p(:,all(abs(diffs)<=eps)) = [];

% check the number of points
if(size(p,2)<3)
	if(~isempty(points) && cra_cfg('get','polyApproxEn'))
		%NOTE: recursive possible
		p = poly_regu(points,eps*1e3); % use original points
	else
		p = zeros(2,0);
	end 
	return;
end

% check if the polygon is simple
if(~poly_isSimple(p))
	if(cra_cfg('get','polyApproxEn'))
		p = poly_convexHull(p);
	else
		p = zeros(2,0);
	end
end
