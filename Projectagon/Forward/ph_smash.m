function ph = ph_smash(ph)
% ph = ph_smash(ph)
% The function precomputes faces for computing forward projectagon
% 	A slice is a structure with fields
%		'nf': number of faces
%		'faces': faces of this slice
%	A face is a strucuture with fields
%		'edge': the corresponding projection edge
%		'edgeLP','lineLP','innerLP','boundLP': LP of the edge 
%			A edge has four constraints. Their order in edgeLP is
%			[outside normal, forward dir, inside normal, backward dir]
%			lineLP and innerLP are the outside and inside normal
%			boundLP are [forward,inside,backward]
%		'bbox','bboxLP': the bounding box and its corresponding LP of the edge
%		'projLP': projected LP of faceLP, used for ph_realBloatAmt
%		'faceLP': face to move forward, result of ph_face
%		'models': result of models
%		'projs': result of ph_forward

opt = ph.fwd.opt;
ph.fwd.lp = lp_and(ph.hullLP,ph.bboxLP);

if(strcmpi(opt.object,'ph'))
	return;
end

% create slices
ns = ph.ns; m = eye(ph.dim);
slices = struct('nf',cell(ns,1),'faces',cell(ns,1));
for i=1:ns 
	plane = ph.planes(i,:); dims = m(:,plane);
	poly = poly_breakLongEdge(ph.polys{i},opt.maxEdgeLen); 
	nf = size(poly,2); poly = poly(:,[1:end,1]);
	% create a face
	faces = struct('edge',cell(nf,1), 'edgeLP',cell(nf,1),...  
		'lineLP',cell(nf,1), 'innerLP',cell(nf,1), 'boundLP',cell(nf,1),...
		'bbox',cell(nf,1), 'bboxLP',cell(nf,1), 'projLP',cell(nf,1),...
		'faceLP',cell(nf,1),'modelLP',cell(nf,1), 'models',cell(nf,1), 'projs',cell(nf,1));
	for j=1:nf
		edge = poly(:,[j,j+1]);
		edgeLP = lp_createByHull(edge,dims); % isnorm = true;
		%% NOTE: bloat the edgeLP a little to avoid empty intersection of adjacent faceLP caused by computation error
		%edgeLP = lp_bloat(edgeLP,10*eps); 
		lineLP= lp_create(edgeLP.A(1,:),edgeLP.b(1),[],[],true);
		innerLP = lp_create(edgeLP.A(3,:),edgeLP.b(3),[],[],true);
		boundLP= lp_create(edgeLP.A([1,2,4],:),edgeLP.b([1,2,4]),[],[],true);
		bbox = [min(edge,[],2),max(edge,[],2)];
		bboxLP = lp_createByBox(bbox,ph.dim,plane);
		faces(j)  = struct('edge',edge,'edgeLP',edgeLP,'lineLP',lineLP,...
			'innerLP',innerLP,'boundLP',boundLP,'bbox',bbox,'bboxLP',bboxLP,...
			'projLP',[], 'faceLP',[], 'modelLP',[],'models',[], 'projs',[]);
	end
	slices(i) = struct('nf',nf,'faces',faces);
end 

ph.fwd.slices = slices;
