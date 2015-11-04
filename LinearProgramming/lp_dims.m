function lp = lp_dims(lp,op,index,n) 
% lp = lp_dims(op,index,n) 
% This function updates the dimensions of a lp
% 	It supports two operations:
%		'project': reduce the number of dimension. 
% 		'augment': increase the number of dimension. 
% 	For example
%		A = [1,0,0] b = 1; lp = lp_create(A,b);
%		lpr = lp_dims(lp,'project',[1,2]); % lpr.A = [1,0];
% 		lpf = lp_dims(lpr,'augment',[4,1],4); % lpf.A = [0,0,0,1]

switch(lower(op)) 
	case {'project','reduce'} 
		dim = size(lp.A,2);
		if(length(index)>dim||any(index>dim)||any(index<=0))
			error('input incorrect');
		end
		lp.A = lp.A(:,index); 
	case {'augment',increase'} 
		dim = size(lp.A,2);
		if(n<dim||length(index)~=dim||any(index>n)||any(index<=0))
			error('input incorrect');
		end
		A = zeros(size(lp.A,1),n);
		A(:,index) = lp.A; lp.A = A; 
	otherwise
		error('do not support operation'); 
end
