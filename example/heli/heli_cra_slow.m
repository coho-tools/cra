% Using CRA on the helicopter demo
function heli
  cra_open(true);
  %cra_cfg('set','lpSolver','matlabjava');
  %cra_cfg('set','projSolver','matlabjava')  
  ha = heli_ha;
  ha = ha_reach(ha);
  ha_reachOp(ha,@(reachData)(phs_display(reachData.sets)));
  
  cra_close; 
end

function ha = heli_ha
  % initial 
  dim = 28;
  coords = (1:dim)'; 
  planes = [coords coords+1];
  planes(dim,2) = 1;
  
  bbox = [zeros(dim, 1) 0.11*ones(dim,1)];
  initPh = ph_createByBox(dim,planes,bbox);
  initPh = ph_convert(initPh,'convex');
  
  % states
  phOpt.fwdOpt = ph_getOpt;
  phOpt.fwdOpt.object = 'ph';
  callBacks.exitCond=ha_callBacks('exitCond','maxFwdT',30);
  states(1) = ha_state('s1',@(lp)(heli_model(lp)),[],phOpt,callBacks); 
  
  source = 's1'; 
  ha = ha_create('heli',states,[],source,initPh);
end

