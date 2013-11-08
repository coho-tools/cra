function ph = ph_simplify(ph,eps)
% ph = ph_simplify(ph,eps)
% This function simplify a projectagon by reducing
% 	its number of faces.
% The result is canoical if the input is.
if(ph_isempty(ph)), return; end
if(ph.type==2), return; end
if(nargin<2), eps=0.02; end;

polys = cell(ph.ns,1); hulls = cell(ph.ns,1);
for i=1:ph.ns
	% remove short edges of convex hull
	hulls{i} = poly_simplify(ph.hulls{i},eps); 
	switch(ph.type)
		case 0 
			% remove concave point 
			% remove short edges for non-canonical projectagon 
			polys{i} = poly_simplify(ph.polys{i},eps,~ph.iscanon);
		case 1
			polys{i} = hulls{i};
		otherwise
			error('unknown projectagon type');
	end
end

ph = ph_create(ph.dim,ph.planes,hulls,polys,ph.type,ph.iscanon);
