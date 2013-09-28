function line = java_readLine
% line = java_readLine
% This function read the next non-comment line
fid = cra_cfg('get','javaOut');
while 1
    line = fgetl(fid);
	if(~ischar(line))% EOF
		cra_cfg('set','javaCrashed',true);
		exception = MException('COHO:JavaInterface:EOF', 'Reach the End of File');
		throw(exception);
	end;
    if(~isempty(line) && ... % not empty
		    isempty(regexpi(line,'^\s*$'))) % not white space
        if(isempty(regexpi(line,'^\s*%'))) % not comment
        break;
		elseif(~isempty(regexpi(line,'^\s*% EXCEPTION'))) % java exception
			err = sprintf('%s\n',line(3:end)); % remove '% '
			done = false;
			while(~done)
				line = fgetl(fid);
				err = sprintf('%s%s\n',err,line(3:end));
				done = ~isempty(regexpi(line,'^\s*% END EXCEPTION'));
			end;
			cra_cfg('set','javaCrashed',true);
			exception = MException('COHO:JavaInterface:Exception',err);
			throw(exception);
    end
  end
end
