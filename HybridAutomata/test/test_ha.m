function test_ha
  test_ex0;
%	test_ex1;
%	test_ex2;

function test_ex0
	init = ph_createByBox(2,[1,2],[1,2;2,3]); 
	f1 = @(lp)(int_create( eye(2),zeros(2,1),ones(2,1)*1e-6));  
	f2 = @(lp)({int_create(-eye(2),zeros(2,1),ones(2,1)*1e-6)});  
  inv1 = lp_createByBox([1,3;2,4]);
  inv2 = lp_createByBox([2,3;3,4]);
  states(1) = ha_state('s1',f1,inv1); 
  states(2) = ha_transState('s2',f2,inv2);
	trans(1) = ha_trans('s1','s2');
	ha = ha_create('ex0',states,trans,'s1',init); 
	ha_reach(ha);

function test_ex1
	dim =2; planes=[1,2];
	ph = ph_createByBox(dim,planes,[0.5,0.6;0.0,0.1]);
	ph = ph_convert(ph,1);
	
	opt = ph_getOpt;
	opt.object = 'ph';
	opt.model = 'bloatAmt';
	phOpt.fwdOpt = opt;

	% state 1
	% leave the region eventually as xdot > 0
	name1 = 'state1';
	modelFunc = @(lp)ex1_model(lp,1);
	inv = lp_createByBox([0.5,1;0.0,0.1]);
	phOpt.type = 1;       % convex, default planes 
  callBacks.exitCond   = ha_callBacks('exitCond','transit');  % exit when empty
  callBacks.sliceCond  = ha_callBacks('sliceCond','transit');% always slice
	callBacks.beforeStep = @(info)ex1_step_callback(info,1,0); 
	callBacks.afterStep  = @(info)ex1_step_callback(info,1,1); 
	states(1) = ha_state(name1,modelFunc,inv,phOpt,callBacks);

	% state 2
	% stuck in the region as 0 is in the initial region
	name2='state2';
	modelFunc = @(lp)ex1_model(lp,2);
	inv = lp_createByBox([0.9,1;0,1]);
	phOpt.type = 2;       % bbox
	phOpt.planes = [2,1]; % change planes
  callBacks.exitCond   = ha_callBacks('exitCond','stable'); % exit when converge
  callBacks.sliceCond  = ha_callBacks('sliceCond','stable');
	callBacks.beforeStep = @(info)ex1_step_callback(info,2,0); 
	callBacks.afterStep  = @(info)ex1_step_callback(info,2,1); 
	states(2) = ha_state(name2,modelFunc,inv,phOpt,callBacks);

	% transition
	trans(1) = ha_trans(name1,name2,1);
	trans(2) = ha_trans(name1,name2,2);
	trans(3) = ha_trans(name1,name2,3);
	trans(4) = ha_trans(name1,name2,4);
	trans(5) = ha_trans(name1,name2,0); % test gate 0

	% automata 
	name = 'ex1';
	inv = lp_createByBox([0.5,1;0.0,1]);
	rpath='.';
	ha = ha_create(name,states,trans,name1,ph,inv,rpath);

	% computation
	ha_reach(ha);
	ha_reachOp(ha,@(phs,ts)phs_display(phs));

function ldi = ex1_model(lp,state) 
	switch(state)
		case 1  % increase x
			A = [1,0;0,0]; b = [0;0];
		case 2  % increase y
			A = [0,0;0,1]; b = [0;0];
		otherwise
			error('do not support');
	end
	u = [0;0];
	ldi{1} = int_create(A,b,u);

function ph = ex1_step_callback(info,state,boa)
	fig = state;
	ph = info.ph;
	if(boa==0) % before
		ph_display(ph,fig,[],[],'b');
	else % after
		ph_display(ph,fig,[],[],'r');
		pause(1);
	end

function test_ex2
	% test empty hybrid automaton
	ha = ha_create();
	ha_reach(ha);
	test_get(ha);
	
	% test state function
	% not allowd
	%state = ha_state();
	%state = ha_state(' ',' ');
	states(1) = ha_state(' ',@nofunc);
	states(end+1) = ha_state('test1',@sin);
	states(end+1) = ha_state('test2',@sin,lp_createByBox([0,1]));
	% non-coho lp
	%inv = lp_create(rand(4,4),rand(4,1));
	%states(end+1) = ha_state('test2',@sin,inv);
	phOpt.type = 1; phOpt.nofield = 2; phOpt.fwdOpt = ph_getOpt;
	states(end+1) = ha_state('test3',@sin); 
	states(end+1) = ha_state('test4',@sin,[],phOpt);
	callBacks.exitCond = @nofunc;
	states(end+1) = ha_state('test5',@sin,[],[],callBacks); 
	callBacks.sliceCond = @nofunc;
	states(end+1) = ha_state('test6',@sin,[],[],callBacks); 
	callBacks.beforeComp = @nofunc;
	states(end+1) = ha_state('test7',@sin,[],[],callBacks); 
	callBacks.afterComp = @nofunc;
	states(end+1) = ha_state('test8',@sin,[],[],callBacks); 
	callBacks.beforeStep= @nofunc;
	states(end+1) = ha_state('test9',@sin,[],[],callBacks); 
	callBacks.afterStep= @nofunc;
	states(end+1) = ha_state('test10',@sin,[],[],callBacks); 
	states(end+1) = ha_transState('test11',@sin);
	states(end+1) = ha_stableState('test12',@sin,[],[],[1,2]);
	ha = ha_create([],states);
	ha_reach(ha);

	
	% test trans 
	trans(1) = ha_trans('head','tail',1);
	trans(end+1) = ha_trans('head','tail',1,@nofunc);
	trans(end+1) = ha_trans('head','tail',0); 
	%trans(end+1) = ha_trans('head','tail',-1);
	%ha = ha_create('bad',states,trans);
	
	% test ha_create
	%ha = ha_create('empty',[],[],[1,2],cell(2,1),[],'here');
	ha = ha_create('empty',states,[],{'test1',' '},cell(2,1),[],'here');
	ha = ha_op(ha,'remove',{'test2','test3','test4','test5','test6','test7','test8','test9','test10','test11','test12'});
	ha = ha_reach(ha);
	ha_op(ha,'disptrans');

	% test loop
	inv = lp_create(rand(4,2),rand(4,1));
	states(1) = ha_transState('s1',@sin,inv);
	states(2) = ha_transState('s2',@sin,inv);
	states(3) = ha_transState('s3',@sin,inv);
	states(4) = ha_transState('s4',@sin,inv);
	states(5) = ha_transState('s5',@sin,inv);
	states(6) = ha_transState('s6',@sin,inv);
	trans(1) = ha_trans('s1','s2',1);
	trans(2) = ha_trans('s2','s3',1);
	trans(3) = ha_trans('s3','s4',1);
	trans(4) = ha_trans('s4','s5',1);
	trans(5) = ha_trans('s5','s6',1);
	trans(6) = ha_trans('s6','s7',1);
	%trans(7) = ha_trans('s5','s4',1);
	sources = 's1'; initials = [];
	%ha = ha_create('loop',states,trans,sources,initials);
	%ha = ha_reach(ha);
	ha = ha_create('noloop',states,trans,sources,initials);
	%ha = ha_reach(ha);
	
	trans(1) = [];
	sources = {'s1','s2'}; initials = cell(2,1);
	ha = ha_create('twosrcs',states,trans,sources,initials);
	%ha = ha_reach(ha);
	

	% test func
	inv = lp_createByBox([0.5,0.7;0.0,0.2]);
	phOpt.type = 1; phOpt.planes=[2,1]; phOpt.fwdOpt = ph_getOpt('fast');
	callBacks = [];
	callBacks.beforeComp = @(info)disp('init here, hahaha');
	callBacks.afterStep = ha_callBacks('afterStep','display'); 
	callBacks.afterComp = @(info)disp('exit here, haha');
	states(1) = ha_transState('s1',@(lp)ex1_model(lp,1),inv,phOpt); 
	states(1) = ha_state('s1',@(lp)ex1_model(lp,1),inv,phOpt,callBacks); 
	sources = {'s1','s2'}; 
	dim = 2; planes = [1,2]; ph = ph_createByBox(dim,planes,[0.5,0.6;0,0.2]);
	initials = {ph,[]};
	ha = ha_create('func',states,trans,sources,initials);
	ha = ha_reach(ha);
	

function test_get(ha)
	fields = {'name','states','trans','sources','initials','inv','rpath','order','last'};
	for i=1:length(fields)
		val = ha_get(ha,fields{i});
	end
	states = ha_get(ha,'states');
	for i=1:length(states)
		val = ha_get(ha,'state',states(i).name);
	end
	%val = ha_get(ha,'state','foo');

