function p = poly_regu(points,eps)
% p = poly_regu(points,eps)
% This function computes an over-approximated 
% polygon for a in-regular polygon.
%
% The algorithm is similar with poly_orh. 
% It use svd to find an ORH, which will be bloated if too thin. 

% special case
if(isempty(points))
	error('no point specified');
end

if(nargin<2||isempty(eps))
	eps = 1e-6;
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
end

% convert to new coordinate
proj = M'*pts; 
bnds = [min(proj,[],2),max(proj,[],2)]; 
dist = diff(bnds,[],2); ind = find(dist <eps); 

% replace with ORH if area is too small
if(~isempty(ind) || poly_area(points)<eps*max(dist))
	% bloat the bounding box if too small
	bnds(ind,:) = repmat(mean(bnds(ind,:),2),1,2) + ...
		repmat([-1,1],length(ind),1)*eps/2; 
	rec = [bnds(1,[1,2,2,1]);bnds(2,[1,1,2,2])]; % ccw order 
	% tranfor back to original coordinate
	p = M*rec; 
  %p = poly_create(p);
else  % unchanged
	p = points;
end

