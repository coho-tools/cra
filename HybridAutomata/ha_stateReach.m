function [phs,timeSteps,state] = ha_stateReach(state,init,ginv)
% This function computes the reachable region of a state

% get information
name = state.name; modelFunc = state.modelFunc;
inv = state.inv; phinfo = state.phinfo; fwdOpt = state.fwdOpt;
exitFunc = state.exitFunc; doSlice = state.doSlice;
entryAct = state.entryAct; stepAct = state.stepAct; exitAct = state.exitAct;
sgates = state.sgates; ng = length(sgates);
tol = cra_cfg('get','tol');

% set modelFunc
cra_cfg('set','modelFunc',modelFunc);

% set fwdOpt
if(isempty(fwdOpt)) 
	fwdOpt = ph_getOpt; % use the default one 
end
binv = lp_bloat(inv,tol); % bloat outward
fwdOpt.constraintLP = lp_and(fwdOpt.constraintLP,lp_and(ginv,binv)); % add constraints

% compute LP for slicing
faceLPs = cell(ng,1);
for i=1:ng
	gate = sgates(i);
	lp = lp_create(-inv.A(gate,:),-inv.b(gate));
	faceLPs{i} = lp_bloat(lp,tol); % bloat inward
end

% Compute initial region 
initPh = init;
% update projectagon type
if(isfield(phinfo,'type')&&~isempty(phinfo.type))
	initPh = ph_convert(initPh,phinfo.type);
end
if(isfield(phinfo,'planes')&&~isempty(phinfo.planes))
	initPh = ph_chplanes(initPh,phinfo.planes);
end
% trim it by lp
initPh = ph_canon(initPh,lp_and(ginv,inv)); 
if(ph_isempty(initPh))
	log_write(sprintf('Empty inital region for state %s, skip the computation',state.name),true);
	phs = cell(0,1); timeSteps = []; 
	return;
end

% execute user provided functions when entering the states
if(~isempty(entryAct))
	if(iscell(entryAct))
		for i=1:length(entryAct)
			entryAct{i}();
		end
	else
		entryAct();
	end
end

% Perform reachability computation
N = 1000; phs = cell(N,1); timeSteps=zeros(N,1); % pre-allocate 
t = 0; complete= false; step = 0; faces = cell(ng,1);  ph = initPh;
while(~complete)
	step = step+1;
	log_write(sprintf('Computing forward reachable region of step %d from time %d',step,t));

	% Compute advance projectagon
	% tph is bloated outward to compute intersection face
	if(ph_isempty(ph)) 
		error('The exitFunc of state %s is incorrect, the new reachable region is empty.',name); 
	end
	[tph,prevPh] = ph_advanceSafe(ph,fwdOpt);
	ph = ph_canon(tph,inv); % trim with state invariant
	phs{step} = ph; timeSteps(step) = prevPh.fwd.timeStep;
	t = t+prevPh.fwd.timeStep; 

	% execute user provided functions to update ph
	if(~isempty(stepAct))
		if(iscell(stepAct))
			for i=1:length(stepAct)
				stepAct{i}(ph,prevPh);
			end
		else
			stepAct(ph,prevPh);
		end
	end

	% is the computation done? 
	complete = exitFunc(ph,rmfield(prevPh,'fwd'),t,step);

	% compute the slice or not?
	if(~iscell(doSlice))
		ds = doSlice(complete,t,step);
	else
		ds = zeros(state.ng,1);
		for i=1:state.ng
			func = doSlice{i};
			ds(i) = func(complete,t,step);
		end
	end
	if(any(ds)) 
		% Compute the reachable region in time [t_i,t_i+1]
		% NOTE: tph might be empty when the step size is large.
		% Therefore, we reconstruct the reachabe region without state inv constraints 
		if(~isempty(tph))
			iph = ph_succ(prevPh,tph); 
		else
			eps = 1e-3; % bloat outward a little
			iph = ph_succ(prevPh,ph_canon(ph_construct(prevPh)),eps);
			iph = ph_canon(iph,fwdOpt.constraintLP);
		end
		% compute the intersection slices for each gate 
		for i = 1:ng
			if(length(ds)==1 || ds(i)) 
				face = ph_intersectLP(iph,faceLPs{i}); 
				faces{i}{end+1} = ph_simplify(face);  % canonical 
			end
		end
	end
	% save temporal file if the computation is too slow
	N = 50;
	if(mod(step,N)==0)
		save('/tmp/ha_phs','phs','timeSteps','faces');
	end
end
phs = [{initPh};phs(1:step)]; % add the initial region
timeSteps = timeSteps(1:step);

% save slices for initial region of other states
for i=1:ng
	slice = ph_canon(ph_simplify(ph_union(faces{i})));
	state.slices{sgates(i)} = slice;
	if(isempty(slice)) 
		log_write(sprintf('The slice on gate %s:%d is empty',name,sgates(i)));
	end
end

% execute user provided functions when leaving the state
if(~isempty(exitAct))
	if(iscell(exitAct))
		for i=1:length(exitAct)
			exitAct{i}(phs,timeSteps,state);
		end
	else
		exitAct(phs,timeSteps,state);
	end
end
