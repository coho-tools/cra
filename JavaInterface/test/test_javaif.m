function test_javaif
  disp('****Testing JavaInterface package.****'); 
	disp('**Test Java linear programming solver.**');
	test_lp
	disp('**Test Java polygon solver.**');
	test_poly;
	disp('**Restart Java when error encountered'); 
	test_err

function test_err
	disp('Send invalid inputs to the java program');
	p1 = [0,1;0,0];
	p2 = [0,1,1,0;1,1,2,2];
	try
	p = java_polyUnion({p1,p2});
	catch 
		disp('Find an error'); % restart java
		disp('Restart java solver');
		java_close;
		java_open;
	end
	disp('Send valid inputs to the new java thread');
	p1 = [0,1,1,0;0,0,1,1];
	p2 = [0,1,1,0;1,1,2,2];
	p = java_polyUnion({p1,p2});

function test_lp
	disp('1. Try to solve a very simple LP');
	f = ones(2,1);
	A = [eye(2);-eye(2)]; b = [ones(2,1);zeros(2,1)];
	[v,x,status] = java_lpSolve(f, A, b);
	if(x~=0) 
		error('The result from Java LP solver is incorrect'); 
	end
  
  disp('2. Try to solver a very simple projection problem');	
	lp.A = A; lp.b = b;
	lp.Aeq = zeros(0,2);
	lp.beq = zeros(0,1);
	x = [1;0]; y = [0;1];
	hull = java_lpProject(lp, x, y,1e-3);
	if(~all(all(hull==[0,1,1,0;0,0,1,1]))) 
		error('The result from Java projection solver is incorrect'); 
	end
%end
	
	
function test_poly
	% 1. union
	disp('1. Test java_polyUnion');
	% normal 
	p1 = [0,1,1,0;0,0,1,1];
	p2 = [0,1,1,0;1,1,2,2];
	p = java_polyUnion({p1,p2});
	% input is invalid
	p1 = [0,1,1,0;0,0,1,1];
	p2 = p1(:,1:2);
	%p = java_polyUnion({p1,p2});
	% input is disjoint, return the left-most
	p1 = [0,1,1,0;0,0,1,1];
	p2 = p1+2;
	p = java_polyUnion({p1,p2});
	% connected by a point, result is not simple polygon, error.
	p1 = [0,1,1,0;0,0,1,1];
	p2 = p1+1;
	% p = java_polyUnion({p1,p2});
	% connect by the left-lower point
	p1 = [0,1,1,0;0,0,1,1];
	p2 = [0,1,1;0,-1,-2];
	p = java_polyUnion({p1,p2});
	
	% 2. intersection 
	disp('2. Test java_polyIntersect');
	% normal 
	p1 = [0,1,1,0;0,0,1,1];
	p2 = p1+0.5;
	p = java_polyIntersect(p1,p2);
	% null
	p1 =  [0,1,1,0;0,0,1,1];
	p2 = p1+10;
	p = java_polyIntersect(p1,p2);
	% a point
	p1 =  [0,1,1,0;0,0,1,1];
	p2 = p1+1;
	p = java_polyIntersect(p1,p2);
	% a segment
	p1 =  [0,1,1,0;0,0,1,1];
	p2 = p1; p2(1,:) = p2(1,:)+1;
	p = java_polyIntersect(p1,p2);
	% a poly with segment
	p1 = [0,1,1,2,1,0; 0,0,1,1,2,1];
	p2 = [1,2,2,1,0,1; 0,0,1,2,1,1];
	p = java_polyIntersect(p1,p2);
	% a non-simple polygon
	p1 = [0,1,1,3,3,4,4,0; -1, -1, 0 ,0, -1, -1, 1, 1];
	p2 = [p1(1,:);-p1(2,:)];
	p2 = p2(:,end:-1:1);
	p = java_polyIntersect(p1,p2);
	
	% 3. reduce
	disp('3. Test java_polySimplify');
	eps = 1e-6; tol = 0.02;
	% reduce a point
	p1 = [0,1,1-eps,1,0;0,0,1-eps,1,1];
	p = java_polySimplify(p1,tol,true);
	% reduce an edge
	p1 = [0,1,1+eps,1+eps,1,0;0,0,0.5-eps,0.5+eps,1,1];
	p = java_polySimplify(p1,tol,true); % reduce edge
	p = java_polySimplify(p1,eps,false); % not reduce edge
	% result is non-simple, 
	% incorrect example, do not reduce an edge if any vertex is concave
	% need an example later
	p1 = [0,1,1,0.8,0.8,0.5,0.5,0;
		  0,0,1,1-9*eps,1-10*eps,1-10*eps,1,1];
	p = java_polySimplify(p1,eps*0.5,true); % non-simple
	
	% 4. poly_contain
	disp('4. Test java_polyContain');
	p1 =  [0,1,1,0;0,0,1,1];
	p2 = p1*2;
	iscontain = java_polyContain(p2,p1);
	
	% 5. set, get configuration
	disp('5. Test java_getParams');
	v = java_getParams(1);
	disp(['The old value is ',num2str(v)]);
	disp('Set the parameter as 1'); 
	java_setParams(1,1.0);
	v = java_getParams(1);
	disp(['The new value is ',num2str(v)]);
%end
