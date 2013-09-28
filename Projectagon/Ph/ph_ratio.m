function ratio = ph_ratio(ph1,ph2)
if(ph_isempty(ph1) && ph_isempty(ph2)) 
	ratio = NaN;
	return;
end
if(ph_isempty(ph1))
	ratio = 0;
	return;
end
if(ph_isempty(ph2))
	ratio = Inf;
	return;
end

phs = ph_promote({ph1,ph2}); ph1 = phs{1}; ph2 = phs{2};
%if( ph1.dim~=ph2.dim || ~all(ph1.planes(:)==ph2.planes(:)) )
%	error('ph1 and ph2 must have the same structure');
%end
ns = ph1.ns;

ratio = 1;
for i=1:ns
	f = poly_area(ph1.polys{i})/poly_area(ph2.polys{i});
	ratio = ratio * f;
end
ratio = ratio^(ph1.dim/(2*ns));
