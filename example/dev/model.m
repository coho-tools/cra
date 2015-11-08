% xdot = -y-x^3+x
% ydot = x-y^3+y
% zdot = 2x^2-2z
function result = ex_3vdp_model(bbox,A,b) 
  compu = (nargin==3);
	x=1;y=2;z=3;
	avgs = mean(bbox,2);  extras = diff(bbox,[],2)/2;
	A = [1-3*avgs(x)^2, -1, 			      0;... 
   		 1, 			      1-3*avgs(y)^2, 	0;...
		   4*avgs(x), 	  0,				      -2];
	b = [2*avgs(x)^3-3/2*avgs(x)*extras(x)^2;...
		   2*avgs(y)^3-3/2*avgs(y)*extras(y)^2;...
		   -2*avgs(x)^2+extras(x)^2];
	u = [extras(x)^3+3/2*abs(avgs(x))*extras(x)^2;...
		   extras(y)^3+3/2*abs(avgs(y))*extras(y)^2;...
		   extras(x)^2];
	ldi = int_create(A,b,u); 
  if(compu)
    % ignore A/b, for test purpose
    result = 2*u;
  else
    result = ldi;
  end
