function test_ph
  disp('**** Test Basic Projectagon Operations ****');
	disp('** Test creating projectagons **');
	test_create
	disp('** Test projectagons operations **');
	test_op;
	disp('** Test displaying projectagons **');
	test_display
	disp('** Test making projectagons canonical **');
	test_canon
	disp('** Test measure projectagons volumn **');
	test_volumn;

function test_volumn
	disp('  Create a random 3D projectagon');
	dim = 3; planes = [1,2;1,3];
	ph = ph_rand(dim,planes,0);
	N = 100;
	vs = zeros(N,1);
	disp('  Measure the volumn for 100 times');
	for i=1:N
		vs(i) = ph_volumn(ph,10^3);
	end
	disp('  Plot the results:');
	figure;
	plot(sort(vs));

function test_op
	dim = 3; 
	planes = [1,2;1,3]; 
	N = 1000; pts = rand(dim,N)*2-1;
	line.Aeq = [1,0,0]; line.beq = 0; % x=0
	type = [0,1,2]; canon = true;
	%type = 2;

	for t = type 
		disp(sprintf('Testing type %d:',t));
		figs = [2*t+1,2*t+2];

		% generate random projectagons
		disp('  Generate two random projectagons');
		ph1 = ph_rand(dim,planes,t,canon);
		ph2 = ph_rand(dim,planes,t,canon);

		disp('  Display the projectagons');
		ph_display(ph1,figs,'poly',[],'k-*'); 
		ph_display(ph2,figs,'poly',[],'k:o');

		% check ph_contain ph_containPts
		disp('  Check if ph1 contains ph2 by ph_contain');
		isc = ph_contain(ph1,ph2);

		disp('  Check if ph1 contains points by ph_containPts');
		isc = ph_containPts(ph1,pts);

		% check ph_intersect
		disp('  Compute the intersection of ph1 and ph2 by ph_intersect');
		phi = ph_intersect({ph1,ph2});
	  ph_display(phi,figs,'poly',[],'r');

		disp('  Compute the intersection of ph1 and a line by ph_intersectLine');
		phl = ph_intersectLine(ph1,line); % ph_intersectLP
	  ph_display(phl,figs,'poly',[],'g');

		% check ph_union
		disp('  Compute the union of ph1 and ph2 by ph_union');
		phu = ph_union({ph1,ph2});
	  ph_display(phu,figs,'poly',[],'b');

		% check ph_regu ph_simplify 
		disp('  Check ph_regu');
		phr = ph_regu(ph1);
	  ph_display(phr,figs,'poly',[],'c-d');

		disp('  Simplify the projectagon by ph_simplify');
		phs = ph_simplify(ph1);
	  ph_display(phs,figs,'poly',[],'m-h');

		disp('  Project projectagons onto 2d planes by ph_project');
		[polys,hulls] = ph_project(ph1,[2,3]);

		if(t==2) 
			disp('  Check ph_minkSum');
			phm = ph_minkSum(ph1,ph2); % only for bbox
			ph_display(phm,figs,'poly',[],'y-*');
		end
	end

function test_display
	dim = 3;
	planes = [1,2;1,3];
	disp('  Generate a random 3D projectagon by ph_rand.'); 
	ph = ph_rand(dim,planes,0);
	disp('  Display the projectagon by ph_display3d.');
	opt.showPrism = 0;
	ph_display3d(ph,opt);

% test ph_canon function
function test_canon
	% #of iterations and Performance 
	% reduce=true, tol=0.02, engine=java, iter=1.83, t = 21.74; (60%)
	% reduce=true, tol=0.02, engine=matlab, iter=3.71, t = 36.34;
	% reduce=false, tol=0.02, engine=java, iter=1.88, t = 16.07 (60%)
	% reduce=false, tol=0.02, engine=matlab, iter=4.09, t = 25.26

	% generate a random projectagon
	disp('  Make a projectagon canonical by ph_canon'); 
	disp('  Generate ten 3D random projectaogns'); 
	disp('  Compute canonical projectagon using Java and SAGA polygon solvers'); 
	dim = 3;
	planes = [1,2;1,3];
	c1 = 0; c2 = 0; t1 = 0; t2= 0;
	for i=1:10
		ph = ph_rand(dim,planes,0);
		% use the java method
		opt = struct('iters',10,'eps',1e-3,'tol',0.01); 
		cra_cfg('set','polySolver','java'); 
		t = cputime;
		[cph,iter] = ph_canon(ph,[],opt); % iter 1.85 on average
		t1 = t1+(cputime-t);
		c1 = c1+iter;
		% use the SAGA method
		% this often generates non-simple polygons
		cra_cfg('set','polySolver','saga'); 
		t = cputime;
		[cph,iter] = ph_canon(ph,[],opt);
		t2 = t2+(cputime-t);
		c2 = c2+iter;
	end
	disp('  Show the results:'); 
	fprintf('    The number of iterations of Java method is %f\n',c1/10);
	fprintf('    The number of iterations of SAGA method is %f\n',c2/10);
	fprintf('    The running time of iterations of Java method is %f\n',t1);
	fprintf('    The running time of iterations of SAGA method is %f\n',t2);

% test create a projectagon
function test_create
	% test ph_create;
	dim = 3;
	planes =[1,2;1,3];
	hulls{1} = [0,1,1,0;0,0,1,1];
	hulls{2} = [0,1,1,0;0,0,1,1];
	polys = hulls;

	disp('  Create a projectagon by projected polygons (ph_create).');
	ph = ph_create(dim,planes,hulls,[]);
	ph = ph_create(dim,planes,hulls,polys);

	disp('  Create a projectagon by LP (ph_createByLP).');
	ph = ph_createByLP(dim,planes,ph.hullLP);

	disp('  Create a projectagon by bounding box (ph_createByBox).');
	ph = ph_createByBox(dim,planes,[0,1;0,1;0,1]);

	% test ph_get
	disp('  Get projectagon structure infor by ph_get.');
	dim = ph_get(ph,'dim');
	planes = ph_get(ph,'planes');
	type = ph_get(ph,'type');
	iscanon = ph_get(ph,'iscanon');
	hulls = ph_get(ph,'hulls');
	polys = ph_get(ph,'polys');

	% test ph_convert
	disp('  Convert projectagon types by ph_convert.'); 
	disp('  Types: 1 for non-convex, 2 for convex, 3 for hyper-rectangle.'); 
	ph = ph_rand(dim,planes,0);
	ph_display(ph,1:2,'poly',[],'k');
	ph = ph_convert(ph,1);
	ph_display(ph,1:2,'poly',[],'r');
	ph = ph_convert(ph,2);
	ph_display(ph,1:2,'poly',[],'g');
