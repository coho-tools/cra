function ph = ph_face(ph,bloatAmt)
% This function prepare faces to be advanced

% compute bloatAmt and bloatLP
dim = ph.dim;
switch(numel(bloatAmt))
	case 1
		bloatAmt = repmat(bloatAmt,dim,2);
	case dim
		bloatAmt = repmat(bloatAmt(:),1,2);
	case 2*dim
		bloatAmt = reshape(bloatAmt,dim,2);
	otherwise
		error('incorrect number of bloatAmt');
end
ph.fwd.bloatAmt = bloatAmt;
ph.fwd.bloatLP = lp_bloat(ph.fwd.lp,bloatAmt);
% get opt
opt = ph.fwd.opt; object = opt.object; useInterval = opt.useInterval;

% Do not need to find face for 'ph' method
if(strcmpi(object,'ph')), return; end 

% Compute the globalLP for all faces
if(strcmpi(object,'face-height')) % increase the height
	phLP = ph.fwd.bloatLP; 
else 
	phLP = ph.fwd.lp; 
end
globalLP = lp_and(phLP,opt.constraintLP);

% Work on each face
for i=1:ph.ns
	slice = ph.fwd.slices(i); index = ph.planes(i,:);
	for j=1:slice.nf
		face = slice.faces(j);
		% Compute the face to move forward
		% projection of the face
		if(strcmp(object,'face-bloat')) 
			% all points that can move to this edge
			% max(eps,bloatAmt) for java solver? faceLP may be infeasible because of round-off error
			% NOTE: Use sum(bloatAmt) for soundness prove? 
			projLP = lp_bloat(face.innerLP,bloatAmt(:,[2,1]));
			projLP = lp_and(face.boundLP,projLP);
			sbbox = lp_box(lp_dims(projLP,'project',index));%bbox contains face
			bboxLP = lp_createByBox(sbbox,ph.dim,index);
			projLP = lp_and(projLP,bboxLP);
		else
			sbbox = face.bbox; % for interval
			projLP = lp_and(face.edgeLP,face.bboxLP); % bboxLP for lp_bloat
		end
		% use interval closure to find more accurate face. 
		% NOTE, if the face is infeasible under interval closure
		% we disable interval closure by setting bboxLP as [];
		if(useInterval && ph.type==0) 
			bbox = ph.bbox;
			bbox(index,:) = sbbox;
			bbox = ph_interval(ph,bbox,opt.intervalOpt);
			if(~isempty(bbox) && strcmpi(object,'face-height'))%increase height
				bbox = [bbox(:,1)-bloatAmt(:,1),bbox(:,2)+bloatAmt(:,2)];
			end
			%assert(isempty(bbox)||all(bbox(:,2)>=bbox(:,1)));
			bboxLP = lp_createByBox(bbox);		
		else
			bboxLP = [];
		end
		% construct faceLP
		% which might be infeasible for 'face-none','face-height','face-all'
		% because of round-off error in bboxLP computation. 
		% but usually it is not a problem:
		% 	we use bloatAmt in ph_model
		% 	int_forward adds 10*eps 
		faceLP = lp_and(lp_and(projLP,bboxLP),globalLP);
		%% NOTE: bloat the edgeLP a little to avoid empty intersection of adjacent faceLP caused by computation error
		%faceLP = lp_bloat(faceLP,1e-9); 
		face.projLP = projLP; % save for ph_realBloatAmt;
		face.faceLP = faceLP; % for ph_model and ph_forward;
		slice.faces(j) = face;
	end
	ph.fwd.slices(i) = slice;
end
