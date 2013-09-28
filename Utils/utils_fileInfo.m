function info = utils_fileInfo(level)
if(nargin<1||isempty(level))
	level  = 1; % currrent function
end
val = dbstack;
if(length(val)<level+1)
	info=struct('file','base','name','base','line',0);
else
	info = val(level+1);
end
