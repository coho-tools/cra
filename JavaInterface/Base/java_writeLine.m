function java_writeLine(str)
% java_writeLine(str)
% This function writes a new line to java
% It add '\n' to the string automatically.
javaIn = cra_cfg('get','javaIn'); 
idx = cra_cfg('get','currThread');
fprintf(javaIn(idx), '%s\n',str);
