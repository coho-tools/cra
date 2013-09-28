function ldi = int_create(A,b,u)
% ldi = int_create(A,b,u)
% creates an integrator for linear models with error
%       xdot(t) = Ax + b +/- u
% Intput:
% 	A: non-singular matrix
% 	b: vector
% 	u: non-negative vector

n = size(A,1);
if(numel(b)==1)
	b = repmat(b,n,1);
end
if(numel(u)==1)
	u = repmat(u,n,1);
end
if(~all(u>=0))
	error('Error turn u must be non-negative');
end
% assert(size(A,1)==n);
% assert(size(b)==[n,1]);
% assert(size(u)==[n,1]);
ldi = struct('A',A,'b',b,'u',u);
