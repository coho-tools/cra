package coho.interp;

import java.util.Enumeration;
import java.util.TreeMap;

public class PTevalFactory implements PTfactory {
	private PTfactory pf;
	private TreeMap<Object, Evaluator> evalMap;

	public PTevalFactory(PTfactory _pf) {
		pf = (_pf != null) ? _pf : (new PTnodeFactory());
		evalMap = new TreeMap<Object, Evaluator>();
	}
	public PTevalFactory(SymbolName symbols) { this(new PTnodeFactory(symbols)); }
	public PTevalFactory() { this(new SymbolName()); }

	//implementation for the PTnode interface. create a node with its value
	public PTeval create(int key, String text) {
		PTnode nd = pf.create(key, text);
		return(new BasicPTeval(nd, eval(nd)));
	}
	public PTeval create(int key, PTnode[] c) {
		PTnode nd = pf.create(key, c);
		return(new BasicPTeval(nd, eval(nd)));
	}

	// rules for the producation of evaluator?
	// according to this rule, we can map PTnode to production and find the approperate evaluator
	// each evaluator's production should have the same form,
	// otherwise we can't find it.
	public static String production(PTnode nd) {
		StringBuffer buf = new StringBuffer();
		buf.append(nd.toString());
		if(!(nd.isTerminal())) {
			buf.append(':');
			for(int i = 0; i < nd.n_children(); i++) {
				buf.append(' ');
				buf.append(nd.child(i).toString());
			}
		}
		return(buf.toString());
	}
	//get the evaluator by the node's production
	protected Evaluator eval(PTnode nd) {
		return((Evaluator)(evalMap.get(production(nd))));
	}

	//build the tree map
	//map the evaluator with its production
	public PTevalFactory nodeOperations(Enumeration ops) {
		while(ops.hasMoreElements()) {
			Evaluator ev = (Evaluator)(ops.nextElement());
			Object p = ev.production();
			if(p instanceof String) evalMap.put(p, ev);
			else {
				String[] pp = (String[])p;
				for(int i = 0; i < pp.length; i++)
					evalMap.put(pp[i], ev);
			}
		}
		return(this);
	}
}
