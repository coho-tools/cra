function ha = ha_create(name,states,trans,sources,initials,inv,rpath)
if(nargin<1), name = []; end 
if(nargin<2), states = []; end
if(nargin<3), trans = []; end
if(nargin<4), sources = []; end
if(nargin<5), initials = []; end
if(nargin<6), inv = []; end
if(nargin<7), rpath = []; end

% check parameters
if(~( isempty(name) || ischar(name) ))
	error(' name must be a non-empty string');
end
if(isempty(states) && ~(isempty(trans)&&isempty(sources)&&isempty(initials)) )
	error(' there is no state in the automaton, so there should be no transitions, sources, or initials');
end
if(~( isempty(sources) || ischar(sources) || iscell(sources) ))
	error('sources must be a string or a cell of strings');
end
if(~( isempty(initials) || isstruct(initials) || iscell(initials) ))
	error('sources must be a projectagon structure or a cell of strings');
end
if(~( isempty(inv) || lp_iscoho(inv) ))
	error('inv must be a COHO linear program');
end
if(~( isempty(rpath) || ischar(rpath) ))
	error('rpath must be a directory');
end

% set default parameters
if(isempty(name))
	name = 'anonymous'; 
end
if(isempty(states))
	states = repmat(ha_state('nouse',@nofunc),0,1);
end
if(isempty(trans))
	trans = repmat(ha_trans('nowhere',1,'nowhere'),0,1);
end
if(isempty(rpath))
	rpath = './';
end
if(~isempty(initials)&&~iscell(initials))
	initials = {initials}; % make the following codes simplier
end


% count the number of elements
ns = length(states); nt = length(trans); 
if(ischar(sources))
	nc = 1;
else
	nc = length(sources);
end
% allow one source and initial region is empty
if(nc==1&&isempty(initials))
	initials = cell(1,1);
end
ni = length(initials);
if(ni~=nc)
	error('The number of sources and initials must be the same');
end

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% get all state names and assign ID
snames = {states.name}; 
if(length(unique(snames))~=ns)
	error('state name must be unique');
end
% convert sources to ID.
sources = utils_strs2ids(sources,snames);
if(any(sources<1))
	error('source state not found');
end

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% process transistion
heads = {trans.head}; tails = {trans.tail}; 
hids = utils_strs2ids(heads,snames);
tids = utils_strs2ids(tails,snames); % allow edges to 'nowhere'
if(any(hids==0))  
	error('head state not found');
end
gates = reshape([trans.gate],nt,1);
ngs = reshape([states.ng],ns,1);
if(any(gates<1) || any(gates>ngs(hids)))
	error('gate is out of range');
end
HEAD = 1; GATE=2; TAIL = 3; 
edges(:,[HEAD,GATE,TAIL]) = [hids,gates,tids]; 
transActs = {trans.transAct}; % record transActs

% We compute slices only necessary 
for i=1:ns
	tid = (edges(:,HEAD)==i); % source from this state
	gs = unique(edges(tid,GATE)); % gate of this transistion
	states(i).sgates  = gs; % update states
end

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Compute the reachability computation order 
% 1. break incoming edge of sources
tedges = edges;
cinds = utils_graphEdge(tedges(:,[HEAD,TAIL]),sources,'incoming');
tedges(cinds,:) = [];

% 2. remove dangling states except sources
while(true) % remove edges from dangling nodes recursively
	% find zero in-degree nodes
	nzs = tedges(:,TAIL); nzs(nzs==0) = []; % do not consider 'nowhere' state
	isd = true(ns,1); isd([nzs;sources]) = false; dsids = find(isd);
	% remove edges from dangling states
	inds = utils_graphEdge(tedges(:,[HEAD,TAIL]),dsids,'outgoing');
	tedges(inds,:) = [];
	if(isempty(inds))
		break;
	end
end
if(~isempty(dsids)) % dangling states found
	dsnames = snames(dsids);
	cstr = strcat('''',dsnames,''',');
	str = strcat(cstr{:});
	str = sprintf('{%s}',str(1:end-1));
	log_write(sprintf('Dangling states found! Remove them using:\n ha_op(ha,''remove'',%s)\n',str)); 
end

% 3. compute computation flow from sources until no new reachable state
order = sources; news = sources;
while(true) % remove edges from sources recursively
	inds = utils_graphEdge(tedges(:,[HEAD,TAIL]),news,'outgoing');
	tedges(inds,:) = [];
	% find zero in-degree nodes
	nzs  = tedges(:,TAIL); nzs(nzs==0) = []; 
	isn = true(ns,1); isn([nzs;order;dsids]) = false;
	news = find(isn); order = [order;news];
	if(isempty(news))
		break;
	end
end 
if(length(order)+length(dsids)~=ns)
	error('unsupported loop found');
end
last = 0; % no state has been computed

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% create the structure
ha = struct('name',name, 'states',states, 'snames',{snames}, ... % states
		'edges',edges,  'transActs',{transActs}, ... % transitions
		'sources',sources, 'initials',{initials}, ... % sources
		'inv',inv, 'rpath',rpath, 'order',order, 'last',last); % other info

