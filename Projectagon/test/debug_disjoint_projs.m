function test_forward
	% a 3d exampels
	test_3vdp;


% 3D Van der Pol oscillator
function test_3vdp
	% init ph
	dim = 3; planes = [1,2;1,3]; 
	bbox = [1.0,1.2;-0.05,0.05;0.9,1.1];
	ph = ph_createByBox(dim,planes,bbox);
	initPhs{1} = ph_convert(ph,'concave');
	initPhs{2} = ph_convert(ph,'convex');
	initPhs{3} = ph;

	% set functions
	cra_cfg('set','modelFunc',@vdp_model);

	% set solver
	cra_cfg('set','lpSolver','java');	
	cra_cfg('set','projSolver','java');	

	% methods of ph_advance	
	OBJ{1} = {'face-bloat','face-none','face-height','face-all'};
	OBJ{2} = {'face-bloat','face-none','face-height','face-all','ph'};
	OBJ{3} = {'face-bloat','face-none','face-height','face-all','ph'};
	models = {'guess-verify','timeStep','bloatAmt'};

	% set opt
	opt = ph_getOpt; 
	opt = ph_setOpt(opt,'maxBloat',0.1);
	opt = ph_setOpt(opt,'timeStep',0.05); 
	opt = ph_setOpt(opt,'riters',0); 
	maxTs = [7,7,2]; 

	% compute reachability region
	%for p = 1:length(initPhs)
	for p = 1; 
		initPh = initPhs{p}; objects = OBJ{p}; maxT = maxTs(p);
		% try all possible combinations
		%for i=1:length(objects)
		for i=1; 
			opt = ph_setOpt(opt,'object',objects{i});
			%for j=1:length(models)
			for j=1; 
				disp('------------------------');
				fprintf('Using projectagon %d, object %d, model %d\n',p,i,j);
				opt = ph_setOpt(opt,'model',models{j});
				opt = ph_setOpt(opt,'prevBloatAmt',[],'prevTimeStep',[]);
				ph = initPh;
				fig(1) = figure; fig(2) = figure;
				ph_display(ph,fig,[],[],'r');
				phs = cell(0,1); t = 0;
				for k=1:Inf 
					fprintf('Working on the %dth step at time %f\n',k,t); 
					[fwdPh,ph,opt] = ph_advanceSafe(ph,opt); 
					%[fwdPh,ph,opt] = ph_advance(ph,opt); 
				  %if(k==3) error('debug'); end;
					t = t+ph.fwd.timeStep;
					phs{k} = ph; 
					ph = fwdPh;	
					ph_display(ph,fig,[],[],'b'); 
					if(t>=maxT),break;end
				end 
				id = ['3vdp_',num2str(p),'_',num2str(i),'_',num2str(j)];
				save(id,'phs','opt');
				print(fig(1),'-depsc2',[id,'_1']);
				print(fig(2),'-depsc2',[id,'_2']);
			end
		end
	end
%end


function ldi = vdp_model(lp)
% xdot = -y-x^3+x
% ydot = x-y^3+y
% zdot = 2x^2-2x
	x=1;y=2;z=3;
	bbox = lp_box(lp);
	avgs = mean(bbox,2);  extras = diff(bbox,[],2)/2;
	A = [1-3*avgs(x)^2, -1, 			0;... 
   		 1, 			1-3*avgs(y)^2, 	0;...
		 4*avgs(x), 	0,				-2];
%	b = [2*avgs(x)^3-3/2*avgs(x)*extras(x)^2;...
%		 2*avgs(y)^3-3/2*avgs(y)*extras(y)^2;...
%		 -2*avgs(x)^2+extras(x)^2];
%	u = [extras(x)^3+3/2*abs(avgs(x))*extras(x)^2;...
%		 extras(y)^3+3/2*abs(avgs(y))*extras(y)^2;...
%		 extras(x)^2];
% This doesn't converge well. And has the problem that projs doens't intersect. 
% Need to debug.
	b = 2*[avgs(x)^2; avgs(y)^2; -avgs(x)^2];
	u = [extras(x)^2*(extras(1)+3*abs(avgs(x))); ...
		 extras(y)^2*(extras(2)+3*abs(avgs(y))); ...
		 2*extras(x)^2];
	ldi{1} = int_create(A,b,u); 
