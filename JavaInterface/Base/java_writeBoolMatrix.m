function java_writeBoolMatrix(Min, name)
% java_writeBoolMatrix(Min, name)
% This function convert Min into a boolean matrix and write to java

M = (Min ~= 0);
java_writeMatrix(M,name,'boolMatrix');
