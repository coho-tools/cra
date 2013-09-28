function test_ha
	dbstop if error;
	warning off all;
	addpath('../../');
	coho_addpath('HybridAutomata');
	java_open;
	log_open;
%	test_ex1;
	test_ex2;
	log_close;
	java_close;
function test_ex2
	% test empty hybrid automaton
	ha = ha_create();
	%ha_reach(ha);
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
	phinfo.type = 1; phinfo.nofield = 2;
	states(end+1) = ha_state('test3',@sin,[],phinfo);
	fwdOpt = ph_getOpt;
	states(end+1) = ha_state('test4',@sin,[],[],fwdOpt);
	states(end+1) = ha_state('test5',@sin,[],[],[],@nofunc);
	states(end+1) = ha_state('test6',@sin,[],[],[],[],@nofunc);
	states(end+1) = ha_state('test7',@sin,[],[],[],[],[],@nofunc);
	states(end+1) = ha_state('test8',@sin,[],[],[],[],[],{@nofunc,@nofunc});
	states(end+1) = ha_state('test9',@sin,[],[],[],[],[],[],@nofunc);
	states(end+1) = ha_state('test10',@sin,[],[],[],[],[],[],[],@nofunc);
	states(end+1) = ha_transState('test11',@sin);
	states(end+1) = ha_stableState('test12',@sin,[],[],[],[],[],[],[1,2]);
	states(end+1) = ha_fastStableState('test13',@sin,[],[],[],[],[],[],[3,4],100,[],0.1);
	ha = ha_create([],states);
	%ha_reach(ha);

	
	% test trans 
	trans(1) = ha_trans('head',1,'tail');
	trans(end+1) = ha_trans('head',1,'tail',@nofunc);
	%trans(end+1) = ha_trans('head',0,'tail'); 
	%trans(end+1) = ha_trans('head',-1,'tail');
	%ha = ha_create('bad',states,trans);
	
	% test ha_create
	%ha = ha_create('empty',[],[],[1,2],cell(2,1),[],'here');
	ha = ha_create('empty',states,[],{'test1',' '},cell(2,1),[],'here');
	ha = ha_op(ha,'remove',{'test2','test3','test4','test5','test6','test7','test8','test9','test10','test11','test12','test13'});
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
	trans(1) = ha_trans('s1',1,'s2');
	trans(2) = ha_trans('s2',1,'s3');
	trans(3) = ha_trans('s3',1,'s4');
	trans(4) = ha_trans('s4',1,'s5');
	trans(5) = ha_trans('s5',1,'s6');
	trans(6) = ha_trans('s6',1,'s7');
	%trans(7) = ha_trans('s5',1,'s4');
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
	phinfo.type = 1; phinfo.planes=[2,1];
	fwdOpt = ph_getOpt('fast');
	entryAct = @()disp('init here, hahaha');
	stepAct = @(ph,prevPh)(ph_display(ph));
	exitAct = @(phs,timeSteps,state)disp('exit here, haha');
	states(1) = ha_transState('s1',@(lp)ex1_model(lp,1),inv,phinfo,fwdOpt,entryAct,stepAct,exitAct);
	sources = {'s1','s2'}; 
	dim = 2; planes = [1,2]; ph = ph_createByBox(dim,planes,[0.5,0.6;0,0.2]);
	initials = {ph,[]};
	ha = ha_create('func',states,trans,sources,initials);
	ha = ha_reach(ha);
	
	%state = ha_state(name,modelFunc,inv,phinfo,fwdOpt,exitFunc,doSlice,entryAct,stepAct,exitAct)

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

function test_ex1
	dim =2; planes=[1,2];
	ph = ph_createByBox(dim,planes,[0.5,0.6;0.0,0.1]);
	ph = ph_convert(ph,1);
	
	opt = ph_getOpt;
	opt.object = 'ph';
	opt.model = 'bloatAmt';
	stepAct = @(ph,prevPh)(ph_display(ph));
	% state 1
	name1 = 'state1';
	modelFunc = @(lp)ex1_model(lp,1);
	inv = lp_createByBox([0.5,1;0.0,0.1]);
	phinfo.type = 1;
	states(1) = ha_transState(name1,modelFunc,inv,phinfo,opt,[],stepAct);
	% state 2
	name2='state2';
	modelFunc = @(lp)ex1_model(lp,2);
	inv = lp_createByBox([0.9,1;0,1]);
	phinfo.type = 2;
	phinfo.planes = [2,1];
	states(2) = ha_fastStableState(name2,modelFunc,inv,phinfo,opt,[],stepAct);
	% transition
	trans(1) = ha_trans(name1,1,name2);
	trans(2) = ha_trans(name1,2,name2);
	trans(3) = ha_trans(name1,3,name2);
	trans(4) = ha_trans(name1,4,name2);
	% automata 
	name = 'ex1';
	inv = lp_createByBox([0.5,1;0.0,1]);
	rpath='./';
	ha = ha_create(name,states,trans,name1,ph,inv,rpath);
	% computation
	ha_reach(ha);
	ha_reachOp(ha,@(phs,ts)phs_display(phs));

function ldi = ex1_model(lp,state) 
	switch(state)
		case 1
			A = [1,0;0,0]; b = [0;0];
		case 2
			A = [0,0;0,1]; b = [0;0];
		otherwise
			error('do not support');
	end
	u = [0;0];
	ldi{1} = int_create(A,b,u);
