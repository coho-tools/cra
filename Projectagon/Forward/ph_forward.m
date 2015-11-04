function ph = ph_forward(ph,timeStep)
% [fwdPh,ph] = ph_forward(ph,timeStep,opt)
% This function computes the forward projectagon

ph.fwd.timeStep = timeStep;
opt = ph.fwd.opt;
object = opt.object; tol =  opt.tol;

% work on the projecagon
if(strcmpi(object,'ph'))
	% move forward the projectagon
	fwdLPs = ints_forward(ph.fwd.lp,ph.fwd.models,timeStep);
	% project onto each slice
	projs= cell(ph.ns,1); 
	for i=1:ph.ns 
		% normal vector of current polygons
		plane = ph.planes(i,:);
		nonzero = true(ph.dim,1); nonzero(plane) = false;
		ndir = ph.fwd.lp.A'; ind = all(ndir(nonzero,:)==0);
		ndir = ndir(:,ind);
		projs{i} = lps_project(fwdLPs,plane,tol,ndir); 
	end
	ph.fwd.projs = projs;
	return;
end

% work on each face
for i=1:ph.ns
	slice = ph.fwd.slices(i);
	if(strcmpi(object,'face-all')) % project onto all slices
		np = ph.ns; planes = ph.planes;
	else % current slice only
		np = 1; planes = ph.planes(i,:);
	end
	for j=1:slice.nf
		face = slice.faces(j);
		% move forward each face
		fwdLPs = ints_forward(face.faceLP,face.models,timeStep);
		projs = cell(np,1);	
		for k=1:np
			if(np==1||k==i) % project onto the current slice
				ndir = face.edgeLP.A'; % guid directions
			else
				ndir =zeros(ph.dim,0); % no guide
			end
			% project advanced faces onto slices
			projs{k} = lps_project(fwdLPs,planes(k,:),tol,ndir);
		end
		face.projs = projs;
		slice.faces(j) = face;
	end
	ph.fwd.slices(i)=slice;
end

% projects a set of lps and computes its intersection
function poly = lps_project(lps,plane,tol,ndir)
	nlp = length(lps); ps = cell(nlp,1);
	for i=1:nlp
		lp = lps{i}; 
		ndir = lp.bwd*ndir; ndir = ndir(plane,:);
		opt.angles = atan2(ndir(2,:),ndir(1,:))';
		ps{i} = lp_project(lp,plane,tol,opt);
		if(isempty(ps{i}))
			msg = 'projectagon empty';
			exception = MException('COHO:Projectagon:EmptyProjection',msg); 
			throw(exception);
		end
	end
	poly = poly_intersect(ps);
%end

% NOTE we only support LDI now. Use function handle for extension. 
function fwdLPs = ints_forward(lp,models,timeStep)
  fwdLPs = cell(length(models),1);
  for i=1:length(models)
    fwdLPs{i} = int_forward(lp,models{i},timeStep);
	end
