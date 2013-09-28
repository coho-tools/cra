package coho.interp;
/**
 * A basic implematation for PTeval. 
 * A node for grammer tree with evaluation method.
 * @author chaoyan
 *
 */
//it's just a combination of PTnode and Evaluator
public class BasicPTeval implements PTeval {
	private PTnode nd;	//node of the grammer tree
	private Evaluator ev;	//evaluator for this node

	// implement the PTnode interface
	public boolean isTerminal() { return(nd.isTerminal()); }
	public int key() { return(nd.key()); }
	public String text() throws IllegalArgumentException { return(nd.text()); }
	public PTnode child(int i) throws IllegalArgumentException
	{ return(nd.child(i)); }
	public int n_children() throws IllegalArgumentException
	{ return(nd.n_children()); }
	public String toString(int indent) { return(nd.toString(indent)); }
	public String toString() { return(nd.toString()); }

	// implement the Evaluator interface
	public Object production() { return(ev.production()); }
	public Value eval(PTnode nd) throws EvalException {
		if(ev != null) return(ev.eval(nd));
		else return(null);
	}

	// constructor
	public BasicPTeval(PTnode _nd, Evaluator _ev) { nd = _nd; ev = _ev; }
	//we add the special PTnode to indicate the end of ParserThread
	public static BasicPTeval voidPTeval = new BasicPTeval(null,null);
	public static boolean isVoidPTeval(PTeval p){return p==voidPTeval;}
}
