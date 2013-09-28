function val = utils_hashtable(op,index,data)
% val = utils_hashtable(op,index,data)
% This function implmentes a simple hastable indexed by string.
% It support operations including 
% 	data = utils_hashtable('get',index);	
% 		get the data for given index, return [] if not found
% 	isc  = utils_hashtable('contain',index): 	
% 		check if the data for an index is available or not
% 	ise  = utils_hashtable('isempty'):	
% 		check if the hastable is empty or not
% 	len  = utils_hashtable('length'): 	
% 		the length of the hastable
% 	flag = utils_hashtable('insert',index,data): 
% 		insert or update the data of an index. 
% 		flag=0 if insert; =1 if update 	
%	data = utils_hashtable('delete',index): 	
% 		delete data for the given index. return [] if not found
% 	[] 	 = utils_hashtable('destroy'): 		
% 		remove all data

if(nargin<2), index = []; end
if(nargin<3), data = []; end

persistent HASH_DATA HASH_IND; % store data and string indices

val = [];
id = utils_strs2ids(index,HASH_IND); % find index by string
switch(lower(op))
	case {'get'}
		if(id>0)
			val = HASH_DATA{id};
		end
	case {'contain'}
		val = id>0;
	case {'isempty'}
		val = isempty(HASH_IND);
	case {'length'}
		val = length(HASH_IND);	
	case {'insert'}
		if(id>0) 
			val = 1; % update
		else
			id = length(HASH_IND)+1;
			val = 0; % insert
		end
		HASH_IND{id} = index;
		HASH_DATA{id} = data;
	case {'delete'}
		if(id>0)
			val = HASH_DATA{id};
			HASH_IND{id} = [];
			HASH_DATA{id} = [];
		end
	case {'destroy'}
		HASH_IND=cell(0,1);
		HASH_DATA=cell(0,1);
	otherwise
		error('unknown operator');
end
