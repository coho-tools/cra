function isc = ph_containPts(ph,pts)
% isc = ph_containPts(ph,pts)
% The function check whether a point is in a projectagon or not. 
% Inputs
% 	ph:	a projectagon which may not be canonical
%   pts: a matrix with each column as a point
% 		ph and pts have the same number of dimenions
% Output:
% 	isc: a row vector, true if ph contains the corresponding point

% Algorithm:
% a point pt is contained in ph iff
% 	1) pt satisfies the hullLP and 
% 	2) projected pt is contained in each projected polygon

if(ph_isempty(ph)), isc=false; return; end

[d,n] = size(pts);
if(d~=ph.dim)
	error('ph and pts must have the same dimenions'); 
end

switch(ph.type)
	case 0
		isc = lp_sat(ph.hullLP,pts); % check lp
		for i=1:ph.ns
			plane = ph.planes(i,:);
			p = pts(plane,isc); % only check possible inside points
			in = poly_containPts(ph.polys{i},p);	
			isc(isc) = isc(isc) & in;
		end
	case 1 
		isc = lp_sat(ph.hullLP,pts);
	case 2
		bbox = ph.bbox; l = bbox(:,1); h = bbox(:,2);
		isc = all(pts>=repmat(l,1,n),1)' & all(pts<=repmat(h,1,n),1)';
	otherwise
		error('unknown projectagon type');
end
