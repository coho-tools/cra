function ha  = ha_reach(ha)
% ha  = ha_reach(ha)
% This function computes reachable regions for all states of a hybrid automata. 
% The computation result is added to the hybrid automata. 
% Reachable sets and tubes are saved to disk specified by ha.rpath as they are 
% usually very large.

SRC=1; GATE=2; TGT=3; 

% Get information
name  = ha.name; edges = ha.edges; 
sources = ha.sources; initials = ha.initials;
inv = ha.inv; rpath = ha.rpath; 
order = ha.order; start = ha.last+1;

% Create the directory
utils_system('mkdir',rpath);

try
	log_write(sprintf('\nStart to compute reachable region for hybrid automata %s',name));
	haT = cputime;

  % Compute reachable region for each state
	for i=start:length(order)
		sid = order(i);  state = ha.states(sid); stateT = cputime;
		log_write(sprintf('Computing reachable region of the %s state (%d/%d) ...',state.name,i,length(order)));
	
		% Compute init region from all sources states
		if(any(sid==sources)) % user provides
			init = initials{sid==sources};
		else % union of slices
			ind = edges(:,TGT)==sid;	
			hid = edges(ind,SRC); gid = edges(ind,GATE); 
			nh = length(hid); slices = cell(nh,1);
			% NOTE, Large error from union op, trim by invarint 
			sinv = lp_and(inv,state.inv);
			for s=1:nh
				resetMap = ha.resetMaps{s};
				if(gid(s)==0) % gate 0
					slices{s} = ha.states(hid(s)).slices{end}; 
				else
				  slices{s} = ha.states(hid(s)).slices{gid(s)}; 
				end
				if(~isempty(resetMap))
					slices{s} = ph_canon(resetMap(slices{s}),sinv);
				end
			end 
			init = ph_canon(ph_simplify(ph_union(slices)),sinv);
		end
		
		% Compute reachable region
		[state,reachData] = ha_stateReach(state,init,inv);
		ha.states(sid) = state; ha.last=i; ha.times(sid) = (cputime-stateT); 
		log_write(sprintf('Computation in the %s state is completed in %d mins',state.name,(cputime-stateT)/60));

		% Save state computation result 
		sfile = ha_get(ha,'statefile',sid);
		log_write(sprintf('Writing state reachable regions data to %s ...',sfile)); 
		save(sfile,'reachData'); 
	end

	% Save ha file at the end
	hfile = ha_get(ha,'hafile');
	log_write(sprintf('Writing the final hybrid automata to %s ...', hfile));
	save(hfile,'ha');

	log_write(sprintf('Computation of hybrid automata %s is completed in %d mins',name,(cputime-haT)/60));
catch ME
	% Save the automata, which is time-consuming.
	hfile = ha_get(ha,'hafile');
	log_write(sprintf('Exception found! The automata is saved in %s', hfile));
	save(hfile,'ha');
	rethrow(ME); 
end
