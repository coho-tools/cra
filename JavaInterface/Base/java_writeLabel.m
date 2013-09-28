function java_writeLabel
% java_writeLabel
% This function begin a new transaction to java
% each transaction must call this function before. 
TC = cra_cfg('get','javaTC')+1;
cra_cfg('set','javaTC',TC);
str = sprintf('println(''%%TRANSACTION %d\\n'');',TC); 
java_writeLine(str);
