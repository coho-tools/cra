% This is the 3D Play-Doh example from "Reachability Analysis Using Polygonal Projections".
function ex_3pd
  addpath('~/cra');
	cra_open;
	ha = ex_3pd_ha;
	ha = ha_reach(ha);
	ha_reachOp(ha,@(reachData)(phs_display(reachData.sets)));
	cra_close;

% Timestep = 0.05 may be too large, make Java crashed.
% Guess-verify is too slow and also crashed. 
% BloatAmt does not generate correct result.
function ha = ex_3pd_ha
	% states
	phOpt.fwdOpt = ph_getOpt;
	%phOpt.fwdOpt.object = 'face-none';
	phOpt.fwdOpt.maxBloat = 0.2; 
	phOpt.fwdOpt.model = 'timeStep';
	phOpt.fwdOpt.timeStep = 0.1; 

	callBacks.exitCond = ha_callBacks('exitCond','maxFwdStep',20); 
	callBacks.sliceCond = @(info)(0);  % do not slice
	states(1) = ha_state('s1',@(lp)(ex_3pd_model(lp)),[],phOpt,callBacks);

	% source
	source = 's1'; 

	% initial 
	dim = 3; planes = [1,2;1,3]; 
	bbox = [-1,1; -1,1; -0.1,0.1]; 
	initPh = ph_createByBox(dim,planes,bbox);
%	p1 = [ -1,   -1, -0.5, 0.5,    1,   1, 0.5, -0.5; 
%	      0.5, -0.5,   -1,  -1, -0.5, 0.5,   1,    1];
%	p2 = [  -1,   -1,    1,   1; 
%	        0.1,-0.1, -0.1, 0.1];
%	initPh = ph_create(dim,planes,{p1,p2});
	initPh = ph_convert(initPh,'non-convex'); % must use non-convex

	ha = ha_create('3pd',states,[],source,initPh);

% xdot = -0.01x 
% ydot = x-y
% zdot = (2x^2-1-y)/2
function ldi = ex_3pd_model(lp) 
	x=1;y=2;z=3;
	bbox = lp_box(lp);
	avgs = mean(bbox,2);  extras = diff(bbox,[],2)/2;
	A = [    -0.01,    0, 0; ...
		           1,   -1, 0; ...
		   2*avgs(x), -0.5, 0]; 
	b = [0; ...
		   0; ...
		   -0.5-avgs(x)^2+extras(x)^2/2]; 
	% add addition error for ill-conditioned polygon
	u = [0; 
	     0; 
			 extras(x)^2/2]+1e-9; 
	ldi = int_create(A,b,u); 
