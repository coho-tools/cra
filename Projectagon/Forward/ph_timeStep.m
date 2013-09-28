function [timeStep,ph] = ph_timeStep(ph)
opt = ph.fwd.opt;
object = opt.object;

bloatAmt = ph.fwd.bloatAmt;
if(strcmpi(object,'ph'))
	modelLP = lp_and(ph.fwd.bloatLP,opt.constraintLP); % same in ph_model
	timeStep = model_maxTimeStep(ph.fwd.models,modelLP,bloatAmt);
else
	timeStep = Inf;
	for i=1:ph.ns
		slice = ph.fwd.slices(i);
		for j=1:slice.nf
			face = slice.faces(j);
			t = model_maxTimeStep(face.models,face.modelLP,bloatAmt);
			timeStep = min(t,timeStep);
		end
	end
end
timeStep = min(timeStep,opt.maxStep);
ph.fwd.timeStep = timeStep;

function maxT = model_maxTimeStep(models,lp,bloatAmt) 
	n = size(bloatAmt,1);
	range = [ones(n,1)*-Inf,ones(n,1)*Inf];
	for i=1:length(models) 
		% NOTE we only support LDI only, use function handle for extension.
		bbox = int_range(models{i},lp);  
		range = [max(range(:,1),bbox(:,1)),min(range(:,2),bbox(:,2))];
	end
	ts = [-bloatAmt(:,1),bloatAmt(:,2)]./range; 
	maxT = min(ts(ts>0));
%end
