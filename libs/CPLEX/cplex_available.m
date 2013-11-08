function isa = cplex_available
% isa = cplex_available
% This function detects if the cplex license avaliable or not.

% detects if $ILOG_LICENSE_FILE variable set 
[status,license] = unix('echo $ILOG_LICENSE_FILE'); 
license=license(1:end-1); 
isa = ~isempty(license); 
