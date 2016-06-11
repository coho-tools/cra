function fwdLP = int_forward(lp,ldi,t,method)
% fwdLP = int_forward(lp,ldi,t,method)
% Construct the forward time projection of lp after time t with dynamic integ 
%
% Given LP: Px <= q, with linear model xdot = Ax+b,
% The reachable space at time t is LP: 
% 	PEx <= q + P(I-E)A^(-1)b 
%   E = e^(-At)
% When the model has an uncertain input as:
% 	xdot = Ax+b+/-u 
% The new LP is :
% 	PEx <= q +  P(I-E)A^(-1)b + ints
% 	ints(i) = \int_0^{t} n'*e^(-As)u*(s) ds
%   u*(t) = sign(e^(-A't)*n)*u;
%   n = P(i,:)'
% 
% For advanced LP: P*Eo*x<= q, let PP = P*Eo
% The new LP is 
%   PPEx <= q + PP(I-E)A^(-1)b + ints(PP)
%   P*Eo*E*x <= q + PP(I-E)*A^(-1)*b + ints(PP)
%    


	if(nargin<4||isempty(method))
		method = 'default';
	end;
	
	P = lp.A;  q = lp.b;
	A = ldi.A; b = ldi.b; u = ldi.u;
	[nc,dim] = size(P);

  if(~isempty(lp.bwd))
    Eo = lp.bwd; invEo = lp.fwd;
    PP = P*Eo;
  else 
    Eo = eye(dim); invEo = eye(dim);
    PP = P;
  end
	
	% 1. compute E
	E = expm(-A*t); invE = expm(A*t);
	% 2. compute  (I-E)*A^{-1}
	mc = int_inv(A,0,t);
	% 3. compute ints 
	switch(lower(method)) 
		% assume the optimal point does not change,
		% most of times, the sign does not change.  
		case {'default'}  % use constant u, max of all possible vaules.
			ints = abs(PP*mc)*u; 
		case {'under'}  % under-approximation
			ints = abs(PP*mc*u); 
		case {'init','fix'} % use optimal point of at a particular time
			%s = sign(PP); 
			s = sign(expm(-A'*t/2)*PP')'; 
			ints = (PP*mc.*s*u); 
			ints = ints+10*eps; % for round-off error
			% NOTE, ints should always be positive. 
			% However, this method may give negative results. see test/bug1
			% The bug has been fixed.
			assert(all(ints>=0)); 
		case {'num','sample'} % sample N time points 
			N = 20; ts = linspace(0,t,N+1); 
			% compute integral
			int = 0;
			midT = (ts(1:end-1)+ts(2:end))/2; 
			for i=1:N
				s = sign(PP*expm(-A*midT(i))); 
				int = int+((PP*int_inv(A,ts(i),ts(i+1))).*s); 
			end 
			ints = int*u;
		case {'exact'} % "exact" solution 
			ints = zeros(nc,1); 
			for i=1:nc % for each face 
				n = PP(i,:)'; % normal of face 
	
				% find critical time, 5 order is enough 
				c = [(-A)^5*n/120,(-A)^4*n/24,(-A)^3*n/6,(-A)^2*n/2,(-A)*n,n]; 
				ts = zeros(0,1); 
				for j=1:dim 
					r = roots(c(j,:)); 
					r = r(imag(r)==0); 
					r = r(r>0 & r < t); 
					ts(end+1:end+length(r),1) = r; 
				end 
				ts = unique([0;ts;t]); 
	
				% compute integral, use middle to avoid error of roots 
				midT = (ts(1:end-1)+ts(2:end))/2; 
				int = 0; 
				for j=1:length(ts)-1 
					nt =expm(-A'*midT(j))*n; 
					int = int+ int_inv(A,ts(j),ts(j+1))*(sign(nt).*u); 
				end 
				ints(i,1) = n'*int; %q(i) = q(i)+n'*int; 
			end
		otherwise
			error('do not support');
	end
	
	q = q + PP*mc*b+ints;
	fwdLP = lp_create(P,q);
	fwdLP.bwd = Eo*E; fwdLP.fwd = invEo*invE;

%  global debug
%  d.A = A; 
%  d.t = t;
%  d.E = E;
%  d.invE = invE;
%  debug{end+1} = d;
end

function v = int_inv(A,t0,t1)
% Compute (e^(-At0)-e^(-At1))*inv(A) 
	if(rcond(A)>1e-10) % well-condition
		%v = (expm(-A*t0)-expm(-A*t1))*inv(A);
		v = (expm(-A*t0)-expm(-A*t1))/A;
	else % singular
		% reduce computation error;
		p = nextpow2(t1); c = 2^p;
		A = -A*c; t0 = t0/c; t1 = t1/c;
	
		% default 5 order	
		v = A^0*(t1-t0) + A^1*(t1^2-t0^2)/2 + A^2*(t1^3-t0^3)/6 + A^3*(t1^4-t0^4)/24 +...
		   	A^4*(t1^5-t0^5)/120 + A^5*(t1^6-t0^6)/720; 
		% use more
		i = 6; nA = norm(A);
		while(i<100 && nA^i/factorial(i+1) > 1e-15 ) % eigenvalue is less than any norm
			v = v + A^i*(t1^(i+1)-t0^(i+1))/factorial(i+1);
			i = i+1;
			v = v + A^i*(t1^(i+1)-t0^(i+1))/factorial(i+1);
			i = i+1;
		end;

		v = v*c;
	end
end
