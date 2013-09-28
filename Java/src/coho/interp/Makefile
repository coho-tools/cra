default: parse
parse: Scanner Parser.cup
	java JLex.Main Scanner &&\
	java java_cup.Main -symbols Sym -nonterms -parser Parser \
	  -package coho.interp < Parser.cup


