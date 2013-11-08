function test_poly
  disp('**** Testing Polygon package. ****'); 
  disp('** Testing poly_intersect function. **'); 
  test_intersect
  disp('** Testing poly_union function. **'); 
	test_union
  disp('** Testing poly_simplify function. **'); 
	test_simplify
  disp('** Testing poly_contain function. **'); 
	test_contain
  disp('** Testing poly_interval function. **'); 
	test_interval; % poly_interval
  disp('** Testing poly_orh function. **'); 
	test_orh % orh method
  disp('** Testing poly_split function. **'); 
	test_split
  disp('** Testing poly_polyline function. **'); 
	test_polyline;
  disp('** Testing poly_intersectLine function. **'); 
	test_intersectLine; % poly_intersectLine;
  disp('** Testing line_intersect function. **'); 
	test_line; % line_intersect
%end	

function test_split
	disp('Test the function poly_split to split a polygon into polylines');
	disp('Test random polygons with default mode'); 
	N = 30;
	p = poly_rand(N,false);
	[lpl,upl] = poly_split(p);
	figure; clf; hold on;
	poly_display(p); 
	plot(lpl(1,:),lpl(2,:),'r');
	plot(upl(1,:),upl(2,:),'g');
	
	disp('Test random polygons with mode 1 (diagonal)'); 
	p = [0,1,1,0;0,0,1,1];
	[lpl,upl] = poly_split(p,1);
	figure; clf; hold on;
	poly_display(p); 
	plot(lpl(1,:),lpl(2,:),'r');
	plot(upl(1,:),upl(2,:),'g');

	disp('Test random polygons with mode 2 (overlap)'); 
	[lpl,upl] = poly_split(p,2);
	figure; clf; hold on;
	poly_display(p); 
	plot(lpl(1,:),lpl(2,:),'r*');
	plot(upl(1,:),upl(2,:),'go');
	
	disp('Test random polygons with mode 3 (disjoint)'); 
	[lpl,upl] = poly_split(p,2);
	[lpl,upl] = poly_split(p,3);
	figure; clf; hold on;
	poly_display(p); 
	plot(lpl(1,:),lpl(2,:),'r');
	plot(upl(1,:),upl(2,:),'g');

function test_polyline
	disp('Test the function polyline_crop that crops a polyline given a range');
	x = sort(rand(1,10));
	y = rand(1,10);
	xbnd = [min(x),max(x)];
	xx = sort(rand(1,2))*diff(xbnd)+xbnd(1);
	pl = [x;y];
	pl = polyline_crop(pl,xx);
	plot(x,y,'o',pl(1,:),pl(2,:));


function test_interval
	disp('Test the interval closure method.'); 
	disp('The result must be checked manually.');
	for i=1:10
		figure; hold on;
		p = poly_rand(20,false);
		poly_display(p);
		bbox = sort(rand(2,2)*2-1,2);
		poly_display(poly_createByBox(bbox),'k');
		ps{i} = p; bboxs{i} = bbox;
		bbox1 = poly_interval(p,bbox);
		poly_display(poly_createByBox(bbox1),'r-o');
		if(~isempty(bbox1))
			assert(all(bbox1(:,1)>=bbox(:,1))&all(bbox1(:,2)<=bbox(:,2)));
		end
	end

function test_intersectLine % line_intersect 
	disp('Test function poly_intersectLine that computes the intersection of polygon and line'); 
	% convex bbox
	bbox = [0,1;0,1];
	p = poly_createByBox(bbox);

	disp('A polygon and an inside segment/line with two intersection points'); 
	% inside polygon
	% two intersections
	line = [0,1;0.5,0.5];
	points = poly_intersectLine(p,line,true);
	assert(all(all(points ==[0,1;0.5,0.5])));
	points = poly_intersectLine(p,line,false);
	assert(all(all(points==line)));

	disp('A polygon and an inside segment/line with one intersection point'); 
	% one intersection
	line = [0.1,1;0.5,0.5];
	points = poly_intersectLine(p,line,true);
	assert(all(all(points ==[0,1;0.5,0.5])));
	points = poly_intersectLine(p,line,false);
	assert(all(all(points==line(:,[2,1]))));

	disp('A polygon and an inside segment/line with no intersection point'); 
	% no intersection
	line = [0.1,0.9;0.5,0.5];
	points = poly_intersectLine(p,line,true);
	assert(all(all(points ==[0,1;0.5,0.5])));
	points = poly_intersectLine(p,line,false);
	assert(all(all(points==line)));

	disp('A polygon and polygon edge with two intersection points'); 
	% edge of polygon
	% two intersections
	line = [0,1;1,1];
	points = poly_intersectLine(p,line,true);
	assert(all(all(points ==[0,1;1,1])));
	points = poly_intersectLine(p,line,false);
	assert(all(all(points==line)));

	disp('A polygon and polygon edge with one intersection points'); 
	% one intersection
	line = [0.1,1;1,1];
	points = poly_intersectLine(p,line,true);
	assert(all(all(points ==[0,1;1,1])));
	points = poly_intersectLine(p,line,false);
	assert(all(all(points==line(:,[2,1]))));

	disp('A polygon and polygon edge with no intersection points'); 
	% no intersection
	line = [0.1,0.9;1,1];
	points = poly_intersectLine(p,line,true);
	assert(all(all(points ==[0,1;1,1])));
	points = poly_intersectLine(p,line,false);
	assert(all(all(points==line)));

	disp('A case where the intersection is empty'); 
	% empty
	line = [0,1;2,2];
	points = poly_intersectLine(p,line,true);
	assert(isempty(points));
	points = poly_intersectLine(p,line,false);
	assert(isempty(points));

	disp('Test random polygons, must check manully'); 
	for i=1:10
		p = poly_rand(20,false);
		line = [0,1;0.5,0.5];
		figure; hold on;
		poly_display(p,'b');
		poly_display(line,'k');
		points = poly_intersectLine(p,line,true);
		poly_display(points,'g*');
		points = poly_intersectLine(p,line,false);
		poly_display(points,'ro');
		assert(~isempty(points));
	end

function test_line % line_intersect
	disp('Test the function line_intersect that computes the intersections of two lines/segments');

	disp('Test a simple case');
	% normal
	x1 = [0;1]; y1=[0;0];
	x2 = [0.5;0.5]; y2=[-1;1];
	[x,y] = line_intersect(x1,y1,x2,y2);
	assert(x==0.5&y==0);
	[x,y] = line_intersect(x1,y1,x2,y2,false);
	assert(x==0.5&y==0);

	disp('Test a case that line 1 touches line 2');
	% touch 
	x1 = [0;1]; y1=[0;0];
	x2 = [1;1]; y2=[0;1];
	[x,y] = line_intersect(x1,y1,x2,y2);
	assert(x==1&y==0);
	[x,y] = line_intersect(x1,y1,x2,y2,false);
	assert(x==1&y==0);

	disp('Test a case that seg 1 does not intersect seg 2'); 
	% no intersection for segments 
	x1 = [0;1]; y1=[0;0];
	x2 = [2;2]; y2=[-1;1];
	[x,y] = line_intersect(x1,y1,x2,y2);
	assert(x==2&y==0);
	[x,y] = line_intersect(x1,y1,x2,y2,false);
	assert(isnan(x)&isnan(y));

	disp('Test parallel lines'); 
	% parallel lines
	x1 = [0;1]; y1=[0;0];
	x2 = [0;1]; y2=[1;1];
	[x,y] = line_intersect(x1,y1,x2,y2);
	assert(isnan(x)&isnan(y));
	[x,y] = line_intersect(x1,y1,x2,y2,false);
	assert(isnan(x)&isnan(y));

	disp('Test two disjoint segments on the same line');
	% same line, two segments
	x1 = [0;1]; y1=[0;0];
	x2 = [2;3]; y2=[0;0];
	[x,y] = line_intersect(x1,y1,x2,y2);
	assert(isinf(x)&isinf(y));
	[x,y] = line_intersect(x1,y1,x2,y2,false);
	assert(isnan(x)&isnan(y));

	disp('Test two touched segments on the same line');
	% same line, touched segments
	x1 = [0;1]; y1=[0;0];
	x2 = [1;2]; y2=[0;0];
	[x,y] = line_intersect(x1,y1,x2,y2);
	assert(isinf(x)&isinf(y));
	[x,y] = line_intersect(x1,y1,x2,y2,false);
	assert(x==1&&y==0);

	disp('Test two overlapped segments on the same line');
	% same line, overlapped segments
	x1 = [0;2]; y1=[0;0];
	x2 = [1;3]; y2=[0;0];
	[x,y] = line_intersect(x1,y1,x2,y2);
	assert(isinf(x)&isinf(y));
	[x,y] = line_intersect(x1,y1,x2,y2,false);
	assert(isinf(x)&&isinf(y));

	disp('Test two identical segments'); 
	% same segment 
	x1 = [0;1]; y1=[0;0];
	x2 = [0;1]; y2=[0;0];
	[x,y] = line_intersect(x1,y1,x2,y2);
	assert(isinf(x)&isinf(y));
	[x,y] = line_intersect(x1,y1,x2,y2,false);
	assert(isinf(x)&isinf(y));

	disp('Test a line and a point with no intersection'); 
	% point and a line, no intersection
	x1 = [0;0]; y1=[0;0];
	x2 = [0;1]; y2=[1;1];
	[x,y] = line_intersect(x1,y1,x2,y2);
	assert(isnan(x)&isnan(y));
	[x,y] = line_intersect(x1,y1,x2,y2,false);
	assert(isnan(x)&isnan(y));

	disp('Test a line and a point that on the line'); 
	% point on a line
	x1 = [0;0]; y1=[0;0];
	x2 = [1;2]; y2=[0;0];
	[x,y] = line_intersect(x1,y1,x2,y2);
	assert(x==0&y==0);
	[x,y] = line_intersect(x1,y1,x2,y2,false);
	assert(isnan(x)&isnan(y));

	disp('Test a segment and a point that on the segment'); 
	% point on a segment 
	x1 = [0;0]; y1=[0;0];
	x2 = [0;1]; y2=[0;0];
	[x,y] = line_intersect(x1,y1,x2,y2);
	assert(x==0&y==0);
	[x,y] = line_intersect(x1,y1,x2,y2,false);
	assert(x==0&y==0);

	disp('Test two points'); 
	% two points 
	x1 = [0;0]; y1=[0;0];
	x2 = [1;1]; y2=[0;0];
	[x,y] = line_intersect(x1,y1,x2,y2);
	assert(isnan(x)&isnan(y));
	[x,y] = line_intersect(x1,y1,x2,y2,false);
	assert(isnan(x)&isnan(y));

	disp('Test two identical points'); 
	% same points 
	x1 = [0;0]; y1=[0;0];
	x2 = [0;0]; y2=[0;0];
	[x,y] = line_intersect(x1,y1,x2,y2);
	assert(x==0&y==0);
	[x,y] = line_intersect(x1,y1,x2,y2,false);
	assert(x==0&y==0);

	disp('Test disjoint, touched, overlapped, and same line/segments'); 
	% disjoint, touched, overlapped, same
	x1 = [0,0,0,0;0,1,2,1]; y1 = [1,0,0,0;0,0,0,0]; 
	x2 = [2,1,1,0;3,2,3,1]; y2 = [0,0,0,0;0,0,0,0];
	[x,y] = line_intersect(x1,y1,x2,y2);
	assert(x(1)==0&&all(isinf(x(2:4))));
	assert(y(1)==0&&all(isinf(y(2:4))));
	[x,y] = line_intersect(x1,y1,x2,y2,false);
	assert(isnan(x(1))&&x(2)==1&&all(isinf(x(3:4))))
	assert(isnan(y(1))&&y(2)==0&&all(isinf(y(3:4))))

	disp('Test random lines'); 
	% test vector
	x1 = rand(2,100); y1 = rand(2,100);
	x2 = rand(2,100); y2 = rand(2,100);
	[x,y] = line_intersect(x1,y1,x2,y2);
	[x,y] = line_intersect(x1,y1,x2,y2,false);


function test_simplify
	% 3. reduce
	eps = 1e-6; tol = 0.02;
	% reduce a point
	disp('Reduce a point');
	p1 = [0,1,1-eps,1,0;0,0,1-eps,1,1];
	p = poly_simplify(p1);

	disp('Reduce an edge');
	% reduce an edge
	p1 = [0,1,1+eps,1+eps,1,0;0,0,0.5-eps,0.5+eps,1,1];
	p = poly_simplify(p1,tol,true); % reduce edge
	p = poly_simplify(p1,eps,false); % not reduce edge

	disp('Try another example where edge is not reduced'); 
	p1 = [0,1,1,0.8,0.8,0.5,0.5,0;
		  0,0,1,1-9*eps,1-10*eps,1-10*eps,1,1];
	p = poly_simplify(p1,eps*0.5,true); % non-simple

function test_union
	disp('First try a simiple case with both Java and SAGA solvers');
	% 1. union
	% normal 
	p1 = [0,1,1,0;0,0,1,1];
	p2 = [0,1,1,0;1,1,2,2];
	r1 = poly_union({p1,p2},'java');
	r2 = poly_union({p1,p2},'saga');
	assert(abs(poly_area(r1)-poly_area(r2))<1e-15);

	disp('Try two disjoint polygons'); 
	disp('Java solver returns the left-most polygon'); 
	disp('SAGA solver returns the convex hull of two polygons');
	% disjoint, return the left-most
	p1 = [0,1,1,0;0,0,1,1];
	p2 = p1+2;
	r1 = poly_union({p1,p2},'java');% incorrect
	r2 = poly_union({p1,p2},'saga');% convex hull
	%assert(abs(poly_area(r1)-poly_area(r2))<1e-15);

	disp('Two polygons connected by a point'); disp('Java solver throws exception as infinit-loop found'); disp('SAGA solver returns the left-most polygon');
	% connected by a point
	p1 = [0,1,1,0;0,0,1,1];
	p2 = p1+1;
	%r1 = poly_union({p1,p2},'java'); % infinite loop
	r2 = poly_union({p1,p2},'saga'); % incorrect, part
	%assert(abs(poly_area(r1)-poly_area(r2))<1e-15);

	disp('Two polygons connected by the left-lower point'); 
	disp('Both solver returns the convex hull');
	% connect by the left-lower point
	p1 = [0,1,1,0;0,0,1,1];
	p2 = [0,1,1;0,-2,-1];
	r1 = poly_union({p1,p2},'java'); % a part -> convex hull 
	r2 = poly_union({p1,p2},'saga'); % contain NaN -> convex hull 
	assert(abs(poly_area(r1)-poly_area(r2))<1e-15);
	
	disp('Union of a set of polygons'); 
	% a set of polygons
	p1 = [0,1,1,0;0,0,1,1]; 
	p2 = p1; p2(1,:) = p2(1,:)+1;
	p3 = p1+1;
	p4 = p1; p4(2,:) = p4(2,:)+1;
	r1 = poly_union({p1,p2,p3,p4},'java');
	r2 = poly_union({p1,p2,p3,p4},'saga');
	assert(abs(poly_area(r1)-poly_area(r2))<1e-15);

	disp('Try with random polygons. The result must be checked manually.'); 
	disp('NOTE: the result may not be simple polygon thus break the Java thread');
	% random
	for i=1:10
  	p1 = poly_rand(10,false); % may be non-simple
  	p2 = poly_rand(10,false);
  	r1 = poly_union({p1,p2},'java');
  	r2 = poly_union({p1,p2},'saga');
  	figure(i);  hold on;
  	poly_display(p1,'k');
  	poly_display(p2,'b');
  	poly_display(r1,'r');
  	poly_display(r2,'g');
  	assert(abs(poly_area(r1)-poly_area(r2))<1e-12);
	end

function test_intersect
	disp('First try a simiple case with both Java and SAGA solver');
	% normal 
	p1 = [0,1,1,0;0,0,1,1];
	p2 = p1+0.5;
	r1 = poly_intersect({p1,p2},'java');
	r2 = poly_intersect({p1,p2},'saga');
	assert(all(all(r1==r2)));

	disp('Try a simple case where the intersection is empty'); 
	% null
	p1 =  [0,1,1,0;0,0,1,1];
	p2 = p1+10;
	r1 = poly_intersect({p1,p2},'java'); % []
	r2 = poly_intersect({p1,p2},'saga'); % []
	assert(all(all(r1==r2)));

	disp('Try a simple case where the intersection is a point'); 
	disp('Java solver returns empty'); 
	disp('SAGA solver bloats the point into a small rectangle'); 
	% a point
	p1 =  [0,1,1,0;0,0,1,1];
	p2 = p1+1;
	r1 = poly_intersect({p1,p2},'java'); % []
	r2 = poly_intersect({p1,p2},'saga'); % bloated point
	assert(abs(poly_area(r1)-poly_area(r2))<=2e-12);
	%assert(all(all(r1==r2)));

	disp('Try a simple case where the intersection is a segment'); 
	disp('Java solver returns empty'); 
	disp('SAGA solver bloats the segment into a thin rectangle'); 
	% a segment
	p1 =  [0,1,1,0;0,0,1,1];
	p2 = p1; p2(1,:) = p2(1,:)+1;
	r1 = poly_intersect({p1,p2},'java'); % []
	r2 = poly_intersect({p1,p2},'saga'); % bloated segment
	assert(abs(poly_area(r1)-poly_area(r2))<=1e-6);
	%assert(all(all(r1==r2)));

	disp('Try a simple case where the intersection is a polygon with a segment'); 
	disp('Both solvers returns the polygon');
	% a poly with segment
	p1 = [0,1,1,2,1,0; 0,0,1,1,2,1];
	p2 = [1,2,2,1,0,1; 0,0,1,2,1,1];
	r1 = poly_intersect({p1,p2},'java');
	r2 = poly_intersect({p1,p2},'saga');
	assert(abs(poly_area(r1)-poly_area(r2))<=1e-15);
	%assert(all(all(r1==r2)));

	disp('Try a simple case where the intersection is a non-simple polygon'); 
	disp('Java solver returns a part of the polygon as a simple polygon');
	disp('SAGA solver returns the convex hull'); 
	% a non-simple polygon
	p1 = [0,1,1,3,3,4,4,0; -1, -1, 0 ,0, -1, -1, 1, 1];
	p2 = [p1(1,:);-p1(2,:)];
	p2 = p2(:,end:-1:1);
	r1 = poly_intersect({p1,p2},'java'); % a part
	r2 = poly_intersect({p1,p2},'saga'); % convex hull
	%assert(abs(poly_area(r1)-poly_area(r2))<=1e-15);
	%assert(all(all(r1==r2)));

	disp('Try with random polygons. The result must be checked manually.'); 
	disp('NOTE: the result may not be simple polygon thus break the Java thread');
	for i=1:10
  	p1 = poly_rand(10,false);
  	p2 = poly_rand(10,false);
  	r1 = poly_intersect({p1,p2},'java');
  	r2 = poly_intersect({p1,p2},'saga');
  	figure; hold on;
  	poly_display(p1,'k');
  	poly_display(p2,'k');
  	poly_display(r1,'r');
  	poly_display(r2,'g');
  	assert(abs(poly_area(r1)-poly_area(r2))<1e-12);
	end
	

function test_contain
	disp('First try a simple case');
	p = [0,1,1,0;0,0,1,1];
	pts = [0,-1e-6,-1e-12,-1e-15,1e-6,1e-12,1e-15,1e-6,1e-12,1e-15,0.5;
		   0,-1e-6,-1e-12,-1e-15,1e-6,1e-12,1e-15,-1e-6,-1e-12,-1e-15,1];
	isc = poly_containPts(p,pts);
	assert(all(isc==[1,0,0,0,1,1,1,0,0,0,1]));

	disp('Test points on polygon edges');
	pp = [0,0.5,1,1,0;0,0.5,0,1,1];
	pts = [0,0.2,1,1,0;0,0.2,0,1,1];
	isc = poly_containPts(pp,pts);
	assert(all(isc)); 

	disp('Test polygon with its vertices');
	isc = poly_contain(p,p);
	assert(isc==true);

	disp('Test polygon with its vertices using intersect method');
	isc = poly_contain(p,p,'intersect');
	assert(isc==true);

	disp('Test a case where points and intersect methods return different result');
	disp('All points are contained in the polygon');
	pp = [0,0.5,1,1,0;-0.1,0.5,-0.1,1,1];
	isc = poly_contain(pp,p,'points'); 
	assert(isc==true); 
	disp('But the polygon is not contained by the first polygon');
	isc = poly_contain(pp,p,'intersect');
	assert(isc==false);

function test_orh
	disp('Test the poly_orh and poly_regu function');

	disp('The input polygon can not be empty');
	% empty 
	p = [];
	%hull = poly_orh(p);
	%hull = poly_regu(p);

	disp('Test a point');
	% a point
	p = [0;0];
	hull = poly_orh(p);
	if(~all(hull(:)==0));
		error('Unexpected result');
	end
	hull = poly_regu(p);

  disp('Test a segement');	
	% a segment
	p = [0,1;0,1];
	hull = poly_orh(p);
	hull = poly_regu(p);

  disp('Test a thin polygon');	
	% a thin polygon
	figure; hold on;
	p = [0,1,1,0;0,0,1e-16,1e-16];
	poly_display(p,'k');
	hull = poly_orh(p);
	poly_display(hull,'b');
	hull = poly_regu(p);
	poly_display(hull,'r');
	
  disp('Test a rectangle');	
	% a rectangle
	figure; hold on;
	p = [0,1,1,0;0,0,1,1];
	poly_display(p,'k');
	hull = poly_orh(p);
	poly_display(hull,'b');
	hull = poly_regu(p);
	poly_display(hull,'r');

	
  disp('Test a polygon with repeated points');	
	% repeate point
	figure; hold on;
	p = [0,1,1;0,0,0];
	hull = poly_orh(p);
	hull = poly_regu(p);
	p = [0,1,1,0,0;0,0,1,1,1];
	hull = poly_orh(p);
	poly_display(hull,'b');
	hull = poly_regu(p);
	poly_display(hull,'r');

  disp('Test a random polygon'); 
	% rand
	figure; hold on;
	p = poly_rand(20,false);
	poly_display(p,'k');
	hull = poly_orh(p);
	poly_display(hull,'b');
	hull = poly_regu(p);
	poly_display(hull,'r');
