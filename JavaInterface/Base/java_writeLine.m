function java_writeLine(str)
% java_writeLine(str)
% This function writes a new line to java
% It add '\n' to the string automatically.
javaIn = cra_cfg('get','javaIn'); 
fprintf(javaIn, '%s\n',str);
