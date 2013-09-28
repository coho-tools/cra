function p = poly_convert(p)
% p = poly_convert(p)
% This function convert the order of a 2D polygon.
% The output is counter-clock wise if the input is clock wise;
% the output is clock wise if the input is counter-clock wise.
p = p(:,(end:-1:1));
