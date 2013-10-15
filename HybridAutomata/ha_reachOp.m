function ha_reachOp(ha,func,range)
% ha_reachOp(ha,func,range)
% This function apply a function to state reachable data 
% Parameters: 
%   ha: hybrida automata
%   func: the user provided function
%   range: [start,end] Range of state to perform the function. 
%         start/end should be the state order in ha.order.
%         [1,ha.last] by default. 
%   The function interface: func(reachData);
%   reachData is structure, for fields, please check ha_stateReach.m
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
	func(data.reachData);  
end
