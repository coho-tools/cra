function isc = int_contain(ldi1, ldi2, bbox) 
% Check if ldi2 contains ldi2 in the range of [L,H]
%   If ldi1.u < ldi2.u => false
%     Proof:
%      Choose any x satisfying L <= x <= H.
%        If ((A2-A1)*x + (b2-b1))_i >= 0, then
%          ((A2-A1)*x + (b2-b1) + (u2 - u1))_i > 0,  by the assumption that u1_i < u2_i
%          (A1*x + b1 + u1)_i < (A2*x + b2 + u2)_i,  a little bit of algebra
%        Otherwise,
%          ((A2-A1)*x + (b2-b1))_i < 0,
%          and we conclude
%          (A1*x + b1 - u1)_i > (A2*x + b2 - u2)_i
%          by analagous reasoning.
%   Otherwise, 
%      ForAll x s.t. L <= x <= H.
%      (A1-A2)*x + (b1-b2) - (u1-u2) <= 0
%    ^ (A1-A2)*x + (b1-b2) + (u1-u2) >= 0
%   Optimization problems
  L = bbox(:,1); H = bbox(:,2);
  A12 = ldi1.A - ldi2.A; b12 = ldi1.b - ldi2.b; u12 = ldi1.u -ldi2.u;
  if(u12<0)
    isc = false;
  elseif(u12==0)
    isc = (all(A12(:)==0) & all(b12==0));
  else
    % find the min and max value of A12*x
    Ap = A12; An = A12;
    Ap(Ap<0)=0; An(An>0)=0;
    minV = Ap*L+An*H
    maxV = Ap*H+An*L
    lo = maxV + b12 - u12
    hi = minV + b12 + u12
    isc = all(lo<=0)&&all(hi>=0);
  end
end
