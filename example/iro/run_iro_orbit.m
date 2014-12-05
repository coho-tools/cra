% This script is used compute the oscillation orbit of N-stage IRO circuits 
% Currently, it computes for 3-9 stages.

addpath('~/cra');
cra_open;
fwdOpt=ph_getOpt;

callBacks.exitCond = ha_callBacks('exitCond','maxFwdStep',150); 
ha = iro_ha('N',3,'fwdOpt',fwdOpt,'type','convex','callBacks',callBacks,'rpath','./results/orbit'); 
ha = ha_reach(ha);
%ha_reachOp(ha,@(reachData)(phs_display(reachData.sets)));

callBacks.exitCond = ha_callBacks('exitCond','maxFwdStep',200); 
ha = iro_ha('N',5,'fwdOpt',fwdOpt,'type','convex','callBacks',callBacks,'rpath','./results/orbit'); 
ha = ha_reach(ha);
%ha_reachOp(ha,@(reachData)(phs_display(reachData.sets)));

callBacks.exitCond = ha_callBacks('exitCond','maxFwdStep',250); 
ha = iro_ha('N',7,'fwdOpt',fwdOpt,'type','convex','callBacks',callBacks,'rpath','./results/orbit'); 
ha = ha_reach(ha);
%ha_reachOp(ha,@(reachData)(phs_display(reachData.sets)));

callBacks.exitCond = ha_callBacks('exitCond','maxFwdStep',300); 
ha = iro_ha('N',9,'fwdOpt',fwdOpt,'type','convex','callBacks',callBacks,'rpath','./results/orbit'); 
ha = ha_reach(ha);
%ha_reachOp(ha,@(reachData)(phs_display(reachData.sets)));

cra_close;
