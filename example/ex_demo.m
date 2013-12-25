% This is a simiple demo to show how to use CRA
function ex_demo
  addpath('~/cra');
	cra_open;


	ha = ex_demo_ha;
	ha = ha_reach(ha);
	ha_reachOp(ha,@(reachData)(phs_display(reachData.sets)));

	cra_close; 

function ha = ex_demo_ha
	% initial 
	x = 1; y = 2; z = 3; dim = 3; planes = [x,y;x,z;y,z]; 
	bbox = [0,0.1;0,0.1;0,0.1];
	initPh = ph_createByBox(dim,planes,bbox);
	initPh = ph_convert(initPh,'convex');

	% states
	bbox1 = [0,1;0,0.1;0,0.1]; inv1 = lp_createByBox(bbox1);
	bbox2 = [0.9,1;0,1;0,0.1]; inv2 = lp_createByBox(bbox2);
	bbox3 = [0.9,1;0.9,1;0,1]; inv3 = lp_createByBox(bbox3);
	states(1) = ha_state('s1',@(lp)(ex_demo_model(lp,1)),inv1); 
	states(2) = ha_state('s2',@(lp)(ex_demo_model(lp,2)),inv2); 
	states(3) = ha_state('s3',@(lp)(ex_demo_model(lp,3)),inv3); 

	% trans
	trans(1) = ha_trans('s1','s2');
	trans(2) = ha_trans('s2','s3');
  
	% source
	source = 's1'; 

	ha = ha_create('demo',states,trans,source,initPh);

function ldi = ex_demo_model(lp,mode) 
	A = zeros(3,3); 
	b = zeros(3,1); b(mode) = 1;
	u = 1e-9;  % to avoid empty projection
	ldi = int_create(A,b,u); 
