function p = poly_union(ps,method)
% p = poly_union(ps,method)
% This function computes the union of a set of polygons. 
% It support two methods
% 	'saga' or 'matlab': It calls SAGA matlab functions.
% 	'java': It calls java functions.
% The SAGA routine is faster, but the JAVA routine is free of round-off error. 
%
% Note: 
% 1. The SAGA routine is not stable. For example, 
%    p1 = [0,1,1,0;0,0,1,1]; p2 = [0,1,1,0;1,1,2,2]; p = polyuni(p1,p2)
%    The result is incorrect (p = p2), which is caused by round-off error
% 	 during the intersection computation. JAVA routine do not have this problem 
% 2. When the union are disjoint polygons, SAGA routine can discover it.
%    The JAVA routine returns an arbitrary one.
% 3. To gurantee an over-approximated result, we do
%    For SAGA result, if p is not a polygon, or disjoint polygons, or the area
%    is less than the maximum area, use the convex hull.
% 	 For JAVA result, if p is not a polygon or the area is less than the maximum one
%    use the convex hull;
% 4. The result may be under-approximated.


% remove empty polygons
ise = false(length(ps),1);
for i=1:length(ps)
	ise(i) = isempty(ps{i});
end
ps = ps(~ise);

% special case
switch(length(ps))
	case 0
		p = zeros(2,0);
		return;
	case 1
		p = ps{1};
		return;
end

% get the method
if(nargin<2||isempty(method))
	method= cra_cfg('get','polySolver'); 
end

% compute the union
switch(lower(method))
case {'saga','matlab'}
	p1 = ps{1};
	for i=2:length(ps)
		p2 = ps{i}; 
		[x,y] = polyuni(p1(1,:),p1(2,:),p2(1,:),p2(2,:)); 
		pts = [x';y'];
		if(size(pts,2)<3|| any(isnan(pts(:))) ||  ...
		   poly_area(pts)+eps < max(poly_area(p1),poly_area(p2)) ) 
			if(cra_cfg('get','polyApproxEn')) 
				pts = poly_convexHull([p1,p2]);
			else
				msg='incorrect poly_union result by SAGA method';
				exception = MException('COHO:Polygon:OpException',msg); 
				throw(exception);
			end
		end
		p1 = pts;
	end
	pts = p1;
case {'java'}
	pts = java_polyUnion(ps);
	as = zeros(length(ps),1);
	for i=1:length(ps)
		as(i) = poly_area(ps{i});
	end
	if(size(pts,2)<3||poly_area(pts)+eps<max(as)) 
		if(cra_cfg('get','polyApproxEn'))
			ps = reshape(ps,1,[]);
			pts = cell2mat(ps);
			pts = poly_convexHull(pts);
		else
			msg='incorrect poly_union result by Matlab method';
			exception = MException('COHO:Polygon:OpException',msg); 
			throw(exception);
		end
	end
otherwise
	error('do not support');
end
p =  poly_create(pts);

%%% Here to debug why the union does not contains all vertices
%for i = 1:length(ps)
%	if(~all(poly_containPts(p,ps{i})))
%		error('The union does not contains all points, please debug it'); 
%	end
%end


