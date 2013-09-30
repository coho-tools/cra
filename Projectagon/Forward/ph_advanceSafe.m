function [fwdPh,ph,opt,fail]= ph_advanceSafe(ph,opt)
% [fwdPh,ph,opt]= ph_advanceSafe(ph,opt)
% This function tries to handle exceptions during computations by change opt
if(nargin<1||ph_isempty(ph))
	error('The input projectagon is empty');
end
if(nargin<2), opt = []; end

try 
	fail = false;
	[fwdPh,ph,opt] = ph_advance(ph,opt);
catch ME
	fail = true;
	log_write(sprintf('Exception %s found in ph_advance',ME.identifier),true);
	if(~isempty(ME.identifier))
	  log_save(ME.identifier,'ph','opt','ME');
  end
	switch(lower(ME.identifier))
		case {lower('COHO:JavaInterface:EOF'),lower('COHO:JavaInterface:Exception')}
			% restart java
			java_close;
			java_open;
			newOpt = ph_safeOpt(opt);
		case lower('COHO:Polygon:OpException')
			% make the polygon bigger
			newOpt = ph_safeOpt(opt);
		case lower('COHO:Projectagon:LargeTimeStep')
			newOpt = opt;
			timeStep = str2double(ME.message);
			newOpt.timeStep = timeStep; 
%			if(opt.timeStep<=timeStep) 
%				% It may fail again because of computation error of realBloatAmt
%				newOpt.model='bloatAmt';
%			else
%				newOpt.timeStep = timeStep; 
%			end
		case lower('COHO:Projectagon:EmptyProjection') 
			% lp_createByHull may return under-approximated result.
			% therefore, faceLP might be infeasible (feasible to cplex, but not to java)
			% this cause empty projection polygon. Therefore, we use face-bloat here. 
			newOpt = ph_safeOpt(opt);
		otherwise
			rethrow(ME); 
			%error('unknown exceptions');
	end

	% try again with new opt
	try
		[fwdPh,ph,newOpt] = ph_advance(ph,newOpt);
	catch ME2
		log_write(sprintf('Exception %s found in ph_advance',ME.identifier),true);
	  if(~isempty(ME.identifier))
		  log_save(ME.identifier,'ph','opt','ME2');
		end
		% switch to use convex projectagon if fails again
		if(ph.type==0)
			ph = ph_convert(ph,1);
			[fwdPh,ph,newOpt] = ph_advance(ph,newOpt);
			fwdPh = ph_convert(ph,0);
		else
			rethrow(ME2);
		end
	end

	% NOTE return the old one.
	opt.prevBloatAmt = newOpt.prevBloatAmt;
	opt.prevTimeStep = newOpt.prevTimeStep;
end

function opt = ph_safeOpt(opt) 
	opt.useInterval=false; 
	opt.model='bloatAmt'; 
	opt.object='face-bloat';
