function bbox = phs_box(phs)
% union of all bboxes
  assert(length(phs)>=1);
  bbox = phs{1}.bbox;
  for i=2:length(phs)
    b = phs{i}.bbox;
    bbox(:,1) = min(bbox(:,1),b(:,1)); 
    bbox(:,2) = max(bbox(:,2),b(:,2)); 
  end 
end
