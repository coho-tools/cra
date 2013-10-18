% This is the 3D Dang and Maler's example from "Reachability Analysis Using Polygonal Projections".
function ex_3dm
  addpath('~/cra');
	cra_open;
	ha = ex_3dm_ha;
	ha = ha_reach(ha);
	ha_reachOp(ha,@(reachData)(phs_display(reachData.sets)));
	cra_close;

% The system converge quickly, so it's ok to use convex projectagon 
% step is too large if use 'guess-verify'
function ha = ex_3dm_ha
	% states
	phOpt.fwdOpt = ph_getOpt;
	phOpt.fwdOpt.object = 'ph'; 
	phOpt.fwdOpt.model = 'timeStep'; % error term is zero
	phOpt.fwdOpt.timeStep = 0.05; % error term is zero

	callBacks.exitCond = ha_callBacks('exitCond','maxFwdT',2); 
	callBacks.sliceCond = @(info)(0);  % do not slice
	states(1) = ha_state('s1',@(lp)(ex_3dm_model(lp)),[],phOpt,callBacks);

	% source
	source = 's1'; 

	% initial 
	dim = 3; planes = [1,2;3,2]; 
	bbox = [-0.025,0.025; -0.1,0.1; 0.05,0.07]; 
	initPh = ph_createByBox(dim,planes,bbox);
	initPh = ph_convert(initPh,'convex'); % must use non-convex

	ha = ha_create('3dm',states,[],source,initPh);

% xdot = -2x 
% ydot = x-2y
% zdot = y-2z
function ldi = ex_3dm_model(lp) 
	A = [ -2,  0,  0; ...
		     1, -2,  0; ...
		     0,  1, -2];
	b = zeros(3,1); u = zeros(3,1);
	ldi = int_create(A,b,u); 
