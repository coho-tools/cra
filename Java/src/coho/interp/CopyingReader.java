package coho.interp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.Writer;
/**
 * This reader copy the input to a log file, which is useful for debug
 * @author chaoyan
 *
 */
public class CopyingReader extends Reader {
	LineNumberReader in;
	Writer log;

	public CopyingReader(LineNumberReader _in, Writer _log)
	{ in = _in; log = _log; }
	public CopyingReader(Reader _in, int size, Writer _log) {
		in = new LineNumberReader(_in, size);
		log = _log;
	}
	public CopyingReader(Reader _in, Writer _log) {
		in = new LineNumberReader(_in);
		log = _log;
	}
	public CopyingReader(InputStream in_str, Writer _log) {
		this(new InputStreamReader(in_str), _log);
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
		if(linePos < 0) getLine();
		while(true) {
			if(line == null) return(-1);
			if(linePos < line.length()) return(line.charAt(linePos++));
			else if(linePos == line.length()) {
				linePos++;
				return('\n');
			} else getLine();
		}
	}

	protected void getLine() throws IOException {
		line = in.readLine();
		linePos = 0;
		if(line!=null){
			log.write(line); log.write('\n'); log.flush();
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
