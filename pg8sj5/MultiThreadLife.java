package pg8sj5;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author fuotaai
 */
public class MultiThreadLife extends Life implements Runnable {
    
    private final int NUMBER_OF_THREADS;
    private final MultiThreadLife ref;
    private final int lineIdxFrom;
    private final int lineIdxTo;
    
    /*
    Constructor used for creating a new Thread
    */
    public MultiThreadLife(int lineIdxFrom, int lineIdxTo, MultiThreadLife ref) {
        super(ref.from.length, ref.from[0].length, ref.torus);
        this.lineIdxFrom = lineIdxFrom;
        this.lineIdxTo = lineIdxTo;
        this.ref = ref;
        this.NUMBER_OF_THREADS = ref.getNumberOfThreads();
    }
    
    /*
    Constructor used in main method
    */
    public MultiThreadLife(int n, int m, boolean torus, int numberOfThreads) {
        super(n, m, torus);
        NUMBER_OF_THREADS = numberOfThreads;
        this.ref = this;
        lineIdxFrom = 0;
        lineIdxTo = 0;
    }

    @Override
    public void run() {
        for( int line = lineIdxFrom; line <= lineIdxTo; line++){
            for( int j=0; j<to[0].length; ++j ){
                switch( living9(line,j) ){
                        case  3: ref.setTo(line, j, true); break;
                        case  4: ref.setTo(line, j, ref.getFrom()[line][j]); break;
                        default: ref.setTo(line, j, false);
                }
            }
        }  
    }
    
    @Override
    public int living9( int n, int m ){
		int living = 0;
		for(int i=-1; i<=1; ++i)
			for(int j=-1; j<=1; ++j)
				if(torus ? torus(n+i,m+j) : deadEnvironment(n+i,m+j))
					++living;
		return living;
	}
    
    @Override
    public long measure( int iterations ){
		long startTime = System.currentTimeMillis();
		iterate(iterations);
		long endTime = System.currentTimeMillis();
		return endTime-startTime;
	}
    
    @Override
    public void iterate( int count ){
		while( count > 0 ){
			step();
			boolean[][] tmp = from; from = to; to = tmp;  // swap from and to
			--count;
		}
	}
    
    /** Compute new state from the array <code>from</code> to the array <code>to</code>. */
    @Override
    protected void step() {
        List<Thread> threads = new ArrayList<>();
        int linesToCalculate = Math.floorDiv(ref.from.length, NUMBER_OF_THREADS);
        for(int i = 0; i<NUMBER_OF_THREADS-1; i++) {
            Thread t = new Thread(new MultiThreadLife(i*linesToCalculate, (i*linesToCalculate)+linesToCalculate-1, ref));
            t.start();
            threads.add(t);
        }
        /*
        Last thread calculates the leftover cells aswell
        */
        Thread t = new Thread(new MultiThreadLife((NUMBER_OF_THREADS-1)*linesToCalculate, linesToCalculate-1, ref));
        t.start();
        threads.add(t);
        
        threads.forEach((thread) -> {
            try {
                thread.join();  
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }
        });    
    }
    
    private boolean torus( int n, int m ){
        n = (n+from.length) % from.length;
        m = (m+from[0].length) % from[0].length;
        return ref.getFrom()[n][m];
    }

    private boolean deadEnvironment( int n, int m ){
        return n >= 0 && m >= 0 && n < from.length && m < from[0].length
               && ref.getFrom()[n][m];
    }
    
    public static void main( String[] args ) throws Exception {
		if( args.length < 6 ){
			new Life(50,50,false).acorn(25,25).animate(200,1,100,0,0,50,50);
		} else {
			int n = Integer.parseInt(args[0]);
			int m = Integer.parseInt(args[1]);
			int iterations = Integer.parseInt(args[2]);
                        int numberOfThreads = Integer.parseInt(args[5]);
			MultiThreadLife multiThreadLife = new MultiThreadLife(n, m, false, numberOfThreads);
			multiThreadLife.fromFile(args[3]);
			System.out.println("Computation lasted "+ multiThreadLife.measure(iterations) +" milliseconds.");
			multiThreadLife.toFile(args[4]);
		}
	}
    
    public int getNumberOfThreads() {
        return NUMBER_OF_THREADS;
    }
    
    public synchronized void setTo(int i, int j, boolean val) {
            to[i][j] = val;
    }
    
    public synchronized boolean[][] getFrom() {
        return from;
    }
}
