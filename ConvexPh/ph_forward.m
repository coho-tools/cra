function phs = ph_forward(ph,ldi,tps)
  phs = cell(length(tps),1);
  for i=1:length(tps)
    % TODO: make this support vectorized computations
	  hullLP = int_forward(ph.hullLP,ldi,tps(i));
    hulls = {};
    bbox = []; bboxLP = []; status = 1; % unprojected
    phs{i} = ph_create(ph.dim, ph.planes, hulls, hullLP, bbox, bboxLP, ph.type, status) ;
  end

  phs = phs_project(phs);
end
