function ise =  ph_isempty(ph)
% ise =  ph_isempty(ph)
% This function returns true if ph = []
% Note: ph should be canonical. Otherwise, the function may return true even the
%  	feasible region of ph is empty. 
ise = isempty(ph);
