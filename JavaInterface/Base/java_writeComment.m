function java_writeComment(str)
% java_writeLabel
% This function begin a new transaction to java
% each transaction must call this function before. 
str = ['%% ',str];
java_writeLine(str);
