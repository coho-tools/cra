function func = ha_funcTemp(type,varargin)
% func = ha_funcTemp(type,varargin)
% This function provides some template function for states 
% 	'exitFunc' if type = 'exitFunc'
% 	'doSlice' if type = 'doSlice'
% 	'entryAct' if type = 'entryAct'
% 	'stepAct' if type = 'stepAct'
% 	'exitAct' if type = 'exitAct'

switch(lower(type))
	case lower('exitFunc')
		func = @(ph,prevPh,t,step)ha_exitFunc(ph,prevPh,t,step,varargin{:});
	case lower('doSlice')
		func = @(complete,t,step)ha_doSlice(complete,t,step,varargin{:});
	case lower('entryAct')
		func = @()ha_entryAct(varargin{:});
	case lower('stepAct')
		func = @(ph,prevPh)ha_stepAct(ph,prevPh,varargin{:});
	case lower('exitAct')
		func = @(phs,timeSteps,state)ha_exitAct(phs,timeSteps,state,varargin{:});
	otherwise
		error('do not support now');
end

function done = ha_exitFunc(ph,prevPh,t,step,opt,varargin)
switch(lower(opt))
	case 'transit'
		done = ph_isempty(ph);
	case 'stable'
		minT = varargin{1};
		if(length(varargin)>1&&~isempty(varargin{2}))
			maxT = varargin{2};
		else
			maxT = Inf;
		end
		done = (t>=minT&ph_contain(prevPh,ph))| t>=maxT;
	case 'faststable'
		maxT = varargin{1}; maxstep = varargin{2};
		tph = varargin{3}; tol = varargin{4};
		done = ha_exitFunc(ph,prevPh,t,step,'stableinv',tol,maxT) | ...
			ha_exitFunc(ph,prevPh,t,step,'maxstep',maxstep)|...
			ha_exitFunc(ph,prevPh,t,step,'target',tph);
	case 'stableinv'
		tol = varargin{1}; maxT = varargin{2};
		if(isempty(tol))
			tol = 0.02; 
		end
		if(isempty(maxT))
			maxT = Inf;
		end
		% stable invariant
		cond1= (ph_contain(prevPh,ph) & ph_ratio(ph,prevPh) > (1-tol));
		% maximu time
		cond2 = t>=maxT;
		done = cond1 | cond2;
		if(cond1)
			log_write('Stable invariant set found. Rerun it if you want to get more accurate result.',true); 
		end
	case 'maxstep'
		maxstep = varargin{1};
		if(isempty(maxstep))
			maxstep = 100; 
		end
		done = step >= maxstep;
		if(done)
			log_write('Maximum computation step achived',true);
		end
	case 'target' % achive a user provided target
		tph = varargin{1};
		done = ph_contain(tph,ph);
		if(done)
			log_write('User specified target achived, the computation is stopped',true);
		end
	case 'maxtime'
		maxT = varargin{1};
		done = t>=maxT;
	case 'default'
		done = ph_isempty(ph) || ph_contain(prevPh,ph);
	otherwise
		error('do not support');
end


function dos = ha_doSlice(complete,t,step,opt,varargin)
switch(lower(opt))
	case 'transit'
		dos = true;
	case 'stable'
		minT = varargin{1};
		dos = (t>=minT);
	case 'faststable'
		minT = varargin{1};
		dos = (complete | (t>=minT));
	otherwise
		error('do not support');
end 

function ha_entryAct(opt,varargin)
	error(' I really do not know what to do');

function ph = ha_stepAct(ph,prevPh,opt,varargin)
switch(lower(opt))
	case 'plot'
		ph_display(ph,1:ph.ns);
	case 'canon'
		lp = varargin{1};
		ph = ph_canon(ph,lp);
	otherwise
		error('do not support now');
end

function ha_exitAct(phs,timeSteps,state,opt,varargin)
switch(lower(opt))
	case 'save'
		file = varargin{1};
		save(file,'phs','timeSteps');
	case 'plot'
		phs_display(phs);
	otherwise
		error('do not support now');
end
