function A = java_readMatrix
%function A = java_readMatrix
% This function read a matrix from a file
% we rely on the fact that the matrix contents proper
% is between '(' and ')';
%
text = java_readLine;
while 1
    RPARpos = findstr(text, ')' );
    if ~isempty(RPARpos)
        LPARpos = findstr(text, '(' );
        text = text(LPARpos+1:RPARpos-1);
        text = [ 'A = ' text ';'];
        text = java_hex2num(text);
        eval(text);
        break;
    end
	line = java_readLine;
    if (isnumeric(line) && line < 0) 
		error('java_readMatrix: unexpected end of file');
    end
    text = [text ' ' line];
end
