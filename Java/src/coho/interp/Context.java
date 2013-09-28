/*
 * Created on 28-Jun-2004
 */
package coho.interp;

import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;

/**
 * This class provides the mapping from variable names to storage locations.
 * Presently, the interpreter has a single context, so we just provide a
 * TreeMap.  If functions are added in the future, we could add operations
 * for pushing and popping contexts, etc.
 * @author Mark Greenstreet (mrg@cs.ubc.ca)
 */
//it's just a Map, we can provide a similar class for {function name, function} pair.
public class Context {
	private static Map<String, Value> vMap = new TreeMap<String, Value>();
	public static void put(String key, Value v) {
		vMap.put(key, v);
	}
	public static Value get(Object key) {
		return((Value)(vMap.get(key)));
	}
	public static Iterator iterator() {
		return(vMap.keySet().iterator());
	}
}
