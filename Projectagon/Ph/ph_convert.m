function ph = ph_convert(ph,dir)
% ph = ph_convert(ph,dir)
% Now we have three types of projectagon: general(concave), convex, and
% bounding box. This function convert a projectagon between these types.
%
% Input: 
% 	ph:		a projectagon
% 	dir: 	conversion direction of
% 			'general','concave','non-convex' or 0: 	convert to a general projectagon
% 			'convex' or 1:	convert to a convex projectagon
% 			'bbox','rectangle',or 2: convert to a hypre-rectangle
% Output:
% 	ph: 	a projectagon with specified type  
% 
% A general projectagon is returned if dir is not provided.
% The projectagon maybe changed (over-approximated) 
%
% Examples:
% 	ph = ph_convert(ph); % convert to general projectagon
% 	ph = ph_convert(ph,'convex'); % convert to convex projectagon
%  	ph = ph_convert(ph,'bbox'); % convert to a bounding box

% Algorithm
% upgrade (cube->convex->concave): update the type only
% downgrade (concave->convex->cube): update polys (over-approximation)

% empty projectagon
if(isempty(ph)), return; end

% conversion direction
if(nargin<2||isempty(dir))
	dir = ph.type; % do not convert
end
if(ischar(dir))
	strs = {'general','concave','non-convex','convex','bbox','rectangle'};
	opts = [0,0,0,1,2,2];
	id = utils_strs2ids(dir,strs);
	if(id<1)
		error('unknown direction');
	else
		dir = opts(id);
	end
end

if(ph.type<dir) % downgrade
	switch(dir)
		%case 0
		case 1 % concave -> convex
			ph.polys = ph.hulls; % lp not changed
		case 2 % concave -> convex -> bbox
			ph = ph_createByBox(ph.dim,ph.planes,ph.bbox);
		otherwise
			error('do not support');
	end
else % upgrade
	% nothing to do
end
ph.type= dir; % update type
