function val = ph_get(ph,filed)
% val = ph_get(ph,filed)
% This function return the field of ph 
% 

if(ph_isempty(ph)), val=[]; return; end;

switch(lower(filed))
	case {'ns','numslices'}
		val = ph.ns;
	case {'dim','n','d'}
		val = ph.dim;
	case {'planes'}
		val = ph.planes;
	case {'type'}
		val = ph.type;
	case {'iscanon','canon'}
		val = ph.iscanon;
	case {'hulls'}
		val = ph.hulls;
	case {'polys'}
		val = ph.polys;
	case {'bbox'}
		val = ph.bbox;
	case {'hullLP'}
		val = ph.hullLP;
	case {'bboxLP'}
		val = ph.bboxLP;
	case {'concavity'}
		val = 1;
		if(ph.type==0)
			for i=1:ph.ns
				r = poly_area(ph.polys{i})/poly_area(ph.hulls{i});
				val = val*r;
			end
		end
	otherwise
		error('does not support');
end
