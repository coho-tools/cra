package coho.interp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
/**
 * A reader which output prompt to err when there is error.
 * @author chaoyan
 *
 */
public class PromptingReader extends Reader {
	LineNumberReader in;
	String prompt;

	public PromptingReader(LineNumberReader _in) { in = _in; }
	public PromptingReader(Reader _in, int size) {
		in = new LineNumberReader(_in, size);
	}
	public PromptingReader(Reader _in) {
		in = new LineNumberReader(_in);
	}
	public PromptingReader(InputStream in_str) {
		this(new InputStreamReader(in_str));
	}

	public PromptingReader setPrompt(String p) {
		prompt = p;
		return(this);
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
			System.out.flush();
			if(prompt != null) System.err.print(prompt);
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
				if(prompt != null) System.err.print(prompt);
				line = in.readLine();
				linePos = 0;
			}
		}
	}

	public int read(char[] cbuf, int off, int len) throws IOException {
		for(int i = 0; i < len; i++) {
			int c = read();
			if(c == -1) {
				if(i == 0) return(-1);
				else return(i);
			} else {
				cbuf[i] = (char)c;
				if(c == '\n') return(i+1);
			}
		}
		return(len);
	}
}
