function [bloatAmt,timeStep,ph,check] = ph_guess(ph)
opt = ph.fwd.opt;
maxBloat = opt.maxBloat; maxStep = opt.maxStep;

% parameters
magic = 4; minR = 0.5; maxR = 0.9; 
bloatInc = 1.1; minGap = 0.8; 

check = true;
if(~isfield(ph.fwd,'tries'))  % initial guess
	prevTimeStep = opt.prevTimeStep; 
	prevBloatAmt = opt.prevBloatAmt; 
	if(~isempty(prevTimeStep) && ~isempty(prevBloatAmt)) % from previous step
		bloatAmt = prevBloatAmt; 
		timeStep = prevTimeStep; 
	else  % from maxBloat
		bloatAmt = maxBloat*ones(ph.dim,2); 
		ph = ph_model(ph,bloatAmt); 
		[timeStep, ph] = ph_timeStep(ph); 
		timeStep = magic*timeStep; 
	end
	ph.fwd.tries = 0; % add field
else % guess based on previous failure
	if(ph.fwd.tries<=opt.ntries)
		bloatAmt = ph.fwd.bloatAmt; timeStep = ph.fwd.timeStep;
		realBloatAmt = ph.fwd.realBloatAmt;
		r = max(realBloatAmt(:))/maxBloat; % the gap of bloatAmt and maxBloat
		if(r<minGap) % bloatAmt is small
			bloatAmt = max(bloatAmt,realBloatAmt)*bloatInc; % increase bloatAmt
		else % timeStep is large
			% the gap of bloatAmt and realBloatAmt
			bgap = min(bloatAmt(:)./realBloatAmt(:)); 
			timeStep = timeStep*min(maxR,max(minR,bgap));
		end
	else % Too many failure, use 'bloatAmt'
		bloatAmt = maxBloat*ones(ph.dim,2);
		ph = ph_model(ph,bloatAmt);
		[timeStep,ph] = ph_timeStep(ph);
		% Reduce timeStep further to ensure correctness even with computation error 
		timeStep = timeStep*0.9; 
		% do not check, otherwise, dead-loop is possible
		check = false; 
	end
end 
ph.fwd.tries = ph.fwd.tries+1;

% make sure the bloatAmt and timeStep is not too tiny.
r = max(bloatAmt(:))/maxBloat; 
% NOTE it is possible r=0 when object='ph' and realBloatAmt=0;
if(r>0 && r < minGap) 
	bloatAmt = bloatAmt/r; 
	timeStep = timeStep*sqrt(1/r);
end

bloatAmt = min(maxBloat,bloatAmt); 
timeStep = min(maxStep,timeStep); 
assert(~isinf(timeStep));
