function state = ha_state(name,modelFunc,inv,phinfo,fwdOpt,exitFunc,doSlice,entryAct,stepAct,exitAct)
if(nargin<2), error('not enough parameters'); end
if(nargin<3), inv = []; end
if(nargin<4), phinfo = []; end
if(nargin<5), fwdOpt = []; end
if(nargin<6), exitFunc = []; end
if(nargin<7), doSlice = []; end
if(nargin<8), entryAct = []; end
if(nargin<9), stepAct = []; end
if(nargin<10), exitAct = []; end

% check parameters
if(isempty(name)||~ischar(name))
	error(' state name must be a non-empty string');
end
if(isempty(modelFunc)||~isa(modelFunc,'function_handle'))
	erorr(' modelFunc must be a  non-empty function handle');
end
if(~isempty(inv)&&~lp_iscoho(inv))
	error(' inv must be a COHO Linear Program');
end
if(~isempty(exitFunc) && ~isa(exitFunc,'function_handle'))
	error(' exitFunc must be a function handle');
end 
if(~isempty(doSlice) && ~iscell(doSlice) && ~isa(doSlice,'function_handle'))
	error(' doSlice must be a function handle or a cell of function handel');
end
if(~isempty(entryAct) && ~iscell(entryAct) && ~isa(entryAct,'function_handle'))
	error(' entryAct must be a function handle or a cell of function handel');
end
if(~isempty(stepAct) && ~iscell(stepAct) && ~isa(stepAct,'function_handle'))
	error(' stepAct must be a function handle or a cell of function handel');
end
if(~isempty(exitAct) && ~iscell(exitAct) && ~isa(exitAct,'function_handle'))
	error(' exitAct must be a function handle or a cell of function handel');
end

% we need exit function 
if(isempty(exitFunc))
	exitFunc = ha_funcTemp('exitFunc','default'); % default template.
end
name = lower(name); % case insenstive

% number of gates, we add this field for transitions 
if(isempty(inv))
	ng = 0;
else
	ng = size(inv.A,1); 
end
slices = cell(ng,1); % slice through each gate
assert(any(length(doSlice)==[1,ng]));

state = struct('name',name, 'modelFunc',modelFunc, ...
	'inv',inv, 'phinfo',phinfo, 'fwdOpt',fwdOpt, ...
	'exitFunc',exitFunc, 'doSlice',doSlice, ...
	'entryAct',{entryAct}, 'stepAct',{stepAct}, 'exitAct',{exitAct}, ...
	'ng',ng, 'sgates',[], 'slices',{slices});
