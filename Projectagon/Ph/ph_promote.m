function phs = ph_promote(phs)
% This function is used to promote a set of projectagons 
% such that they have the same planes.
% It requires that all projectagons has the same number of dimensions.
	np = length(phs);
	if(np<2),return; end
	
	% NOTE It it is better to save in an array rather than a cell.
	% However, I do not have time to update all functions now. 
	% Therefore, I convert to cell here temporally	
	arr = [phs{:}]; dims = [arr.dim];
	if(any(dims~=dims(1)))
		error('All projectagons must have the same number of dimension');
	end
	planes = reshape({arr.planes},[],1);
	planes = cell2mat(planes);
	if(size(planes,1)==size(phs{1}.planes,1)*np)
		pp1 = reshape(planes(:,1),[],np); pp1 = unique(pp1','rows')';
		pp2 = reshape(planes(:,2),[],np); pp2 = unique(pp2','rows')';
		if(size(pp1,2)==1 && size(pp2,2)==1) % all phs has the same plane
			return;
		end
	end
	planes = unique(sort(planes,2),'rows');
	for i=1:np
		phs{i} = ph_chplanes(phs{i},planes);
	end
end
