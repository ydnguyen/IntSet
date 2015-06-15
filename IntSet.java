/** An IntSet is a set of integers.  It is a mutable type.
 *  The implementation is optimized for sets that contain sequences of
 *  several consecutive integers.
 */
import java.util.*;

public class IntSet {
	/**
	 * Effect: Initializes an empty set. Performance: Runs in constant time (not
	 * counting operations in the call to isOK).
	 */
	public IntSet() {
		myIntervals = new ArrayList<Interval>();
		numberOfInts = 0;
		if (!isOK()) {
			throw new IllegalStateException(
					"IllegalStateException found in constructor method!");
		}
	}

	/**
	 * Precondition: iv ­ null. Effects: Adds all the integers from iv.low() to
	 * iv.high(), inclusive, to this set. Performance: Runs in time proportional
	 * in the worst case to the number of intervals in this set (not counting
	 * operations in the call to isOK).
	 */
	public void addAll(Interval iv) {
		if (iv == null) {
			throw new IllegalArgumentException("Argument cannot be null!");
		}
		// If there's nothing in myIntervals, then just add iv to it
		if (myIntervals.size() == 0) {
			myIntervals.add(iv);
		} else {
			for (int k = 0; k < myIntervals.size(); k++) {
				// If the interval to add precedes all other intervals in
				// myIntervals, then simply put it at the beginning of
				// myIntervals
				if (myIntervals.get(k).low() > iv.high()) {
					myIntervals.add(k, iv);
					break;
				}
				// There are two cases to consider: the case where iv is
				// overlapping with the interval we're considering in
				// myIntervals,
				// and the case where iv is adjacent to that interval
				if (myIntervals.get(k).overlaps(iv)) {
					// There are three subcases here: the case where myLow of iv
					// is less than myLow of the current interval, the case
					// where myHigh of iv is greater than myHigh of the current
					// interval, and finally the case where both of these things
					// happen at the same time
					if (iv.low() <= myIntervals.get(k).low()) {
						myIntervals.set(k, new Interval(iv.low(), myIntervals
								.get(k).high()));
					}
					if (iv.high() >= myIntervals.get(k).high()) {
						myIntervals.set(k, new Interval(myIntervals.get(k)
								.low(), iv.high()));
					}
					// There's no reason to consider the last case, since both
					// the conditionals need to be satisfied first before the
					// program exits the loop
					break;
				}
				if (myIntervals.get(k).adjoins(iv)) {
					// There are only two cases to consider here: the case where
					// iv lies to the left of the current interval, and the case
					// where iv lies to the right of the current interval
					// It can't be both at the same time, so it's okay to exit
					// the
					// program right here to prevent the situation where iv is
					// unionized two times by two continuous interval in
					// myIntervals
					if (iv.high() < myIntervals.get(k).low()) {
						myIntervals.set(k, new Interval(iv.low(), myIntervals
								.get(k).high()));
						break;
					}
					if (iv.low() > myIntervals.get(k).high()) {
						myIntervals.set(k, new Interval(myIntervals.get(k)
								.low(), iv.high()));
						break;
					}
				}
				// If the interval to add is not overlapping nor adjoining any
				// other interval in myIntervals, then simply add that interval
				// to the end of myIntervals
				if (k == myIntervals.size() - 1) {
					myIntervals.add(iv);
				}
			}
		}
		// This loop shrinks down the size of myIntervals by combining adjoining
		// intervals
		// to maintain the class invariant: Each interval should be the largest
		// possible
		for (int t = 0; t < myIntervals.size() - 1;) {
			if (myIntervals.get(t).adjoins(myIntervals.get(t + 1))) {
				myIntervals.set(t, new Interval(myIntervals.get(t).low(),
						myIntervals.get(t + 1).high()));
				myIntervals.remove(t + 1);
			} else {
				if (myIntervals.get(t).overlaps(myIntervals.get(t + 1))) {
					if (myIntervals.get(t).low() <= myIntervals.get(t + 1)
							.low()
							&& myIntervals.get(t).high() >= myIntervals.get(
									t + 1).high()) {
						myIntervals.remove(t + 1);
					} else {
						if (myIntervals.get(t).high() >= myIntervals.get(t + 1)
								.low()) {
							myIntervals.set(t, new Interval(myIntervals.get(t)
									.low(), myIntervals.get(t + 1).high()));
							myIntervals.remove(t + 1);
						}
					}
				} else {
					t++;
				}
			}
		}
		// We need to update the variable numberOfInts after the addition of new
		// interval
		int numberOfIntsAfterAdd = 0;
		for (int j = 0; j < myIntervals.size(); j++) {
			numberOfIntsAfterAdd = numberOfIntsAfterAdd
					+ myIntervals.get(j).high() - myIntervals.get(j).low() + 1;
		}
		numberOfInts = numberOfIntsAfterAdd;
		if (!isOK()) {
			throw new IllegalStateException(
					"IllegalStateException found in addAll method!");
		}
	}

	/**
	 * Effects: Adds newElem to this set. Performance: Runs in time proportional
	 * in the worst case to the number of intervals in this set (not counting
	 * operations in the call to isOK).
	 */
	public void add(int newElem) {
		// A single element to add is simply a special case of addAll, so
		// we can just use that
		try {
			addAll(new Interval(newElem));
		} catch (IllegalStateException e) {
			throw new IllegalStateException(
					"IllegalStateException found in add method!");
		}
	}

	/**
	 * Precondition: iv ­ null. Effects: Remove all the integers from iv.low( )
	 * to iv.high( ), inclusive, from this set. Performance: Runs in time
	 * proportional in the worst case to the number of intervals in this set
	 * (not counting operations in the call to isOK).
	 */
	// There are essentially 3 cases of the value of myLow of iv and the value
	// of
	// myLow of the interval we're looking at in myIntervals (less than, equal
	// to, and greater than)
	// There are also 3 cases of the value of myHigh of iv and the value of
	// myHigh
	// of the current interval (less than, equal to, and greater than)
	// For each of the cases of myLow values comparison, we have all 3 cases of
	// values of myHigh to consider (less than, equal to, and greater than)
	// I do not use any break here because the program may have to remove
	// elements for multiple intervals in myIntervals
	public void removeAll(Interval iv) {
		if (iv == null) {
			throw new IllegalArgumentException("Argument cannot be null!");
		}
		Interval currentInterval;
		for (int k = 0; k < myIntervals.size();) {
			currentInterval = myIntervals.get(k);
			if (currentInterval.overlaps(iv)) {
				// First case
				if (currentInterval.low() < iv.low()) {
					// First subcase
					if (currentInterval.high() > iv.high()) {
						myIntervals.set(k, new Interval(currentInterval.low(),
								iv.low() - 1));
						myIntervals.add(k + 1, new Interval(iv.high() + 1,
								currentInterval.high()));
					}
					// Second subcase
					if (currentInterval.high() == iv.high()) {
						myIntervals.set(k, new Interval(currentInterval.low(),
								iv.low() - 1));
					}
					// Third subcase
					if (currentInterval.high() < iv.high()) {
						myIntervals.set(k, new Interval(currentInterval.low(),
								iv.low() - 1));
						iv = new Interval(currentInterval.high() + 1, iv.high());
					}
				}
				// Second case
				if (currentInterval.low() == iv.low()) {
					// First subcase
					if (currentInterval.high() > iv.high()) {
						myIntervals.set(k, new Interval(iv.high() + 1,
								currentInterval.high()));
					}
					// Second subcase
					if (currentInterval.high() == iv.high()) {
						myIntervals.remove(k);
					}
					// Third subcase
					if (currentInterval.high() < iv.high()) {
						myIntervals.remove(k);
						iv = new Interval(currentInterval.high() + 1, iv.high());
					}
				}
				// Third case
				if (currentInterval.low() > iv.low()) {
					// First subcase
					if (currentInterval.high() > iv.high()) {
						myIntervals.set(k, new Interval(iv.high() + 1,
								currentInterval.high()));
					}
					// Second subcase
					if (currentInterval.high() == iv.high()) {
						myIntervals.remove(k);
					}
					// Third subcase
					if (currentInterval.high() < iv.high()) {
						myIntervals.remove(k);
						iv = new Interval(currentInterval.high() + 1, iv.high());
					}
				}
			}
			// We're only moving on to the next interval in myIntervals if the
			// interval to remove is not overlapping with this interval
			else {
				k++;
			}
		}
		// We need to update numberOfInts after performing a removal of an
		// interval
		int numberOfIntsAfterRemove = 0;
		for (int j = 0; j < myIntervals.size(); j++) {
			numberOfIntsAfterRemove = numberOfIntsAfterRemove
					+ myIntervals.get(j).high() - myIntervals.get(j).low() + 1;
		}
		numberOfInts = numberOfIntsAfterRemove;
		if (!isOK()) {
			throw new IllegalStateException(
					"IllegalStateException found in removeAll method!");
		}
	}

	/**
	 * Effects: Removes elem from this set. Performance: Runs in time
	 * proportional in the worst case to the number of intervals in this set
	 * (not counting operations in the call to isOK).
	 */
	public void remove(int elem) {
		// A single element to remove is simply a special case of removeAll, so
		// we can just use that
		try {
			removeAll(new Interval(elem));
		} catch (IllegalStateException e) {
			throw new IllegalStateException(
					"IllegalStateException found in remove method!");
		}
	}

	/**
	 * Precondition: iv ­ null. Effect: Returns true if all integers in the
	 * interval from iv.low( ) to iv.high( ), inclusive, are in this set;
	 * returns false otherwise. Performance: Runs in time proportional in the
	 * worst case to the number of intervals in this set.
	 */
	public boolean containsAll(Interval iv) {
		if (iv == null) {
			throw new IllegalArgumentException("Argument cannot be null!");
		}
		for (int k = 0; k < myIntervals.size(); k++) {
			// First requirement for the current interval containing all of the
			// elements in iv is that iv have to lies inside the current
			// interval, ie. overlaps current interval
			// Second requirement is that myLow of iv has to be larger than
			// myLow of current Interval and myHigh of iv has to be less than
			// myHigh of current interval
			if (myIntervals.get(k).overlaps(iv)
					&& myIntervals.get(k).low() <= iv.low()
					&& myIntervals.get(k).high() >= iv.high()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Effect: Returns true if s contains exactly the same integer elements as
	 * this set, and returns false otherwise.
	 */
	public boolean equals(Object s) {
		try {
			IntSet objectIntSet = (IntSet) s;
			// In order for two IntSet object to be considered equal to each
			// other,
			// their sizes have to be the same, and each interval in one set is
			// contained in the other.
			if (objectIntSet.size() == this.size()) {
				for (int k = 0; k < myIntervals.size(); k++) {
					if (!objectIntSet.containsAll(myIntervals.get(k))) {
						return false;
					}
				}
			} else {
				return false;
			}
			return true;
		} catch (ClassCastException e) {
			return false;
		}
	}

	/**
	 * Effect: Returns the number of different integer elements in this set.
	 * Performance: Runs in constant time.
	 */
	public int size() {
		return numberOfInts;
	}

	/**
	 * Effect: Returns a copy of this set.
	 */
	public IntSet copy() {
		IntSet copyIntervals = new IntSet();
		// Since we are adding intervals from a main version to a copy, we can
		// just use addAll
		for (int k = 0; k < myIntervals.size(); k++) {
			copyIntervals.addAll(myIntervals.get(k));
		}
		return copyIntervals;
	}

	/**
	 * Effect: Returns a String representation of this set. The resulting String
	 * will have the following format: {} for an empty set (no spaces before "{"
	 * or after "}" and no spaces between them). {x} for a one-element set (no
	 * spaces before "{" or after "}" and no spaces around the element between
	 * them. {x0,x1,...,xk} for a set of more than one element, i.e., no spaces
	 * before "{" or after "}", with a comma and no spaces separating
	 * consecutive elements. The elements will appear in ascending order.
	 */
	public String toString() {
		String intString = new String("");
		// This loop will go over all the intervals in myIntervals
		for (int k = 0; k < myIntervals.size(); k++) {
			// We need a second loop inside the first loop to go over all the
			// integer in the current interval
			for (int t = myIntervals.get(k).low(); t < myIntervals.get(k)
					.high() + 1; t++) {
				intString = intString + t + " ";
			}
		}
		// We need to trim intString since there will be a single blank space
		// left over after the last integer is concatenated into intString
		// Next, we just replace all the blank spaces with commas, add in curly
		// brackets, and we have a nice display of a set of integers.
		return "{" + intString.trim().replace(" ", ",") + "}";
	}

	/**
	 * Effect: Changes the internal state so that !isOK ( ). Precondition: k >=
	 * 0.
	 */
	public void invalidate(int k) {
		if (k < 0) {
			throw new IllegalArgumentException(
					"Please enter an integer larger than or equal to 0!");
		}
		if (k == 0) {
			myIntervals.add(null);
		}
		if (k == 1) {
			myIntervals.add(new Interval(3, 5));
		}
		if (k == 2) {
			// This is to prevent calls to invalidate (2) when the target IntSet
			// has less than two intervals
			if (myIntervals.size() >= 2) {
				myIntervals.remove(1);
			} else {
				throw new IllegalArgumentException(
						"Please add at least two intervals into your IntSet first!");
			}
		}
		if (k == 3) {
			myIntervals.clear();
		}
		if (k == 4) {
			numberOfInts = 0;
		}
		if (k == 5) {
			numberOfInts = -3;
		}
		if (k == 6) {
			numberOfInts = 50;
		}
		if (k == 7) {
			myIntervals = null;
		}
	}

	/**
	 * Effect: Returns true if this set satisfies the class invariant; returns
	 * false otherwise.
	 */
	public boolean isOK() {
		if (myIntervals == null) {
			return false;
		}
		for (int s = 0; s < myIntervals.size(); s++) {
			if (myIntervals.get(s) == null) {
				return false;
			}
		}
		int sizeOfIntervals = 0;
		for (int j = 0; j < myIntervals.size(); j++) {
			sizeOfIntervals = sizeOfIntervals + myIntervals.get(j).high()
					- myIntervals.get(j).low() + 1;
		}
		if (sizeOfIntervals != numberOfInts) {
			return false;
		}
		for (int k = 0; k < myIntervals.size(); k++) {
			for (int t = 0; t < myIntervals.size(); t++) {
				if (myIntervals.get(k).adjoins(myIntervals.get(t)) && k != t) {
					return false;
				}
				if (myIntervals.get(k).overlaps(myIntervals.get(t)) && k != t) {
					return false;
				}
			}
		}
		return true;
	}

	public static void main(String[] args) {
		IntSet myIntSet = new IntSet();
		myIntSet.addAll(new Interval(-2, 1));
		myIntSet.addAll(new Interval(-3, 2));
		System.out.println(myIntSet);
		for (int k = 0; k < myIntSet.myIntervals.size(); k++) {
			System.out.println(myIntSet.myIntervals.get(k));
		}
	}

	/* Private variables (you may add more here) */
	public ArrayList<Interval> myIntervals;
	// This new variable numberOfInts is to keep track of how many integers are
	// in the set
	private int numberOfInts;
}
