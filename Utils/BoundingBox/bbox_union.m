function bbox = bbox_union(bbox1,bbox2)
	bbox = [min(bbox1(:,1),bbox2(:,1)), max(bbox1(:,2),bbox(:,2))];
