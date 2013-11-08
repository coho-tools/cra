% This is the aircraft example from 
% Computational Techniques for the verification of hybrid systems
% Here is a simple problem, only compute maximum forward reachable states.  
function ex_3plane
  addpath('~/cra');
	cra_open;
	ha = ex_3plane_ha;
	ha = ha_reach(ha);
	ha_reachOp(ha,@(reachData)(phs_display(reachData.sets)));
	cra_close;

function ha = ex_3plane_ha
	% states
	phOpt.fwdOpt = ph_getOpt;
	phOpt.fwdOpt.object = 'ph';
	callBacks.exitCond = ha_callBacks('exitCond','maxFwdStep',20); 
	callBacks.sliceCond = @(info)(0);  % do not slice
	states(1) = ha_state('s1',@(lp)(ex_3plane_model(lp)),[],phOpt,callBacks);

	% source
	source = 's1'; 

	% initial 
	dim = 3; planes = [1,2;1,3]; 
	bbox = [2,3; 2,3; -0.01, 0.01]; 
	initPh = ph_createByBox(dim,planes,bbox);
	%initPh = ph_convert(initPh,'non-convex');
	initPh = ph_convert(initPh,'convex');

	ha = ha_create('3plane',states,[],source,initPh);

% xdot = -v + v*cosz + u*y
% ydot = v*sinz - u*x
% zdot = d - u
% d,u \in [-1,1]
% Simplify the problem by make d = -1; u = 1; 
function ldi = ex_3plane_model(lp) 
	x=1;y=2;z=3;
	bbox = lp_box(lp);
	avgs = mean(bbox,2);  extras = diff(bbox,[],2)/2;
	d = -1; u = 1; v = 10;

	minZ = bbox(z,1); maxZ = bbox(z,2); 
	k = floor(minZ/(2*pi));
	minZ = minZ - k*2*pi; maxZ = maxZ - k*2*pi;
	minSinZ =  min(sin(minZ),sin(maxZ));
	maxSinZ =  max(sin(minZ),sin(maxZ)); 
	if(pi >= minZ && pi<=maxZ), minSinZ = -1; end
	if(2*pi >= minZ && 2*pi <=maxZ), maxSinZ = 1; end;
	minCosZ =  min(cos(minZ),cos(maxZ));
	maxCosZ =  max(cos(minZ),cos(maxZ));
	if(3*pi/2 >= minZ && 3*pi/2 <=maxZ), minCosZ = -1; end;
	if(pi/2 >= minZ && pi/2<=maxZ), maxCosZ = 1; end

  % TODO: how to make the error term smaller?
	A = [0, u, 0; ...
	     -u, 0, 0; ...
	     0, 0 ,0];
  b = [-v; 0; (d-u)]; % d and u are constant
	b(1) = b(1)+v*(minCosZ+maxCosZ)/2;
	b(2) = b(2)+v*(minSinZ+maxSinZ)/2;
	err(1) = v*(maxCosZ-minCosZ)/2; 
	err(2) = v*(maxSinZ-minSinZ)/2;
	err(3) = 1e-9; % d,u is determined, so zero.
	ldi = int_create(A,b,u); 
