function ex_pll 
  addpath('~/cra');
	cra_open;
	ha = ex_pll_ha;
	%ha = ex_pll_ha_full;
	ha = ha_reach(ha);
	ha_reachOp(ha,@(reachData)(phs_display(reachData.sets)));
	cra_close;

function ha = ex_pll_ha
	% states
	inv1 = lp_create(-eye(2),zeros(2,1));
	inv2 = lp_create([0,1],0); 
	phOpt.fwdOpt = ph_getOpt;
	phOpt.fwdOpt.object = 'ph'; % Linear model with no error term
	callBacks.exitCond = ha_callBacks('exitCond','phempty'); 
	callBacks.sliceCond = @(info)([0;0;1]); % no slice, compute tubes only
	states(1) = ha_state('s1',@(lp)(ex_pll_model(lp,1)),inv1,phOpt,callBacks);
	callBacks.exitCond = ha_callBacks('exitCond','maxFwdStep',45); 
	callBacks.sliceCond = @(info)([0]); % no slice, no tube
	states(2) = ha_state('s2',@(lp)(ex_pll_model(lp,2)),inv2,phOpt,callBacks);

  % transistion
	trans(1) = ha_trans('s1','s2');

	% source
	source = 's1'; 

	% initial 
	initPh = ph_createByBox(2,[1,2],[0.96,1.02;0.05,0.1]);

	ha = ha_create('pll',states,trans,source,initPh);

function ha = ex_pll_ha_full
	% states
	inv1 = lp_create(-eye(2),zeros(2,1));
	inv2 = lp_create([0,1],0); 
	phOpt.fwdOpt = ph_getOpt;
	phOpt.fwdOpt.object = 'ph'; % Linear model with no error term
	callBacks.exitCond = ha_callBacks('exitCond','phempty'); 
	callBacks.sliceCond = @(info)([0;0;1]); % no slice, compute tubes only
	states(1) = ha_state('s1',@(lp)(ex_pll_model(lp,1)),inv1,phOpt,callBacks);
	callBacks.exitCond = ha_callBacks('exitCond','phempty'); 
	callBacks.sliceCond = @(info)([info.fwdStep>5;0]); % slice, no tube 
	states(2) = ha_state('s2',@(lp)(ex_pll_model(lp,2)),inv2,phOpt,callBacks);
	callBacks.exitCond = ha_callBacks('exitCond','maxFwdStep',9); 
	callBacks.sliceCond = @(info)([0;0;0]); % no slice, no tube 
	states(3) = ha_state('s3',@(lp)(ex_pll_model(lp,1)),inv1,phOpt,callBacks);

  % transistion
	trans(1) = ha_trans('s1','s2');
	trans(2) = ha_trans('s2','s3',1);

	% source
	source = 's1'; 

	% initial 
	initPh = ph_createByBox(2,[1,2],[0.96,1.02;0.05,0.1]);

	ha = ha_create('pll',states,trans,source,initPh);


function ldi = ex_pll_model(lp,mode)
  p = struct('fref',2,  'g1', -0.01, 'g1_ph', -0.1, 'g2', -0.002,'cmin',0.9,...
		         'cmax',1.1,'ccode',1.0, 'KT',0.5,'N',1,'a7',2.429570434864088);
  switch(mode)
		case 1
		  c1 = p.a7*p.g1; 
		case 2
		  c1 = -p.a7*p.g1; 
		otherwise
			error('incorrect mode');
	end	
	c2 = -p.KT; c3 = -p.fref;
	A = [0,0;1,c2]; b = [c1;c3]; u = [0;0];
	ldi = int_create(A,b,u);

