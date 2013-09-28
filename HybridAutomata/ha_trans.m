function trans = ha_trans(head,gate,tail,transAct)
if(nargin<3), error('not enough parameters'); end
if(nargin<4), transAct = []; end

if(isempty(head)||~ischar(head))
	error(' head must be a non-empty string');
end
if(isempty(tail)||~ischar(tail))
	error(' tail must be a non-empty string');
end
if(isempty(gate)||~isnumeric(gate)||gate<1)
	error(' gate must be a positive integer');
end


% gate is a positive integer
gate = uint32(gate);
% head and tail are case insensitive
head = lower(head); 
tail = lower(tail);

trans = struct('head',head,'gate',gate,'tail',tail,'transAct',transAct);
