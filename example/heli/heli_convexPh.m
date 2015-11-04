
  dim = 28;
  bbox = [zeros(dim, 1) 0.1*ones(dim,1)];
  ph = ph_createByBox(bbox);
  ldi = heli_model;
  tps = 0:0.1:30;
  phs = ph_forward(ph,ldi,tps);
  phs = phs_project(phs);
