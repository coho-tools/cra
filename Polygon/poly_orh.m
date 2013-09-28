function [rec,M,bnds] = poly_orh(points)
% [rec,M,bnds] = poly_orh(points)
% This function computes an oriented rectangular hull contains the polygon. 
% 	points: 2xn matrix, points(1/2,:) for x/y
%	rec: the rectangle that contains all points
%	bnds: the lower/upper bound of x/y value in the new coordinate 
%	M: M(1/2,:) is the direction of long/short axis of the rectangle 
%
% The algorithm uses svd to find direction of hull  
% This may not genereate the minimum ORH that contains the polygon. 
% The accuracy depends on the number of sampled points in the polygon. 
% The result may be far from optimal when using only vertices. 
% For example: p = [0,1,1,0;0,0,1,1];  

% special case
if(isempty(points))
	error('no point specified');
end

pts = unique(points','rows')';

% find the axis of oriented rectangular hull 
% the error of svd is large when the number of points is small
switch(size(pts,2))
	case 1  
		M = eye(2);
	case 2 
		u = diff(pts,[],2); % segment direction
		u = u./norm(u);
		M = [u,[-u(2);u(1)]];
	otherwise 
		[u,s,v] = svd(pts); 
		M = [u(:,1),[-u(2,1);u(1,1)]]; % det(u)=-1 
		%M = poly_orh_lg(pts);
end

% convert to new coordinate
proj = M'*pts;
bnds = [min(proj,[],2),max(proj,[],2)];
rec = [bnds(1,[1,2,2,1]);bnds(2,[1,1,2,2])]; % ccw order

% tranfor back to original coordinate
rec = M*rec; % tranfor back to original coordinate

function m = poly_orh_lg(pts)
% find the axis of rectangle by linear regression 
    x = pts(1,:); y = pts(2,:);
    c = polyfit(x,y,1); % y = c(1)*x+c(2)
    if(isinf(c(1))) % vertical 
        ldir = [0;1];
    else
        ldir = [1;c(1)]; ldir = ldir./norm(ldir);
    end;
    sdir = [-ldir(2);ldir(1)]; % direction of short axis
    m = [ldir,sdir]; %M*M' = I, det(M) = 1 
%end;

