function phs=test2
bbox = [1.0,1.2;-0.05,0.05]; 
ph = ph_createByBox(bbox); 

opt.tend = 1; 
% For debug purpose, force chaning LDI model for each step
opt.tstepL = 0.01;
opt.tstepS = 0.01;
opt.maxu = 0.2; 
% CRA's behavior, projecting each time
%phs = ph_advance(ph,@(bbox)(ex_2vdp_model(bbox)), opt,1);
%for i=1:length(phs)
%  polys{i} = phs{i}.hulls{1};
%end
%polys_display(polys,'g');
%
global debug 
debug = {};
global model 
model = {};
% New behavior, no projecting
phs = ph_advance(ph,@(bbox)(ex_2vdp_model(bbox)), opt,0);
for i=1:length(phs)
  polys{i} = phs{i}.hulls{1};
end
polys_display(polys,'r');


% xdot = -y-x^3+x
% ydot = x-y^3+y
function ldi = ex_2vdp_model(bbox) 
	x=1;y=2;
	avgs = mean(bbox,2);  extras = diff(bbox,[],2)/2;
	A = [1-3*avgs(x)^2, -1            ;...
   		 1, 			      1-3*avgs(y)^2 ];
	b = [2*avgs(x)^3-3/2*avgs(x)*extras(x)^2;...
		   2*avgs(y)^3-3/2*avgs(y)*extras(y)^2]; 
	u = [extras(x)^3+3/2*abs(avgs(x))*extras(x)^2;...
		   extras(y)^3+3/2*abs(avgs(y))*extras(y)^2]; 
  u = 0; 
	ldi = int_create(A,b,u); 
%  global model
%  m.bbox=bbox;
%  m.A = A;
%  model{end+1} = m;

