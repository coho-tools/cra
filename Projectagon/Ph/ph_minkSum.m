function ph = ph_minkSum(ph1,ph2)
% ph = ph_minkSum(ph1,ph2)
% This function computes the Minkowski sum of two projectagons.

if(ph_isempty(ph1)), ph=ph2; return; end
if(ph_isempty(ph2)), ph=ph1; return; end

phs = ph_promote({ph1,ph2}); ph1 = phs{1}; ph2 = phs{2};
%if( ph1.dim~=ph2.dim || ~all(ph1.planes(:)==ph2.planes(:)) )
%	error('ph1 and ph2 must have the same structure');
%end
dim = ph1.dim; planes = ph1.planes; 
type = min(ph1.type,ph2.type);

switch(type)
	case {0,1}
		error('do not support now');
	case 2
		bbox1 = ph1.bbox; bbox2 = ph2.bbox;
		bbox = bbox1+bbox2;
		ph = ph_createByBox(dim,planes,bbox);
	otherwise
		error('unknown projectagon type');
end
