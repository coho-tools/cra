function ph = ph_intersectLP(ph,lp)
% ph = ph_intersectLP(ph,lp)
% This function computes the intersection of a projectagon and a lp. 
% 	The output is not canonical if the input is a non-convex projectagon,
% 	otherwise, it is canonical

if(isempty(ph)), return; end
if(nargin<2||isempty(lp)), return; end

switch(ph.type)
	case 0 % non-convex 
		% make hull feasible 
		lp = lp_and(ph.hullLP,lp);
		lph = ph_createByLP(ph.dim,ph.planes,lp); 
		if(ph_isempty(lph)), ph = []; return; end; 
		% intersect polygons
		polys = cell(ph.ns,1); hulls = cell(ph.ns,1);
		for i=1:ph.ns
			poly = poly_intersect({lph.hulls{i}, ph.polys{i}});
			if(isempty(poly)), ph = []; return; end
			polys{i} = poly;
			hulls{i} = poly_convexHull(polys{i});
		end 
		ph = ph_create(ph.dim,ph.planes,hulls,polys,ph.type,false); 

	case 1 % convex
		lp = lp_and(ph.hullLP,lp);
		ph = ph_createByLP(ph.dim,ph.planes,lp);

	case 2 % bbox 
		bbox = lp_box(lp_and(ph.hullLP,lp)); 
		ph = ph_createByBox(ph.dim,ph.planes,bbox);

	otherwise
		error('unknown projectagon type');
end
