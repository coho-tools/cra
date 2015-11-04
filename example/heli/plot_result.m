cra_open
load data
polys = hulls(:,2);

figure
polys_display(polys)
print -dpng all

figure
polys_display(polys(200:300))
print -dpng circle 

cra_close
