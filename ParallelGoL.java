import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

/** Game of Life. */
public class ParallelGoL {

	public static int N;
	public static int M;
	
	/** Invariant: <code>
		from != null & to != null &
		from.length == to.length != 0 &
		from[i] != null & to[i] != null &
		from[i].length == to[j].length != 0 </code> (for all sensible <code>i,j</code>)
	 */
	protected volatile boolean[][] from;
	protected volatile boolean[][] to;
	
	private final int taskCount = Runtime.getRuntime().availableProcessors();
	
    private final CountDownLatch startLatch = new CountDownLatch(1);
    private final CyclicBarrier barrier = new CyclicBarrier(taskCount,
			new Runnable() {

				@Override
				public void run() {
					swapMatrices();
				}
			});
    
    private void swapMatrices() {
		boolean[][] tmp = from;
		from = to;
		to = tmp;
	}


	/** Whether the world is a torus.
		Otherwise cells die ouside of the world.
	 */
	private final boolean torus; 

	/** A new n x m field with no living cells. */
	public ParallelGoL( int n, int m, boolean torus ){
		if( n <= 0 || m <= 0 ) throw new IllegalArgumentException();
		from = new boolean[n][m];
		to = new boolean[n][m];
		this.torus = torus;
	}

	/** Compute new state from <code>from</code> to <code>to</code>. */
	protected void step(int fromLine, int toLine){
		for( int i=fromLine; i<toLine; ++i ){
			for( int j=0; j<to[0].length; ++j ){
				switch( living9(i,j) ){
				case  3: to[i][j] = true; break;
				case  4: to[i][j] = from[i][j]; break;
				default: to[i][j] = false;
				}
			}
		}
	}

	/** Count living cells in a 3x3 block with center <code>(n,m)</code>. */
	public int living9( int n, int m ){
		int living = 0;
		for(int i=-1; i<=1; ++i)
			for(int j=-1; j<=1; ++j)
				if( get(n+i,m+j) )
					++living;
		return living;
	}

	/** Retrive cell. Effect depends whether the world is a torus. */
	public boolean get( int n, int m ){
		return torus ? torus(n,m) : deadEnvironment(n,m);
	}

	private boolean torus( int n, int m ){
		n = (n+from.length) % from.length;
		m = (m+from[0].length) % from[0].length;
		return from[n][m];
	}

	private boolean deadEnvironment( int n, int m ){
		return n >= 0 && m >= 0 && n < from.length && m < from[0].length
				&& from[n][m];
	}

	/** Put a Blinker at position <code>(n,m)</code>. */
	public ParallelGoL blinker(int n, int m){
		from[n+2][m+1] = from[n+2][m+2] = from[n+2][m+3] = true;
		return this;
	}

	/** Put an Acorn at position <code>(n,m)</code>. */
	public ParallelGoL acorn(int n, int m){
		from[n+1][m+2] = from[n+2][m+4] = from[n+3][m+1] = from[n+3][m+2] = from[n+3][m+5] = from[n+3][m+6] = from[n+3][m+7] = true;
		return this;
	}




	public static void main( String[] args ) throws Exception {
		//new Animated(20,50,false,1,100,0,0,20,50).blinker(0,0).iterate(100);
		//new Animated(300,300,false,100,0,140,100,20,70).acorn(150,150).iterate(6000);
		long start=System.currentTimeMillis();
                runFromFile(args[0],args[1]);
                System.out.println(System.currentTimeMillis()-start);
	}

	public boolean[][] calculate(int iterations) {
		Thread[] threads = new Thread[taskCount];

		for (int t = 0; t < taskCount; ++t) {
			threads[t] = new Thread(new Worker(t, iterations));
			threads[t].start();
		}

		startLatch.countDown();

		try {
			for (int t = 0; t < taskCount; ++t) {
				threads[t].join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// return result
		return 0 < iterations % 2 ? from : to;
	}
	
	/** Read input file, run Game of Life, print into output file. */
	public static void runFromFile( String infile, String outfile ) throws java.io.IOException {
		
                        java.util.Scanner scanner = new java.util.Scanner(new java.io.File(infile));
                        java.io.PrintWriter printer = new java.io.PrintWriter(new java.io.File(outfile));

			int n = scanner.nextInt(), m = scanner.nextInt();
                        N=n; M=m;
			ParallelGoL pgol = new ParallelGoL(n,m,false);
			int iterations = scanner.nextInt();
			while( scanner.hasNext() ){
				pgol.from[ scanner.nextInt() ][ scanner.nextInt() ] = true;
			}
			
			pgol.calculate(iterations);
			
			printer.println(n + " " +m);
			printer.println(iterations);
			for( int i = 0; i < pgol.from.length; ++i ){
				for( int j = 0; j < pgol.from[0].length; ++j ){
					if( pgol.from[i][j] ){
						printer.println(i + " " +j);
					}
				}
			}
                        scanner.close();
                        printer.close();
		
	}
        
        /** Animate iteration by printing the world regularly on the standard output. */
	public static class Animated extends ParallelGoL {

		public Animated( int n, int m, boolean torus, int steps, int delay, int row, int col, int rows, int cols ){
			super(n,m,torus);
			this.steps=steps; this.delay=delay; this.row=row; this.col=col; this.rows=rows; this.cols=cols;
			StringBuilder builder = new StringBuilder();
			for( int i=0; i<cols; ++i ) builder.append('-');
			builder.append('+');
			line = builder.toString();
		}

		private int steps, delay, row, col, rows, cols;
		private String line;
		private int counter = 0;
	

		@Override
		public boolean[][] calculate(int iterations) {
			counter = 0;
			return super.calculate(iterations);
		}



		@Override protected void step(int a, int b){
			super.step(a, b);
			++counter;
			if(counter == steps){
				counter = 0;
				print(row,col,rows,cols);
				try {Thread.sleep(delay);}catch(InterruptedException e){}
			}
		}

		private void print( int row, int col, int rows, int cols ){
			for( int i=row; i<row+rows; ++i ){
				for( int j=col; j<col+cols; ++j ){
					System.out.print(from[i][j]?'@':' ');
				}
				System.out.println("|");
			}
			System.out.println(line);
		}

	}

	private class Worker implements Runnable {
		private final int taskNumber;
		private final int iterations;

		public Worker(int taskNumber, int iterations) {
			this.taskNumber = taskNumber;
			this.iterations = iterations;
		}

		@Override
		public void run() {
			try {
				startLatch.await();

				int sliceBegin = getSliceFrom(taskNumber);
				int sliceEnd = getSliceTo(taskNumber);

				for (int n = 0; n < iterations; ++n) {

					step(sliceBegin, sliceEnd);

					barrier.await();
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				e.printStackTrace();
			} catch (BrokenBarrierException e) {
				e.printStackTrace();
			}
		}

		private int getSliceFrom(int t) {
			int cells = N;
			return t * (cells / taskCount);
		}

		private int getSliceTo(int t) {
			int cells = N;

			if (t + 1 == taskCount) {
				return cells;
			}

			return (t + 1) * (cells / taskCount);
		}
	}

}
