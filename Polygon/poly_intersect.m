function p = poly_intersect(ps,method)
% p = poly_intersect(ps,method)
% This function computes the intersection of a set of polygons
% It support two methods
% 	'saga' or 'matlab': It calls SAGA matlab functions.
% 	'java': It calls java functions.
% The SAGA routine is faster, but the JAVA routine is free of round-off error. 
% When the intersection are disjoint polygons, SAGA routine return the convex hull
% while the java routine return an arbitrary one. (under approximated)

% NOTE
% When the intersection is a point or a segment, JAVA routine return [],
% but the polyints return such this point or segment. 
% Of course, now poly_create return [] at the end. But we may change it later. 

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
	method = cra_cfg('get','polySolver');
end

% compute the intersection
switch(lower(method)) 
case {'saga','matlab'}
	p = ps{1};
	for i=2:length(ps)
		p = poly_intersect_saga(p,ps{i});
		if(isempty(p)), break; end
	end
case {'java'}
	p = ps{1};
	for i=2:length(ps)
		p = java_polyIntersect(p,ps{i});
		if(isempty(p)), break; end
	end
otherwise
	error('do not support');
end
p = poly_create(p);

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
function pts = poly_intersect_saga(p1,p2)
	% compute intersection point 
	[x,y] = polyints(p1(1,:),p1(2,:),p2(1,:),p2(2,:));

	% Special case (saga can not handle if p1 contain p2)
	if(isempty(x)) 
		if(poly_contain(p1,p2))
			pts = p2;
		elseif(poly_contain(p2,p1))
			pts = p1;
		else
			pts = zeros(2,0);
		end
		return;
	end

	% remove points or segments
	sep = find(isnan(x));
	ss = [1;sep+1]; ee = [sep-1;length(x)];
	lens = ee-ss+1;
	pind = find(lens>2);
	switch(length(pind))
		case 0 % segment or point
			[nouse,pind] = max(lens); 
			pts = [x(ss(pind):ee(pind)),y(ss(pind):ee(pind))]';
		case 1 % a polygon
			pts = [x(ss(pind):ee(pind)),y(ss(pind):ee(pind))]';
		otherwise % disjoint polygons
			if(cra_cfg('get','polyApproxEn')) 
				pts = zeros(2,0);
				for i=1:length(pind)
					poly = [x(ss(pind(i)):ee(pind(i))),y(ss(pind(i)):ee(pind(i)))]';
					pts = [pts,poly];
				end
				pts = poly_convexHull(pts); 
			else 
				msg = 'result of ph_intersect are disjoint polygons';
				exception = MException('COHO:Polygon:OpException',msg); 
				throw(exception);
			end 
	end
