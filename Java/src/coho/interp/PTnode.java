package coho.interp;
/**
 * The node for the grammer tree.
 * @author chaoyan
 *
 */
public interface PTnode {
	public boolean isTerminal();	
	public int key();
	public String text() throws IllegalArgumentException;
	public PTnode child(int i) throws IllegalArgumentException;
	public int n_children() throws IllegalArgumentException;
	public String toString(int indent);
	public String toString();
}
