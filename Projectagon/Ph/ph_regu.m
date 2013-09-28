function ph = ph_regu(ph,eps)
% ph = ph_regu(ph,eps)
% This function bloat a ill-conditioned projectagon into a regular one. 
% The result is not canonical

if(nargin<2||isempty(eps))
	eps = 1e-6;
end

if(ph_isempty(ph)), return; end

% for interval
switch(ph.type)
	case {0,1} % concave/convex projectagon
		update = false;
		hulls = ph.hulls; polys = ph.polys;
		for i=1:ph.ns
			% replace small polygon with its hull.
			if( poly_area(hulls{i})<eps) 
				rpoly = poly_regu([hulls{i},polys{i}],eps);
				hulls{i} = rpolys; polys{i} = rpoly; 
				update = true;
			end
		end
		if(update)
			%iscanon = ph.iscanon;
			ph = ph_create(ph.dim,ph.planes,hulls,polys,ph.type,false);
			%if(iscanon), ph = ph_canon(ph); end
		end
	case 2 % bbox
		bbox = ph.bbox;
		ind = find(diff(bbox,[],2)<eps);
		if(~isempty(ind))
			bbox(ind,:) = bbox(ind,:)+repmat([-1,1],length(ind),1)*eps/2;
			ph = ph_createByBox(ph.dim,ph.planes,bbox);
		end
	otherwise
		error('unknown projectagon type');
end
