function ph = ph_model(ph,bloatAmt)
% ph = ph_model(ph)
% The function computes dynamic model required for computing forward projectagon
% 	It assumee each point in the projectagon can move at most by bloatAmt.  


ph = ph_face(ph,bloatAmt);
opt = ph.fwd.opt;
object = opt.object; 

modelFunc = cra_cfg('get','modelFunc');
globalLP = lp_and(ph.fwd.bloatLP,opt.constraintLP);

if(strcmpi(object,'ph')) 
	modelLP = globalLP; 
	%ph.fwd.modelLP = modelLP;
	models = modelFunc(modelLP);
	if(~iscell(models)), models = {models}; end % convert LDI models to cell
	ph.fwd.models = models; 
	return;
end 

% work on each face
for i=1:ph.ns
	slice = ph.fwd.slices(i);
	for j=1:slice.nf
		face = slice.faces(j);
		modelLP = lp_bloat(face.faceLP,bloatAmt);
		modelLP = lp_and(modelLP,globalLP);
		face.modelLP = modelLP; % save for ph_timeStep
		models = modelFunc(modelLP);
		if(~iscell(models)), models = {models}; end % convert to cell
		face.models = models; 
		slice.faces(j) = face;
	end
	ph.fwd.slices(i) = slice; 
end
