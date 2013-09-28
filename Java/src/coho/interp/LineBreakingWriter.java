package coho.interp;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

public class LineBreakingWriter extends FilterWriter {
	private Writer w;
	private int lineLimit, linePos;
	private boolean noBreak, closed;
	private StringBuffer buf;


	public LineBreakingWriter(Writer _w, int _limit) {
		super(_w);
		w = _w;
		lineLimit = _limit;
		linePos = 0;
		noBreak = false;
		closed = false;
		buf = new StringBuffer();
	}


	protected void doBreak() throws IOException {
		if((linePos + buf.length()) > lineLimit) {
			w.write('\n');
			linePos = 0;
		}
	}


	public void write(char c)  throws IOException {
		if(closed) throw new IOException("attempt to write on closed Writer");
		if(Character.isWhitespace(c)) {
			if(buf.length() > 0) {
				if(!noBreak) doBreak();
				w.write(buf.toString());
				linePos += buf.length();
				buf.setLength(0);
			}
			if(c == '\n') linePos = 0;
			else {
				doBreak();
				linePos++;
			}
			w.write(c);
			noBreak = false;
		} else buf.append(c);
	}

	public void write(char[] cbuf, int off, int len) throws IOException {
		if(closed) throw new IOException("attempt to write on closed Writer");
		for(int i = 0; (i < len) && ((off+i) < cbuf.length); i++)
			write(cbuf[off+i]);
	}

	public void write(char[] cbuf) throws IOException {
		write(cbuf, 0, cbuf.length);
	}

	public void write(String s, int off, int len) throws IOException {
		for(int i = 0; (i < len) && ((off+i) < s.length()); i++)
			write(s.charAt(off+i));
	}

	public void write(String s) throws IOException { write(s, 0, s.length()); }


	public void flush() throws IOException {
		if(buf.length() > 0) {
			if(!noBreak) doBreak();
			w.write(buf.toString());
			linePos += buf.length();
			buf.setLength(0);
			noBreak = true;
		}
		w.flush();
	}
}
