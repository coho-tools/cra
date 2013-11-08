function ph = ph_succ(ph1,ph2,bloatAmt)
% ph = ph_succ(ph1,ph2,bloatAmt)
% This function over-approximation the reachable region during time [t1,t2]
% where the reachabe region at time t1/t2 is ph1/ph2. 
% It computs the convex hull of ph1 and ph2, and then bloat it by bloatAmt. 
% The result is always a convex projectagon. 

if(ph_isempty(ph1)), ph = ph2; return; end
if(ph_isempty(ph2)), ph = ph1; return; end

if( ph1.dim~=ph2.dim || ~all(ph1.planes(:)==ph2.planes(:)) )
	error('ph1 and ph2 must have the same structure');
end
if(nargin<3)
	bloatAmt = [];
end
assert(all(bloatAmt>=0));

dim = ph1.dim; planes = ph1.planes; ns = ph1.ns;
type = min(ph1.type,ph2.type);

switch(type)
	case {0,1} 
		hulls = cell(ns,1);
		for i=1:ns 
			hulls{i} = poly_convexHull([ph1.hulls{i},ph2.hulls{i}]); 
		end
		ph = ph_create(dim,planes,hulls,[],1,false);
		if(~isempty(bloatAmt)) 
			lp = lp_bloat(ph.hullLP,bloatAmt);
			ph = ph_createByLP(dim,planes,lp);
		end
	case 2 % bbox
		bbox1 = ph1.bbox; bbox2 = ph2.bbox;	
		bbox = [min(bbox1(:,1),bbox2(:,1)),max(bbox1(:,2),bbox2(:,2))];
		if(~isempty(bloatAmt))
			bbox(:,1) = bbox(:,1)-bloatAmt(:,1);
			bbox(:,2) = bbox(:,2)+bloatAmt(:,2);
		end
		ph = ph_createByBox(dim,planes,bbox);
	otherwise
		error('unknown projectagon type');
end
