function test_ha
  disp('**** Test Hybrid Automata ****');
  disp('** Test a Simple Hybrid Automata **');
  %test_ex0;
  disp('** Test a More Complicated Hybrid Automata **');
	%test_ex1;
  disp('** Test Function Corner Cases **'); 
	test_ex2;

function test_ex0
	disp('  Specify state dyanmics');  
	f1 = @(lp)(int_create( eye(2),zeros(2,1),ones(2,1)*1e-6));  
	f2 = @(lp)(int_create(-eye(2),zeros(2,1),ones(2,1)*1e-6));  

	disp('  Specify state invariant');  
  inv1 = lp_createByBox([1,3;2,4]);
  inv2 = lp_createByBox([2,3;3,4]);

	disp('  Create States');  
  states(1) = ha_state('s1',f1,inv1); 
  states(2) = ha_transState('s2',f2,inv2);

	disp('  Create Transistions');  
	trans(1) = ha_trans('s1','s2');
	
	disp('  Specify sources and initial regions');  
	init = ph_createByBox(2,[1,2],[1,2;2,3]); 

	disp('  Create the hybrid automata'); 
	ha = ha_create('ex0',states,trans,'s1',init); 

	disp('  Perform reachability computation for the automata'); 
	ha_reach(ha);

function test_ex1
  % phOpt
  disp('  Set projectagon optionals by phOpt.fwdOpt');
	opt = ph_getOpt;
	opt.object = 'ph';
	opt.model = 'bloatAmt';
	phOpt.fwdOpt = opt;


  disp('  Create automata states'); 
	% state 1
	% leave the region eventually as xdot > 0
  disp('    Create the first state'); 
	name1 = 'state1';
  disp('      Specify system dynamics, reachable region will leave the state'); 
	modelFunc = @(lp)ex1_model(lp,1);
  disp('      Specify state invariant'); 
	inv = lp_createByBox([0.5,1;0.0,0.1]);
  disp('      Specify projectagon types by phOpt.type'); 
	phOpt.type = 1;       % convex, default planes 
  disp('      Specify state callBacks using ha_callBacks'); 
  callBacks.exitCond   = ha_callBacks('exitCond','transit');  % exit when empty
  callBacks.sliceCond  = ha_callBacks('sliceCond','transit');% always slice
  disp('      Specify state callBacks using specified functions. Show projectagons on each computation step.'); 
	callBacks.beforeStep = @(info)ex1_step_callback(info,1,0); 
	callBacks.afterStep  = @(info)ex1_step_callback(info,1,1); 
	states(1) = ha_state(name1,modelFunc,inv,phOpt,callBacks);

	% state 2
	% stuck in the region as 0 is in the initial region
  disp('    Create the second state'); 
	name2='state2';
  disp('      Specify system dynamics, reachable region stays in the state'); 
	modelFunc = @(lp)ex1_model(lp,2);
  disp('      Specify state invariant'); 
	inv = lp_createByBox([0.9,1;0,1]);
  disp('      Specify projectagon types by phOpt.type. Change to use bbox.'); 
	phOpt.type = 2;       % bbox
  disp('      Specify projectagon planes by phOpt.planes. Use planes [x2,x1]'); 
	phOpt.planes = [2,1]; % change planes
  disp('      Specify state callBacks using ha_callBacks'); 
  callBacks.exitCond   = ha_callBacks('exitCond','stable'); % exit when converge
  callBacks.sliceCond  = ha_callBacks('sliceCond','stable');
  disp('      Specify state callBacks using specified functions. Show projectagons on each computation step.'); 
	callBacks.beforeStep = @(info)ex1_step_callback(info,2,0); 
	callBacks.afterStep  = @(info)ex1_step_callback(info,2,1); 
	states(2) = ha_state(name2,modelFunc,inv,phOpt,callBacks);

	% transition
  disp('  Create automata transistion'); 
  disp('    Link state1 to state 2, using all four gates.');
	trans(1) = ha_trans(name1,name2,1);
	trans(2) = ha_trans(name1,name2,2);
	trans(3) = ha_trans(name1,name2,3);
	trans(4) = ha_trans(name1,name2,4);
  disp('    Link state1 to state 2, using virutal gate 0.');
	trans(5) = ha_trans(name1,name2,0); % test gate 0
  
	% initial regions
  disp('  Specify initial region'); 
	dim =2; planes=[1,2];
	initPh = ph_createByBox(dim,planes,[0.5,0.6;0.0,0.1]);

  disp('  Specify global invariant'); 
	inv = lp_createByBox([0.5,1;0.0,1]);

  disp('  Save reachable data on to local dir'); 
	rpath='.';

	% automata 
  disp('  Create the automata'); 
	ha = ha_create('ex1',states,trans,name1,initPh,inv,rpath);

	% computation
  disp('  Perform computation'); 
	ha = ha_reach(ha);
	
  disp('  Perform operations on reachable data: display all tubes'); 
	ha_reachOp(ha,@(reachData)phs_display(reachData.tubes,[],[],[],'y'));

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
	ldi = int_create(A,b,u);

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
	disp('  Test an empty hybrid automata');
	ha = ha_create();
	ha_reach(ha);
	test_get(ha);
	
	% test state function
	disp('  Test state functions');
	% not allowd
	disp('    Must specify state name and dynamics'); 
	%state = ha_state();
	%state = ha_state(' ',' ');
	disp('    State name could be any string'); 
	states(1) = ha_state(' ',@nofunc);
	disp('    Provide name and dynamics'); 
	states(end+1) = ha_state('test1',@sin);
	disp('    Provide state invariant'); 
	states(end+1) = ha_state('test2',@sin,lp_createByBox([0,1]));
	% non-coho lp
	disp('    State invariant must be Coho LP'); 
	%inv = lp_create(rand(4,4),rand(4,1));
	%states(end+1) = ha_state('test2',@sin,inv);
	disp('    Specify phOpt'); 
	phOpt.type = 1; phOpt.nofield = 2; phOpt.fwdOpt = ph_getOpt;
	states(end+1) = ha_state('test3',@sin); 
	states(end+1) = ha_state('test4',@sin,[],phOpt);
	disp('    Specify state callBacks'); 
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
	disp('    Test transition state'); 
	states(end+1) = ha_transState('test11',@sin);
	disp('    Test stable state'); 
	states(end+1) = ha_stableState('test12',@sin,[],[],[1,2]);
	disp('    Create an automata with only state.'); 
	disp('    Dangling states are found.');
	ha = ha_create([],states);
	disp('    Perform computation: nothing as no transitions and initials'); 
	ha_reach(ha);

	
	% test trans 
	disp('  Test transistion functions');
	disp('    Provide only source and target.'); 
	trans(1) = ha_trans('head','tail');
	disp('    Use virtual gate 0.'); 
	trans(end+1) = ha_trans('head','tail',0); 
	disp('    Use real gate 1.'); 
	trans(end+1) = ha_trans('head','tail',1);
	disp('    Gate can not be negative'); 
	%trans(end+1) = ha_trans('head','tail',-1);
	disp('    Provide resetMap function'); 
	trans(end+1) = ha_trans('head','tail',1,@nofunc);
	disp('    Can not uses these transistions as faked states'); 
	%ha = ha_create('bad',states,trans);
	
	% test ha_create
	disp('  Test ha_create');
	disp('    Empty automata must have nothing');
	%ha = ha_create('empty',[],[],[1,2],cell(2,1),[],'here');
	disp('    Create an automata');
	ha = ha_create('empty',states,[],{'test1',' '},cell(2,1),[],'here');
	disp('    Remove states by ha_op');
	ha = ha_op(ha,'remove',{'test2','test3','test4','test5','test6','test7','test8','test9','test10','test11','test12'});
	disp('    Print state transistion');
	ha_op(ha,'disptrans');
	disp('    Preform reachability computation');
	ha = ha_reach(ha);

	% test loop
	disp('  Test loop in the automata'); 
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
	disp('    Can not have loop in the automata'); 
	%trans(7) = ha_trans('s5','s4',1);
	sources = 's1'; initials = [];
	%ha = ha_create('loop',states,trans,sources,initials);
	%ha = ha_reach(ha);
	ha = ha_create('noloop',states,trans,sources,initials);
	%ha = ha_reach(ha);
	
	disp('  Test an automata without trans');
	trans(1) = [];
	sources = {'s1','s2'}; initials = cell(2,1);
	ha = ha_create('twosrcs',states,trans,sources,initials);
	%ha = ha_reach(ha);
	

	% test func
	disp('  Test a meanful hybrid automata');
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

