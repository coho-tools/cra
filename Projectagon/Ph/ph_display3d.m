function h = ph_display3d(ph,opt)
% h = ph_display3d(ph,opt)
%	The function use isosurface to show a 3D projectagon
%	ph:     The 3D projectagon to display.
%	opt:	option to control the result.
%       opt.gridNum:    The number of grids on each dimension. The deafult 51. 
%       opt.showHull:   Show the projectagon or its hull. logical value.
%                       The default is false
%		opt.margin:		We add extra margin to the bounding box to make sure the 
%						projectagon is visible. The default value is 0.1.
%		opt.fig:        The figure handle to display. Create figure by default.
%		opt.phColor:    The color of the projectagon. The default is 'red'.
%       opt.phFaceAlpha:The FaceAlpha value for patch function to draw the ph. (1 by default).     
%		opt.showGrid:	Show the grid(axis) or not
%		opt.showPrism:	Show the prism if it is 1. The default is 1.
%			opt.prismColor:		The color of each prism. Cell Array. 
%								The default is {'g','m','y'} otherwise.
%           opt.prismFaceAlpha: The FaceAlpha value for patch function to draw prisms           
%           opt.prismLineStyle: The edge style of each prism. ':' by default
%	h:      The handle of the plot
%		phH: 	The handle of the projecatgon
%		prismHs:The vector if handles of the prisms. Returned when showPrism==1 	
%
%	by chaoyan@cs.ubc.ca 2007/12/11

%% Algorithm
% We create large amount of grid points with the bounding box of
% projectaton. Then compute the distance of point to the boundary of the
% projectagon. If the point is contained in the projectagon, the distance
% is negative. If the point is outside the projecatagon, the distance is
% possitive. Then we use isosurface to show the projectagon. 
%
% We compute the distance by projecting each point to each slice and
% compute the distance of the 2D point and the slice polygon of
% projectagon. We use the max of all of these distance as the distance
% between the 3D point and the projectagon. Of course, if the point is
% outside the projectagon, the distance is the largest one, not the closest
% one as the case when the point is inside the projecaton. (I try to use
% the closest distance for both case, but it seems the result is not better
% or better enough).
% 
% The current problem is that the edge of projectagon has sawtooth.
% 

%% Immplemenation.

	% extract options
	assert(ph.dim==3); %function for 3D projectagon only.
	if(nargin<2)
		opt = [];
	end;
	if(~isfield(opt,'fig'))
		fig = figure;
	else
		fig = opt.fig;
	end;
	if(~isfield(opt,'phColor'))
		phColor = 'red';
	else
		phColor = opt.phColor;
	end;
	if(~isfield(opt,'phFaceAlpha'))
	    phFaceAlpha = 1;
	else
	    phFaceAlpha = opt.phFaceAlpha;
	end;
	if(~isfield(opt,'showGrid'))
		showGrid = 1;
	else
		showGrid = opt.showGrid;
	end;
	if(~isfield(opt,'showPrism'))
		showPrism = 1;
	else
		showPrism = opt.showPrism;
	end;
	if(showPrism)
		if(~isfield(opt,'prismColor'))
			prismColor = {'g','m','y'};
		else
			prismColor = opt.prismColor;
		end;
		if(~isfield(opt,'prismFaceAlpha'))
			prismFaceAlpha = 0.2;
		else
			prismFaceAlpha = opt.prismFaceAlpha;
		end;
	    if(~isfield(opt,'prismLineStyle'))
	        prismLineStyle = ':';
	    else
	        prismLineStyle = opt.prismLineStyle;
	    end;
	end;
	
	% compute data
	if(showPrism)
		[xs,dist,bbox,projPolys] = ph_isosurface(ph,opt);
	else
		[xs,dist,bbox] = ph_isosurface(ph,opt);
	end;
	
	% Plot the projectagon. 
	figure(fig);
	hold on;
	phH = patch(isosurface(xs{:}, dist, 0));
	set(phH, 'EdgeColor', 'none', 'FaceColor', phColor,'FaceAlpha',phFaceAlpha);
	isonormals(xs{:}, dist, phH);
	h.phH = phH;
	
	
	% Plot the projection polygon
	if(showPrism)
	    prismHs = cell(ph.ns,1);
		for i=1:ph.ns
			color = prismColor{utils_imod(i,length(prismColor))};
			loPoly = projPolys{i,1};
			hiPoly = projPolys{i,2};
	        n = size(loPoly,2); prismH = zeros(n,1);
			for j = 1:n
	            pts = [loPoly(:,[j,utils_imod(j+1,n)]),hiPoly(:,[utils_imod(j+1,n),j])];        
	            prismH(j) = patch(pts(1,:)',pts(2,:)',pts(3,:)',color);
	            set(prismH(j),'EdgeColor',color,'FaceAlpha',prismFaceAlpha,'LineStyle',prismLineStyle);
				%line([loPoly(1,j);hiPoly(1,j)],[loPoly(2,j);hiPoly(2,j)],[loPoly(3,j);hiPoly(3,j)],'lineStyle',':');
			end;
	        prismHs{i} = prismH;
		end;
	    h.prismHs = prismHs;
	end;
	
	
	% add lights, change view.
	
	% lables;
	if(showGrid)
		view(3);
		camlight headlight;
		view(60, 15);
		camlight right;
		daspect([ 1 1 1 ]);
		xlabel('x');  ylabel('y');  zlabel('z');
		axis(reshape(bbox',1,6));
		grid on; 
	end;
	hold off;
end %ph_display3d
	

function [xs,dist,bbox,projPolys] = ph_isosurface(ph,opt)
	if(~isfield(opt,'gridNum'))
	    n = 51;
	else
	    n = opt.gridNum;
	end;
	if(~isfield(opt,'showHull'))
	    showHull = false;
	else
	    showHull = opt.showHull;
	end;
	if(~isfield(opt,'margin'))
		margin = 0.1;
	else
		margin = opt.margin;
	end;
	if(~isfield(opt,'showPrism'))
		showPrism = 1;
	else
		showPrism = opt.showPrism;
	end;
	
	% find the bounding box and create meshgrids
	bbox = ph.bbox;
	bbox = bbox+([bbox(:,1)-bbox(:,2),bbox(:,2)-bbox(:,1)])*margin;
	grids(1,:) = linspace(bbox(1,1),bbox(1,2),n);
	grids(2,:) = linspace(bbox(2,1),bbox(2,2),n);
	grids(3,:) = linspace(bbox(3,1),bbox(3,2),n); %use same grid size to smooth edge?
	[xs{1:3}] = meshgrid(grids(1,:),grids(2,:),grids(3,:)); %n*n*n
	
	% Compute the distance data;
	dist = -Inf*ones(n,n,n);  	% The distance of each grid.
	if(showPrism)
		projPolys = cell(ph.ns,2);	% The projected polygon. 
	end;
	    
	for i=1:ph.ns
	    if(showHull)
	        poly = ph.hulls{i};
	    else
	        poly = ph.polys{i};
	    end;
	
	    %-------------------------------------------------------------%
	    %	Method I, generate grid points and test if it is in the projectagon
	    %-------------------------------------------------------------%
	
	    %-------------------------------------------------------------%
	    %	Method II, with optimized performance.
	    % 	[X,Y,Z] = meshgrid(x,y,z);
	    %	X/Y/Z is n*n*n matrix represents the x/y/z value of grid points
	    %	However, the order is not x-y-z, it is y-x-z.
	    %	Therefore, if you want to get ith slice on x, use X/Y/Z(:,i,:)
	
	    % First, make the slice x-y-z order. e.g. convert y-x to x-y
		index = ph.planes(i,:);
	    if(index(1)>index(2))
	        poly = [poly(2,end:-1:1);poly(1,end:-1:1)]; %maintain anti-clock-wise order
	    end;
	    index = reshape(sort(index),[],1);
	    if(all(index==[1;2])) % x-y slice,, z are the same. 
	        same = 3;
	    elseif(all(index==[1;3])) % x-z slice, y are the same.
	        same = 2;
	    elseif(all(index==[2;3])) % y-z slice, x are the same.
	        same = 1;
	    else
	        error('unexpected error');
	    end;
	
	
	    % Second, project grid points onto slice to reduce num of pts;
	    % pts has the same order with projecting grid points onto slice. 
	    switch(same)
	        case 1 % project on x direction.
	            Y = reshape(xs{2}(:,1,:),n,n);
	            Z = reshape(xs{3}(:,1,:),n,n);
	            pts = [reshape(Y,1,[]);reshape(Z,1,[])];
	        case 2 % project on y direction.
	            X = reshape(xs{1}(1,:,:),n,n);
	            Z = reshape(xs{3}(1,:,:),n,n);
	            pts = [reshape(X,1,[]);reshape(Z,1,[])];
	        case 3 % project on z direction.
	            X = reshape(xs{1}(:,:,1),n,n);
	            Y = reshape(xs{2}(:,:,1),n,n);
	            pts = [reshape(X,1,[]);reshape(Y,1,[])];
	        otherwise
	            error('unexpected error');
	    end;
	
	    % Third, compute the distance and if it is contained by the polygon.
	    d = poly_ptsDist(poly,pts);
	    inside = poly_containPts(poly,pts);
		d(inside) = -d(inside); % make inner distance negative.
	
	    % Finally, duplicated the result for all grid points with same projected point.
	    switch(same)
	        case 1 %duplicate on x direction.
	            d = repmat(reshape(d,n,1,n),[1,n,1]);
	        case 2 %duplicate on y direction. 
	            d = repmat(reshape(d,1,n,n),[n,1,1]);
	        case 3 %duplicate on z direction. 
	            d = repmat(reshape(d,n,n,1),[1,1,n]);
	        otherwise
	            error('unexpected error');
	    end;
		%
	    %-------------------------------------------------------------%
	      
		% collect data to dispaly the projectagon. 
		dist = max(dist,d); %if outside, use the max distance to each slice?
		if(showPrism)
			po = zeros(3,size(poly,2));
			po(index,:) = poly; 
			po(same,:) = bbox(same,1)*ones(1,size(poly,2));
			projPolys{i,1} = [po,po(:,1)]; % projection on lower bound.
			po(same,:) = bbox(same,2)*ones(1,size(poly,2));
			projPolys{i,2} = [po,po(:,1)]; % projection on upper bound.
		end;
	end;
end % ph_isosurface
