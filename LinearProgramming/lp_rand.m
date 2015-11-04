function lp = lp_rand(dim,siz,type)
% lp = lp_rand(dim,siz,type)
% This function generates a random LP.  
% Input:
% 	dim: the number of variables
% 	siz: the number of equality constraints and inequality constraints
% 	type: a string chosen from
% 		'lp': generate a general lp (default)
% 		'coho': generate a coho lp
if(nargin<3||isempty(type))
	type = 'lp';
end

switch(lower(type))
case {'general','lp'}
	A = rand(siz(1),dim); 
	b = rand(siz(1),1);
case 'coho'
	A = zeros(siz(1),dim);
	b = rand(siz(1),1);
	for i=1:siz(1)
		pos = round(rand(1,2)*(dim-1))+1; % random place
		A(i,pos) = rand(1,2);
	end
otherwise
	error('do not support');
end

A = A.*sign(rand(size(A))-0.5); % sign
b = b.*sign(rand(size(b))-0.5); 
lp = lp_create(A,b);
