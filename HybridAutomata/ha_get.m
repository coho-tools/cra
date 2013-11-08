function val = ha_get(ha,field,varargin)
% val = ha_get(ha,field,varargin)
% This function is an interface for the hybrid automaton structure
% Field can be 
%   name: label of the ha
%   states: all ha states
%   trans: all states transitions
%   sources: source states
%   initials: initial regions 
%   inv: ha global invariant
%   rpath: path to save reachable set/tubes
%   haFile: the file for automata data
%   stateId: given the state name, find the internal state ID
%   stateFile: the file for state reachable sets/tubes
%   order: order of ha states to perform reachability analysis
%   last:  the last state id which completed rechable computation 

snames = ha.snames; 
switch(lower(field))
	% values required to rebuild the hybrid automata 
	case 'name'
		val = ha.name;
	case 'states'
		val = ha.states;
	case 'trans' % restore transition array 
		edges = ha.edges; nt = size(edges,1); resetMaps = ha.resetMaps; 
		SRC=1; GATE=2; TGT=3; 
		trans = repmat(ha_trans('nouse','nouse'),nt,1);
		for i=1:nt
			if(edges(i,TGT)==0)
				to = 'nowhere';
			else
				to = snames{edges(i,TGT)};
			end
			trans(i) = ha_trans(snames{edges(i,SRC)},to,edges(i,GATE),resetMaps{i});
		end
		val = trans;
	case 'sources'
		val = snames(ha.sources);
	case 'initials'
		val = ha.initials;
	case 'inv'
		val = ha.inv;
	case 'rpath'
		val = ha.rpath;
	% computation order
	case 'order'
		val = ha.order;
	case 'last'
		val = ha.last;
	case 'hafile' % file of this ha data
		val = sprintf('%s/%s_ha.mat',ha.rpath,ha.name);
	% each element
	case 'stateid' 
		sname = varargin{1};
		sid = utils_strs2ids(sname,snames); 
		if(sid<1)
			error('state %s not found',sname);
		end
		val = ha.states(sid);
	% file to save reachable region
	case 'statefile' % file of reachable region of a state
		sid = varargin{1};
		if(ischar(sid))
			sid = ha_get(ha,'stateid',sid);
		end
		if(sid<1 || sid>length(snames))
			error('sid id %d is out of range',sid);
		end
		val = sprintf('%s/%s_%s.mat',ha.rpath,ha.name,snames{sid});
	otherwise
		error('do not support now');
end
