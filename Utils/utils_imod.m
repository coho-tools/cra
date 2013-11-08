function index = utils_imod(i,n)
% index = utils_imod(i,n)
% This function is same with mod except it return n rather than 0. 
index = mod(i,n);
index = index+n*(index==0);
