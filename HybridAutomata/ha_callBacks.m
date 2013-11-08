function func = ha_callBacks(callback, method, varargin) 
% func = ha_callBacks(callback, varargin) 
% This function provides some template function for HA callbacks 
%   callback could be 
% 	  'exitCond':   method could be:  
%        'default', 'transit'/'phEmpty','phConverge','maxFwdStep','maxFwdT',
%        'maxCompT','target','stable'
% 	  'sliceCond':  method could be:
%        'default'/'transit'/'always', 'nil/never', 'minFwdT', 
%        'stable'/'complete'
% 	  'beforeComp': method could be: 
%        'default'/'nil','display'
% 	  'afterComp':  method could be: 
%        'default'/'nil','display','save'
% 	  'beforeStep':   method could be: 
%        'default'/'nil','display','trim'
% 	  'afterStep':   method could be: 
%        'default'/'nil','display','trim'
% Method is 'default' if not provided

  if(nargin<2||isempty(method)), method = 'default'; end

	switch(lower(callback))
		case lower('exitCond')
			func = @(info)ha_exitCond(info,method,varargin{:});
		case lower('sliceCond')
			func = @(info)ha_sliceCond(info,method,varargin{:});
		case lower('beforeComp')
			func = @(info)ha_beforeComp(info,method,varargin{:});
		case lower('afterComp')
			func = @(info)ha_afterComp(info,method,varargin{:});
		case lower('beforeStep')
			func = @(info)ha_beforeStep(info,method,varargin{:});
		case lower('afterStep')
			func = @(info)ha_afterStep(info,method,varargin{:});
		otherwise
			error('do not support now');
	end
end

function done = ha_exitCond(info,method,varargin) 
	switch(lower(method))
		case 'default'                        % ph is empty or converged
			done = ph_isempty(info.ph) || ph_contain(info.prevPh,info.ph);

		case {'phempty','transit'}            % Stop when projectagon is empty  
			done = ph_isempty(info.ph);

		case 'phconverge'                     % Stop when ph converged
			done = ph_contain(info.prevPh,info.ph);
			
		case 'maxfwdstep'                     % Stop when max foward step reached
			if(isempty(varargin) || isempty(varargin{1}))
				error('Maximum forward steps must be provided');
			end
			maxFwdStep = varargin{1};           
			done = (info.fwdStep >= maxFwdStep);

		case 'maxfwdt'                        % Stop when max foward time reached
			if(isempty(varargin) || isempty(varargin{1}))
				error('Maximum forward time must be provided');
			end
			maxFwdT = varargin{1};
			done = (info.fwdT>=maxFwdT);

		case 'maxcompt'                       % Stop when max comp time reached
			if(isempty(varargin) || isempty(varargin{1}))
				error('Maximum computation time must be provided');
			end
			maxCompT = varargin{1};
			done = (info.compT>=maxCompT);

		case 'target'                         % Stop when a target reached
			if(isempty(varargin) || isempty(varargin{1}))
				error('Target projectagon must be provided');
			end
			tph = varargin{1};
			done = ph_contain(tph,info.ph);

		case 'stable'                         % Stop when converge or max time
			minFwdT = 0; maxFwdT = Inf;
			if(~isempty(varargin) && ~isempty(varargin{1})), maxFwdT = varargin{1}; end
			if(length(varargin)>1 && ~isempty(varargin{2})), minFwdT = varargin{2}; end
			done = (info.fwdT>=maxFwdT) || ...
				     (info.fwdT>=minFwdT && ph_contain(info.prevPh,info.ph)); 
			
		otherwise
			error('do not support');
	end
end


function dos = ha_sliceCond(info,method,varargin)
	switch(lower(method))
		case {'default','transit','always'}   % Slice on each step
			dos = true;
		case {'nil','never'}                  % Never slice 
			dos = false;
		case 'minfwdt'                        % Slice after some time
			if(isempty(varargin)||isempty(varargin{1}))
				error('minimum forward time must be provided');
			end
			minT = varargin{1};
			dos = (info.fwdT>=minT);
		case {'complete','stable'}            % Slice when leave the state
			dos = info.complete;
		otherwise
			error('do not support');
	end 
end

function ha_beforeComp(info,method,varargin)
	% By default, do nothing
	switch(lower(method))
		case {'default','nil'}
			% do nothing
		case 'display'               % Display the initial projectagons
			ph_display(info.initPh);
		otherwise
			erorr('do not support now');
	end
end

function ha_afterComp(info,method,varargin)
	switch(lower(method))
		case {'default','nil'}
			% do nothing
		case 'display'              % Display all projectagons
			phs_display(info.phs);
		case 'save'                 % Save the computation result to some file
			file = varargin{1};
			save(file,'info'); 
		otherwise
			error('do not support now');
	end
end % function

function ph = ha_beforeStep(info,method,varargin)
  ph = info.ph;
	switch(lower(method))
		case {'default','nil'}
			% do nothing
		case 'display'               % Display the projectagon before each step
			ph_display(ph); 
		case 'trim'                  % Trim the projectagon before each step
			lp = varargin{1};
			ph = ph_canon(ph,lp);
		otherwise
			error('do not support now');
	end
end


function ph = ha_afterStep(info,method,varargin)
  ph = info.ph;
	switch(lower(method))
		case {'default','nil'}
			% do nothing
		case 'display'               % Display the projectagon after each step
			ph_display(ph); 
		case 'trim'                  % Trim the projectagon after each step
			lp = varargin{1};
			ph = ph_canon(ph,lp);
		otherwise
			error('do not support now');
	end
end

