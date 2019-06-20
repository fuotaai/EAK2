package pg8sj5;

import java.util.*;
import java.util.concurrent.*;

/** An immutable board of non-attacking queens. */
public class Board {

	/** An immutable linked list of int values. */
	protected static class Node {
		protected final int value;
		protected final Node next;
		protected Node( int value, Node next ){
			this.value = value;
			this.next = next;
		}
	}

	protected final Node queens;   // list of non-attacking queens
	protected final int nrQueens;  // length of queens

	/** An empty board. */
	protected Board(){
		queens = null;
		nrQueens = 0;
	}

	/** Adding a new column to a board. */
	protected Board( int queen, Board board ){ 
		queens = new Node(queen,board.queens);
		nrQueens = board.nrQueens + 1;
	}

	/** Whether a new queen attacks existing queens on this board. */
	protected boolean attacks( int queen ){
		int distance = 0;
		Node cursor = queens;
		while( cursor != null ){
			++distance;
			if( queen == cursor.value || Math.abs(queen - cursor.value) == distance ){
				return true;
			}
			cursor = cursor.next;
		}
		return false;
	}

	/** Compute all possible solutions starting from the current board. */
	protected List<Board> continuations( int size ){
		if( nrQueens == size ){ // no more queens have to be added
                    List<Board> result = new ArrayList<>();
                    result.add(this);
                    return  result;   // return a single solution: this
		} else {
			// add one more column by trying all non-attacking rows in that column,
			// and recursively find all continuations from all the resulting boards
			List<Board> boards = new LinkedList<>();
			for( int row = 1; row <= size; ++row ){
				if( !attacks(row) ){
					boards.addAll((new Board(row,this)).continuations(size));
				}
			}
			return boards;
		}
	}

	/** Place <code>size</code> queens on a size x size board in all possible ways. */
	public static List<Board> solve( int size ){
		return (new Board()).continuations(size);
	}

}

class NQueens {
	public static void main( String[] args ){
		System.out.println( Board.solve( Integer.parseInt(args[0]) ).size() );
	}
}
