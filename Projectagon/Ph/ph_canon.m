function [ph,iter] = ph_canon(ph,lp,opt)
% ph = ph_canon(ph,lp,opt)
%  The function compute the canonical form of ph with the constraint of lp.
%
% Inputs: 
% 	ph: ph is required. If it is empty, empty is returned.
% 	lp: The constraint of ph. 
%
% Output:
% 	ph: canonical form projectagon
%
% Examples
% 	ph = ph_canon(ph); % remove infeasible region 
% 	ph = ph_canon(ph,lp): % trim projectagon ph according to lp 
% 

if(nargin<2), lp = []; end
% empty or canonical projectagon
if(ph_isempty(ph) || (ph.iscanon&&isempty(lp)) )
	return;
end

switch(ph.type)
	case 0 % make poly feasible to hull	
		% get parameters
		if(nargin<3||isempty(opt))
      phOpt = cra_cfg('get','phOpt');
			opt = phOpt.canonOpt;
		end
		eps = opt.eps;  % area change
		niters = opt.iters; % max number of iterations
		tol = opt.tol; % simplify tolerance 
		
		% trim infeasible region
		lp = lp_and(ph.hullLP,lp); 
		%for iter=1:Inf % to remove warning message
		for iter=1:1e12
			% make hull feasible
			lph = ph_createByLP(ph.dim,ph.planes,lp); 
			if(ph_isempty(lph)), ph = []; return; end;
			hulls = lph.hulls; 
			% make polys feasible
			polys = ph.polys; 
			for i=1:ph.ns 
				polys{i} = poly_intersect({hulls{i},polys{i}}); 
				if(isempty(polys{i})), ph = []; return; end;
			end 
			% simplify polygons, do not change convex hull
			for i=1:ph.ns 
				polys{i} = poly_simplify(polys{i},tol,false); 
			end
			% exit condition
			if(iter>=niters), break; end % done after a number of iterations
			hullChange = false;
			nhulls = cell(ph.ns,1); 
			for i=1:ph.ns
				nhulls{i} = poly_convexHull(polys{i});
				% compare hull with new hull 
				a0 = poly_area(hulls{i}); a1 = poly_area(nhulls{i});
				if(abs(a0-a1)/a0> eps) 
					hullChange = true; 
				end
			end
			if(~hullChange), break; end % done if canonical 
			% simplify hull
			for i=1:ph.ns 
				nhulls{i} = poly_simplify(nhulls{i},tol,true); 
			end
			% update hull and repeat
			ph = ph_create(ph.dim,ph.planes,nhulls,polys,ph.type,false); 
			if(isempty(ph)), ph = []; return; end
			lp = ph.hullLP;
		end 
		% use hulls rather than nhull to make sure polys are feasible to hulls
		ph = ph_create(ph.dim,ph.planes,hulls,polys,ph.type,true);
		
	case 1 % lp_project
		lp = lp_and(ph.hullLP,lp);
		ph = ph_createByLP(ph.dim,ph.planes,lp);

	case 2 % bounding box 
		bbox = lp_box(lp_and(ph.hullLP,lp)); 
		ph = ph_createByBox(ph.dim,ph.planes,bbox);

	otherwise
		error('unknown projectagon type');
end
