package coho.interp;
/**
 * A basic implementation for the interface PTnode. It's a node 
 * for the grammer tree.
 * @author chaoyan
 *
 */
public class BasicPTnode implements PTnode {
	private boolean isTerminal;
	private int key;
	private String text;
	private PTnode[] children;
	private SymbolName sname;

	public boolean isTerminal() { return(isTerminal); }
	public int key() { return(key); }
	public String text() throws IllegalArgumentException {
		if(isTerminal) return(text);
		else throw new IllegalArgumentException(
				"text() invoked on non-terminal PTnode");
	}
	public PTnode child(int i) throws IllegalArgumentException {
		if(!isTerminal) return(children[i]);
		else throw new IllegalArgumentException(
				"child() invoked on terminal PTnode");
	}
	public int n_children() throws IllegalArgumentException {
		if(!isTerminal) return(children.length);
		else throw new IllegalArgumentException(
				"n_children() invoked on terminal PTnode");
	}

	/**
	 * create a new terminal node
	 * @param k
	 * @param t
	 * @param s
	 */
	public BasicPTnode(int k, String t, SymbolName s) {
		isTerminal = true;
		key = k;
		text = t;
		sname = s;
	}

	/**
	 * create a new non-terminal node
	 * @param k
	 * @param c
	 * @param s
	 */
	public BasicPTnode(int k, PTnode[] c, SymbolName s) {
		isTerminal = false;
		key = k;
		children = c;
		sname = s;
	}

	public String toString(int indent) {
		String myName = isTerminal() ? sname.terminalName(key())
				: sname.nonterminalName(key());
		if(indent < 0) return(myName);
		StringBuffer buf = new StringBuffer();
		for(int i = 0; i < indent; i++)
			buf.append(" ");
		buf.append(myName);
		if(isTerminal) {
			buf.append(" ").append(text);
			buf.append("\n");
		} else {
			buf.append("\n");
			for(int i = 0; i < n_children(); i++)
				buf.append(child(i).toString(indent+2));
		}
		return(buf.toString());
	}
	public String toString() { return(toString(-1)); }

//	// we add the special PTnode to indicate the end of ParserThread
//	public static boolean isVoidPTnode(BasicPTnode p){
//	return (p==voidPTnode);
//	}
//	public static BasicPTnode voidPTnode = new BasicPTnode(-1, (PTnode[])null,(SymbolName)null);
}
