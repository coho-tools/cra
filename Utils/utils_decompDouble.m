function [int,dec]= utils_decompDouble(v)
% [int,dec]= utils_decompDouble(m)
% This function finds the integral part and decimal part of a double value.
% 	v: nxm double matrix 
% 	int: nxm integer matrix, the integral part
% 	dec: nxm integer matrix, the decimal part
[n,m] = size(v); 
int = repmat(int32(0),n,m);
dec = repmat(int32(0),n,m);
for i=1:n
	for j=1:m
		s = num2str(v(i,j));
		% find the position of decimal point	
		ind = find(ismember(s,'.'));
		if(~isempty(ind))
			ip = s(1:ind-1); dp = s(ind+1:end);
			int(i,j) = int32(eval(ip)); 
			dec(i,j) = int32(eval(dp));
		else
			int(i,j) = int32(v(i,j));
			dec(i,j) = 0;
		end
	end
end
