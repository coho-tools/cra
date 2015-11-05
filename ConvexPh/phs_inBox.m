function isc = phs_inBox(phs,bbox)
  isc = zeros(length(phs),1);
  for i=1:length(phs)
    isc(i) = bbox_contain(bbox,phs{i}.bbox);
  end
end
