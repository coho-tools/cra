function opt = ph_getOpt(type)
% opt = ph_getOpt(type)
% This function returns a template of opt structure for ph_advance function
%	type can be a string of 
%		'default': the default template of opt (default value of type)
%		'fast': optimize the performance
%		'stable': use the most numerical stable algorithms. 
%		'accurate': optimize for accuracy.
%		
% 	The advanceOpt is a structure with the following fields
%	I. Parameters to compute model
% 	'model': methods to compute valid value of bloatAmt and timeStep 
% 		valid value includes
%			'guess-verify': guess a pair and verify at the end
%			'bloatAmt': Use fixed bloatAmt
%			'timeStep': use fixed timeStep
%	'maxBloat','maxStep': maximum bloatAmt and timeStep allowed. 
%	'bloatAmt','timeStep': the value of bloatAmt or timeStep to use when 
%			'model' is 'bloatAmt' or 'timeStep'. 
%	'prevBloatAmt','prevTimeStep': the value of bloatAmt and timeStep of 
%			of previous ph_advance call. It is used when 'model' is 'guess-verify'
%	'ntries': maximum number of tries, useful only when 'model'='guess-verify'
% 	II. Parameters to compute face
% 	'object': methods to compute object to advance, valid value includes
% 			'ph': advance the whole projectagon, the type of projectagon 
% 					can not be 'concave'. 
%			'face-all': advance all faces and project them onto all slices. 
%			'face-none': advance faces of a slice and project them onto 
%				their corresponding slice.
%			'face-bloat': advance faces of a slice, bloat the faces to 
%				capture all behaviours. 
%			'face-height': advance faces of a slice, increase the height of 
%				faces to capture all behaviours.
%	'maxEdgeLen': maximum length of polygon edges. When object is not 'ph',
%			projection polygons are broken into short edges to reduce error. 
%			Increase the value will reduce the number of faces but increase 
%			model error.
%	'useInterval': indicate whether use the 'interval closure' method to find
%			more accurate face or not for 'concave' projectagon. 
% 	III. Error control
%	'tol': 	tolerrence used to simplify the polygons. 
%	'riters','reps': To reduce model error, ph_advance repeats computation
%			with smaller bloatAmt. The loop exits either the number of 
% 			iterations is greater than 'riters' or the difference of realBloatAmt and  
%			bloatAmt is greater than  'reps'
%	'constraintLP': global contraint of reachable region, it is used to reduce
%			error when computing models and advance faces. 
%	'canonOpt': the parameters of ph_canon function. see ph_canon.
%	'intervalOpt': the parameters of ph_interval function. see ph_interval.
% 				useful only when ph is a concave projectagon.
%	


if(nargin<1||isempty(type))
	type = 'default';
end

% default value
model = 'guess-verify'; 
maxBloat = 0.1; maxStep = Inf; ntries = 3;
bloatAmt = []; timeStep = [];
prevBloatAmt = []; prevTimeStep=[];
object = 'face-none';
useInterval = true;
maxEdgeLen = 0.1;
tol = 0.02;
riters = 1; 
reps = 0.2;
constraintLP = []; 
canonOpt = struct('eps',0.01, 'iters',3, 'tol',0.02); 
intervalOpt = struct('eps',1e-6,'iters',3);

switch(lower(type))
	case 'default' 
	case 'fast'
		object='ph';
		maxBloat = 0.2;
		maxEdgeLen = 0.2;
		useInterval = false;
		reps = 0.4;
		tol = 0.05;
		canonOpt.tol = 0.05;
		canonOpt.iters = 1;
		intervalOpt.iters = 1;
	case 'stable'
		model = 'bloatAmt';
		object = 'face-bloat';
		useInterval = false;
	case 'accurate'
		object = 'face-none';
		maxBloat = 0.05;
		maxEdgeLen = 0.05;
		useInterval = true;
		tol = 0.01;
		riters = 3;
		reps = 0.1;
		canonOpt.eps = 1e-3;
		canonOpt.iters = 5;
		canonOpt.tol = 0.01;
		intervalOpt.iters = 5;
	otherwise
		error('unknow type'); 
end

opt = struct('model',model, 'maxBloat',maxBloat, 'maxStep',maxStep, 'ntries', ntries, ...  
	'bloatAmt',bloatAmt, 'timeStep',timeStep, ...  
	'prevBloatAmt',prevBloatAmt, 'prevTimeStep',prevTimeStep, ...
	'object',object, 'useInterval',useInterval, 'maxEdgeLen',maxEdgeLen, ...
	'tol',tol, 'riters',riters, 'reps',reps, 'constraintLP',constraintLP, ...
	'canonOpt', canonOpt, 'intervalOpt', intervalOpt );
