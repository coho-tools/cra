function val = ha_reachOp(ha,func,range)
% This function apply a function to every ph in a state of ha
% 	func(phs,timeStep,tubes);
% 	val = func(phs,timeStep,tubes); 
if(nargin<3||isempty(range))
	range = [1,ha.last];
end
for i=range(1):range(2)
	sid = ha.order(i);
	file = ha_get(ha,'statefile',sid);
	if(~exist(file,'file'))
		fprintf('Can not find %s\n',file);
		continue;
	else
		fprintf('Working on %s\n',file);
	end
	data = load(file);
	if(nargout==0)
		func(data.phs,data.timeSteps,data.tubes);
	else
		val = func(data.phs,data.timeSteps,data.tubes);
	end
end
