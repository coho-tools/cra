package coho.interp;
import java.io.*;

import java_cup.runtime.Symbol;
import java.util.concurrent.*;
/**
 * The parser tread. It put each parser tree for a statement 
 * to the queue.
 * @author chaoyan
 *
 */
public class ParserThread extends Parser implements Parse, Runnable {
	private Scanner scanner;      public Scanner scanner() { return(scanner); }
	private Reader in;            public Reader in() { return(in); }
	private Symbol pendingToken;
	private PTfactory pf;         public PTfactory pf() { return(pf); } 
	private SymbolName sname;     public SymbolName sname() { return(sname); }
	//this is not a atom opeartion, so it's not a very good solution
	//public static boolean flag = true;
	private BlockingQueue<PTeval> queue; public BlockingQueue<PTeval> queue(){return queue;}
	
	public ParserThread(Reader _in, PTfactory _pf, BlockingQueue<PTeval> q, SymbolName _sname) {
		super();
		if(_in == null) _in = new InputStreamReader(System.in);
		if(_pf == null) _pf = new PTnodeFactory();
		if(_sname == null) _sname = new SymbolName();
		in = _in;
		pendingToken = null;
		pf = _pf;
		queue = q;
		sname = _sname;
		scanner = new Scanner(in, pf, sname);
	}
	
	public Parse create(Reader in) {
		return(new ParserThread(in, pf,queue, sname));
	}
	
	
	// override parser.scan to use scanner
	public Symbol scan() throws java.io.IOException {
		Symbol s;
		if(pendingToken != null) {
			s = pendingToken;
			pendingToken = null;
		}  else {
			PTnode p;
			p = (PTnode)(scanner.next_token().value);//change after %cup
			s = new Symbol(p.key(), p);
		}
		if(in instanceof PromptingReader)
			((PromptingReader)(in)).setPrompt(null);
		return(s);
	}
	
	// override calc.syntax_error(Symbol) to do nothing.
	//   We'll print the error message ourself when we catch the
	//   Exception thrown by our version of unrecovered_syntax_error().
	public void syntax_error(Symbol cur_token) {
		report_error("Syntax error: "+cur_token.toString(),null);
	}
	
	// override calc.unrecovered_syntax_error(Symbol) to silently throw
	//   an EvalException
	public void unrecovered_syntax_error(Symbol cur_token)throws EvalException {
		PTnode tkn = (PTnode)(cur_token.value);
		if(tkn.key() == Sym.LEX_ERROR)
			throw new EvalException("lexical error: " + tkn.text());    	
		else 
			throw new EvalException("syntax error"+"\n Debug info:"+tkn.toString());
	}
	
	// override error_recovery(boolean) to do nothing (the method in
	//   lr_parser reads three tokens of look-ahead, which messes up
	//   what we want to do in unrecovered_syntax_error().
	protected boolean error_recovery(boolean debug) {
		return(false);  // just fail
	}
	public void report_error(String message, Object info){
		System.out.println("report_error: "+message.toString());
	}
	public void report_fatal_error(String message, Object info){
		report_error(message,info);
		done_parsing();
	}
	
	/* override init_actions() call our version */
	protected void init_actions() { init_actions_2(pf,queue); }
	
	
	public void run(){
		try{
			parse();
			//flag =false;
			//we add a special PTeval instead.
			try{
				queue.put(BasicPTeval.voidPTeval);
			}catch(InterruptedException ee){
				System.err.println("This should never happen: " +
						"put a special void PTnode to the queue");
			}
		}catch(EvalException e){
			BasicFunctions.print_exception(e,"EvalException");
		}catch(Exception e){
			BasicFunctions.print_exception(e,"Unknown ParserThread");
		}finally{
			//System.exit(1);
		}
	}
}
