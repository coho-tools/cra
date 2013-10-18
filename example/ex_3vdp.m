% This is the 3D Vdp example
function ex_3vdp
  addpath('~/cra');
	cra_open;
	ha = ex_3vdp_ha;
	ha = ha_reach(ha);
	ha_reachOp(ha,@(reachData)(phs_display(reachData.sets)));
	cra_close;

function ha = ex_3vdp_ha
	% states
	phOpt.fwdOpt = ph_getOpt;
	phOpt.fwdOpt.object = 'ph'; % Linear model with no error term
	callBacks.exitCond = ha_callBacks('exitCond','maxFwdT',7); 
	callBacks.sliceCond = @(info)(0);  % do not slice
	states(1) = ha_state('s1',@(lp)(ex_3vdp_model(lp)),[],phOpt,callBacks);

	% source
	source = 's1'; 

	% initial 
	dim = 3; planes = [1,2;1,3]; 
	bbox = [1.0,1.2;-0.05,0.05;0.9,1.1];
	initPh = ph_createByBox(dim,planes,bbox);
	initPh = ph_convert(initPh,'convex');

	ha = ha_create('3vdp',states,[],source,initPh);

function ldi = ex_3vdp_model(lp) 
	x=1;y=2;z=3;
	bbox = lp_box(lp);
	avgs = mean(bbox,2);  extras = diff(bbox,[],2)/2;
	A = [1-3*avgs(x)^2, -1, 			      0;... 
   		 1, 			      1-3*avgs(y)^2, 	0;...
		   4*avgs(x), 	  0,				      -2];
	b = [2*avgs(x)^3-3/2*avgs(x)*extras(x)^2;...
		   2*avgs(y)^3-3/2*avgs(y)*extras(y)^2;...
		   -2*avgs(x)^2+extras(x)^2];
	u = [extras(x)^3+3/2*abs(avgs(x))*extras(x)^2;...
		   extras(y)^3+3/2*abs(avgs(y))*extras(y)^2;...
		   extras(x)^2];
	ldi = int_create(A,b,u); 
