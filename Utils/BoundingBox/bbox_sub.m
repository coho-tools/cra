function bbox = bbox_sub(bbox1,bbox2)
	bbox = bbox1 - bbox2(:,[2,1]);
