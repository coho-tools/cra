function state = ha_state(name,modelFunc,inv,phOpt,callBacks)
%  state = ha_state(name,modelFunc,phOpt,inv,callBacks)
%  Create a hybrid automata state. A state consists of 
%    name: label of the state
%    modelFunc: a function handle which defines the system dynamics. 
%      The function must support the interface: 
%        ldi = modelFunc(lp)
%    inv: the invariants defined by a Coho LP. Each constraints defines a 
%      face/gate which are used for transition.
%    phOpt: projectagon config for the state. The structure has the following:
%      type:   the type of projectagon to be used. 
%      planes: projection subspaces of projectagons 
%      fwdOpt: options for ph_advance/ph_advanceSafe
%    callBacks: User provided functions to be executed during the computation. 
%      The structure has following fields (all must be functions)
%      exitCond:  exitCond = exitCond(info); 
%        condition to terminate the reachability computation in the state
%          info is a structure with following fields: 
%            ph: current projectagon 
%            prevPh: previous projectagon
%            fwdStep: forward steps 
%            fwdT: forward time 
%            compT: computation time 
%      sliceCond: sliceCond = sliceCond(info); 
%        condition to slice reachable tubes with invariant faces/gates
%          info has the following fields: 
%            complete: the reachable computation is complete 
%            ph, prevPh, fwdStep, fwdT, compT 
%        sliceCond could be a scalar or vector to have varied conds for faces 
%      beforeComp: ~ = beforeComp(info);
%        called before the reachability computation
%          info has the following fields: 
%            initPh: initial projectagons 
%      afterComp:  ~ = afterComp(info); 
%        called at the end of reachability computation
%          info has the fields (see reachData in ha_stateReach). 
%      beforeStep:  ph = beforeStep(info); 
%        called before each computation step 
%          info has the following fields: 
%            ph, prevPh, fwdStep, fwdT, compT 
%      afterStep:   ph = afterStep(info); 
%        called after each computation step 
%          info has the following fields: 
%            ph, prevPh, fwdStep, fwdT, compT 
%  name, modelFunc are required. Others are empty by default if not provided.

if(nargin<2), error(' name and modelFunc are required'); end
if(nargin<3), inv = []; end
if(nargin<4), phOpt = []; end
if(nargin<5), callBacks = []; end 

% check parameters
if(isempty(name)||~ischar(name))
	error(' state name must be a non-empty string');
end
if(isempty(modelFunc)||~isa(modelFunc,'function_handle'))
	erorr(' modelFunc must be a  non-empty function handle');
end
if(~isempty(inv)&&~lp_iscoho(inv)) % why require cohoLP?
	error(' inv must be a COHO Linear Program');
end
if(~isempty(callBacks))
  if(	isfield(callBacks,'exitCond') && ~isa(callBacks.exitCond,'function_handle')) 
		error(' exitCond must be a function handle');
	end
  if(	isfield(callBacks,'sliceCond') && ~isa(callBacks.sliceCond,'function_handle')) 
		error(' sliceCond must be a function handle');
	end
  if(	isfield(callBacks,'beforeComp') && ~isa(callBacks.beforeComp,'function_handle')) 
		error(' beforeComp must be a function handle');
	end
  if(	isfield(callBacks,'afterComp') && ~isa(callBacks.afterComp,'function_handle')) 
		error(' afterComp must be a function handle');
	end
  if(	isfield(callBacks,'beforeStep') && ~isa(callBacks.beforeStep,'function_handle')) 
		error(' beforeStep must be a function handle');
	end
  if(	isfield(callBacks,'afterStep') && ~isa(callBacks.afterStep,'function_handle')) 
		error(' afterStep must be a function handle');
	end
end 

% we need exit and slice function 
if(isempty(callBacks) || ~isfield(callBacks,'exitCond') || isempty(callBacks.exitCond))
	callBacks.exitCond = ha_callBacks('exitCond','default'); % default template.
end
if(isempty(callBacks) || ~isfield(callBacks,'sliceCond') || isempty(callBacks.sliceCond))
	callBacks.sliceCond = ha_callBacks('sliceCond','default'); % default template.
end
if(~isfield(callBacks,'beforeComp')), callBacks.beforeComp=[]; end;
if(~isfield(callBacks,'afterComp')), callBacks.afterComp=[]; end;
if(~isfield(callBacks,'beforeStep')), callBacks.beforeStep=[]; end;
if(~isfield(callBacks,'afterStep')), callBacks.afterStep=[]; end;

name = lower(name); % case insenstive

% number of gates, we add this field for transitions 
if(isempty(inv))
	ng = 0;
else
	ng = size(inv.A,1); 
end
slices = cell(ng+1,1); % slice through each gate + gate0

state = struct('name',name, 'modelFunc',modelFunc, ...
	'inv',inv, 'phOpt',phOpt, 'callBacks', callBacks, ...
	'ng',ng, 'sgates',[], 'slices',{slices}); 
