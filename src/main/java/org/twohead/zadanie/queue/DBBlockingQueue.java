package org.twohead.zadanie.queue;

import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A queue backed by database. This queue orders elements 
 * FIFO (first-in-first-out). The <em>head</em> of the queue 
 * is that element that has been on the queue the longest time. 
 * The <em>tail</em> of the queue is that element that has been on the queue
 * the shortest time. New elements are inserted at the tail of the queue,
 * and the queue retrieval operations obtain elements at the head of the queue.
 *
 * @param <E> the type of elements held in this collection
 */
public abstract class DBBlockingQueue<E> {

	/** Main lock guarding all access */
	final ReentrantLock lock;
	/** Condition for waiting takes */
	private final Condition notEmpty;
	/** Condition for waiting puts */

	/**
	 * Throws NullPointerException if argument is null.
	 *
	 * @param v the element
	 */
	private static void checkNotNull(Object v) {
		if (v == null)
			throw new NullPointerException();
	}

	
	/**
	 * Inserts element into its database table.
	 * 
	 * @throws SQLException
	 */
	protected abstract void insertItem(E element) throws SQLException;
	
	/**
	 * Returns the first element from its table and deletes it.
	 * 
	 * @param element
	 * @throws SQLException
	 */
	protected abstract E extractItem() throws SQLException;
	
	/**
	 * Returns the element at the given position of its table.
	 * 
	 * @param element
	 * @throws SQLException
	 */
	protected abstract E itemAt(int i) throws SQLException;
	
	/**
	 * Returns the count of all element in their table. 
	 * 
	 * @param int
	 * @throws SQLException 
	 */
	protected abstract int count() throws SQLException;
	
	/**
	 * Removes element at the given position of its table.
	 *  
	 * @throws SQLException  
	 */
	protected abstract void removeItemAt(int i) throws SQLException;
	
	/**
	 * Removes all elements from their table.
	 *  
	 * @throws SQLException  
	 */
	protected abstract void clearTable() throws SQLException;

	/**
	 * Inserts element at the end and signals. Call only when holding lock.
	 * @throws SQLException 
	 */
	private void insert(E x) throws SQLException {
		insertItem(x);
		notEmpty.signal();
	}

	/**
	 * Extracts the first element.
	 * @throws SQLException 
	 */
	private E extract() throws SQLException {
		E x = extractItem();
		return x;
	}

	/**
	 * Deletes item at position i. Call only when holding lock.
	 * @throws SQLException 
	 */
	void removeAt(int i) throws SQLException {
		removeItemAt(i);
	}

	/**
	 * Creates an {@code DBArrayBlockingQueue} with default access policy.
	 *
	 */
	public DBBlockingQueue() {
		this(true);
	}

	/**
	 * Creates an {@code ArrayBlockingQueue} with specified access policy.
	 *
	 * @param fair     if {@code true} then queue accesses for threads blocked on
	 *                 insertion or removal, are processed in FIFO order; if
	 *                 {@code false} the access order is unspecified.
	 */
	public DBBlockingQueue(boolean fair) {
		lock = new ReentrantLock(fair);
		notEmpty = lock.newCondition();
	}


	/**
	 * Inserts the specified element at the tail of this queue.
	 *
	 * @throws InterruptedException {@inheritDoc}
	 * @throws SQLException 
	 * @throws NullPointerException {@inheritDoc}
	 */
	public void put(E e) throws InterruptedException, SQLException {
		checkNotNull(e);
		final ReentrantLock lock = this.lock;
		lock.lockInterruptibly();
		try {
			insert(e);
		} finally {
			lock.unlock();
		}
	}

    /**
     * Retrieves and removes the head of this queue, waiting if necessary
     * until an element becomes available.
     *
     * @return the head of this queue
     * @throws InterruptedException if interrupted while waiting
     * @throws SQLException 
     */
	public E take() throws InterruptedException, SQLException {
		final ReentrantLock lock = this.lock;
		lock.lockInterruptibly();
		try {
			while (count() == 0)
				notEmpty.await();
			return extract();
		} finally {
			lock.unlock();
		}
	}

    /**
     * Retrieves and removes the head of this queue, waiting up to the
     * specified wait time if necessary for an element to become available.
     *
     * @param timeout how long to wait before giving up, in units of
     *        {@code unit}
     * @param unit a {@code TimeUnit} determining how to interpret the
     *        {@code timeout} parameter
     * @return the head of this queue, or {@code null} if the
     *         specified waiting time elapses before an element is available
     * @throws InterruptedException if interrupted while waiting
     * @throws SQLException 
     */
	public E take(long timeout, TimeUnit unit) throws InterruptedException, SQLException {
		long nanos = unit.toNanos(timeout);
		final ReentrantLock lock = this.lock;
		lock.lockInterruptibly();
		try {
			while (count() == 0) {
				if (nanos <= 0)
					return null;
				nanos = notEmpty.awaitNanos(nanos);
			}
			return extract();
		} finally {
			lock.unlock();
		}
	}
	
	/**
     * Retrieves, but does not remove, the head of this queue,
     * or returns {@code null} if this queue is empty.
     *
     * @return the head of this queue, or {@code null} if this queue is empty
	 * @throws SQLException 
     */
	public E peek() throws SQLException {
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			return (count() == 0) ? null : itemAt(0);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Returns the number of elements in this queue.
	 *
	 * @return the number of elements in this queue
	 * @throws SQLException 
	 */
	public int size() throws SQLException {
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			return count();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Removes a single instance of the specified element from this queue, if it is
	 * present. More formally, removes an element {@code e} such that
	 * {@code o.equals(e)}, if this queue contains one or more such elements.
	 * Returns {@code true} if this queue contained the specified element (or
	 * equivalently, if this queue changed as a result of the call).
	 *
	 * <p>
	 * Removal of interior elements in circular array based queues is an
	 * intrinsically slow and disruptive operation, so should be undertaken only in
	 * exceptional circumstances, ideally only when the queue is known not to be
	 * accessible by other threads.
	 *
	 * @param o element to be removed from this queue, if present
	 * @return {@code true} if this queue changed as a result of the call
	 * @throws SQLException 
	 */
	public boolean remove(Object o) throws SQLException {
		if (o == null)
			return false;
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			final int count = count();
			for (int i = 0; i < count; i++) {
				if (o.equals(itemAt(i))) {
					removeAt(i);
					return true;
				}
			}
			return false;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Returns {@code true} if this queue contains the specified element. More
	 * formally, returns {@code true} if and only if this queue contains at least
	 * one element {@code e} such that {@code o.equals(e)}.
	 *
	 * @param o object to be checked for containment in this queue
	 * @return {@code true} if this queue contains the specified element
	 * @throws SQLException 
	 */
	public boolean contains(Object o) throws SQLException {
		if (o == null)
			return false;
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			final int count = count();
			for (int i = 0; i < count; i++) {
				if (o.equals(itemAt(i))) {
					return true;
				}
			}
			return false;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Returns an array containing all of the elements in this queue, in proper
	 * sequence.
	 *
	 * <p>
	 * The returned array will be "safe" in that no references to it are maintained
	 * by this queue. (In other words, this method must allocate a new array). The
	 * caller is thus free to modify the returned array.
	 *
	 * <p>
	 * This method acts as bridge between array-based and collection-based APIs.
	 *
	 * @return an array containing all of the elements in this queue
	 * @throws SQLException 
	 */
	public Object[] toArray() throws SQLException {
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			final int count = this.count();
			Object[] a = new Object[count];
			for (int i = 0; i < count; i++) {
				a[i] = itemAt(i);
			}
			return a;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Prints all elements to the standard output.
	 * @throws SQLException 
	 * 
	 */

	public void printAll() throws SQLException {
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			System.out.println("- print all -------------------------------");
			final int count = this.count();
			for (int i = 0; i < count; i++) {
				System.out.println(itemAt(i));
			}
			System.out.println("-------------------------------------------");
			System.out.println();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Returns an array containing all of the elements in this queue, in proper
	 * sequence; the runtime type of the returned array is that of the specified
	 * array. If the queue fits in the specified array, it is returned therein.
	 * Otherwise, a new array is allocated with the runtime type of the specified
	 * array and the size of this queue.
	 *
	 * <p>
	 * If this queue fits in the specified array with room to spare (i.e., the array
	 * has more elements than this queue), the element in the array immediately
	 * following the end of the queue is set to {@code null}.
	 *
	 * <p>
	 * Like the {@link #toArray()} method, this method acts as bridge between
	 * array-based and collection-based APIs. Further, this method allows precise
	 * control over the runtime type of the output array, and may, under certain
	 * circumstances, be used to save allocation costs.
	 *
	 * <p>
	 * Suppose {@code x} is a queue known to contain only strings. The following
	 * code can be used to dump the queue into a newly allocated array of
	 * {@code String}:
	 *
	 * <pre>
	 * String[] y = x.toArray(new String[0]);
	 * </pre>
	 *
	 * Note that {@code toArray(new Object[0])} is identical in function to
	 * {@code toArray()}.
	 *
	 * @param a the array into which the elements of the queue are to be stored, if
	 *          it is big enough; otherwise, a new array of the same runtime type is
	 *          allocated for this purpose
	 * @return an array containing all of the elements in this queue
	 * @throws SQLException 
	 * @throws ArrayStoreException  if the runtime type of the specified array is
	 *                              not a supertype of the runtime type of every
	 *                              element in this queue
	 * @throws NullPointerException if the specified array is null
	 */
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) throws SQLException {
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			final int count = this.count();
			final int len = a.length;
			if (len < count) {
				a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), count);
			}
			for (int i = 0; i < count; i++) {
				a[i] = (T) itemAt(i);
			}
			if (len > count) {
				a[count] = null;
			}
			return a;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public String toString() {
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			int count = count();
			if (count == 0)
				return "[]";

			StringBuilder sb = new StringBuilder();
			sb.append('[');
			for (int i = 0; i < count; i++) {
				Object e = itemAt(i);
				sb.append(e == this ? "(this Collection)" : e);
				sb.append(',').append(' ');
			}
			return sb.append(']').toString();
		} catch (SQLException e1) {
			e1.printStackTrace();
			return "[]";
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Atomically removes all of the elements from this queue. The queue will be
	 * empty after this call returns.
	 * @throws SQLException 
	 */
	public void clear() throws SQLException {
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			clearTable();
		} finally {
			lock.unlock();
		}
	}

    /**
     * Removes all available elements from this queue and adds them
     * to the given collection.  This operation may be more
     * efficient than repeatedly polling this queue.  A failure
     * encountered while attempting to add elements to
     * collection {@code c} may result in elements being in neither,
     * either or both collections when the associated exception is
     * thrown.  Attempts to drain a queue to itself result in
     * {@code IllegalArgumentException}. Further, the behavior of
     * this operation is undefined if the specified collection is
     * modified while the operation is in progress.
     *
     * @param c the collection to transfer elements into
     * @return the number of elements transferred
     * @throws SQLException 
     * @throws UnsupportedOperationException if addition of elements
     *         is not supported by the specified collection
     * @throws ClassCastException if the class of an element of this queue
     *         prevents it from being added to the specified collection
     * @throws NullPointerException if the specified collection is null
     * @throws IllegalArgumentException if the specified collection is this
     *         queue, or some property of an element of this queue prevents
     *         it from being added to the specified collection
     */
	public int drainTo(Collection<? super E> c) throws SQLException {
		checkNotNull(c);
		if (c == this)
			throw new IllegalArgumentException();
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {

			final var count = count();
			for (int i = 0; i < count; i++) {
				c.add(extractItem());
			}
			return count;
		} finally {
			lock.unlock();
		}
	}

    /**
     * Removes at most the given number of available elements from
     * this queue and adds them to the given collection.  A failure
     * encountered while attempting to add elements to
     * collection {@code c} may result in elements being in neither,
     * either or both collections when the associated exception is
     * thrown.  Attempts to drain a queue to itself result in
     * {@code IllegalArgumentException}. Further, the behavior of
     * this operation is undefined if the specified collection is
     * modified while the operation is in progress.
     *
     * @param c the collection to transfer elements into
     * @param maxElements the maximum number of elements to transfer
     * @return the number of elements transferred
     * @throws SQLException 
     * @throws UnsupportedOperationException if addition of elements
     *         is not supported by the specified collection
     * @throws ClassCastException if the class of an element of this queue
     *         prevents it from being added to the specified collection
     * @throws NullPointerException if the specified collection is null
     * @throws IllegalArgumentException if the specified collection is this
     *         queue, or some property of an element of this queue prevents
     *         it from being added to the specified collection
     */
	public int drainTo(Collection<? super E> c, int maxElements) throws SQLException {
		checkNotNull(c);
		if (c == this)
			throw new IllegalArgumentException();
		if (maxElements <= 0)
			return 0;
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			final var count = count();
			final var max = (maxElements < count) ? maxElements : count;

			for (int i = 0; i < max; i++) {
				c.add(extractItem());
			}
			return max;
		} finally {
			lock.unlock();
		}
	}

    public boolean isEmpty() throws SQLException {
        return size() == 0;
    }
}
