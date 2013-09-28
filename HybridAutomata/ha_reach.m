function [ha,phs,timeSteps] = ha_reach(ha)
% This function solves reachability problems for all states of a hybrid automata. 

% NOTE: Ususally, there are more than one states available, 
% thus, we can speed up the computation by parrallel computation. 

% get information
name  = ha.name; 
edges = ha.edges; transActs = ha.transActs; 
HEAD=1; GATE=2; TAIL=3; 
sources = ha.sources; initials = ha.initials;
inv = ha.inv; rpath = ha.rpath; 
order = ha.order; start = ha.last+1;

% create the directory
utils_system('mkdir',rpath);

try
	log_write(sprintf('\nStart to compute reachable region for hybrid automata %s',name));
	t = cputime;
	for i=start:length(order)
		sid = order(i);  state = ha.states(sid);
		log_write(sprintf('Computing reachable region of the %s state (%d/%d) ...',state.name,i,length(order)));
		tt = cputime;
	
		% compute init region from all sources
		if(any(sid==sources)) % user provides
			init = initials{sid==sources};
		else % union of slices
			ind = edges(:,TAIL)==sid;	
			hid = edges(ind,HEAD); gid = edges(ind,GATE); transAct = transActs(ind); 
			nh = length(hid); slices = cell(nh,1);
			% NOTE, the union operation may generate large error.
			% Therefore, we trim it by state invariant first
			sinv = lp_and(inv,state.inv);
			for s=1:nh
				resetMap = transAct{s};
				slices{s} = ha.states(hid(s)).slices{gid(s)}; 
				if(~isempty(resetMap))
					%slices{s} = resetMap(slices{s});
					slices{s} = ph_canon(resetMap(slices{s}),sinv);
				end
			end 
			%init = ph_canon(ph_simplify(ph_union(slices)));
			init = ph_canon(ph_simplify(ph_union(slices)),sinv);
		end
		
		% compute reachable region
		[phs,timeSteps,state] = ha_stateReach(state,init,inv);
		ha.states(sid) = state; ha.last=i;
	
		% save reachable region 
		log_write(sprintf('Writting reachable regions data to %s ...',ha_get(ha,'statefile',sid)));
		save(ha_get(ha,'statefile',sid),'phs','timeSteps');
		log_write(sprintf('Computation in the %s state is completed in %d mins',state.name,(cputime-tt)/60));
	end
	log_write('Writing the final hybrid automata to disk ...');
	save(ha_get(ha,'hafile'),'ha');
	log_write(sprintf('Computation of hybrid automata %s is completed in %d mins\n',name,(cputime-t)/60));
catch ME
	% Make sure the hybrid automata file is saved
	% Save a hybrid automata is time-consuming (I also hate it!).
	% I do not want to save it after each state computation. 
	% Therefore, I save it here if something bad happens.
	log_write('Exceptions found. Remember to save the hybrid automata to disk!!!');
	%save(ha_get(ha,'hafile'),'ha');
	%[phs,timeSteps,state] = ha_stateReach(state,init,inv); % repeat the error
	rethrow(ME); 
end
