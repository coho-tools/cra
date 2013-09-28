package coho.interp;
//import java.lang.reflect.Constructor;
/**
 * A basic factory to create PTnode
 * @author chaoyan
 *
 */
public class PTnodeFactory implements PTfactory {
	private SymbolName symbols;
//	private Constructor[] constructTerminal;
//	private Constructor[] constructNonterminal;

	public PTnodeFactory(SymbolName s) {
		if(s == null) s = new SymbolName();
		symbols = s;
//		constructTerminal = new Constructor[symbols.n_terminals()];
//		constructNonterminal = new Constructor[symbols.n_nonterminals()];
	}
	public PTnodeFactory() { this(null); }

	public PTnode create(int key, String text) {
		return(new BasicPTnode(key, text, symbols));
	}
	public PTnode create(int key, PTnode[] c) {
		return(new BasicPTnode(key, c, symbols));
	}
}
