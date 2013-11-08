function d = poly_ptsDist(poly,pts)
% function d = poly_ptsDist(poly,pts)
%	This function compute the minimum distance between point and boudary of polygon.  
%	poly:	2xn matrix. Each column is a vertex of polygon
%	pts:	2xm matrix, each column is point 
%	d:		mx1 vector, non-negative value. The minimum distance between pt and polygon boudary

%% Algorithm: For each point, we compute the distance between pt and vertices of polygon,
% compute the distance between pt and edges of polygon if the projection of pt is on the edge. 

if(isempty(poly)), d = []; return; end

%% Vectorize the outer loop
n = size(poly,2);	m = size(pts,2);
pts = pts'; poly = poly'; %conver to nx2 matrix. 

% duplicated pts and poly.  
ptsDup = reshape(repmat(pts,n,1),m,n,2); %mxnx2 matrix. ptsDup(i,j,:) is the ith pt
polyDup= reshape(repmat(reshape(poly,1,[]),m,1),m,n,2);%mxnx2 matrix. polyDup(i,j,:) is the jth vertex

% compute vector from vertex to pt
% p2ptVec is a mxnx2 matrix. p2ptVec(i,j,:) is the vector from jth vertex to ith pt
p2ptVec = ptsDup - polyDup;

% compute vector for each polygon edge
% p2pVec is a mxnx2 matrix, p2pVec(:,j,:) is the vector for the jth polygon edge. 
p2pVec = polyDup(:,[2:end,1],:) - polyDup;

% computer inner product of p2ptVec and p2pVec. 
% mxn matrix. the dot product of ith pt vector and jth edge vector. 
ppdot = dot(p2pVec,p2pVec,3); %duplicated computation, improve speed? 
ptptdot = dot(p2ptVec,p2ptVec,3); 
ptpdot = dot(p2ptVec,p2pVec,3); 

% compute the cross product of p2ptVec and p2pVec. 
% mxn matrix. cross product of ith pt vector and jth edge vector 
ptpcross = p2ptVec(:,:,1).*p2pVec(:,:,2) - p2ptVec(:,:,2).*p2pVec(:,:,1);

% compute the distance of pt vector. mxn matrix. length of pt vector from jth vertex to ith pt 
pt2vDist = sqrt(ptptdot); 
% compute the distance of pt to edge. mxn matrix, length of jth edge vector
pt2eDist = abs(ptpcross)./sqrt(ppdot);
% compute of pt projection on edge. mxn matrix, if ith pt vector projected on jth edge vector
ptPjedge = (ptpdot >=0 & ptpdot <= ppdot);

% compute the minimum distance of each pt 
pt2eDist(~ptPjedge) = Inf; %ingnore projection not on edge case.
d = min([pt2vDist,pt2eDist],[],2);

%% The for loop implementation
%n = size(poly,2);	m = size(pts,2);
%d = zeros(m,1);
%for i = 1:m %compute each pt
%	pt = pts(:,i);
%	%compute distance between pt and vertices/edge of polgyon
%	pvd = zeros(n,1); ped=zeros(n,1); pjon = false(n,1);
%	for j=1:n %compute for each edge/vertex 
%		spt = poly(:,j); ept=poly(:,utils_imod(j+1,n));
%		a = pt-spt; b = ept-spt;
%		pvd(j) = sqrt(dot(a,a)); 
%		bb = dot(b,b); ab = dot(a,b);
%		ped(j) = det([a,b])/sqrt(bb);
%		pjon(j) = (ab>=0 && ab<=bb);
%	end;
%	d(i) = min([pvd;abs(ped(pjon))]);
%end
%dd = d;

%% Vectorize the inner loop
%n = size(poly,2);	m = size(pts,2);
%d = zeros(m,1);
%for i = 1:m %compute each pt
%	pt = pts(:,i);
%	%compute distance between pt and vertices/edge of polgyon
%	%a is vector from spt->pt, b is vector from spt->ept(edge of polygon)
%	a = repmat(pt,1,n)-poly; b = poly(:,[2:end,1])-poly; 
%	aa = dot(a,a); bb = dot(b,b); ab = dot(a,b);
%	acb = a(1,:).*b(2,:)-a(2,:).*b(1,:); %cross product of 2d case
%	pvd = sqrt(aa);  %pt->vertex distance
%	ped = acb./sqrt(bb); %pt->edge distance
%	pjon = (ab>=0 & ab<=bb); %projection of pt on edge
%	d(i) = min([pvd,abs(ped(pjon))]);
%end
%dd = d;

