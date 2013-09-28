function [ids,status] = utils_strs2ids(strs,opts)
% [ids,status] = utils_strs2ids(strs,opts)
% This function find the position of string in a cell array of string
%	For example, 
%		strs = 'POLY', opts = {'poly','hull'}
%	then the result 
%		ids = 1.
% 	0 will be returned if not found
%	The comparison is not case sensitive.	
%	status = 0, if only one match found	
%		   = -1, if no match found (ids = [])
%		   = 1, if more than one matches found (ids includes all matches)
% If strs is a cell array of string, the result are column vectors
% opts can not contain redundant strings
if(isempty(strs))
	ids = zeros(0,1); status = zeros(0,1);
	return;
end

if(isempty(opts)) % not found
	if(ischar(strs)), ns = 1; else ns = length(strs); end
	ids = zeros(ns,1); status = -1*ones(ns,1);
	return;
end

if(ischar(strs))
	ids = find(strcmpi(strs,opts));
	status = max(1,length(ids)-1);
	if(isempty(ids)), ids = 0; end
elseif(iscell(strs))
	ns = length(strs); ids = zeros(ns,1); status = zeros(ns,1);
	for i=1:ns
		id = find(strcmpi(strs{i},opts));
		status(i) = max(1,length(id)-1);
		if(~isempty(id)), ids(i) = id; end
	end
else
	error('unsupported type');
end
