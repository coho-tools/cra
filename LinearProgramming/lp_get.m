function val = lp_get(lp, what)
% val = lp_get(lp, what)
% The function gets a value from an LP object
%
% Input: 
% 	lp is a linear constraint structure
% 		A * x <= b
% 	what: a string chosen from
% 		'dim'   : the dimension (number of columns of A) 
% 		'ineqs' : the number of inequality constraints
% 		'A','b','fwd','bwd': the corresponding fields of lp	
% Output:
% 	val is the value you wanted, which may be a vector or matrix
if(lp_isempty(lp)), val=[]; return; end
switch lower(what)
	case 'dim'
		val = size(lp.A,2);  
	case 'ineqs'
		val = length(lp.b);
  case 'a'
    val = lp.A;
  case 'b'
    val = lp.b;
  otherwise
    error([ what 'is not supported by lp_get' ]);
end
