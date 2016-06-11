function text = java_hex2num(text, hexMarker)
% text = java_hex2num(text, hexMarker)
% This function convert hex string to num string
%
% replace $abcdef976543210 with hex2num('abcdef976543210');
% the bin hex representation is expected to contain exactly 16 chars;

if nargin < 2
    hexMarker = '\$';
end

regex = [hexMarker '([0-9a-f]+)'];
rep = 'fasthex2num(''$1'')';
text = regexprep(text, regex, rep);
