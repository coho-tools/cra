function test_simu
  for i=1:1000
    y0 = 10*(rand(2,1)-0.5);
    [t,y] = ode45(@ex_2vdp_model, [0,8], y0);
    hold on; plot(y(:,1),y(:,2));
  end

% xdot = -y-x^3+x
% ydot = x-y^3+y
function Xdot = ex_2vdp_model(t,X) 
  x = X(1); y = X(2); 
  xdot = -y-x^3+x;
  ydot = x-y^3+y; 
  Xdot = [xdot;ydot];
  bbox = [X-0.2,X+0.2];
  ldi = ex_2vdp_model_ldi(bbox);
  Xdot = ldi.A*X+ldi.b;

function ldi = ex_2vdp_model_ldi(bbox) 
	x=1;y=2;
	avgs = mean(bbox,2);  extras = diff(bbox,[],2)/2;
	A = [1-3*avgs(x)^2, -1            ;...
   		 1, 			      1-3*avgs(y)^2 ];
	b = [2*avgs(x)^3-3/2*avgs(x)*extras(x)^2;...
		   2*avgs(y)^3-3/2*avgs(y)*extras(y)^2]; 
	u = [extras(x)^3+3/2*abs(avgs(x))*extras(x)^2;...
		   extras(y)^3+3/2*abs(avgs(y))*extras(y)^2]; 
	ldi = int_create(A,b,u); 

