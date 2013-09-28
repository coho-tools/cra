function test_ph
  disp('**** Test Basic Projectagon Operations ****');
	disp('** Test creating projectagons **');
	test_create
	disp('** Test displaying projectagons **');
	test_display
	disp('** Test making projectagons canonical **');
	test_canon
	disp('** Test projectagons operations **');
	test_op;
	disp('** Test measure projectagons volumn **');
	test_volumn;

function test_volumn
	dim = 3; planes = [1,2;1,3];
	ph = ph_rand(dim,planes,0);
	N = 100;
	vs = zeros(N,1);
	for i=1:N
		vs(i) = ph_volumn(ph,10^3);
	end
	plot(sort(vs));

function test_op
	dim = 3; 
	planes = [1,2;1,3]; 
	N = 1000; pts = rand(dim,N)*2-1;
	line.Aeq = [1,0,0]; line.beq = 0; % x=0
	type = [0,1,2]; canon = true;
	type = 2;
	for t = type 
		figs = [2*t+1,2*t+2];
		% generate random projectagons
		ph1 = ph_rand(dim,planes,t,canon);
		ph2 = ph_rand(dim,planes,t,canon);
		if(true)
			ph_display(ph1,figs,'poly',[],'k-*'); 
			ph_display(ph2,figs,'poly',[],'k:o');
		end
		% check ph_contain ph_containPts
		isc = ph_contain(ph1,ph2);
		if(false && isc)
			ph_display(ph2,figs,'both',[],'k');
		end
		isc = ph_containPts(ph1,pts);
		if(false) % plot points
			for i=1:2
				plane = planes(i,:);
				pt = pts(plane,:);
				figure(figs(i)); hold on;
				plot(pt(1,~isc),pt(2,~isc),'g+');
				plot(pt(1,isc),pt(2,isc),'ro');
			end
		end
		% check ph_intersect
		phi = ph_intersect({ph1,ph2});
		phl = ph_intersectLine(ph1,line); % ph_intersectLP
		if(true)
			ph_display(phi,figs,'poly',[],'r');
			ph_display(phl,figs,'poly',[],'g');
		end
		% check ph_union
		phu = ph_union({ph1,ph2});
		if(true)
			ph_display(phu,figs,'poly',[],'b');
		end
		% check ph_regu ph_simplify 
		phr = ph_regu(ph1);
		if(false)
			ph_display(phr,figs,'poly',[],'c-d');
		end
		phs = ph_simplify(ph1);
		if(false)
			ph_display(phs,figs,'poly',[],'m-h');
		end
		[polys,hulls] = ph_project(ph1,[2,3]);
		if(false)
			figure(10+t);
			poly_display(polys{1});
		end
		if(t==2) 
			phm = ph_minkSum(ph1,ph2); % only for bbox
			if(false)
				ph_display(phm,figs,'poly',[],'y-*');
			end
		end
	end

function test_display
	dim = 3;
	planes = [1,2;1,3];
	ph = ph_rand(dim,planes,0);
	ph_display3d(ph);

% test ph_canon function
function test_canon
	% #of iterations and Performance 
	% reduce=true, tol=0.02, engine=java, iter=1.83, t = 21.74; (60%)
	% reduce=true, tol=0.02, engine=matlab, iter=3.71, t = 36.34;
	% reduce=false, tol=0.02, engine=java, iter=1.88, t = 16.07 (60%)
	% reduce=false, tol=0.02, engine=matlab, iter=4.09, t = 25.26
	% generate a random projectagon
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
	fprintf('The number of iterations of Java method is %f\n',c1/100);
	fprintf('The number of iterations of SAGA method is %f\n',c2/100);
	fprintf('The running time of iterations of Java method is %f\n',t1);
	fprintf('The running time of iterations of SAGA method is %f\n',t2);

% test create a projectagon
function test_create
	dim = 3;
	planes =[1,2;1,3];
	hulls{1} = [0,1,1,0;0,0,1,1];
	hulls{2} = [0,1,1,0;0,0,1,1];
	polys = hulls;
	% test ph_create;
	ph = ph_create(dim,planes,hulls,[]);
	ph = ph_create(dim,planes,hulls,polys);
	ph = ph_createByLP(dim,planes,ph.hullLP);
	ph = ph_createByBox(dim,planes,[0,1;0,1;0,1]);
	%ph_display(ph,1:2,'both');
	% test ph_get
	dim = ph_get(ph,'dim');
	planes = ph_get(ph,'planes');
	type = ph_get(ph,'type');
	iscanon = ph_get(ph,'iscanon');
	hulls = ph_get(ph,'hulls');
	polys = ph_get(ph,'polys');
	% test ph_convert
	ph = ph_rand(dim,planes,0);
	ph_display(ph,1:2,'poly',[],'k');
	ph = ph_convert(ph,1);
	ph_display(ph,1:2,'poly',[],'r');
	ph = ph_convert(ph,2);
	ph_display(ph,1:2,'poly',[],'g');
