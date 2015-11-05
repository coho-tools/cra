function isc = bbox_contain(bbox1,bbox2)
  isc = all(bbox1(:,1)<=bbox2(:,1)) & all(bbox1(:,2)>=bbox2(:,2));
