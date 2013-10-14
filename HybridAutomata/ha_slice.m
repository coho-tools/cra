function ha = ha_slice(ha,stateName,vars,grids)
% This function partitions an automata state by slicing a set of variables into grids 
% The transitions between sub-states are determinate by the order of grids.
% For example, if the grids is [0,0.5,1], there are two sub-states for [0,0.5] -> [0.5,1.0];
% 	if the grids is [1,0.5,0], sub-states are [1,0.5]->[0.5,0] 
% This is useful to split monotonically increasing/decreasing variables in order to 
% reduce approximation error or (may) reduce computation time.
% For example
% 	ha = ha_slice(ha,'state1',1,0:0.2:1.8);
% 	ha = ha_slice(ha,'state2',[1,2],{0:0.2:1.8,1.8:-0.1:0});

if(length(vars)~=length(grids))
	error('The length of vars and grids must be the same');
end
nd = length(vars); siz = zeros(1,nd);
for i=1:nd
	siz(i) = length(grids{i})-1; % number of states of each dimensions
end

% Get automata information
states = ha_get(ha,'states'); trans = ha_get(ha,'trans');
sources = ha_get(ha,'sources'); initials = ha_get(ha,'initials');

%% Update states
% NOTE, the following operations depends on the structure of state (ha_state)
% Remember to update the code if ha_state is modified.
% 1. find the states
osind = utils_strs2ids(stateName,ha.snames);
% 2. delete the old state
ostate = states(osind); states(osind)=[]; 
% 3. create new states
if(nd==1)
	nstates = repmat(ostate,siz,1); 
else
	nstates = repmat(ostate,siz); 
end
sinds = cell(1,nd);
for ind=1:prod(siz)
	[sinds{:}] = ind2sub(siz,ind);
	state = ostate;

	% update 'name','inv','sliceCond','exitCond' fields
	name = state.name; sinv = state.inv; 
	exitCond = state.callBacks.exitCond; sliceCond = state.callBacks.sliceCond; 
	[ng,dim] = size(sinv.A);
	if(~iscell(sliceCond)) % convert to cell
		sliceCond = repmat({sliceCond},ng,1);
	end
	for d=1:nd % for each variable (dimension)
		dind = sinds{d}; % the index of this dimension 
		% name 
		name = [name,'_',num2str(dind)];
		% inv
		gd = grids{d}; from = gd(dind); to = gd(dind+1);
		A = zeros(2,dim); 
		A(:,vars(d))  = [-1;1]; b = [-from;to];
		if(from > to)
			A = -A; b = -b;
		end
		lp = lp_create(A,b);
		sinv = lp_and(sinv,lp); % two new gates, may produce duplicated gate on the boudary.
	end 
	% exitCond
	if(ind~=prod(siz))  % the last state may be stable state
		exitCond = ha_callBacks('exitCond','transit'); % override the exitCond
	end
	% sliceCond 
	func = ha_callBacks('sliceCond','transit'); % The variable varies monotonically 
	sliceCond = [sliceCond;repmat({func},2*nd,1)];
	state.name = name;
	state.inv = sinv;
	state.callBacks.exitCond = exitCond; state.callBacks.sliceCond = sliceCond; 
	state.ng= state.ng+2*nd; state.slices = cell(state.ng,1); % new gates
	nstates(sinds{:}) = state;
end

%% Update transistions
% 1. create transistions between states and sub-states
ntrans = repmat(trans,0,1);
for i=1:length(trans)
	t = trans(i); 
	if(strcmpi(t.src,stateName))
		% from all states
		for j = 1:prod(siz)
			t.src = nstates(j).name;
			ntrans(end+1,1) = t;
		end
	end
	t = trans(i);
	if(strcmpi(t.tgt,stateName))
		% to all states
		for j=1:prod(siz)
			t.tgt = nstates(j).name;
			ntrans(end+1,1) = t;
		end
	end
end
% 2. create transitions between sub-states
for ind =1:prod(siz)
	[sinds{:}] = ind2sub(siz,ind);
	src = nstates(sinds{:}).name;
	for d=1:nd
		if(sinds{d}<siz(d))
			nind = sinds; nind{d} = nind{d}+1;
			tgt = nstates(nind{:}).name;
			gate = ostate.ng+2*d; % the d-th new 'to' gate
			ntrans(end+1,1) = ha_trans(src,gate,tgt);
		end
	end
end
% 3. delete old transistion
inds = false(length(trans),1);
for i=1:length(trans)
	t = trans(i);
	if(strcmpi(t.src,stateName) || strcmpi(t.tgt,stateName))
		inds(i) = true;
	end
end
trans(inds) = [];

% Update sources and initials
for i=1:length(sources)
	if(strcmpi(sources{i},stateName)) 
		% replace the old one with a new one (the first sub-state) 
		sources{i} = nstates(1).name;
		break;
	end
end

% We assume the state is not the source
states = [states(:);nstates(:)]; trans = [trans(:);ntrans(:)];
ha = ha_create(ha.name,states,trans,sources,initials,ha.inv,ha.rpath);
