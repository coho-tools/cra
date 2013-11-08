function polys = ph_derivative(ph,var,var2,fast)
% polys = ph_derivative(ph,var,var2,fast)
% This function  plots the brockett annulus for a signal
	if(nargin<3), var2 = []; end
	if(nargin<4||isempty(fast)), fast = false; end
	if(fast)
		poly = lp_der(ph.hullLP,var);
		polys = {poly};
	else
		MAXLEN = 0.1; eps = 1e-6;
		poly = ph_project(ph,[var,var2]); poly = poly{1};
		poly = poly_breakLongEdge(poly,MAXLEN);
		m = size(poly,2);
		poly = poly(:,[1:end,1]); polys = cell(m,1);
		dims = zeros(ph.dim,2); dims(var,1) = 1; dims(var2,2) = 1;
		for i=1:m
			seg = poly(:,[i,i+1]);
			segLP = lp_createByHull(seg,dims);
			faceLP = lp_and(ph.hullLP,lp_bloat(segLP,eps));
			polys{i} = lp_der(faceLP,var,var2,seg);
		end
	end
end


function poly = lp_der(lp,var,var2,seg)
	if(nargin<3), var2=[]; end;
	if(nargin<4), seg=[]; end;
	C = 1e10; fast = ~isempty(var2)&~isempty(seg);
	models = circuit_model(lp,var); 
	polys = cell(length(models),1);
	for i=1:length(models) 
		ldi = models{i}; 
		A = ldi.A/C; b = ldi.b/C; u = ldi.u/C; 
		ind = find(A~=0); ind = ind(ind~=var); 
		if(fast && (isempty(ind)||(length(ind)==1&&ind==var2)) ) % related on [var,var2]
			p = seg; % do not need to project again
			p(2,:) = A(var)*p(1,:)+A(var2)*p(2,:)+b;
			p = poly_create(p); % seg -> rectangle
		elseif(isempty(ind)) % does not depend on other variables
			bbox = lp_box(lp); p = zeros(2,2);
			p(1,:) = bbox(var,:);
			p(2,:) = A(var)*p(1,:)+b;	
		else % project to [x,y] planes
			x = A(var); y = A(ind); % other non-zero 
			xdim = zeros(length(A),1); ydim = zeros(length(A),1); 
			xdim(var) = 1; ydim(ind) = y/norm(y); 
			p = java_lpProject(lp,xdim,ydim,1e-3); % project onto [x,y] plane 
			p(2,:) = x*p(1,:)+p(2,:)*norm(y)+b; 
		end
		p1 = p; p1(2,:) = p1(2,:)-u; 
		p2 = p; p2(2,:) = p2(2,:)+u; 
		p = poly_convexHull([p1,p2]);
		polys{i} = poly_create(p);
	end 
	poly = poly_intersect(polys);
	poly(2,:) = poly(2,:)*C;
end

%% compute derivative for a LP
%function poly = lp_der(lp,var) 
%	C = 1e10;
%	models = circuit_model(lp,var); 
%	polys = cell(length(models),1);
%	for i=1:length(models) 
%		ldi = models{i}; 
%		A = ldi.A; b = ldi.b; u = ldi.u; 
%		ind = find(A~=0); ind = ind(ind~=var); 
%		if(isempty(ind)) % do not depend other variables
%			bbox = lp_box(lp); p = zeros(2,2);
%			p(1,:) = bbox(var,:);
%			p(2,:) = A(var)*p(1,:)+b;	
%		else 
%			x = A(var); y = A(ind); % other non-zero 
%			xdim = zeros(length(A),1); ydim = zeros(length(A),1); 
%			xdim(var) = 1; ydim(ind) = y/norm(y); 
%			p = java_lpProject(lp,xdim,ydim,1e-3);  % project onto [x,y] plane
%			p(2,:) = x*p(1,:)+p(2,:)*norm(y)+b; 
%		end
%		p1 = p; p1(2,:) = (p1(2,:)-u)/C; 
%		p2 = p; p2(2,:) = (p2(2,:)+u)/C; 
%		p = poly_convexHull([p1,p2]);
%		polys{i} = poly_create(p);
%	end 
%	poly = poly_intersect(polys);
%	poly(2,:) = poly(2,:)*C;
%end
