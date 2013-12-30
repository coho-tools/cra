function [fwdPh,ph,opt,tube]= ph_advance(ph,opt)
% [fwdPh,ph,opt,tube]= ph_advance(ph,opt)
% This function advances a projectagon. 
% Inputs
%	  ph: a canonical projectagon
%	  opt: seee ph_getOpt. 
% Outputs:
%	  fwdPh: advanced projectagon
%	  ph: the input projectagon with an extra 'fwd' fields. 
%	  opt: the input opt with prevBloatAmt and prevTimeStep udpated.
%   tube: reachable tube
if(nargin<1||ph_isempty(ph))
	error('The input projectagon is empty');
end
if(nargin<2||isempty(opt))
	opt = cra_cfg('get','phOpt'); 
end
ph_checkOpt(opt,ph);

dim = ph.dim; 
% update size of maxBloat
maxBloat = opt.maxBloat; 
switch(numel(maxBloat))
	case 1
		maxBloat = repmat(maxBloat,dim,2);
	case dim
		maxBloat = repmat(maxBloat(:),1,2);
	case 2*dim
		maxBloat = reshape(maxBloat,dim,2);
	otherwise
		error('incorrect number of maxBloat');
end
opt.maxBloat = maxBloat;
ph.fwd.opt = opt; 

% break polygons
if(~ph.iscanon), ph = ph_canon(ph); end;
ph = ph_smash(ph);

% compute a valid pair of <bloatAmt,timeStep>
switch(lower(opt.model))
	case 'bloatamt' % fixed bloatAmt
		if(isempty(opt.bloatAmt))
			bloatAmt = maxBloat; 
		else
			bloatAmt = opt.bloatAmt;
		end
		ph = ph_model(ph,bloatAmt);
		[timeStep,ph] = ph_timeStep(ph);
		ph = ph_forward(ph,timeStep);
		[valid,ph] = ph_verify(ph);
    %% NOTE valid may fail because of over-approx in realBloatAmt; 
		%% For example: ph_simplify increases the poly_area slightly, 
		%% but may change the ph_realBloatAmt significantly. 
%%		while(~valid)
%%		  %%log_write('WARN:: Verify failed for model bloatAmt.'); 
%%		  timeStep = timeStep * 0.9; % reduce timeStep;
%%		  ph = ph_forward(ph,timeStep);
%%		  [valid,ph] = ph_verify(ph);
%%		end
	case 'timestep' % fixed timeStep
		bloatAmt = maxBloat; 
		timeStep = opt.timeStep;
		ph = ph_model(ph,bloatAmt);
		ph = ph_forward(ph,timeStep);
		[valid,ph] = ph_verify(ph);
    %% NOTE valid may fail because of over-approx in realBloatAmt; 
		if(~valid)
			timeStep = ph_timeStep(ph);
			if(opt.timeStep > timeStep)
			  msg = num2str(timeStep); % return the maximum timeStep
			  exception = MException('COHO:Projectagon:LargeTimeStep',msg); 
			  throw(exception);
			else
		    %%log_write('WARN:: Verify failed for model timeStep.'); 
			end
		end
	case 'guess-verify'
		while(true)
			[bloatAmt,timeStep,ph,check] = ph_guess(ph);
			ph = ph_model(ph,bloatAmt);
			ph = ph_forward(ph,timeStep);
			[valid,ph] = ph_verify(ph); 
			if(~check||valid)
				break;
			end
		end
	otherwise
		error('do not support');
end

% Try to reduce model error
for i=1:opt.riters
	% compare the bloat region
	bbox1 = diff(ph.fwd.realBloatAmt,[],2);
	bbox2 = diff(ph.fwd.bloatAmt,[],2);
	r = prod(bbox1./bbox2)^(1/dim);
	if(1-r>opt.reps), break; end % stop if the gap is small
	bloatAmt = min(ph.fwd.realBloatAmt,maxBloat);
	ph = ph_model(ph,bloatAmt);
	ph = ph_forward(ph,timeStep);
	[valid,ph] = ph_verify(ph);
  %% NOTE valid may fail because of over-approx in realBloatAmt; 
%%	if(~valid)
%%		%%log_write('WARN:: Verify failed during reducing model error.'); 
%%	end
end

fwdPh = ph_construct(ph);
tube  = ph_canon(ph_succ(ph,fwdPh),opt.constraintLP,opt.canonOpt);
fwdPh = ph_canon(fwdPh,opt.constraintLP,opt.canonOpt);

% update opt 
opt.prevBloatAmt = min(maxBloat,min(bloatAmt,ph.fwd.realBloatAmt)); 
opt.prevTimeStep = timeStep;

