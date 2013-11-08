function [ohull,uhull,err] = lp_projectAsympt(lp,plane,tol,opt)
% [ohull,uhull,err] = lp_projectAsympt(lp,plane,tol,opt)
% Input:
% 	plane: index of plane axis

if(nargin<4||isempty(opt)) 
	opt = struct('angles',[],'niters',3,'method','both');
end
if(~isfield(opt,'angles'))
	opt.angles =[];
end
if(~isfield(opt,'niters')||isempty(opt.niters))
	opt.niters =3;
end
if(~isfield(opt,'method')||isempty(opt.method))
	opt.method='both';
end

initAngles=opt.angles; niters=opt.niters; method = opt.method; 

% use the normal vector of the LP
if(isempty(initAngles))
	ndir = lp.A'; % each column is a normal vector
	ndir = ndir(plane,:); % project onto plane
	initAngles = atan2(ndir(2,:),ndir(1,:))'; % compute their angle
end 
angles = [];

% solve lp recursive until the gap between ohull and uhull is smaller than tol.
% TODO: LP are resolved again in the next iteration.
for iter=1:niters
	angles = [angles;initAngles];
	[ohull,uhull] = lp_hulls(lp,angles,plane);
	if(isempty(ohull))
		ohull = zeros(2,0); uhull = zeros(2,0); err = NaN;
		return;
	end
	a1 = poly_area(ohull); a2 = poly_area(uhull);
	err = (a1-a2)/a2;
	if(err<=tol), break; end
	switch(lower(method)) 
		case {'both'}
			N = 4*2^iter; % n-regular polygon
			angles = linspace(0,1,N+1)';	
			angles = angles (1:end-1)*2*pi;
			ulp = lp_createByHull(uhull);  % norm of uhull
			ua = atan2(ulp.A(:,2),ulp.A(:,1)); 
			angles = [angles;ua];
		case {'regular','bisect','bisection'}  % n-regular polygon
			N = 4*2^iter; % bisect 
			angles = linspace(0,1,N+1)';	
			angles = angles(1:end-1)*2*pi; 
		case {'hull','uhull','norm'} % add the norm of uhull	
			ulp = lp_createByHull(uhull); 
			angles = atan2(ulp.A(:,2),ulp.A(:,1));
		otherwise 
			error('do not support'); 
	end
end
