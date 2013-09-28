function lp = lp_operate(lp, operation, operand)
% lp = lp_operate(lp, operation, operand)
% performs a matrix operation on a set of linear constraints  
%
% Input:
% 	lp is a linear constraint structure
%   operation is a string chosen from
% 		'bAdd'      : lp.b = lp.b + operand
%     	'bMult'     : lp.b = operand * lp.b
%     	'ALeftMult' : lp.A = operand * lp.A
%     	'ARightMult': lp.A = lp.A * operand
%     	'AAdd'      : lp.A = lp.A + operand
%   operation can also be the same operators for beq and Aeq respectively
%
% Output: the modified lp
%
% NOTE: that this operation is assumed to destroy any normalization present 
% in the linear constraints
if(lp_isempty(lp)),return;end
switch lower(operation)
  case 'aleftmult'
    lp.A = operand * lp.A;
  case 'arightmult'
    lp.A = lp.A * operand;
  case 'aadd'
    lp.A = lp.A + operand;

  case 'badd'
    lp.b = lp.b + operand;
  case 'bmult'
    lp.b = operand * lp.b;

  case 'aeqleftmult'
    lp.Aeq = operand * lp.Aeq;
  case 'aeqrightmult'
    lp.Aeq = lp.Aeq * operand;
  case 'aeqadd'
    lp.Aeq = lp.Aeq + operand;

  case 'beqadd'
    lp.beq = lp.beq + operand;
  case 'beqmult'
    lp.beq = operand * lp.beq; 

  otherwise
    error([ operation ' is not an understood operation' ]);
end
lp.isnorm = false;
