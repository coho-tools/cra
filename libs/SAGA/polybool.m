function [xo,yo,ind] = polybool(x1,y1,x2,y2,flag)
% POLYBOOL Boolean operations on polygons.
%	[XO,YO] = POLYBOOL(X1,Y1,X2,Y2,FLAG)
%	calulates results of Boolean operations on
%	a pair of polygons.
%	FLAG Specifies the type of the operation:
%	1 - Intersection (P1 & P2)
%	2 - Union (P1 | P2)
%	3 - Difference (P1 & ~P2)

%  Copyright (c) 1995 by Kirill K. Pankratov,
%       kirill@plume.mit.edu.
%       06/25/95, 09/07/95 

%	This program calls the following functions:
%	AREA, ISINTPL, ISCROSS, INTSECL.

% Algorithm:
%  1. Check boundary contour directions (area).
%     For intersection and union make all
%     counter-clockwise. For difference make the second
%    contour clock-wise.
%  2. Calculate matrix of intersections (function ISINTPL).
%     Quick exit if no intersections.
%  3. For intersecting segments calculate intersection
%     coordinates (function INTSECL).
%  4. Sort intersections along both contours.
%  5. Calculate sign of cross-product between intersectiong
%     segments. This will give which contour goes "in" and
%     "out" at intersections.
%	
%  6. Start with first intersection:
%     Determine direction to go ("in" for intersection,
%     "out" for union).
%     Move until next intersection, switch polygons at each
%     intersection until coming to the initial point.
%     If not all intersections are encountered, the 
%     resulting polygon is disjoint. Separate output 
%     coordinates by NaN and repeat procedure until all 
%     intersections are counted.

 % Default for flag
flag_dflt = 1; % 1- intersec., 2-union, 3 - diff.

 % Handle input
if nargin==0, help polybool, return, end
if nargin < 4
  error(' Not enough input arguments')
end
if nargin<5, flag = flag_dflt; end

x1 = x1(:); y1 = y1(:);
x2 = x2(:); y2 = y2(:);
l1 = length(x1);
l2 = length(x2);

 % Check areas and reverse if negative
nn1 = area(x1,y1);
if nn1<0, x1 = flipud(x1); y1 = flipud(y1); end
nn2 = area(x2,y2);
if ((nn2<0 && flag<3) || (nn2>0 && flag==3))
  x2 = flipud(x2); y2 = flipud(y2); 
end

 % If both polygons are identical ........
if l1==l2
  if(all(x1==x2) && all(y1==y2))
    if(flag<3), xo = x1; yo = y1; ind = 1:l1; 
    else xo = []; yo = []; ind = []; end
    return
  end
end

 % Calculate matrix of intersections .....
[is,C] = isintpl(x1,y1,x2,y2);
is = any(any(C));

 % Quick exit if no intersections ........
if ~is
  if flag==1       % Intersection
    xo=[]; yo = [];
  elseif flag==2   % Union
    xo = [x1; nan; x2];
    yo = [y1; nan; y2];
  elseif flag==3   % Difference
    xo = x1; yo = y1;
  end
  return
end

 % Mark intersections with unique numbers
i1 = find(C);
ni = length(i1);
C(i1) = 1:ni;

 % Close polygon contours
x1 = [x1; x1(1)]; y1 = [y1; y1(1)];
x2 = [x2; x2(1)]; y2 = [y2; y2(1)];
l1 = length(x1);  l2 = length(x2);

 % Calculate intersections themselves
[i1,i2,id] = find(C);
xs1 = [x1(i1) x1(i1+1)]'; ys1 = [y1(i1) y1(i1+1)]';
xs2 = [x2(i2) x2(i2+1)]'; ys2 = [y2(i2) y2(i2+1)]';

 % Call INTSECL ............................
[xint,yint] = intsecl(xs1,ys1,xs2,ys2);

 % For sements belonging to the same line
 % find interval of intersection ...........
ii = find(xint==inf);
%if ii~=[]
if(~isempty(ii))
  [is,inx] = interval(xs1(:,ii),xs2(:,ii));
  [is,iny] = interval(ys1(:,ii),ys2(:,ii));
  xint(ii) = mean(inx);
  yint(ii) = mean(iny);
end

 % Coordinate differences of intersecting segments
xs1 = diff(xs1); ys1 = diff(ys1);
xs2 = diff(xs2); ys2 = diff(ys2);

 % Calculate cross-products
cp = xs1.*ys2-xs2.*ys1;
cp = cp>0;
if flag==2, cp=~cp; end % Reverse if union
cp = double(cp); % by chaoyan
cp(ii) = 2*ones(size(ii));

 % Sort intersections along the contours
ind = (xint-x1(i1)').^2+(yint-y1(i1)').^2;
ind = ind./(xs1.^2+ys1.^2);
cnd = min(ind(ind>0));
ind = ind+i1'+i2'/(ni+1)*cnd*0;
[xo,ii] = sort(ind);
xs1 = id(ii);
[xo,ind] = sort(xs1);
ind = rem(ind,ni)+1;
xs1 = xs1(ind);

ind = (xint-x2(i2)').^2+(yint-y2(i2)').^2;
ind = ind./(xs2.^2+ys2.^2);
cnd = min(ind(ind>0));
[xo,ii] = sort(i2'+ind+i1'/(ni+1)*cnd*0);
xs2 = id(ii);
[xo,ind] = sort(xs2);
ind = rem(ind,ni)+1;
xs2 = xs2(ind);

 % Combine coordinates in one vector
x1 = [x1; x2]; y1 = [y1; y2];

 % Find max. possible length of a chain
xo = find(any(C'));
xo = diff([xo xo(1)+l1]);
mlen(1) = max(xo);
xo = find(any(C));
xo = diff([xo xo(1)+l2]);
mlen(2) = max(xo);

 % Check if multiple intersections in one segment
xo = diff([i1 i2]);
is_1 = ~all(all(xo));

 % Begin counting intersections *********************

 % Initialization ..................
int = zeros(size(xint));
nn = 1;   % First intersection
nn1 = i1(nn); nn2 = i2(nn);
b = cp(nn);
is2 = b==2;
xo = []; yo = []; ind = [];
closed = 0;

 % Proceed until all intersections are counted 
while ~closed  % begin counting `````````````````````0

  % If contour closes, find new starting point
  if(int(nn) && ~all(int))
    ii = find(int);
    C(id(ii)) = zeros(size(ii));
    nn = find(~int,1,'first');  % Next intersection
    nn1 = i1(nn);
    nn2 = i2(nn);
    xo = [xo; nan];        % Separate by NaN 
    yo = [yo; nan];
    ind = [ind; nan];
    % Choose direction ......
    b = cp(nn);
  end

  % Add current intersection ......
  xo = [xo; xint(nn)];
  yo = [yo; yint(nn)];
  ind = [ind; 0];
  int(nn) = 1;
  closed = all(int);

  % Find next segment
  % Indices for next intersection
  if(~b), nn = xs1(nn);
  else  nn = xs2(nn);
  end
  if(~b), pt0 = nn1; else  pt0 = nn2; end

  nn1 = i1(nn);
  nn2 = i2(nn);

  if(b), pt = nn2; else pt = nn1; end

  if(b), pt0 = pt0+l1; pt = pt+l1; end
  ii = (pt0+1:pt);


  % Go through the beginning ..............
  cnd = pt<pt0 | (pt==pt0 & is_1 & flag>1);
  if cnd
    if(~b),  ii = [pt0+1:l1 1:pt];
    else    ii = [pt0+1:l1+l2 l1+1:pt];
    end
  end
  len = length(ii);
  cnd = b & len>mlen(2);
  cnd = cnd | (~b & len>mlen(1));
  if(is2 || cnd), ii=[]; end


  % Add new segment
  xo = [xo; x1(ii)];
  yo = [yo; y1(ii)];
  ind = [ind; ii'];

  % Switch direction
  if(cp(nn)==2) 
	b = ~b; is2 = 1;
  else 
	b = cp(nn); is2 = 0; 
  end

end    % End while (all intersections) '''''''''''''''0

%%%%%%%%%%%%%%%%%%%%%%
% by Chao Yan
% [xo,yo] is a set of polygons(points, segments) seperated by NaN
% Remove duplicated points
ri = find( abs(diff(xo))<=eps & abs(diff(yo))<=eps ); % by chaoyan
xo(ri) = []; yo(ri) = []; ind(ri) = [];

% Remove coincident successive points
% <a,b,c> on the same line <==> det(a,1;b,1;c,1) = 0
sep = find(isnan(xo));
ss = [0; sep]; ee = [sep; length(xo)+1];  % add NaN at begin/end
ri = false(length(xo),1); 
for i=1:length(ss) % for each polygon
  s = ss(i)+1; e = ee(i)-1;
  prev = e; 
  for curr = s:e % for each point
	next = curr+1;
	if(next>e), next = s; end;
	ii = unique([prev;curr;next]);		
	if(length(ii)==3)
		tri = [xo(ii),yo(ii),ones(3,1)];
		if(abs(det(tri))<=10*eps) 
		  ri(curr) = true;
		else
		  prev = curr;
		end	
	else % a segment
		ri(s:e) = true;
		% left-lower point
		xp = xo(s:e); yp = yo(s:e);
		lxi = find(xp == min(xp));
		lyi = find(yp(lxi)==min(yp(lxi)),1); 
		lli = lxi(lyi)+(s-1);
		% right-upper point
		rxi = find(xp == max(xp));
		ryi = find(yp(rxi)==max(yp(rxi)),1); 
		rri = rxi(ryi)+(s-1);
		ri([lli,rri]) = false;
		continue;
	end
  end
end
xo(ri) = []; yo(ri) = []; ind(ri)=[];

% NOTE: We disable this and expose the disjoint polygons to poly_union. 
% For example: p1 = [0,1,1,0;0,0,1,1]; p2 = [0,1,1,0;1,1,2,2]; p = polyuni(p1,p2)
% 	the result is not correct. To avoid an under-approximated result, we 
%   expose this to poly_uion and use its convex hull.
% For poly_intersect, poly_intersect return the polygon with largest area.
%% If the result are disjoint polygons, remove non-polygon ones 
%sep = find(isnan(xo));
%ss = [0; sep]; ee = [sep; length(xo)+1];  % add NaN at begin/end
%lens = ee-ss-1; % length of polygons
%ri = false(length(xo),1); % to be removed
%ip = find(lens==1); % point
%ri(max(1,[ss(ip);ss(ip)+1])) = true;
%is = find(lens==2); % segment
%ri(max(1,[ss(is);ss(is)+1;ss(is)+2])) = true;
%if(all(ri)) % leave the first object
%	ri(1:(ee(1)-1)) = false;
%end
%xo(ri) = []; yo(ri) = []; ind(ri) = [];
