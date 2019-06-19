public class QuickSort {

	public void sort( long[] array ){
		if( array == null ) throw new IllegalArgumentException("Argument must not be null.");
		sort(array,0,array.length-1);
	}

	protected void sort( long[] arr, int lo, int hi ){
		assert arr != null && 0 <= lo && hi < arr.length && -1 <= hi - lo;
		if( lo < hi ){  // there are at least two elements to sort
			final int pivot = split( arr, lo, hi );
			assert lo <= pivot && pivot <= hi;
			sort( arr, lo,      pivot-1 );
			sort( arr, pivot+1, hi      );
		}
	}

	protected int split( long[] arr, int lo, int hi ){
		assert arr != null && 0 <= lo && hi < arr.length && lo < hi;
		final long pivotValue = arr[hi];
		final int originalHi = hi;
		--hi;
		while( lo <= hi ){
			while( lo <= hi && arr[lo] <= pivotValue ) ++lo;
			while( lo <= hi && arr[hi] >= pivotValue ) --hi;
			if( lo < hi ){  // swap and then incr lo & decr hi
				assert arr[hi] < pivotValue && pivotValue < arr[lo];
				long tmp = arr[lo];
				arr[lo] = arr[hi];
				arr[hi] = tmp;
				++lo; --hi;
			}
		}
		assert lo == hi + 1 && hi < originalHi;
		arr[originalHi] = arr[lo];    // for simplicity, we do this
		arr[lo] = pivotValue;         // even when lo == originalHi
		return lo;
	}

}


