function val = lp_get(lp, what)
% val = lp_get(lp, what)
% The function gets a value from an LP object
%
% Input: 
% 	lp is a linear constraint structure
% 		A * x <= b
%  		Aeq * x = beq
% 		isnorm is boolean
% 	what: a string chosen from
% 		'dim'   : the dimension (number of columns of A and Aeq)
% 		'ineqs' : the number of inequality constraints
% 		'eqs'   : the number of equality constraints
% 		'A','b','Aeq','beq','isnorm': the corresponding fields of lp	
% Output:
% 	val is the value you wanted, which may be a vector or matrix
if(lp_isempty(lp)), val=[]; return; end
switch lower(what)
	case 'dim'
		val = size(lp.A,2);  
	case 'ineqs'
		val = length(lp.b);
	case 'eqs'
		val = length(lp.beq);
    case 'a'
        val = lp.A;
    case 'aeq'
        val = lp.Aeq;
    case 'b'
        val = lp.b;
    case 'beq'
        val = lp.beq;
    case 'isnorm'
        val = lp.isnorm;
    otherwise
        error([ what 'is not supported by lp_get' ]);
end
