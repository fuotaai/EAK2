package pg8sj5;

/** Game of Life. */
public class Life {

	/** Invariant: <code>
		from != null & to != null &
		from.length == to.length != 0 &
		from[i] != null & to[i] != null &
		from[i].length == to[j].length != 0 </code> (for all sensible <code>i,j</code>)
	*/
	protected boolean[][] from, to;

	/** Whether the world is a torus.
		Otherwise cells die ouside of the world.
	*/
	protected boolean torus; 
        
	/** A new n x m field with no living cells. */
	public Life( int n, int m, boolean torus ){
		if( n <= 0 || m <= 0 ) throw new IllegalArgumentException();
		from = new boolean[n][m];
		to = new boolean[n][m];
		this.torus = torus;
	}

	/** Plays <code>count</code> generations of Game of Life. */
	public void iterate( int count ){
		while( count > 0 ){
			step();
			boolean[][] tmp = from; from = to; to = tmp;  // swap from and to
			--count;
		}
	}

	/** Compute new state from the array <code>from</code> to the array <code>to</code>. */
	protected void step(){
		for( int i=0; i<to.length; ++i ){
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

	/** Retrive cell. Depends on whether the world is a torus. */
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


	/////////////////////////////////////////
	// Put some well-known patterns if a life
	/////////////////////////////////////////

	/** Put a Blinker at position <code>(n,m)</code>. */
	public Life blinker(int n, int m){
		from[n+2][m+1] = from[n+2][m+2] = from[n+2][m+3] = true;
		return this;
	}

	/** Put an Acorn at position <code>(n,m)</code>. */
	public Life acorn(int n, int m){
		from[n+1][m+2] = from[n+2][m+4] = from[n+3][m+1] = from[n+3][m+2] = from[n+3][m+5] = from[n+3][m+6] = from[n+3][m+7] = true;
		return this;
	}

	/** Put an "Infinite Growth with 10 cells" at position <code>(n,m)</code>. */
	public Life inf10(int n, int m){
		from[n+2][m+7] = from[n+4][m+6] = from[n+4][m+7] = from[n+6][m+3] = from[n+6][m+4] = from[n+6][m+5] = from[n+8][m+2] = from[n+8][m+3] = from[n+8][m+4] = from[n+9][m+3] = true;
		return this;
	}

	/** Put a Gosper Glider Gun at position <code>(n,m)</code>. */
	public Life gosperGliderGun(int n, int m){
		from[n+1][m+5] = from[n+1][m+6] = from[n+2][m+5] = from[n+2][m+6] = from[n+11][m+5] = from[n+11][m+6] = from[n+11][m+7] = from[n+12][m+4] = from[n+12][m+8] = from[n+13][m+3] = from[n+13][m+9] = from[n+14][m+3] = from[n+14][m+9] = from[n+15][m+6] = from[n+16][m+4] = from[n+16][m+8] = from[n+17][m+5] = from[n+17][m+6] = from[n+17][m+7] = from[n+18][m+6] = from[n+21][m+3] = from[n+21][m+4] = from[n+21][m+5] = from[n+22][m+3] = from[n+22][m+4] = from[n+22][m+5] = from[n+23][m+2] = from[n+23][m+6] = from[n+25][m+1] = from[n+25][m+2] = from[n+25][m+6] = from[n+25][m+7] = from[n+35][m+3] = from[n+35][m+4] = from[n+36][m+3] = from[n+36][m+4] = true;
		return this;
	}


	////////////////////////////////
	// Input-output from/to textfile
	////////////////////////////////

	public Life fromFile( String fname ) throws java.io.IOException {
		try(
			java.util.Scanner scanner = new java.util.Scanner(new java.io.File(fname));
		){
			while( scanner.hasNext() )
				from[ scanner.nextInt() ][ scanner.nextInt() ] = true;
			return this;
		}

	}

	public void toFile( String fname ) throws java.io.IOException {
		try(
			java.io.PrintWriter printer = new java.io.PrintWriter(new java.io.File(fname));
		){
			for( int i=0; i<from.length; ++i ){
				for( int j=0; j<from[0].length; ++j ){
					if( from[i][j] ){
						printer.println(i + " " +j);
					}
				}
			}
		}

	}

	/////////////////////////////////////////////////////////
	// Animation to visualize Game of Life on standard output
	/////////////////////////////////////////////////////////

	public void animate( int iterations, int steps, int delay, int row, int col, int rows, int cols ){
		String line; {
			StringBuilder builder = new StringBuilder();
			for( int i=0; i<cols; ++i ) builder.append('-');
			builder.append('+');
			line = builder.toString();
		}
		print(row,col,rows,cols,line);
		for( int i = 0; i < iterations; i+= steps ){
			iterate(steps);
			print(row,col,rows,cols,line);
			try {Thread.sleep(delay);} catch (InterruptedException e){}

		}
	}

	private void print( int row, int col, int rows, int cols, String line ){
		for( int i=row; i<row+rows; ++i ){
			for( int j=col; j<col+cols; ++j ){
				System.out.print(from[i][j]?'@':' ');
			}
			System.out.println("|");
		}
		System.out.println(line);
	}


	///////////////////////////
	// Measuring execution time
	///////////////////////////

	public long measure( int iterations ){
		long startTime = System.currentTimeMillis();
		iterate(iterations);
		long endTime = System.currentTimeMillis();
		return endTime-startTime;
	}		


	///////
	// main
	///////

	public static void main( String[] args ) throws Exception {
		if( args.length < 5 ){
			//new Life(20,50,false).blinker(0,0).animate(100,1,100,0,0,20,50);
			new Life(50,50,false).acorn(25,25).animate(200,1,100,0,0,50,50);
			/*new Life(500,500,false).inf10(5,20).animate(100,1,100,0,0,20,50);
			new Life(500,500,false).gosperGliderGun(5,5).animate(200,1,100,0,0,50,80);
			new Life(300,300,false).acorn(150,150).animate(6000,100,0,140,100,20,70);*/
		} else {
			int n = Integer.parseInt(args[0]);
			int m = Integer.parseInt(args[1]);
			int iterations = Integer.parseInt(args[2]);
			Life life = new Life(n,m,false);
			life.fromFile(args[3]);
			System.out.println("Computation lasted "+ life.measure(iterations) +" milliseconds.");
			life.toFile(args[4]);
		}
	}

}
