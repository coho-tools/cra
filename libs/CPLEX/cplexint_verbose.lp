\Problem name: cplexint_problem

Minimize
 obj: - x1 - 2 x2 - 3 x3 - x4
Subject To
 c1: - x1 + x2 + x3 + 10 x4 <= 20
 c2: x1 - 3 x2 + x3 <= 30
 c3: x2 - 3.5 x4  = 0
Bounds
 0 <= x1 <= 40
      x2 Free
      x3 Free
 0 <= x4 <= 3
Binaries
 x4 
End
