function [valid,ph] = ph_verify(ph)
opt = ph.fwd.opt;
tol = opt.tol;
bloatAmt = ph.fwd.bloatAmt;
[realBloatAmt,ph] = ph_realBloatAmt(ph);
%NOTE realBloatAmt might be zero when object='ph' and ph is shrinking
%assert(~all(realBloatAmt(:)==0));
valid = all(realBloatAmt(:)<=(1+tol)*bloatAmt(:));
