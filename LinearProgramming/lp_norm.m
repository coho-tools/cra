function lp = lp_norm(lp)
% lp = lp_norm(lp)
%	Scale the rows of lp.A (and elements of lp.b) so that each row
%	has an L2 norm of 1.  
%	If a row of lp.A is all zeros,  error is reported.

if(lp_isempty(lp)),return; end
if(isfield(lp,'isnorm') && lp.isnorm)
	return;
end

% Don't know how to handle it of bwd is not empty
assert(isempty(lp.bwd));

[A,b] = do_norm(lp.A,lp.b);
lp = lp_create(A,b);

function [A,b] = do_norm(A,b)
	n = sqrt(sum(A.*A,2));
	if(all(n>0))
		A = A./repmat(n,1,size(A,2));
		b = b./n;
	else
		error('The norm is zero');
		% replace that row by zero if required 
	end;
%end

