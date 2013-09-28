function [ind,vind] = utils_combineInd(nvars,nc,lo2hi)
% [ind,vind] = utils_combineInd(nvars,nc,lo2hi)
% Given nvars variables, each has nc possible values, in the form of (nvars x nc) matrix
% the function computes the indices for all combinations. 
%	nvars: 	the number of variables
%	nc: 	the number of values of each variable
% 	lo2hi: 	low to high order if true.
%	ind:	dx(nc^nvars) matrix, each column is the indices of one combination. 
%	vind:	dx(nc^nvars) matirx, vind(i,j) is which value is used for the 
% 			ith variable of the jth combinations. 
if(nargin<3||isempty(lo2hi))
	lo2hi = true;
end
np = nc^nvars;
if(lo2hi)
	vind = mod(floor(repmat(0:(np-1),nvars,1)./repmat(nc.^((nvars-1):-1:0)',1,np)),nc)+1;
else
	vind = mod(floor(repmat(0:(np-1),nvars,1)./repmat(nc.^(0:+1:(nvars-1))',1,np)),nc)+1;
end
ind = sub2ind([nvars,nc],repmat((1:nvars)',1,np),vind);
