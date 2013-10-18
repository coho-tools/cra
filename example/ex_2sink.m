% This is the 2D sink  example
function ex_2sink
  addpath('~/cra');
	cra_open;
	ha = ex_2sink_ha;
	ha = ha_reach(ha);
	ha_reachOp(ha,@(reachData)(phs_display(reachData.sets)));
	cra_close;

function ha = ex_2sink_ha
	% states
	phOpt.fwdOpt = ph_getOpt;
	phOpt.fwdOpt.object = 'ph'; % Linear model with no error term
	phOpt.fwdOpt.model = 'timeStep';
	phOpt.fwdOpt.timeStep = 0.05;  % do not use too large steps;
	callBacks.exitCond = ha_callBacks('exitCond','maxFwdT',2); 
	callBacks.sliceCond = @(info)(0);  % do not slice
	states(1) = ha_state('s1',@(lp)(ex_2sink_model(lp)),[],phOpt,callBacks);

	% source
	source = 's1'; 

	% initial 
	hulls = { [ 0.1, 0.3, 0.4, 0.1; 0.1, 0.1, 0.4, 0.3 ] };
	initPh = ph_create(2,[1,2],hulls,hulls,1,true); 

	ha = ha_create('2sink',states,[],source,initPh);

% xdot = m*x
%	m = [-2,-3;3,-2];
function ldi = ex_2sink_model(lp) 
	A = [-2,-3;3,-2];
	b = zeros(2,1); u = zeros(2,1);
	ldi = int_create(A,b,u);
