function bbox = ph_interval(ph,bbox,opt)
% bbox = ph_interval(ph,bbox,opt)
%		Given a projectagon ph and a bounding box bbox, 
%		compute the bounding box of the intersection of the ph and bbox.
%		We use iteration method to compute the interval for each variables. 
%		We stop the iteration once the change of intervals are all less then eps
%			 return [] if the intersection is empty
assert(ph.iscanon);

% TODO: test this function 

% do not need for type 1 and 2.
switch(ph.type)
	case 0 % non-convex 
		if(nargin<3||isempty(opt)) 
			phOpt = cra_cfg('get','phOpt');
			opt = phOpt.intervalOpt;
		end
		eps = opt.eps; niters = opt.iters;
		for iter = 1:niters
			nbbox = ph_interval_help(ph,bbox);
			if(isempty(nbbox)), bbox = []; return; end 
			if(all(abs(nbbox(:)-bbox(:))<=eps))
				bbox = nbbox;
				return; 
			end
		end
	case 1 % lp
		bbox = ph_interval_help(ph,bbox);
	case 2 % bbox
		bbox = [max(bbox(:,1),ph.bbox(:,1)), min(bbox(:,2),ph.bbox(:,2))];
	otherwise
		error('do not support');
end

function bbox = ph_interval_help(ph,bbox)
	for i=1:ph.ns
		index = ph.planes(i,:);
		sbox = bbox(index,:);
		points = poly_interval(ph.polys{i},sbox);
		if(~isempty(points))
			nbox = [min(points(1,:)),max(points(1,:)); ...
					min(points(2,:)),max(points(2,:))];
			sbox = [max(sbox(:,1),nbox(:,1)), min(sbox(:,2),nbox(:,2))];
			bbox(index,:) = sbox;
		else
			bbox = [];
			return;
		end
	end
%end

