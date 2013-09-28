package coho.interp;

import java.io.*;
import java.util.concurrent.*;

class MyParser {
	private static void usage() {
		System.err.println(
		"usage:  java Coho.geom.interp.MyParser [-l inLogFile] [-o outLogFile] < input");
		System.exit(1);
	}
	
	/* a simple test method */
	public static void main(String[] args) throws Exception {
//		String prompt = null;
		FileWriter logfile = null;
		FileInputStream infile = null;
		
		// process command line arguments
		for(int i = 0; i < args.length; i++) {
//			if(args[i].compareTo("-p") == 0) {
//				if(args.length <= i+1) usage();
//				else prompt = args[++i];
//			} 
//			else 
			if (args[i].compareTo("-l") == 0) {
				if(args.length <= i+1) usage();
				else {
					try { 
						i++; 
						logfile = new FileWriter(args[i]);         
					} catch (IOException e) {
						System.err.println("Could not open " + args[i] + " to write.");
						System.exit(1);
					}
				}
			} else if(args[i].compareTo("-o")==0){
				if(args.length<=i+1) usage();
				else{
					try{
						i++;
						BasicFunctions.setLogWriter(new FileOutputStream(args[i]));
					}catch(IOException e){
						System.err.print("Could not open "+args[i]+" to write.");
						System.exit(1);
					}
				}
			}else if(args[i].compareTo("<") == 0) {
				try { 
					i++; 
					infile = new FileInputStream(args[i]); 
				} catch (IOException e) {
					System.err.println("Could not open " + args[i] + " to read.");
					System.exit(1);
				}
				System.setIn(infile);
			} else {
				System.err.println("unrecognized option:  " + args[i]);
				System.exit(1);
			}
		}
		
		
		PTevalFactory pf = new PTevalFactory();
		ValueFactory.init();
		Eval.init();
		pf.nodeOperations(Eval.nodeOperations());
		//	Eval.setDebug(true);
		
		// set input for the parser
		PromptingReader in = null;
		if(logfile == null) 
			in = new PromptingReader(System.in);
		else 
			in = new PromptingReader(new CopyingReader(System.in, logfile));
		BlockingQueue<PTeval> q = new LinkedBlockingQueue<PTeval>(256);
		ParserThread pThread = new ParserThread(in, pf, q, null);
		BasicFunctions.setParser((Parse)pThread);		
		EvalThread eThread = new EvalThread(q);
		(new Thread(pThread)).start();
		(new Thread(eThread)).start();
	}
}
