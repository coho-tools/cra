% This is the 2D Vdp example from "Reachability Analysis Using Polygonal Projections".
function ex_2vdp
  addpath('~/cra');
	cra_open;
	ha = ex_2vdp_ha;
	ha = ha_reach(ha);
	ha_reachOp(ha,@(reachData)(phs_display(reachData.sets)));
	cra_close;

% This is similar with 3vdp. The timestep is larger, thus larger error. 
% We limited the timeStep instead to get accurate result.
% Or use non-convex projectagons
function ha = ex_2vdp_ha
	% states
	phOpt.fwdOpt = ph_getOpt;
	phOpt.fwdOpt.object = 'ph';
	phOpt.fwdOpt.model = 'timeStep'; 
	phOpt.fwdOpt.timeStep = 0.05; 
	callBacks.exitCond = ha_callBacks('exitCond','maxFwdT',7); 
	callBacks.sliceCond = @(info)(0);  % do not slice
	states(1) = ha_state('s1',@(lp)(ex_2vdp_model(lp)),[],phOpt,callBacks);

	% source
	source = 's1'; 

	% initial 
	dim = 2; planes = [1,2]; 
	bbox = [1.0,1.2;-0.05,0.05]; 
	initPh = ph_createByBox(dim,planes,bbox);
	initPh = ph_convert(initPh,'convex');

	ha = ha_create('2vdp',states,[],source,initPh);

% xdot = -y-x^3+x
% ydot = x-y^3+y
function ldi = ex_2vdp_model(lp) 
	x=1;y=2;
	bbox = lp_box(lp);
	avgs = mean(bbox,2);  extras = diff(bbox,[],2)/2;
	A = [1-3*avgs(x)^2, -1            ;...
   		 1, 			      1-3*avgs(y)^2 ];
	b = [2*avgs(x)^3-3/2*avgs(x)*extras(x)^2;...
		   2*avgs(y)^3-3/2*avgs(y)*extras(y)^2]; 
	u = [extras(x)^3+3/2*abs(avgs(x))*extras(x)^2;...
		   extras(y)^3+3/2*abs(avgs(y))*extras(y)^2]; 
	ldi = int_create(A,b,u); 
