function [phs,timeSteps,tubes,state] = ha_stateReach(state,init,ginv)
% This function computes the reachable region of a state

%% Get state information
name = state.name; modelFunc = state.modelFunc; 
inv = state.inv; phOpt = state.phOpt; 
% gates
sgates = state.sgates; ng = state.ng; nsg = length(sgates);
% non-empty callbacks 
exitCond = state.callBacks.exitCond; sliceCond = state.callBacks.sliceCond;
% maybe empty callbacks
beforeComp = state.callBacks.beforeComp; afterComp = state.callBacks.afterComp;
beforeStep = state.callBacks.beforeStep; afterStep = state.callBacks.afterStep;
% parse phOpt;
phType = []; phPlanes = []; fwdOpt = []; 
if(isfield(phOpt,'type')), phType = phOpt.type; end;
if(isfield(phOpt,'planes')), phPlanes = phOpt.planes; end;
if(isfield(phOpt,'fwdOpt')), fwdOpt = phOpt.fwdOpt; end; 
if(isempty(fwdOpt)), fwdOpt = ph_getOpt; end % set default fwdOpt

% Prepare global info
% get tol 
tol = cra_cfg('get','tol');
% set modelFunc
cra_cfg('set','modelFunc',modelFunc); 

% Prepare for slicing
% set state constraints
binv = lp_bloat(inv,tol); % bloat outward for slicing
fwdOpt.constraintLP = lp_and(fwdOpt.constraintLP,lp_and(ginv,binv)); 
% compute LP for slicing
faceLPs = cell(nsg,1); % for virtual gate 0
for i=1:nsg
	gid = sgates(i);
	if(gid==0) % for virtual gate 0, no lp to intersect
		faceLPs{i} = [];
	else
	  lp = lp_create(-inv.A(gid,:),-inv.b(gid));
	  faceLPs{i} = lp_bloat(lp,tol); % bloat inward
	end
end

% Compute initial region 
initPh = init;
% update projectagon type
if(~isempty(phType))
	initPh = ph_convert(initPh,phType); 
end
if(~isempty(phPlanes))
	initPh = ph_chplanes(initPh,phPlanes);
end
% trim it by lp
initPh = ph_canon(initPh,lp_and(ginv,inv)); % no slice, no bloat
if(ph_isempty(initPh))
	log_write(sprintf('Empty inital region for state %s, skip the computation',state.name),true);
	phs = cell(0,1); timeSteps = []; 
	return;
end
% execute user provided functions when entering the states
if(~isempty(beforeComp))
	info.initPh = initPh;
  beforeComp(info);
end

% Perform reachability computation
N = 1000; phs = cell(N,1); tubes = cell(N,1);
timeSteps=zeros(N,1); faces = cell(nsg,1);  
startT = cputime; saveT = cputime; fwdT = 0; compT = 0;
ph = initPh; prevPh = []; complete= false; fwdStep = 0; 
while(~complete)
	fwdStep = fwdStep+1;
	log_write(sprintf('Computing forward reachable region of fwdStep %d from time %d',fwdStep,fwdT));

	% Compute forward reachable sets and tubes 
	if(~isempty(beforeStep)) % Callback before each fwdStep
		cbInfo = struct('ph',ph,'prevPh',prevPh,'fwdStep',fwdStep,'fwdT',fwdT,'compT',compT);
	  ph = beforeStep(cbInfo); 
	end
	if(ph_isempty(ph)) 
		error('Exception in state %s, projectagon to be advanced is empty.',name); 
	end
	% foward reachable sets
	[nextPh,prevPh] = ph_advanceSafe(ph,fwdOpt); % prevPh = ph+fwdInfo
	% forward reachable tubes
  if(~isempty(nextPh))
    tube = ph_succ(prevPh,nextPh); 
  else
    % NOTE: nextPh might be empty when the fwdStep size is large.
    % We reconstruct the reachabe region without state inv constraints 
		bNextPh = ph_canon(ph_construct(prevPh));
		tube = ph_canon(ph_succ(prevPh,bNextPh),fwdOpt.constraintLP); 
  end
	% nextPh/tube trimmed by bloated invariant, ph trimmed by invariant. 
	ph = ph_canon(nextPh,inv);
	phs{fwdStep} = ph; tubes{fwdStep} = tubes; 
	timeSteps(fwdStep)=prevPh.fwd.timeStep; fwdT=fwdT+prevPh.fwd.timeStep; 
	compT = cputime-startT;
	cbInfo = struct('ph',ph,'prevPh',prevPh,'fwdStep',fwdStep,'fwdT',fwdT,'compT',compT);
	if(~isempty(afterStep)) % Callback after each fwdStep 
	  ph = afterStep(cbInfo); 
	end

	complete = exitCond(cbInfo); % is the computation done? 

  % compute the intersection slices for each gate 
	cbInfo.complete = complete; % compute the slice or not?
	ds = sliceCond(cbInfo);
	assert(numel(ds)==1 || numel(ds)==ng+1);
	if(length(ds)==1), ds = repmat(ds,ng+1,1); end;
	for i = 1:nsg
		gid = sgates(i); 
		if(gid==0), gid = ng+1; end;
	  if(ds(gid))
		  face = ph_intersectLP(tube,faceLPs{i}); 
		  faces{i}{end+1} = ph_simplify(face);  % canonical 
	  end
  end

	% save temporal file per hour 
	if((cputime-saveT)>=3600) 
	  path = cra_cfg('get','threadPath');
		log_write(sprintf('Writing projectagons on to %s',path));
		save([path,'/tmp'],'phs','tubes','timeSteps','faces');
		saveT = cputime;
	end
end
phs = [{initPh};phs(1:fwdStep)]; % add the initial region
tubes = tubes(1:fwdStep);
timeSteps = timeSteps(1:fwdStep);

% save slices for initial region of other states
for i=1:nsg  % the end is for gate 0
	slice = ph_canon(ph_simplify(ph_union(faces{i})));
	gid = sgates(i); 
	if(gid==0), gid=ng+1; end;
	state.slices{gid} = slice;
	if(isempty(slice)) 
		log_write(sprintf('The slice on gate %s:%d is empty',name,gid));
	end
end

% execute user provided functions when leaving the state
if(~isempty(afterComp)) 
	info = struct('phs',phs,'tubes',tubes,'timeSteps',timeSteps,'faces',faces); 
  afterComp(info); 
end
