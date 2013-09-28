function bbox = bbox_intersect(bbox1,bbox2)
	bbox = [max(bbox1(:,1),bbox2(:,1)), min(bbox1(:,2),bbox(:,2))];
