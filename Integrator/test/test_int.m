function test_int
	addpath('../../'); % Matlab 
	coho_addpath('Integrator');
	coho_addpath('Polygon');
	%test_demo
	test_bug
	
function test_demo
	A = zeros(2); b = ones(2,1); u = zeros(2,1);
	A = rand(2,2); b = rand(2,1); u = rand(2,1);
	ldi = int_create(A,b,u);
	hull = [0,1,1,0;0,0,1,1];
	lp = lp_createByHull(hull);
	t =1;
	fwdLP = int_forward(lp,ldi,t);
	fwdHull = lp_project(fwdLP,[1;2]);
	figure(1);clf;hold on;
	poly_display(hull);
	poly_display(fwdHull,'r');

% We found a bug that ints in int_forward is negative. 
% It is caused by the computation of (e^(-At0)-e^(-At1))*inv(A) 
% I incorrectly use 
%		v = (expm(-A*t0)-expm(-A*t1))/inv(A);
% The correct one is
%		v = (expm(-A*t0)-expm(-A*t1))/A;
function test_bug
	close all;
	load bug1.mat;
	ldinull = ldi; ldinull.u = zeros(2,1);
	fwdLP1 = int_forward(lp,ldi,t);
	fwdLP2 = int_forward(lp,ldi,t,'under');
	fwdLP3 = int_forward(lp,ldinull,t);
	%error('here')
	hull = lp_project(lp,[1,2]);
	hull1 = lp_project(fwdLP1,[1,2]);
	hull2 = lp_project(fwdLP2,[1,2]);
	hull3 = lp_project(fwdLP3,[1,2]);
	figure; hold on;
	poly_display(hull,'k');
	poly_display(hull1,'r');
	poly_display(hull2,'g');
	poly_display(hull3,'c');
	poly_area(hull)/poly_area(hull2)
	poly_area(hull)/poly_area(hull3)

