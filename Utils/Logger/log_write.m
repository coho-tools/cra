function log_write(str,addinfo)
% This function write message to logfile.
if(nargin<2||isempty(addinfo))
	addinfo = false;
end

wfids = cra_cfg('get','logWFIds'); 
if(addinfo)
	info = utils_fileInfo(2); % parent
	name = info.name; line = info.line;
	str = sprintf('%s(%d):\t%s',name,line,str);
end
str = [str,'\n'];
for i=1:length(wfids)
	fprintf(wfids(i),str);
end
