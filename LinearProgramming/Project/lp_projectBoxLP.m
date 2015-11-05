function hull = lp_projectBoxLP(lp,xx,yy) 
% This is for a special case of lp_project, where 
%   PEx<=q
% where Px<=q is a box LP
% 
%    PEx <= q -> (xx,yy) 
% ==>Px<=q -> (invE'xx,invE'(yy)) === (X,Y)
% solving
%    max (sin(theta)*X+cos(theta)Y)*x s.t. Px<=q == (L < x < H)
%    the optimial point p(theta) is 
%            p(i) = (cos(theta)*Xi + sin(theta)*Yi) >0 ? H : L
% so the critical point of theta is 
%    theta_i = atan(-Xi/Yi) + pi/0
% after finding all optimal points, the polygon is P
% convert back to (xx,yy) system, as inv(E)P

  n = size(lp.A,2);
  A = lp.A; b = lp.b; 
  if (~isempty(lp.bwd)) 
  	E = lp.bwd; invE = lp.fwd; 
  else 
    E = eye(n); invE = eye(n);
  end

  assert(all(all(A(1:n,:)==-eye(n)))); assert(all(all(A(n+1:end,:)==eye(n))));
  L = -b(1:n); H = b(n+1:end);

  % convert the project to project L<=x<=H onto (X,Y)
  X = invE'*xx; Y = invE'*yy; 
  X = X/norm(X); Y = Y-X'*Y*X; Y = Y/norm(Y);

  % For L<=x<=H, the optimal point for d = cos(theta)*X + sin(theta)*Y changes iff 
  % some elements of d changes its signs. So we find the critial value of theta_i such that d(i)=0   
  % This is the norm of the i-th projected polygon edges. Note the bbox is symmetric, so the theta value 
  % will change the sign back again for theta_i+pi. We sort all norms by its value of theta, then use 
  % dirction between them as the optimal direction to find all optimal points. This will produce the exact
  % polygon, not under-approximation
  % 
  % atan(1/0) or atan(-1/0) OK, atan(0/0) is NaN 
  theta = sort(atan(-X./Y));
  theta(isnan(theta))=[]; % remove NaN
  % theta in the range of [0,2*pi], while atan is in [-pi/2,pi/2].
  theta = [theta;theta+pi; theta(1)+2*pi];  % append the first normal to make a loop
  % all optimal directions as beteen two adjacent norms 
  opt_theta = (theta(1:end-1)+theta(2:end))/2;
  % all optimal points
  K = length(opt_theta); 
  P = zeros(n,K); 
  for i=1:K
    pos = (cos(opt_theta(i))*X+sin(opt_theta(i))*Y)>0;
    % for pos=0, don't care
    P(:,i) = L; P(pos,i) = H(pos);
  end

%  % when the opt direction is the norm, either point could be optimal, lead to under-approximation. 
%  % consider a 2d rectangle for example. 
%if 1  % for performance
%  LL = repmat(L,1,n); HH = repmat(H,1,n);
%  p = LL; pp = HH; 
%  THETA = repmat(reshape(theta,1,[]),n,1);
%  XX = repmat(X,1,n); YY = repmat(Y,1,n);
%  pos = (cos(THETA).*XX+sin(THETA).*YY)>0;
%  p(pos) = HH(pos); pp(pos) = LL(pos);
%else  % for understanding
%  p = zeros(n,n); pp = zeros(n,n);
%  for i=1:n
%    % NaN leads to more optimal points, but ok, removed by convex
%    %if(isNaN(theta)), break; end
%    pos = (cos(theta(i))*X+sin(theta(i))*Y)>0;
%    %pos(i) = 0;  % doesn't matter positive or negative
%    p(:,i) = L; p(pos,i) = H(pos);
%    pp(:,i) = L; pp(~pos,i) = H(~pos); % symmatric
%  end
%end
%  P = [p,pp];

  invP = invE*P;
  invP = [xx,yy]\invP;  % convert to 2D

if 1
  % do we still need it? 
  index = convhull(invP(1,:),invP(2,:));
  hull = invP(:,index(1:end-1));
else
  hull = invP;
end

  
