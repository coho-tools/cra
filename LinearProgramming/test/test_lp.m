function test_lp
	test_create;
	test_opt;
	test_proj;
  test_op;
	test_other;

function test_opt
	disp('Test LP solver');
	bbox = [0,1;0,1];
	lp = lp_createByBox(bbox);
	if(cra_info('has_cplex'))
	  disp('cplex solver');
	  [v,x,flag] = lp_opt(lp,[1,0,-1,0;0,1,0,-1],'cplex');
	  disp('cplexjava solver');
	  [v,x,flag] = lp_opt(lp,[1,0,-1,0;0,1,0,-1],'cplexjava');
  end
	disp('matlab solver');
	[v,x,flag] = lp_opt(lp,[1,0,-1,0;0,1,0,-1],'matlab');	
	disp('matlabjava solver');
	[v,x,flag] = lp_opt(lp,[1,0,-1,0;0,1,0,-1],'matlabjava');	
	disp('java solver');
	[v,x,flag] = lp_opt(lp,[1,0,-1,0;0,1,0,-1],'java');	

function test_proj
	disp('Test projection solvers');
	p = poly_rand(10);
	lp = lp_createByHull(p);
	opt.angles = atan2(lp.A(:,2),lp.A(:,1));
	disp('using java projection solver');
	p1 = lp_project(lp,[1,2],0,[],'java');
	disp('using matlab projection solver');
	p2 = lp_project(lp,[1,2],0.1,opt,'matlab');
	figure; hold on;
	poly_display(p,'k');
	poly_display(p1,'r');
	poly_display(p2,'g');

function test_create
	disp('Test lp create functions');
	bbox = repmat([0,1],5,1);
	lp = lp_createByBox(bbox);
	hull = [0;0];
	lp = lp_createByHull(hull);
	hull = [0,1;0,1];
	lp = lp_createByHull(hull);
	lp = lp_rand(2,[4,2],'lp');
	lp = lp_rand(5,[4,2],'coho');
	assert(lp_iscoho(lp));

function test_op
	lp1 = lp_rand(2,[4,2]);
	lp2 = lp_rand(2,[4,2]);
	disp('Test lp_and function');
	lp = lp_and(lp1,lp2);
	disp('Test lp_norm function');
	lp = lp_norm(lp);
	disp('Test lp_unique function');
	lp = lp_unique(lp);
	disp('Test lp_operate function');
	lp = lp_operate(lp,'bAdd',1);
	lp = lp_operate(lp,'bMult',10);
	lp = lp_operate(lp,'AAdd',10);
	lp = lp_operate(lp,'ALeftMult',rand(8,8));
	lp = lp_operate(lp,'ARightMult',rand(2,2));
	lp = lp_operate(lp,'beqAdd',1);
	lp = lp_operate(lp,'beqMult',10);
	lp = lp_operate(lp,'AeqAdd',10);
	lp = lp_operate(lp,'AeqLeftMult',rand(4,4));
	lp = lp_operate(lp,'AeqRightMult',rand(2,2));
	disp('Test lp_bloat function');
	lp = lp_bloat(lp,1);

function test_other
	disp('Test lp_get functions');
	A = rand(4,2); b = rand(4,1);
	Aeq = rand(2,2); beq = rand(2,1);
	lp = lp_create(A,b,Aeq,beq);
	lp = lp_convert(lp);
	lp_get(lp,'A');
	lp_get(lp,'b');
	lp_get(lp,'Aeq');
	lp_get(lp,'beq');
	lp_get(lp,'isnorm');
	lp_get(lp,'dim');
	lp_get(lp,'ineqs');
	lp_get(lp,'eqs');
