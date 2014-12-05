% This function uses N-stage IRO circuits to check the performance of CRA. 
% We computes 50 steps from Init region [H,H,L,...,H,L]

function run_iro_perf(mode)
if(nargin<1||isempty(mode)), mode = 0; end
addpath('~/cra');
cra_open;

fwdOpt=ph_getOpt; 
callBacks.exitCond = ha_callBacks('exitCond','maxFwdStep',50); 

MAX_S = 21;
type = 'convex';
times = zeros(MAX_S,1);
switch(mode)
  case 0
    fwdOpt.object='ph';
    for N = 3:2:MAX_S
      t = cputime;
      ha = iro_ha('N',N, 'fwdOpt',fwdOpt, 'type',type, 'callBacks',callBacks, 'rpath','./results/perf/ph'); 
      ha = ha_reach(ha);
      times(N) = cputime-t;
      save('results/perf/ph/times','times');
    end
  case 1
    fwdOpt.object='convex';
    for N = 3:2:MAX_S
      t = cputime;
      ha = iro_ha('N',N, 'fwdOpt',fwdOpt, 'type',type, 'callBacks',callBacks, 'rpath','./results/perf/face'); 
      ha = ha_reach(ha);
      times(N) = cputime-t;
      save('results/perf/face/times','times');
    end
  case 2
    type = 'non-convex';
    for N = 3:2:MAX_S
      t = cputime;
      ha = iro_ha('N',N, 'fwdOpt',fwdOpt, 'type',type, 'callBacks',callBacks, 'rpath','./results/perf/noncov'); 
      ha = ha_reach(ha);
      times(N) = cputime-t;
      save('results/perf/noncov/times','times');
    end
end
cra_close;
