% This script is to compute the oscillation orbit of N-stage RRO circuits. 
addpath('~/cra');
cra_open;

disp('WARNING: This may several days to complete all the computations');
% obj=ph doesn't work
fwdOpt = ph_getOpt;
callBacks.exitCond = ha_callBacks('exitCond','maxFwdStep',200); 

for s=2:2:6
  ha = rro_ha('N',s,'r',1,'fwdOpt',fwdOpt,'type','convex','callBacks',callBacks,'rpath','./results/orbit'); 
  ha = ha_reach(ha);
  ha_reachOp(ha,@(reachData)(phs_display(reachData.sets)));
end

cra_close;
