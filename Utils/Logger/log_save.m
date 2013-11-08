function file = log_save(name,varargin)
% file = log_save(name,varargin)
% This function save data to disk
% The interface is the same with builtin 'save' function. 
% It saves data in the directory 'threadPath' automatically
if(nargin<1||isempty(name))
	error('The file name can not be empty'); 
end
% get variable vars
if(isempty(varargin))
	vars = evalin('caller','who');
else
	vars = varargin;
end
% get value of vars
for i=1:length(vars)
	val = evalin('caller',vars{i});
	eval([vars{i},'= val;']);
end
% save mat file
threadPath = cra_cfg('get','threadPath'); 
file = sprintf('%s/%s',threadPath,name);

save(file,vars{:});
