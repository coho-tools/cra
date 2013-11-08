package coho.interp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
//this class is not used. See PromptingReader instead
public class LineReader extends Reader {
	LineNumberReader in;

	public LineReader(Reader _in, int size) {
		in = new LineNumberReader(_in, size);
	}
	public LineReader(Reader _in) { in = new LineNumberReader(_in); }
	public LineReader(InputStream in_str) {
		this(new InputStreamReader(in_str));
	}

	String line = "";
	int linePos = -1;

	// operations that we forward to 'in'
	public void close() throws IOException { in.close(); }
	public void mark(int limit) throws IOException { in.mark(limit); }
	public boolean markSupported() { return(in.markSupported()); }
	public boolean ready() throws IOException {
		return((linePos < line.length()) || in.ready());
	}
	public void reset() throws IOException { in.reset(); }
	public long skip(long n) throws IOException { return(in.skip(n)); }
	public int getLineNumber() { return(in.getLineNumber()); }
	public void setLineNumber(int lineNumber) { in.setLineNumber(lineNumber); }

	// implement read
	public int read() throws IOException {
		if(linePos < 0) {
			line = in.readLine();
			linePos = 0;
		}
		while(true) {
			if(line == null) return(-1);
			if(linePos < line.length()) return(line.charAt(linePos++));
			else if(linePos == line.length()) {
				linePos++;
				return('\n');
			} else {
				line = in.readLine();
				linePos = 0;
			}
		}
	}

	public int read(char[] cbuf, int off, int len) throws IOException {
		boolean onlyOneMore = false;
		for(int i = 0; i < len; i++) {
			int c = read();
			if(c == -1) {
				if(i == 0) return(-1);
				else return(i);
			} else {
				cbuf[i] = (char)c;
				if((c == '\n') || onlyOneMore) return(i+1);
				onlyOneMore = c == ';';
			}
		}
		return(len);
	}

	public boolean endOfLine(String white) {
		if((line == null) || (linePos < 0)) return(true);
		int i = linePos;
		while((i < line.length()) && (white.indexOf(line.charAt(i)) >= 0))
			i++;
		return(i >= line.length());
	}
	public boolean endOfLine() {
		if((line == null) || (linePos < 0)) return(true);
		int i = linePos;
		while((i < line.length()) && Character.isWhitespace(line.charAt(i))) {
			i++;
		}
		return(i >= line.length());
	}
}
