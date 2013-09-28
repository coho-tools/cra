function java_writeDummy
% java_writeDummy
% This function write two empty line to java.
% It is used to fix the bug of java compiler to get the result.
str = 'println(''\n'');';
str = sprintf('%s\n%s',str,str);
java_writeLine(str);
