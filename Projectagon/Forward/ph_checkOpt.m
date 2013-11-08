function valid = ph_checkOpt(opt,ph)
% valid = ph_checkOpt(ph,opt)
% This function checks if the input of ph_advance valid or not.
if(nargin<2)
	ph = [];
end

% check opt is a structure
if(~isstruct(opt))
	error('The opt must be a structure');
end

% check opt has all fields
names = {'model','object','maxEdgeLen','tol','maxBloat','maxStep',...
	'riters','reps','useInterval','bloatAmt','timeStep', ...
	'prevBloatAmt','prevTimeStep','constraintLP','canonOpt','intervalOpt'};
for i=1:length(names)
	if(~isfield(opt,names{i}))
		error(['The opt does not have the ',names{i},' filed']);
	end
end

% check all fields are valid
names = fieldnames(opt); 
for i=1:length(names)
	name = names{i};
	value = opt.(name);
	switch(name)
		case 'model'
			if(~ischar(value)||~any(strcmpi(value,{'guess-verify','bloatAmt',...
					'timeStep'})))
				error(['opt.',name,' can only be [''guess-verify''|''bloatAmt''|''timeStep'']']);
			end
		case {'maxEdgeLen','maxStep'}
			if(~isfloat(value)||value<=0)
				error(['opt.',name,' must be a positive value']);
			end
		case {'timeStep','prevTimeStep'}
			if(isempty(value)), continue; end
			if(~isfloat(value)||value<=0)
				error(['opt.',name,' must be a positive value']);
			end
		case {'maxBloat','bloatAmt','prevBloatAmt'}
			if(isempty(value)&&~strcmp(name,'maxBloat')), continue; end
			if(~isfloat(value)||any(value(:)<0))
				error(['opt.',name,' must be a non-negative value']);
			end
			if(~isempty(ph))
				dim = ph.dim; 
				if(~any(numel(value)==[1,dim,dim*2]))
					error(['the size of opt.',name,' can only be [1,',dim,',',dim*2,']']);
				end
			end
		case 'object'
			if(~ischar(value)||~any(strcmpi(value,{'ph','face-all',...
					'face-none','face-bloat','face-height'})))
				error(['opt.',name,' can only be [''ph''|''face-all''|''face-none''|''face-bloat''|''face-height'']']);
			end
		case 'useInterval'
			if(~islogical(value))
				error(['opt.',name,' must be a logical value']);
			end
		%
		case {'riters','ntries'}
			if(~isfloat(value)||value<0)
				error(['opt.',name,' must be a non-negative value']);
			end
		case {'reps','tol'}
			if(~isfloat(value)||value<=0|| value>=1)
				error(['opt.',name,' must be a positive value in (0,1)']);
			end
		case 'constraintLP'
			if(isempty(value)), continue; end
			if(~isstruct(value))
				error(['opt.',name,' must be a LP from lp_create']);
			end
		case {'canonOpt','intervalOpt'}
			if(isempty(value)), continue; end
			if(~isstruct(value))
				error(['opt.',name,' must be a structure']);
			end
			% do not check the fileds now
		otherwise 
			error(['The opt has extra filed ',name]); 
	end 
end


% check the value
if(~isempty(ph)&&ph.type==0&&strcmpi(opt.object,'ph'))
	error(' If opt.object is ''ph'', the result is always convex');
end
if(strcmpi(opt.object,'timeStep')&&isempty(opt.timeStep))
	error('opt.timeStep can not be empty when opt.object is ''timeStep'' ');
end
if(~isempty(opt.timeStep)&&opt.timeStep>opt.maxStep)
	error('opt.timeStep can not be greater than opt.maxStep');
end
if(~isempty(opt.prevTimeStep)&&opt.prevTimeStep>opt.maxStep)
	error('opt.prevTimeStep can not be greater than opt.maxStep');
end
if(~isempty(opt.bloatAmt)&&any(opt.bloatAmt(:)>opt.maxBloat(:)))
	error('opt.bloatAmt can not be greater than opt.maxBloat');
end
if(~isempty(opt.prevBloatAmt)&&any(opt.prevBloatAmt(:)>opt.maxBloat(:)))
	error('opt.prevBloatAmt can not be greater than opt.maxBloat');
end

valid = true;
