package coho.interp;

/**
 * An interface for factory to create PTnode, including terminal node and 
 * non-terminal node.
 * @author chaoyan
 */
public interface PTfactory {
	public PTnode create(int key, String text);  // for terminals
	public PTnode create(int key, PTnode[] c);   // for non-terminals
}
