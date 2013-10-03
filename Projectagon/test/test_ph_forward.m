function test_ph_forward
  disp('**** 2D Sink example ****');
	% a 2d exampels
	test_sink;
  disp('**** 3D VDP example  ****');
	% a 3d exampels
	%test_3vdp;

% 2D sink example.
% This example shows how to use projectagon package
function test_sink
	% Step 1: Create initial ph
	%   We support three types of projectagon: non-convex, convex, bbox
	dim = 2; planes = [1,2]; 
	polys = {[0.1, 0.35, 0.15, 0.1; 0.1, 0.1, 0.15, 0.35]};
	hulls = { [ 0.1, 0.35, 0.1; 0.1, 0.1, 0.35 ] };
	initPhs{1}= ph_create(dim,planes,hulls,polys,0,true);  % non-convex 
	hulls = { [ 0.1, 0.3, 0.4, 0.1; 0.1, 0.1, 0.4, 0.3 ] };
	initPhs{2}= ph_create(dim,planes,hulls,hulls,1,true); % convex
	bbox = [0.1,0.3;0.1,0.3];
	initPhs{3}= ph_createByBox(dim,planes,bbox); % bbox 

	% Step 2: Tell CRA the dynamic system. 
	%   Given a LP, the function should returns a LDI model. 
	cra_cfg('set','modelFunc',@sink_model);
	
	% Step 3: Configurate CRA, this is optional. 
	cra_cfg('set','projSolver','java');	
	cra_cfg('set','lpSolver','java');

	% Step 4: Set advance options, this is optional. 
	%  a. get the default options
	opt = ph_getOpt;  
	%  b. set max bloat amount and time step. 
	opt = ph_setOpt(opt,'maxBloat',0.1);
	opt = ph_setOpt(opt,'timeStep',0.05); % determinated from maxBloat
	%  c. What object to move forward 
	OBJ{1} = {'face-bloat','face-height','face-none','face-all'};
	OBJ{2} = {'face-bloat','face-height','face-none','face-all','ph'};
	OBJ{3} = {'face-bloat','face-height','face-none','face-all','ph'};
	%  d. How to calculate time step and bloat amount.
	models = {'guess-verify','bloatAmt','timeStep'};


	% Step 5: The computation termination condition
	maxTs = [10,10,2]; 

	disp('Perform the reachability computations with different configurations:'); 
	disp('Try with different projectagon types: non-convex, convex and bbox.'); 
	disp('Try with different advance face: face-bloat,face-height,face-none,face-all, ph'); 
	disp('Try with different modeling methods: guess-verify, bloatAmt, timeStep'); 
	disp('The computation will take long time');

	% compute reachability region with different options
	for p = 1:length(initPhs)
		initPh = initPhs{p}; objects = OBJ{p}; maxT = maxTs(p);
		% try all possible combinations
		for i=1:length(objects)
			opt = ph_setOpt(opt,'object',objects{i});
			for j=1:length(models)
				disp('------------------------');
				fprintf('Using projectagon %d, object %d, model %d\n',p,i,j);
				opt = ph_setOpt(opt,'model',models{j});
				opt = ph_setOpt(opt,'prevBloatAmt',[],'prevTimeStep',[]);
				ph = initPh;
				fig = figure; ph_display(ph,fig,[],[],'r');
				phs = cell(0,1); t = 0; time = cputime;
				for k=1:Inf 
					fprintf('Working on the %dth step at time %f\n',k,t); 
					[fwdPh,ph,opt] = ph_advanceSafe(ph,opt); 
					t = t+ph.fwd.timeStep;
					phs{k} = ph; 
					ph = fwdPh;	
					ph_display(ph,fig,[],[],'b'); 
					if(t>=maxT),break;end
				end 
				id = ['sink_',num2str(p),'_',num2str(i),'_',num2str(j)];
				time = cputime-time;
				save(id,'phs','opt','time');
				print(fig,'-depsc2',id);
			end
		end
	end
%end

function ldi = sink_model(lp)
	% This function compute the LDI model for sink example
	% xdot = m*x
	m = [-2,-3;3,-2];
	A = m; b = zeros(2,1);
	u = ones(2,1)*1e-6;
	%u = zeros(2,1);
	ldi{1} = int_create(A,b,u);

% 3D Van der Pol oscillator
% This example is 3-dim, very similar with above. 
function test_3vdp
	% init ph
	dim = 3; planes = [1,2;1,3]; 
	bbox = [1.0,1.2;-0.05,0.05;0.9,1.1];
	ph = ph_createByBox(dim,planes,bbox);
	initPhs{1} = ph_convert(ph,'non-convex');
	initPhs{2} = ph_convert(ph,'convex');
	initPhs{3} = ph;

	% set functions
	cra_cfg('set','modelFunc',@vdp_model_min); 
%	cra_cfg('set','modelFunc',@vdp_model_jac); % larger error term

	% set solver
	cra_cfg('set','lpSolver','java');	
	cra_cfg('set','projSolver','java');	

	% set opt
	opt = ph_getOpt; 
	opt = ph_setOpt(opt,'maxBloat',0.1);
	opt = ph_setOpt(opt,'timeStep',0.05); 
	% methods of ph_advance	
	OBJ{1} = {'face-bloat','face-height','face-none','face-all'};
	OBJ{2} = {'face-bloat','face-height','face-none','face-all','ph'};
	OBJ{3} = {'face-bloat','face-height','face-none','face-all','ph'};
	models = {'guess-verify','bloatAmt','timeStep'};
  % max computation time
	maxTs = [7,7,2];

	disp('Perform the reachability computations with different configurations:'); 
	disp('Try with different projectagon types: non-convex, convex and bbox.'); 
	disp('Try with different advance face: face-bloat,face-height,face-none,face-all, ph'); 
	disp('Try with different modeling methods: guess-verify, bloatAmt, timeStep'); 
	disp('The computation will take long time');

	% compute reachability region
	%for p = 1:length(initPhs)
	for p = 1; 
		initPh = initPhs{p}; objects = OBJ{p}; maxT = maxTs(p);
		% try all possible combinations
		for i=1:length(objects)
			opt = ph_setOpt(opt,'object',objects{i});
			for j=1:length(models)
				disp('------------------------');
				fprintf('Using projectagon %d, object %d, model %d\n',p,i,j);
				opt = ph_setOpt(opt,'timeStep',0.05); % reset as may change below
				opt = ph_setOpt(opt,'model',models{j});
				opt = ph_setOpt(opt,'prevBloatAmt',[],'prevTimeStep',[]);
				ph = initPh; phs = cell(0,1); t = 0; time = cputime;
				for k=1:200 % max 200 steps (may diverge)
					fprintf('Working on the %dth step at time %f\n',k,t); 
					[fwdPh,ph,opt] = ph_advanceSafe(ph,opt); 
					if(j==3 && ph.fwd.timeStep < opt.timeStep)
						disp('The timeStep is too huge, change to smaller ones');
						opt = ph_setOpt(opt,'timeStep',0.9*opt.timeStep);
					end
					t = t+ph.fwd.timeStep;
					phs{k} = ph; 
					ph = fwdPh;	
					if(t>=maxT),break;end
				end 
				time = cputime - time;
				id = ['3vdp_',num2str(p),'_',num2str(i),'_',num2str(j)];
				save(id,'phs','opt','time');
				fig(1) = figure; fig(2) = figure;
				phs_display(phs,fig);
				print(fig(1),'-depsc2',[id,'_1']);
				print(fig(2),'-depsc2',[id,'_2']);
			end
		end
	end
%end


% xdot = -y-x^3+x
% ydot = x-y^3+y
% zdot = 2x^2-2z
% This is my LDI model based on Jacob matrix, in my master thesis.
function ldi = vdp_model_jac(lp)
	x=1;y=2;z=3;
	bbox = lp_box(lp);
	avgs = mean(bbox,2);  extras = diff(bbox,[],2)/2;
	A = [1-3*avgs(x)^2, -1, 			      0;... 
   		 1, 			      1-3*avgs(y)^2, 	0;...
		   4*avgs(x), 	  0,				      -2];
	% NOTE: an error in master thesis for b term. avgs(x)/avgs(y) should be ^3, not ^2
	b = 2*[avgs(x)^3; avgs(y)^3; -avgs(x)^2];
	u = [extras(x)^2*(extras(x)+3*abs(avgs(x))); ...
		   extras(y)^2*(extras(y)+3*abs(avgs(y))); ...
		   2*extras(x)^2];
	ldi{1} = int_create(A,b,u); 

% This is Ian's model which has smaller error. 
% The basic idea is to add some constant term to b term 
% such that the error term is smaller. 
function ldi = vdp_model_min(lp)
	x=1;y=2;z=3;
	bbox = lp_box(lp);
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
	ldi{1} = int_create(A,b,u); 
