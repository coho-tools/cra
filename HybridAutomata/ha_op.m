function ha = ha_op(ha,op,varargin)
snames = ha.snames;
HEAD=1; GATE=2; TAIL=3;
switch(lower(op))
	case {'remove','trim'} % remove states and its outgoing transitions
		% find state ID 
		rstates = varargin{1};
		rsids = utils_strs2ids(rstates,snames);
		if(any(rsids==0))
			str = strcat('''',rstates(rsids==0),''',');
			str = strcat(str{:});
			error('state {%s} not found',str(1:end-1));
		end

		% map between old ID and new ID
		newInd = true(length(snames),1);
		newInd(rsids) = false;
		newInd = cumsum(newInd); newInd(rsids) = NaN;
		
		% update states and snames
		ha.snames(rsids) = [];
		ha.states(rsids) = [];
	
		% remove all outgoing transistions from these states
		inds = utils_graphEdge(ha.edges(:,[HEAD,TAIL]),rsids,'outgoing');
		ha.edges(inds) = []; ha.transActs(inds) = [];

		% update sources
		sources = newInd(ha.sources); inds = isnan(sources);
		sources(inds) = []; ha.sources = sources;
		ha.initials(inds) = [];
		
		% update order and last
		order = newInd(ha.order); order(isnan(order)) = [];
		ha.order = order;
		ha.last = 0; % force to recompute

	case {'cp','copy','mv','move'}% move the data to a new directory
		oha = ha; 
		npath = varargin{1}; ha.rpath = npath; 
		utils_system('mkdir',npath); 
		for i=1:ha.last 
			sid = ha.order(i); 
			osfile = ha_get(oha,'statefile',sid); 
			nsfile = ha_get(ha,'statefile',sid); 
			if(exist(osfile,'file')) 
				utils_system(op,osfile,nsfile); 
			end 
		end	
		save(ha_get(ha,'hafile'),'ha');
		if(any(strcmpi(op,{'mv','move','rm'})))
			utils_system('rm',ha_get(oha,'hafile'));
		end
	case {'slice','split'} % partition one state into several
		error('see ph_slice');

	case 'disp' % display the hybrid automaton for debug
		ns = length(snames);
		fprintf('Hybrid automaton %s has states:\n',ha.name);
		str='';
		for i=1:ns
			str = sprintf('%s, ''%s''',str,snames{i});	
		end
		fprintf('%s\n',str(3:end));
		fprintf('Hybrid automaton %s has transistisons:\n',ha.name);
		edges = ha.edges; nt = size(edges,1);
		for i=1:nt
			if(edges(i,TAIL)==0)
				to = 'nil';
			else
				to = snames{edges(i,TAIL)};
			end
			fprintf('%s:%d\t->\t%s\n',snames{edges(i,HEAD)},edges(i,GATE),to);
		end
	case 'disptrans' % display the transistion only
		edges = ha.edges; nt = size(edges,1);
		for i=1:nt
			if(edges(i,TAIL)==0)
				to = 'nil';
			else
				to = snames{edges(i,TAIL)};
			end
			fprintf('%s:%d\t->\t%s\n',snames{edges(i,HEAD)},edges(i,GATE),to);
		end
	otherwise
		error('not implemented');
end
