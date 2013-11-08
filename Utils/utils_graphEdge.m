function inds = utils_graphEdge(edges,nodes,dir)
% inds = utils_graphEdge(edges,nodes,dir)
% This function finds all edges from/to nodes
% 	all edges from nodes if dir='outgoing' 
% 	all edges to nodes if dir='incomming'
% 	all edges from/to nodes if dir='all'
nodes = reshape(nodes,1,[]); 
ne = size(edges,1); nn = length(nodes);
out = any( repmat(edges(:,1),1,nn) == repmat(nodes,ne,1), 2 ); % outgoing edges of nodes
in  = any( repmat(edges(:,2),1,nn) == repmat(nodes,ne,1), 2 ); % incomming edges of nodes
switch(lower(dir))
	case 'all'
		inds = in | out;
	case 'incoming'
		inds = in;
	case 'outgoing'
		inds = out;
	otherwise
		error('unknown options');
end
inds = find(inds);
