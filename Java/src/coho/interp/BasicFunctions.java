package coho.interp;

import java.io.*;
import java.util.Enumeration;
import java.util.StringTokenizer;

import coho.debug.STAT;
import coho.geom.twodim.ops.*;

/**
 * Some basic functions to evaluate functions, including 'print' 'println'
 * 'exit' and 'load'.
 * @author chaoyan
 *
 */
public class BasicFunctions {
	//NOTE matlab >7.01 will eat up the first two character of each line. add two white spaces here.
	//for log file, we don't need that.
	// enable it later if the bug happens again.
	//private static Writer w = new PrefixFilterWriter(new OutputStreamWriter(System.out));
	private static Writer w = new OutputStreamWriter(System.out);
	private static Parse p = null;
	private static Writer logW = null; //the output also copy to log file
	public static void setLogWriter(FileOutputStream fos){
		logW = new OutputStreamWriter(fos);
	}

	public static void setParser(Parse _p) { p = _p; }

	public static Enumeration functions() {
		return(new Enumeration() {
			private int i = 0;
			public boolean hasMoreElements() { return(i < functions.length); }
			public Object nextElement() { return(functions[i++]); }});
	}

	private static Function[] functions = new Function[] {
		new Function() {
			public String name() { return("print"); }
			public Value eval(RCvalue args) throws EvalException {
				do_print(args, "");
				return(VoidValue.instance());
			}
		},
		new Function() {
			public String name() { return("println"); }
			public Value eval(RCvalue args) throws EvalException {
				do_print(args, "\n");
				return(VoidValue.instance());
			}
		},
		new Function() {
			public String name() { return("exit"); }
			public Value eval(RCvalue args) throws EvalException {
				if(STAT.stat)STAT.outStat();//XXX for stat
				if(args.size() == 0) System.exit(0);
				else if(args.size() == 1)
					System.exit(BasicArgs.int_arg(args, 0, name()));
				else throw new EvalException(name() +
						": must be invoked with no arguments, or with one" +
				" argument that is a double.");
				return(VoidValue.instance());
			}
		},
		new Function(){
			public String name(){return("step");}
			public Value eval(RCvalue args)throws EvalException{
				if(args.size()!=1)
					throw new EvalException(name()+" step must be invoked with one int arguments");
				int step = BasicArgs.int_arg(args,0,name());
				//TODO: do nothing when running(it possible distrub the system.out); only for stat, use the log file to generate the data again. 
				if(STAT.stat) STAT.outStepStat(step);
				return (VoidValue.instance());
			}
		},
		new Function(){
			public String name(){return("set");}
			public Value eval(RCvalue args)throws EvalException{
				if(args.size()!=2)
					throw new EvalException(name()+"set must be invoked with two arguments");
				int key = BasicArgs.int_arg(args, 0, name());
				double value = BasicArgs.double_arg(args, 1, name());
				switch(key){
				case 1:
					OutwardReduce.maxNewEdgeLen = value;
					break;
				case 2:
					OutwardReduce.maxNewPointDist = value; break;
				default:
					throw new EvalException(name()+"set function does not support the key "+key);
				}
				return (VoidValue.instance());
			}
		},
		new Function(){
			public String name(){return("get");}
			public Value eval(RCvalue args)throws EvalException{
				if(args.size()!=1)
					throw new EvalException(name()+"get must be invoked with one arguments");
				int key = BasicArgs.int_arg(args, 0, name());
				switch(key){
				case 1:
					return new DoubleValue(OutwardReduce.maxNewEdgeLen); 
				case 2:
					return new DoubleValue(OutwardReduce.maxNewPointDist); 
				default:
					throw new EvalException(name()+"set function does not support the key "+key);
				}
			}
		},
		new Function() {
			public String name() { return("load"); }
			public Value eval(RCvalue args) throws EvalException {
				String fname = BasicArgs.string_arg(args, 0, name());
				FileReader rd;
				try { rd = new FileReader(fname); }
				catch (IOException e) {
					throw new EvalException("could not open " + fname + " to read.");
				}
				Parse oldp = p;
				if(oldp == null)
					throw new EvalException("INTERNAL ERROR: BasicFunctions.load -- can't find the parser.");
				p = oldp.create(rd);

				try {
					while(true) {
						PTeval pt = (PTeval)(p.parse().value);
						pt.eval(pt);
					}
				} catch (EvalEndException _end) {
//				} catch (EvalException e) {
//					System.err.println("Error in " + fname + ":");
//					System.err.println("  " + e.getMessage());
				} catch (Exception e) {
					StackTraceElement s[] = e.getStackTrace();
					String stack = "";
					for(int i = 0; i < s.length; i++)
						stack = stack + "\n  " + s[i];
					throw new RuntimeException("INTERNAL ERROR in BasicFunctions.eval:  " + e + "\n  " + stack);
				}
				p = oldp; // restore the parser
				return(VoidValue.instance());
			}
		}
	};
	protected static void print_exception(Exception e, String type){
		print_exception(e,type,"% ");
	}
	protected static void print_exception(Exception e, String type, String prefix){
		String err = "\n"+prefix+"EXCEPTION("+type+"): "+e.toString()+"\n";
		StackTraceElement s[] = e.getStackTrace();
		for (int i=0; i<s.length; i++){
			err = err+prefix+s[i]+"\n";
		}
		err = err+prefix+"END EXCEPTION"+"\n";
		try{
			w.write(err); w.flush();
			if(logW!=null) 
				logW.write(err); logW.flush();
		}catch(IOException ee){
			// send to std err
			System.err.print(err);
		}
	}
	protected static void do_print(RCvalue args, String $) throws EvalException {
		try {
			if(args.size() > 0) {
				int lineLimit = -1, j = 0;
				Value[] opt = new Value[args.size() - 1];
				for(int i = 1; i < args.size(); i++){
					Value v = args.value(i);
					if(v instanceof StringValue) {
						StringTokenizer tokens = new StringTokenizer(v.toString());
						if( tokens.hasMoreTokens() && (tokens.nextToken().compareTo("line") == 0)
							&& tokens.hasMoreTokens()) { // set lineLimit
							try {
								int m = Integer.parseInt(tokens.nextToken());
								if((lineLimit < 0) || (m < lineLimit)) 
									lineLimit = m;
							} catch (NumberFormatException e) {
								// nothing
							}
						}else{
							opt[j++] = v;
						}
					} else{
						opt[j++] = v;
					}
				}
				if(j < opt.length) {
					Value[] _opt = new Value[j];
					for(int i = 0; i < j; i++){
						_opt[i] = opt[i];	
					}
					opt = _opt;
				}
				
				Writer ww = w;
				Writer logWW = logW;
				if(lineLimit > 0){// lineLimit version
					ww = new LineBreakingWriter(w, lineLimit);
					if(logW!=null)
						logWW = new LineBreakingWriter(logW, lineLimit);
				}
				args.value(0).print(ww, opt);
				w.write($); ww.flush();
				if(logWW!=null){
					args.value(0).print(logWW, opt);
					logW.write($);logWW.flush();
				}
			}
		} catch (IOException e) { 
			throw new EvalException(e.getMessage());
		}
	}
}
