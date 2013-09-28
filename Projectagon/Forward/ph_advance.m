function [fwdPh,ph,opt]= ph_advance(ph,opt)
% [fwdPh,ph,opt]= ph_advance(ph,opt)
% This function advances a projectagon. 
% Inputs
%	ph: a canonical projectagon
%	opt: seee ph_getMarchOpt. 
% Outputs:
%	fwdPh: advanced projectagon
%	ph: the input projectagon with an extra 'fwd' fields. 
%	opt: the input opt with prevBloatAmt and prevTimeStep udpated.
if(nargin<1||ph_isempty(ph))
	error('The input projectagon is empty');
end
if(nargin<2||isempty(opt))
	opt = cra_opt('get','phOpt'); 
end
ph_checkOpt(opt,ph);

dim = ph.dim; 
ph.fwd.opt = opt; 
maxBloat = opt.maxBloat; 

% break polygons
if(~ph.iscanon), ph = ph_canon(ph); end;
ph = ph_smash(ph);

% compute a valid pair of <bloatAmt,timeStep>
switch(lower(opt.model))
	case 'bloatamt' % fixed bloatAmt
		if(isempty(opt.bloatAmt))
			bloatAmt = maxBloat*ones(dim,2);
		else
			bloatAmt = opt.bloatAmt;
		end
		ph = ph_model(ph,bloatAmt);
		[timeStep,ph] = ph_timeStep(ph);
		while(true)
		  ph = ph_forward(ph,timeStep);
		  [valid,ph] = ph_verify(ph);
		  % NOTE valid may be false because of computation error in ph_verify
			if(~valid)
				timeStep = timeStep * 0.9; % reduce timeStep;
			else
				break; 
			end
		end
	case 'timestep' % fixed timeStep
		bloatAmt = maxBloat*ones(dim,2);
		timeStep = opt.timeStep;
		ph = ph_model(ph,bloatAmt);
		ph = ph_forward(ph,timeStep);
		[valid,ph] = ph_verify(ph);
		if(~valid)
			timeStep = ph_timeStep(ph);
			msg = num2str(timeStep); % return the maximum timeStep
			exception = MException('COHO:Projectagon:LargeTimeStep',msg); 
			throw(exception);
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
  tmpPh = ph; 
	% compare the bloat region
	bbox1 = sum(tmpPh.fwd.realBloatAmt,2);
	bbox2 = sum(tmpPh.fwd.bloatAmt,2);
	r = prod(bbox1./bbox2)^(1/dim);
	if(1-r>opt.reps), break; end
	tmpBloatAmt = min(tmpPh.fwd.realBloatAmt,maxBloat);
	tmpPh = ph_model(tmpPh,tmpBloatAmt);
	tmpPh = ph_forward(tmpPh,timeStep);
	[valid,tmpPh] = ph_verify(tmpPh);
	if(valid)
		ph = tmpPh;
		bloatAmt = tmpBloatAmt;
	else
		break;
	end;
end

fwdPh = ph_construct(ph);
fwdPh = ph_canon(fwdPh,opt.constraintLP,opt.canonOpt);

% update opt 
opt.prevBloatAmt = min(maxBloat,min(bloatAmt,ph.fwd.realBloatAmt)); 
opt.prevTimeStep = timeStep;
