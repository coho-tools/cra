function ASSERT(cond)
% This function is used when 'assert' function is not avaliable in some Matlab versions.
if(~cond)
	error('assumption incorrect');
end
