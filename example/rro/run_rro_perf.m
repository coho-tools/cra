% This function uses N-stage RRO circuits to check the performance of CRA. 
% We computes 50 steps from Init region [H,L,H,L,...,H,L]

function run_rro_perf(mode)
if(nargin<1||isempty(mode)), mode = 0; end
addpath('~/cra');
cra_open;

disp('WARNING: This may several weeks to complete all the computations');

MAX_S = 10;
fwdOpt=ph_getOpt; 
callBacks.exitCond = ha_callBacks('exitCond','maxFwdStep',50); 
type = 'convex';
times = zeros(MAX_S,1);
switch(mode)
  case 0
    fwdOpt.object='ph';
    for N = 2:2:MAX_S
      t = cputime;
      ha = rro_ha('N',N, 'fwdOpt',fwdOpt, 'type',type, 'callBacks',callBacks, 'rpath','./results/rro/perf/ph'); 
      ha = ha_reach(ha);
      times(N) = cputime-t;
      save('results/rro/perf/ph/times','times');
    end
  case 1
    fwdOpt.object='convex';
    for N = 2:2:MAX_S
      t = cputime;
      ha = rro_ha('N',N, 'fwdOpt',fwdOpt, 'type',type, 'callBacks',callBacks, 'rpath','./results/rro/perf/face'); 
      ha = ha_reach(ha);
      times(N) = cputime-t;
      save('results/rro/perf/face/times','times');
    end
  case 2
    type = 'non-convex';
    for N = 2:2:MAX_S
      t = cputime;
      ha = rro_ha('N',N, 'fwdOpt',fwdOpt, 'type',type, 'callBacks',callBacks, 'rpath','./results/rro/perf/noncov'); 
      ha = ha_reach(ha);
      times(N) = cputime-t;
      save('results/rro/perf/noncov/times','times');
    end 
end

cra_close;
