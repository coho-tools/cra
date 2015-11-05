function ph = ph_projUpdate(ph,hulls)
[bbox,bboxLP] = hull2box(ph.dim,ph.planes,hulls);
%% compute bbox 
%bbox = repmat([Inf,-Inf],ph.dim,1);
%X = 1; Y = 2; LO = 1; HI = 2;
%for i=1:size(ph.planes,1) 
%  hull = hulls{i};  plane = ph.planes(i,:);
%  bbox(plane(X),LO) = min(bbox(plane(X),LO),min(hull(X,:)));
%  bbox(plane(X),HI) = max(bbox(plane(X),HI),max(hull(X,:)));
%  bbox(plane(Y),LO) = min(bbox(plane(Y),LO),min(hull(Y,:)));
%  bbox(plane(Y),HI) = max(bbox(plane(Y),HI),max(hull(Y,:)));
%end
%
%assert(~any(isinf(bbox(:))));
%bboxLP = lp_createByBox(bbox);

% Do not update hullLP, keep bbox form as possible
ph.hulls = hulls;
ph.bbox = bbox;
ph.bboxLP = bboxLP;
ph.status = 0; % projected
