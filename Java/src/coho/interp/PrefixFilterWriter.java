/**
 * 
 */
package coho.interp;

import java.io.*;

/**
 * Maltab after 7.01 ignores the first two characters when reading from fifo. 
 * We add two extra white space before each line.
 * Replace "\n" with "\n  " 
 * Matlab may fixed the bug, therefore, it is not necessary for some versions.
 * @author chaoyan
 */
public class PrefixFilterWriter extends FilterWriter {
	public static String prefix = "  ";
	private Writer w;

	/**
	 * @param out
	 */
	public PrefixFilterWriter(Writer out) {
		super(out);
		w = out;
	}
	
	@Override
	public void write(int c)throws IOException{
		w.write(c);
		if(c=='\n'){
			w.write(prefix);
		}else{
			//
		}
	}
	
	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		for(int i = 0; (i < len) && ((off+i) < cbuf.length); i++)
			write(cbuf[off+i]);
	}
	
	@Override
	public void write(String s, int off, int len) throws IOException {
		for(int i = 0; (i < len) && ((off+i) < s.length()); i++)
			write(s.charAt(off+i));
	}
	
	@Override
	public void write(String s) throws IOException { 
		write(s, 0, s.length()); 
	}
	
	@Override
	public void write(char[] cbuf) throws IOException {
		write(cbuf, 0, cbuf.length);
	}
	
	@Override
	public void flush() throws IOException {
		w.flush();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
