function iss = lp_sat(lp,pts)
% iss = lp_sat(lp,pts)
% The function checks if points satisfy a lp. 
% Inputs
% 	lp: a linear program
%	pts: a matrix, with each column as a point
% 		lp and pts must have the same number of dimension. 
% Output
% 	iss: a row vectors
[d,n] = size(pts);
if(isempty(lp)),iss = false(1,n); return; end
if(d~=size(lp.A,2))
	error('lp and points must have the same dimensions');
end

iss = true(1,n);
if(~isempty(lp.A))
	iss = iss & all(lp.A*pts <= repmat(lp.b,1,n),1);
end
if(~isempty(lp.Aeq))
	iss = iss & all(abs(lp.Aeq*pts-repmat(lp.beq,1,n))<eps,1);
end

