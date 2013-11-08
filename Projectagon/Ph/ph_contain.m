function isc = ph_contain(ph1,ph2)
% isc = ph_contain(ph1,ph2)
%   This function return 1 if ph1 contain (or the same) ph2, return 0 otherwise.
%   ph1,ph2:    A projectagon with the same structures

% Special case
if(ph_isempty(ph2)), isc = 1; return; end
if(ph_isempty(ph1)), isc = 0; return; end

phs = ph_promote({ph1,ph2}); ph1 = phs{1}; ph2 = phs{2};
%if( ph1.dim~=ph2.dim || ~all(ph1.planes(:)==ph2.planes(:)) )
%	error('ph1 and ph2 must have the same structure');
%end
ns = ph1.ns;
type = min(ph1.type,ph2.type);

switch(type)
	case 0 % non-convex 
		for i=1:ns
			isc = poly_contain(ph1.polys{i},ph2.polys{i});
			if(~isc), return; end;
		end
		isc = true;
	case 1 % convex
		for i=1:ns
			isc = poly_contain(ph1.hulls{i},ph2.hulls{i});
			if(~isc), return; end;
		end
		isc = true;
	case 2 % bbox
		bbox1 = ph1.bbox; bbox2 = ph2.bbox;
		isc = all(bbox1(:,1)<=bbox2(:,1)) & all(bbox1(:,2)>=bbox2(:,2));  
	otherwise
		error('unknown projectagon type');
end
