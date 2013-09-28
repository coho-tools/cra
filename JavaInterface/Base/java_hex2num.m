function text = java_hex2num(text, hexMarker)
% text = java_hex2num(text, hexMarker)
% This function convert hex string to num string
%
% replace $abcdef976543210 with hex2num('abcdef976543210');
% the bin hex representation is expected to contain exactly 16 chars;

if nargin < 2
    hexMarker = '$';
end

while 1
    hexMarkerPos = findstr(text, hexMarker);
    if isempty(hexMarkerPos)
        break;
    end
    before = text(1:hexMarkerPos-1);
    numberAsHex = text(hexMarkerPos+1:hexMarkerPos+16);
    after = text(hexMarkerPos+17:end);
    text = [ before 'hex2num(''' numberAsHex ''')' after ];
end
