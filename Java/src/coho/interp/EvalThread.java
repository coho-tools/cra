package coho.interp;
import java.util.concurrent.*;

import coho.debug.STAT;
/**
 * Evaluation thread.It get parser tree for each statement from 
 * the queue and then evaluate it.
 * @author chaoyan
 *
 */
public class EvalThread implements Runnable {
	private final BlockingQueue<PTeval> queue;
	public EvalThread(BlockingQueue<PTeval> q){
		queue = q;
	}
	public void run(){
		try{
			PTeval pt;
			do{
				pt = queue.take();
				pt.eval(pt);
			}while(!BasicPTeval.isVoidPTeval(pt));
		}catch(Exception e){
			BasicFunctions.print_exception(e,"EvalThread");
		}finally{
			if(STAT.stat) STAT.outStat();
			System.out.flush();
			System.err.flush();
			System.exit(1);
		}
	}
}
