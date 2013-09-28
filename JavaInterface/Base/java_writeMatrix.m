function java_writeMatrix(M, name,mtype)
% java_writeMatrix(M, name)
% This function write a matrix to java

% mtype: 'matrix' or 'boolMatrix', for java_writeBoolMatrix
if(nargin<3||isempty(mtype))
	mtype = 'matrix';
end

[rows,cols] = size(M);
fmt = java_format('write');


if(all([rows,cols]==[0,1]))  % java read matrix([[]]) as 1x0 vector
	str = sprintf('%s = transpose(matrix([[]]));',name);
else
	str = sprintf('%s = %s([\n',name,mtype);
	if(rows > 0) 
		for i = 1:rows 
			str = sprintf('%s [',str); 
			if(cols > 0) 
				vstr = sprintf([fmt],M(i,1));
				str = [str,vstr];
				if(cols > 1) 
					vstr = sprintf([', ',fmt],M(i,2:end));
					str = [str,vstr];
				end 
			end 
			str = sprintf('%s ]',str); 
			if (i < rows) 
				str = sprintf('%s ;',str); 
			end 
			str = sprintf('%s \n',str); 
		end
	else 
		str = sprintf('%s []',str); 
		for i = 2:cols 
			str = sprintf('%s, []',str); 
		end 
	end 
	str = sprintf('%s ]);',str);
end;

java_writeLine(str);
