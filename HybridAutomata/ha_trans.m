function trans = ha_trans(src,gate,tgt,resetMap)
% trans = ha_trans(src,gate,tgt,resetMap)
% The function creats directed link between states. 
%   The computation result from source states are used to computate initial 
%   regions of target state.  
% Parameters: 
%   src/tgt: name of source/target states
%   gate:    each constraints of the state invariant defines a "gate". 
%            when gate=0, all reachable regions in the source state are used 
%              as the initial regions of the target state 
%            when gate=i, the intersection of reachable regions and i^th gate 
%              are used as the initial regions of the target state
%   resetMap: User provided function to change the target initial regions. 
%            It's of the form of
%            initPh = resetMap(initPh);

if(nargin<3), error('not enough parameters'); end
if(nargin<4), resetMap = []; end

if(isempty(src)||~ischar(src)), error('src must be a non-empty string'); end
if(isempty(tgt)||~ischar(tgt)), error('tgt must be a non-empty string'); end
if(isempty(gate)||~isnumeric(gate)||gate<0) %NOTE: Gate 0 is supported.
	error('gate must be a non-negative integer');
end
if(~isempty(resetMap) && ~isa(resetMap,'function_handle'))
	error('resetMap must be a function'); 
end

% gate is a positive integer
gate = uint32(gate);
% src and tgt are case insensitive
src = lower(src); 
tgt = lower(tgt);

trans = struct('src',src,'gate',gate,'tgt',tgt,'resetMap',resetMap);
