package coho.interp;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
/**
 * The class for the evaluation process. Including how to evaluate throw the grammer tree.
 * The parser build the grammer three from the input, however, this class evaluation the grammer tree.
 * For functions, fMap record the map functions with its corresponding evaluation process
 * We store ident name and its value with Context class.
 * @author chaoyan
 *
 */
public class Eval {
  private static Map<String,Function> fMap = null;  // the map of evaluation method and functions
  private static boolean debug = false;
  
  //set the default value for fMap
  public static void init() {
    fMap = new TreeMap<String,Function>();
    //add function such as print load and etc
    addFunctions(BasicFunctions.functions());
    //add basic arithmetic functions, such as add, sub and etc.
    addFunctions(BasicValue.functions());
    //add functions defined in this class: show_functions show_variables
    addFunctions(Eval.functions());
    //add matrix functions, such as create matrix.
    addFunctions(MatrixValue.matrixFns());
    //? how to call lp solver?
    addFunctions(LPstuff.functions());
    // polygon functions
    addFunctions(PolygonValue.pgonFns());
  }
  public static void noDefaultFunctions() {
  	fMap = new TreeMap<String,Function>();
  }
  // add a new value for fMap
  public static void addFunctions(Enumeration fns) {
  	if(fMap == null) init();
    while(fns.hasMoreElements()) {
      Function f = (Function)(fns.nextElement());
      fMap.put(f.name(), f);
    }
  }
  
  public static boolean setDebug(boolean newDebug) {
  	boolean oldDebug = debug;
  	debug = newDebug;
  	return(oldDebug);
  }
  

  /**
   * define evaluators for nodes of grammer tree.
   * @return
   */
  public static Enumeration nodeOperations() {
	  final Evaluator[] actions = new Evaluator[] {
	    // methods for tokens
	    new Evaluator() {
	      public Object production() { return("PLAIN_DOUBLE"); }
	      public Value eval(PTnode nd) throws EvalException {
	      	//Double d = new Double(nd.text());
	        return(ValueFactory.create(new Double(nd.text())));
	    } },
	    new Evaluator() {
	      public Object production() { return("HEX_DOUBLE"); }
	      public Value eval(PTnode nd) throws EvalException {
	        String s = nd.text().substring(1);
	        long bits;
	        if(s.length() < 16) bits = Long.parseLong(s, 16);
	        else bits =   (Long.parseLong(s.substring(0,8), 16) << 32)
	                    | (Long.parseLong(s.substring(8,16), 16));
	        return(ValueFactory.create(new Double(Double.longBitsToDouble(bits))));
	    } },
	    new Evaluator() {
	      public Object production() { return("IDENT"); }
	      public Value eval(PTnode nd) throws EvalException {
	        return(ValueFactory.create(nd.text()));
	    } },
	    new Evaluator() {
	      public Object production() { return("STRING"); }
	      public Value eval(PTnode nd) throws EvalException {
	        return(ValueFactory.create(grindString(nd.text())));
	    } },
	    new Evaluator() {								//remove statement:end
	      public Object production() {
	        return(new String[] {
	          "statement: assignment SEMI",
	          "statement: expr SEMI"}); }
	      public Value eval(PTnode nd) throws EvalException {
	        _eval(nd, 0);
	        return(null);
	    } },
	    new Evaluator() {
	      public Object production() { return("assignment: IDENT ASSIGN expr"); }
	      public Value eval(PTnode nd) throws EvalException {
	        Context.put(_eval(nd, 0).toString(), _eval(nd, 2));
	        return(null);
	    } },
	    new Evaluator() {
	      public Object production() {
	        return(new String[] {
	          "expr: PLAIN_DOUBLE", "expr: HEX_DOUBLE", "expr: STRING"}); }
	      public Value eval(PTnode nd) throws EvalException {
	        return(_eval(nd, 0));
	    } },
	    new Evaluator() {
	      public Object production() { return("expr: IDENT"); }
	      public Value eval(PTnode nd) throws EvalException {
	        String id = _eval(nd, 0).toString();
	        Value v = Context.get(id);
	        if(v == null)
	          throw new EvalException("uninitialized variable -- " + id);
	        return(v);
	    } },
	    new Evaluator() {
	      public Object production() { return("expr: IDENT LPAREN row RPAREN"); }
	      public Value eval(PTnode nd) throws EvalException {
	        String id = _eval(nd, 0).toString();
	        Function f = (Function)(fMap.get(id));
	        if(f == null)
	          throw new EvalException("undefined function -- " + id);
	        return(f.eval((RCvalue)(_eval(nd, 2))));
	    } },
	    new Evaluator() {
	      public Object production() {
	        return(new String[] {
	          "expr: LPAREN expr RPAREN",
	          "expr: LBRACKET row RBRACKET",
	          "expr: LBRACKET col RBRACKET"
	      });}
	      public Value eval(PTnode nd) throws EvalException {
	        return(_eval(nd, 1));
	    } },
	    new Evaluator() {
	      public Object production() { return("expr: MINUS expr"); }
	      public Value eval(PTnode nd) throws EvalException {
	        return(_eval(nd, 1).negate());
	    } },
	    new Evaluator() {
	      public Object production() { return("row:"); }
	      public Value eval(PTnode nd) throws EvalException {
	        return(ValueFactory.create(new Value[0], nd.toString()));
	    } },
	    new Evaluator() {
	      public Object production() { return(new String[] {
	        "row: expr", "rowTail: expr", "colTail: expr"}); }
	      public Value eval(PTnode nd) throws EvalException {
	        return(ValueFactory.create(new Value[] {_eval(nd, 0)}, nd.toString()));
	    } },
	    new Evaluator() {
	      public Object production() { return(new String[] {
		"row: expr COMMA rowTail", "rowTail: expr COMMA rowTail",
	        "col: expr SEMI colTail",  "colTail: expr SEMI colTail" }); }
	      public Value eval(PTnode nd) throws EvalException {
	        return(ValueFactory.create(new Value[] {_eval(nd, 0), _eval(nd, 2)},
	                         nd.toString()));
	    } }
	  }; // Evaluator[] actions = ...;
		return(new Enumeration() {
			private int i = 0;
			public boolean hasMoreElements() { return(i < actions.length); }
			public Object nextElement() { return(actions[i++]); }});
}



  protected static Value _eval(PTnode nd, int i) throws EvalException {
    PTnode child = nd.child(i);
    if(debug)
      System.out.println("{evaluating -- " + PTevalFactory.production(child));
    Value v = ((Evaluator)(child)).eval(child);
    if(debug){ 
    	if(v!=null)
    		System.out.println("got " + v.toString() + "}");
    	else
    		System.out.println("null value");
    }
    return(v);
  }


  protected static String grindString(String s0) {
    // strip the enclosing quotes and handle backslash conversions.
    //   (Matlab backslash conversions defined by 'help sprintf').
    String s1 = s0.substring(1, s0.length()-1);
    StringBuffer buf = new StringBuffer();
    for(int i = 0; i < s1.length(); i++) {
      char c = s1.charAt(i);
      if(c == '\\') {
        c = s1.charAt(++i);
        switch(c) {
          case 'n': c = '\n'; break;
          case 'r': c = '\r'; break;
          case 't': c = '\t'; break;
          case 'b': c = '\b'; break;
          case 'f': c = '\f'; break;
          default: break;
        }
      }
      buf.append(c);
    }
    return(buf.toString());
  }
  // some function to show functions and variables of the input.
  public static Enumeration functions() {
	  final Function[] functions = new Function[] {
	    new Function() {
	      public String name() { return("show_functions"); }
	      public Value eval(RCvalue args) throws EvalException {
	        StringBuffer buf = new StringBuffer();
	        Iterator it = fMap.keySet().iterator();
	        while(it.hasNext()) {
	          String fname = (String)(it.next());
	          buf.append(fname);
	          for(int i = fname.length(); i < 20; i++) buf.append(' ');
	          buf.append("  " + fMap.get(fname).getClass().getName() + "\n");
	        }
	        return(ValueFactory.create(buf.toString()));
	      }
	    },
	    new Function() {
	      public String name() { return("show_variables"); }
	      public Value eval(RCvalue args) throws EvalException {
	        StringBuffer buf = new StringBuffer();
	        Iterator it = Context.iterator();
	        while(it.hasNext()) {
	          String vname = (String)(it.next());
	          buf.append(vname);
	          for(int i = vname.length(); i < 20; i++) buf.append(' ');
	          buf.append("  " + Context.get(vname).typeName() + "\n");
	        }
	        return(ValueFactory.create(buf.toString()));
	      }
	    }
	  };
		return(new Enumeration() {
			private int i = 0;
			public boolean hasMoreElements() { return(i < functions.length); }
			public Object nextElement() { return(functions[i++]); }});
  }

}
