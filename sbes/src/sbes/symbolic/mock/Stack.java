package sbes.symbolic.mock;

import java.util.Collection;
import java.util.Deque;
import java.util.EmptyStackException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import jbse.meta.Analysis;
import sbes.symbolic.CorrespondenceHandler;

/**
 * The <code>Stack</code> class represents a last-in-first-out
 * (LIFO) stack of objects. It extends class <tt>Vector</tt> with five
 * operations that allow a vector to be treated as a stack. The usual
 * <tt>push</tt> and <tt>pop</tt> operations are provided, as well as a
 * method to <tt>peek</tt> at the top item on the stack, a method to test
 * for whether the stack is <tt>empty</tt>, and a method to <tt>search</tt>
 * the stack for an item and discover how far it is from the top.
 * <p>
 * When a stack is first created, it contains no items.
 *
 * <p>A more complete and consistent set of LIFO stack operations is
 * provided by the {@link Deque} interface and its implementations, which
 * should be used in preference to this class.  For example:
 * <pre>   {@code
 *   Deque<Integer> stack = new ArrayDeque<Integer>();}</pre>
 *
 * @author  Jonathan Payne
 * @since   JDK1.0
 */
public class Stack<E> extends CorrespondenceHandler {

	// FROM ABSTRACTLIST
	/**
	 * The number of times this list has been <i>structurally modified</i>.
	 * Structural modifications are those that change the size of the
	 * list, or otherwise perturb it in such a fashion that iterations in
	 * progress may yield incorrect results.
	 *
	 * <p>This field is used by the iterator and list iterator implementation
	 * returned by the {@code iterator} and {@code listIterator} methods.
	 * If the value of this field changes unexpectedly, the iterator (or list
	 * iterator) will throw a {@code ConcurrentModificationException} in
	 * response to the {@code next}, {@code remove}, {@code previous},
	 * {@code set} or {@code add} operations.  This provides
	 * <i>fail-fast</i> behavior, rather than non-deterministic behavior in
	 * the face of concurrent modification during iteration.
	 *
	 * <p><b>Use of this field by subclasses is optional.</b> If a subclass
	 * wishes to provide fail-fast iterators (and list iterators), then it
	 * merely has to increment this field in its {@code add(int, E)} and
	 * {@code remove(int)} methods (and any other methods that it overrides
	 * that result in structural modifications to the list).  A single call to
	 * {@code add(int, E)} or {@code remove(int)} must add no more than
	 * one to this field, or the iterators (and list iterators) will throw
	 * bogus {@code ConcurrentModificationExceptions}.  If an implementation
	 * does not wish to provide fail-fast iterators, this field may be
	 * ignored.
	 */
	protected transient int modCount = 0;

	// FROM VECTOR
	/**
	 * The array buffer into which the components of the vector are
	 * stored. The capacity of the vector is the length of this array buffer,
	 * and is at least large enough to contain all the vector's elements.
	 *
	 * <p>Any array elements following the last element in the Vector are null.
	 *
	 * @serial
	 */
	protected DoubleLinkedList elementData;

	/**
	 * The number of valid components in this {@code Vector} object.
	 * Components {@code elementData[0]} through
	 * {@code elementData[elementCount-1]} are the actual items.
	 *
	 * @serial
	 */
//	protected int elementCount;

	/**
	 * The amount by which the capacity of the vector is automatically
	 * incremented when its size becomes greater than its capacity.  If
	 * the capacity increment is less than or equal to zero, the capacity
	 * of the vector is doubled each time it needs to grow.
	 *
	 * @serial
	 */
	protected int capacityIncrement;



	public static boolean mirrorInitialConservative(Stack<?> stack1, Stack<?> stack2) {
		if (!CorrespondenceHandler.doOrMayCorrespondInInitialState(stack1, stack2))
			return false;

		if (!stack1.mustVisitDuringAssume())
			return true;

		if (!CorrespondenceHandler.setAsCorrespondingInInitialState(stack1, stack2))
			return false;

		if (Analysis.isResolved(stack1, "elementData") || Analysis.isResolved(stack2, "elementData")) {
			if (stack1.elementData == null ^ stack2.elementData == null) {
				return false;
			}
			else if (stack1.elementData != null && stack2.elementData != null) {
				return DoubleLinkedList.mirrorEachOtherInitially_conservative((DoubleLinkedList) stack1.elementData, 
																				   (DoubleLinkedList) stack2.elementData);
			}
		}

		return true;
	}

	public static boolean mirrorFinalConservative(Stack<?> stack1, Stack<?> stack2) {
		if(!stack1.mustVisitDuringAssert()) return true;
		
		if (Analysis.isResolved(stack1, "elementData") || Analysis.isResolved(stack2, "elementData")) {
			if (stack1.elementData == null ^ stack2.elementData == null) {
				return false;
			}
			else if (stack1.elementData != null && stack2.elementData != null) {
				return DoubleLinkedList.mirrorEachOtherAtEnd((DoubleLinkedList) stack1.elementData,
																  (DoubleLinkedList) stack2.elementData);
			}
		}
		
		return true;
	}
	
	
	
	
	
	
	
	
	
	

	/**
	 * Constructs an empty vector with the specified initial capacity and
	 * capacity increment.
	 *
	 * @param   initialCapacity     the initial capacity of the vector
	 * @param   capacityIncrement   the amount by which the capacity is
	 *                              increased when the vector overflows
	 * @throws IllegalArgumentException if the specified initial capacity
	 *         is negative
	 */
	public Stack(int initialCapacity, int capacityIncrement) {
		if (initialCapacity < 0)
			throw new IllegalArgumentException();
		this.elementData = new DoubleLinkedList();
		this.capacityIncrement = capacityIncrement;
	}

	/**
	 * Constructs an empty vector with the specified initial capacity and
	 * with its capacity increment equal to zero.
	 *
	 * @param   initialCapacity   the initial capacity of the vector
	 * @throws IllegalArgumentException if the specified initial capacity
	 *         is negative
	 */
	public Stack(int initialCapacity) {
		this(initialCapacity, 0);
	}

	/**
	 * Constructs an empty vector so that its internal data array
	 * has size {@code 10} and its standard capacity increment is
	 * zero.
	 */
	public Stack() {
		this(10);
	}

	/**
	 * Returns the number of components in this vector.
	 *
	 * @return  the number of components in this vector
	 */
	public synchronized int size() {
		return elementData.size();
	}

	/**
	 * Tests if this vector has no components.
	 *
	 * @return  {@code true} if and only if this vector has
	 *          no components, that is, its size is zero;
	 *          {@code false} otherwise.
	 */
	public synchronized boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * Returns an enumeration of the components of this vector. The
	 * returned {@code Enumeration} object will generate all items in
	 * this vector. The first item generated is the item at index {@code 0},
	 * then the item at index {@code 1}, and so on.
	 *
	 * @return  an enumeration of the components of this vector
	 * @see     Iterator
	 */
	public Enumeration<E> elements() {
		return new Enumeration<E>() {
			int count = 0;

			public boolean hasMoreElements() {
				return count < size();
			}

			public E nextElement() {
				synchronized (Stack.this) {
					if (count < size()) {
						return elementData(count++);
					}
				}
				throw new NoSuchElementException();
			}
		};
	}

	/**
	 * Returns {@code true} if this vector contains the specified element.
	 * More formally, returns {@code true} if and only if this vector
	 * contains at least one element {@code e} such that
	 * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
	 *
	 * @param o element whose presence in this vector is to be tested
	 * @return {@code true} if this vector contains the specified element
	 */
	public boolean contains(Object o) {
		return elementData.contains(o);
	}

	/**
	 * Returns the index of the first occurrence of the specified element
	 * in this vector, or -1 if this vector does not contain the element.
	 * More formally, returns the lowest index {@code i} such that
	 * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
	 * or -1 if there is no such index.
	 *
	 * @param o element to search for
	 * @return the index of the first occurrence of the specified element in
	 *         this vector, or -1 if this vector does not contain the element
	 */
	public int indexOf(Object o) {
		return elementData.indexOf(o);
	}

	/**
	 * Returns the index of the first occurrence of the specified element in
	 * this vector, searching forwards from {@code index}, or returns -1 if
	 * the element is not found.
	 * More formally, returns the lowest index {@code i} such that
	 * <tt>(i&nbsp;&gt;=&nbsp;index&nbsp;&amp;&amp;&nbsp;(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i))))</tt>,
	 * or -1 if there is no such index.
	 *
	 * @param o element to search for
	 * @param index index to start searching from
	 * @return the index of the first occurrence of the element in
	 *         this vector at position {@code index} or later in the vector;
	 *         {@code -1} if the element is not found.
	 * @throws IndexOutOfBoundsException if the specified index is negative
	 * @see     Object#equals(Object)
	 */
//	public synchronized int indexOf(Object o, int index) {
//		if (o == null) {
//			for (int i = index ; i < elementCount ; i++)
//				if (elementData[i]==null)
//					return i;
//		} else {
//			for (int i = index ; i < elementCount ; i++)
//				if (o.equals(elementData[i]))
//					return i;
//		}
//		return -1;
//	}

	/**
	 * Returns the index of the last occurrence of the specified element
	 * in this vector, or -1 if this vector does not contain the element.
	 * More formally, returns the highest index {@code i} such that
	 * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
	 * or -1 if there is no such index.
	 *
	 * @param o element to search for
	 * @return the index of the last occurrence of the specified element in
	 *         this vector, or -1 if this vector does not contain the element
	 */
	public synchronized int lastIndexOf(Object o) {
		return elementData.lastIndexOf(o);
	}

	/**
	 * Returns the index of the last occurrence of the specified element in
	 * this vector, searching backwards from {@code index}, or returns -1 if
	 * the element is not found.
	 * More formally, returns the highest index {@code i} such that
	 * <tt>(i&nbsp;&lt;=&nbsp;index&nbsp;&amp;&amp;&nbsp;(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i))))</tt>,
	 * or -1 if there is no such index.
	 *
	 * @param o element to search for
	 * @param index index to start searching backwards from
	 * @return the index of the last occurrence of the element at position
	 *         less than or equal to {@code index} in this vector;
	 *         -1 if the element is not found.
	 * @throws IndexOutOfBoundsException if the specified index is greater
	 *         than or equal to the current size of this vector
	 */
//	public synchronized int lastIndexOf(Object o, int index) {
//		if (index >= elementCount)
//			throw new IndexOutOfBoundsException(index + " >= "+ elementCount);
//
//		if (o == null) {
//			for (int i = index; i >= 0; i--)
//				if (elementData[i]==null)
//					return i;
//		} else {
//			for (int i = index; i >= 0; i--)
//				if (o.equals(elementData[i]))
//					return i;
//		}
//		return -1;
//	}

	/**
	 * Returns the component at the specified index.
	 *
	 * <p>This method is identical in functionality to the {@link #get(int)}
	 * method (which is part of the {@link List} interface).
	 *
	 * @param      index   an index into this vector
	 * @return     the component at the specified index
	 * @throws ArrayIndexOutOfBoundsException if the index is out of range
	 *         ({@code index < 0 || index >= size()})
	 */
	public synchronized E elementAt(int index) {
		if (index >= size()) {
			throw new ArrayIndexOutOfBoundsException();
		}

		return elementData(index);
	}

	/**
	 * Returns the first component (the item at index {@code 0}) of
	 * this vector.
	 *
	 * @return     the first component of this vector
	 * @throws NoSuchElementException if this vector has no components
	 */
	public synchronized E firstElement() {
		if (size() == 0) {
			throw new NoSuchElementException();
		}
		return elementData(0);
	}

	/**
	 * Returns the last component of the vector.
	 *
	 * @return  the last component of the vector, i.e., the component at index
	 *          <code>size()&nbsp;-&nbsp;1</code>.
	 * @throws NoSuchElementException if this vector is empty
	 */
	public synchronized E lastElement() {
		if (size() == 0) {
			throw new NoSuchElementException();
		}
		return elementData(size() - 1);
	}

	/**
	 * Sets the component at the specified {@code index} of this
	 * vector to be the specified object. The previous component at that
	 * position is discarded.
	 *
	 * <p>The index must be a value greater than or equal to {@code 0}
	 * and less than the current size of the vector.
	 *
	 * <p>This method is identical in functionality to the
	 * {@link #set(int, Object) set(int, E)}
	 * method (which is part of the {@link List} interface). Note that the
	 * {@code set} method reverses the order of the parameters, to more closely
	 * match array usage.  Note also that the {@code set} method returns the
	 * old value that was stored at the specified position.
	 *
	 * @param      obj     what the component is to be set to
	 * @param      index   the specified index
	 * @throws ArrayIndexOutOfBoundsException if the index is out of range
	 *         ({@code index < 0 || index >= size()})
	 */
	public synchronized void setElementAt(E obj, int index) {
		if (index < 0 || index >= size()) {
			throw new ArrayIndexOutOfBoundsException();
		}
		elementData.set(index, obj);
	}

	/**
	 * Deletes the component at the specified index. Each component in
	 * this vector with an index greater or equal to the specified
	 * {@code index} is shifted downward to have an index one
	 * smaller than the value it had previously. The size of this vector
	 * is decreased by {@code 1}.
	 *
	 * <p>The index must be a value greater than or equal to {@code 0}
	 * and less than the current size of the vector.
	 *
	 * <p>This method is identical in functionality to the {@link #remove(int)}
	 * method (which is part of the {@link List} interface).  Note that the
	 * {@code remove} method returns the old value that was stored at the
	 * specified position.
	 *
	 * @param      index   the index of the object to remove
	 * @throws ArrayIndexOutOfBoundsException if the index is out of range
	 *         ({@code index < 0 || index >= size()})
	 */
	public synchronized void removeElementAt(int index) {
		modCount++;
		if (index >= size()) {
			throw new ArrayIndexOutOfBoundsException();
		}
		else if (index < 0) {
			throw new ArrayIndexOutOfBoundsException();
		}
		elementData.remove(index);
	}

	/**
	 * Inserts the specified object as a component in this vector at the
	 * specified {@code index}. Each component in this vector with
	 * an index greater or equal to the specified {@code index} is
	 * shifted upward to have an index one greater than the value it had
	 * previously.
	 *
	 * <p>The index must be a value greater than or equal to {@code 0}
	 * and less than or equal to the current size of the vector. (If the
	 * index is equal to the current size of the vector, the new element
	 * is appended to the Vector.)
	 *
	 * <p>This method is identical in functionality to the
	 * {@link #add(int, Object) add(int, E)}
	 * method (which is part of the {@link List} interface).  Note that the
	 * {@code add} method reverses the order of the parameters, to more closely
	 * match array usage.
	 *
	 * @param      obj     the component to insert
	 * @param      index   where to insert the new component
	 * @throws ArrayIndexOutOfBoundsException if the index is out of range
	 *         ({@code index < 0 || index > size()})
	 */
	public synchronized void insertElementAt(E obj, int index) {
		modCount++;
		if (index > size()) {
			throw new ArrayIndexOutOfBoundsException();
		}
		elementData.add(index, obj);
	}

	/**
	 * Adds the specified component to the end of this vector,
	 * increasing its size by one. The capacity of this vector is
	 * increased if its size becomes greater than its capacity.
	 *
	 * <p>This method is identical in functionality to the
	 * {@link #add(Object) add(E)}
	 * method (which is part of the {@link List} interface).
	 *
	 * @param   obj   the component to be added
	 */
	public synchronized void addElement(E obj) {
		modCount++;
		elementData.add(obj);
	}

	/**
	 * Removes the first (lowest-indexed) occurrence of the argument
	 * from this vector. If the object is found in this vector, each
	 * component in the vector with an index greater or equal to the
	 * object's index is shifted downward to have an index one smaller
	 * than the value it had previously.
	 *
	 * <p>This method is identical in functionality to the
	 * {@link #remove(Object)} method (which is part of the
	 * {@link List} interface).
	 *
	 * @param   obj   the component to be removed
	 * @return  {@code true} if the argument was a component of this
	 *          vector; {@code false} otherwise.
	 */
	public synchronized boolean removeElement(Object obj) {
		modCount++;
		int i = indexOf(obj);
		if (i >= 0) {
			removeElementAt(i);
			return true;
		}
		return false;
	}

	/**
	 * Removes all components from this vector and sets its size to zero.
	 *
	 * <p>This method is identical in functionality to the {@link #clear}
	 * method (which is part of the {@link List} interface).
	 */
	public synchronized void removeAllElements() {
		modCount++;
		elementData.clear();
	}

	/**
	 * Returns a clone of this vector. The copy will contain a
	 * reference to a clone of the internal data array, not a reference
	 * to the original internal data array of this {@code Vector} object.
	 *
	 * @return  a clone of this vector
	 */
//	public synchronized Object clone() {
//		try {
//			@SuppressWarnings("unchecked")
//			Stack<E> v = (Stack<E>) super.clone();
//			v.elementData = Arrays.copyOf(elementData, elementCount);
//			v.modCount = 0;
//			return v;
//		} catch (CloneNotSupportedException e) {
//			// this shouldn't happen, since we are Cloneable
//			throw new InternalError();
//		}
//	}


	// Positional Access Operations

	@SuppressWarnings("unchecked")
	E elementData(int index) {
		return (E) elementData.get(index);
	}

	/**
	 * Returns the element at the specified position in this Vector.
	 *
	 * @param index index of the element to return
	 * @return object at the specified index
	 * @throws ArrayIndexOutOfBoundsException if the index is out of range
	 *            ({@code index < 0 || index >= size()})
	 * @since 1.2
	 */
	public synchronized E get(int index) {
		if (index < 0 || index >= size())
			throw new ArrayIndexOutOfBoundsException();

		return elementData(index);
	}

	/**
	 * Replaces the element at the specified position in this Vector with the
	 * specified element.
	 *
	 * @param index index of the element to replace
	 * @param element element to be stored at the specified position
	 * @return the element previously at the specified position
	 * @throws ArrayIndexOutOfBoundsException if the index is out of range
	 *         ({@code index < 0 || index >= size()})
	 * @since 1.2
	 */
	public synchronized E set(int index, E element) {
		if (index < 0 || index >= size())
			throw new ArrayIndexOutOfBoundsException();

		E oldValue = elementData(index);
		elementData.set(index, element);
		return oldValue;
	}

	/**
	 * Appends the specified element to the end of this Vector.
	 *
	 * @param e element to be appended to this Vector
	 * @return {@code true} (as specified by {@link Collection#add})
	 * @since 1.2
	 */
	public synchronized boolean add(E e) {
		modCount++;
		elementData.add(e);
		return true;
	}

	/**
	 * Removes the first occurrence of the specified element in this Vector
	 * If the Vector does not contain the element, it is unchanged.  More
	 * formally, removes the element with the lowest index i such that
	 * {@code (o==null ? get(i)==null : o.equals(get(i)))} (if such
	 * an element exists).
	 *
	 * @param o element to be removed from this Vector, if present
	 * @return true if the Vector contained the specified element
	 * @since 1.2
	 */
	public boolean remove(Object o) {
		return removeElement(o);
	}

	/**
	 * Inserts the specified element at the specified position in this Vector.
	 * Shifts the element currently at that position (if any) and any
	 * subsequent elements to the right (adds one to their indices).
	 *
	 * @param index index at which the specified element is to be inserted
	 * @param element element to be inserted
	 * @throws ArrayIndexOutOfBoundsException if the index is out of range
	 *         ({@code index < 0 || index > size()})
	 * @since 1.2
	 */
	public void add(int index, E element) {
		insertElementAt(element, index);
	}

	/**
	 * Removes the element at the specified position in this Vector.
	 * Shifts any subsequent elements to the left (subtracts one from their
	 * indices).  Returns the element that was removed from the Vector.
	 *
	 * @throws ArrayIndexOutOfBoundsException if the index is out of range
	 *         ({@code index < 0 || index >= size()})
	 * @param index the index of the element to be removed
	 * @return element that was removed
	 * @since 1.2
	 */
	public synchronized E remove(int index) {
		modCount++;
		if (index >= size())
			throw new ArrayIndexOutOfBoundsException();
		E oldValue = elementData(index);

		elementData.remove(index);
		
		return oldValue;
	}

	/**
	 * Removes all of the elements from this Vector.  The Vector will
	 * be empty after this call returns (unless it throws an exception).
	 *
	 * @since 1.2
	 */
	public void clear() {
		removeAllElements();
	}

	// Bulk Operations

	/**
	 * Returns true if this Vector contains all of the elements in the
	 * specified Collection.
	 *
	 * @param   c a collection whose elements will be tested for containment
	 *          in this Vector
	 * @return true if this Vector contains all of the elements in the
	 *         specified collection
	 * @throws NullPointerException if the specified collection is null
	 */
	public synchronized boolean containsAll(Collection<?> c) {
		for (Object e : c)
			if (!contains(e))
				return false;
		return true;
	}

	/**
	 * Appends all of the elements in the specified Collection to the end of
	 * this Vector, in the order that they are returned by the specified
	 * Collection's Iterator.  The behavior of this operation is undefined if
	 * the specified Collection is modified while the operation is in progress.
	 * (This implies that the behavior of this call is undefined if the
	 * specified Collection is this Vector, and this Vector is nonempty.)
	 *
	 * @param c elements to be inserted into this Vector
	 * @return {@code true} if this Vector changed as a result of the call
	 * @throws NullPointerException if the specified collection is null
	 * @since 1.2
	 */
	public synchronized boolean addAll(DoubleLinkedList c) {
		modCount++;
		Object[] a = c.toArray();
		int numNew = a.length;
		int oldSize = elementData.size();
		for (int i = 0; i < a.length; i++) {
			elementData.add(oldSize + i, a[i]);
		}
		return numNew != 0;
	}

	/**
	 * Removes from this Vector all of its elements that are contained in the
	 * specified Collection.
	 *
	 * @param c a collection of elements to be removed from the Vector
	 * @return true if this Vector changed as a result of the call
	 * @throws ClassCastException if the types of one or more elements
	 *         in this vector are incompatible with the specified
	 *         collection (optional)
	 * @throws NullPointerException if this vector contains one or more null
	 *         elements and the specified collection does not support null
	 *         elements (optional), or if the specified collection is null
	 * @since 1.2
	 */
	public synchronized boolean removeAll(DoubleLinkedList c) {
//		Objects.requireNonNull(c);
		boolean modified = false;
		for (int i = 0; i < c.size(); i++) {
			if (elementData.remove(c.get(i))) {
				modified = true;
			}
		}
		if (elementData.size() == 0) {
			elementData.clear(); //FIXME: manca una regola LICS per l'allineamento dei puntatori quando si svuota la lista
		}
		return modified;
	}

	/**
	 * Retains only the elements in this Vector that are contained in the
	 * specified Collection.  In other words, removes from this Vector all
	 * of its elements that are not contained in the specified Collection.
	 *
	 * @param c a collection of elements to be retained in this Vector
	 *          (all other elements are removed)
	 * @return true if this Vector changed as a result of the call
	 * @throws ClassCastException if the types of one or more elements
	 *         in this vector are incompatible with the specified
	 *         collection (optional)
	 * @throws NullPointerException if this vector contains one or more null
	 *         elements and the specified collection does not support null
	 *         elements (optional), or if the specified collection is null
	 * @since 1.2
	 */
	public synchronized boolean retainAll(DoubleLinkedList c)  {
//		Objects.requireNonNull(c);
		boolean modified = false;
		for (int i = 0; i < elementData.size();) {
			if (c.contains(elementData.get(i))) {
				i++;
			}
			else {
				elementData.remove(i);
				modified = true;
			}
		}
		if (elementData.size() == 0) {
			elementData.clear(); //FIXME: manca una regola LICS per l'allineamento dei puntatori quando si svuota la lista
		}
		return modified;
	}

	/**
	 * Inserts all of the elements in the specified Collection into this
	 * Vector at the specified position.  Shifts the element currently at
	 * that position (if any) and any subsequent elements to the right
	 * (increases their indices).  The new elements will appear in the Vector
	 * in the order that they are returned by the specified Collection's
	 * iterator.
	 *
	 * @param index index at which to insert the first element from the
	 *              specified collection
	 * @param c elements to be inserted into this Vector
	 * @return {@code true} if this Vector changed as a result of the call
	 * @throws ArrayIndexOutOfBoundsException if the index is out of range
	 *         ({@code index < 0 || index > size()})
	 * @throws NullPointerException if the specified collection is null
	 * @since 1.2
	 */
	public synchronized boolean addAll(int index, Collection<? extends E> c) {
		modCount++;
		if (index < 0 || index > size())
			throw new ArrayIndexOutOfBoundsException();

		Object[] a = c.toArray();
		int numNew = a.length;
		
		for (int i = 0; i < a.length; i++) {
			elementData.add(index + i, a[i]);
		}
		
		return numNew != 0;
	}

	/**
	 * Compares the specified Object with this Vector for equality.  Returns
	 * true if and only if the specified Object is also a List, both Lists
	 * have the same size, and all corresponding pairs of elements in the two
	 * Lists are <em>equal</em>.  (Two elements {@code e1} and
	 * {@code e2} are <em>equal</em> if {@code (e1==null ? e2==null :
	 * e1.equals(e2))}.)  In other words, two Lists are defined to be
	 * equal if they contain the same elements in the same order.
	 *
	 * @param o the Object to be compared for equality with this Vector
	 * @return true if the specified Object is equal to this Vector
	 */
	public synchronized boolean equals(Object o) {
		return super.equals(o);
	}

	/**
	 * Returns the hash code value for this Vector.
	 */
	public synchronized int hashCode() {
		return super.hashCode();
	}

	/**
	 * Returns a string representation of this Vector, containing
	 * the String representation of each element.
	 */
	public synchronized String toString() {
		return super.toString();
	}

	/**
	 * Removes from this list all of the elements whose index is between
	 * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.
	 * Shifts any succeeding elements to the left (reduces their index).
	 * This call shortens the list by {@code (toIndex - fromIndex)} elements.
	 * (If {@code toIndex==fromIndex}, this operation has no effect.)
	 */
	protected synchronized void removeRange(int fromIndex, int toIndex) {
		modCount++;
		if (fromIndex > toIndex) {
			throw new ArrayIndexOutOfBoundsException();
		}
		
		for (int i = fromIndex; i < toIndex; i++) {
			elementData.remove(fromIndex);
		}
	}

	/**
	 * Returns a list iterator over the elements in this list (in proper
	 * sequence), starting at the specified position in the list.
	 * The specified index indicates the first element that would be
	 * returned by an initial call to {@link ListIterator#next next}.
	 * An initial call to {@link ListIterator#previous previous} would
	 * return the element with the specified index minus one.
	 *
	 * <p>The returned list iterator is <a href="#fail-fast"><i>fail-fast</i></a>.
	 *
	 * @throws IndexOutOfBoundsException {@inheritDoc}
	 */
//	public synchronized ListIterator<E> listIterator(int index) {
//		if (index < 0 || index > size())
//			throw new IndexOutOfBoundsException();
//		return new ListItr(index);
//	}

	/**
	 * Returns a list iterator over the elements in this list (in proper
	 * sequence).
	 *
	 * <p>The returned list iterator is <a href="#fail-fast"><i>fail-fast</i></a>.
	 *
	 * @see #listIterator(int)
	 */
//	public synchronized ListIterator<E> listIterator() {
//		return new ListItr(0);
//	}

	/**
	 * Returns an iterator over the elements in this list in proper sequence.
	 *
	 * <p>The returned iterator is <a href="#fail-fast"><i>fail-fast</i></a>.
	 *
	 * @return an iterator over the elements in this list in proper sequence
	 */
//	public synchronized Iterator<E> iterator() {
//		return new Itr();
//	}

	/**
	 * An optimized version of AbstractList.Itr
	 */
//	private class Itr implements Iterator<E> {
//		int cursor;       // index of next element to return
//		int lastRet = -1; // index of last element returned; -1 if no such
//		int expectedModCount = modCount;
//
//		public boolean hasNext() {
//			// Racy but within spec, since modifications are checked
//			// within or after synchronization in next/previous
//			return cursor != size();
//		}
//
//		public E next() {
//			synchronized (Stack.this) {
//				checkForComodification();
//				int i = cursor;
//				if (i >= size())
//					throw new NoSuchElementException();
//				cursor = i + 1;
//				return elementData(lastRet = i);
//			}
//		}
//
//		public void remove() {
//			if (lastRet == -1)
//				throw new IllegalStateException();
//			synchronized (Stack.this) {
//				checkForComodification();
//				Stack.this.remove(lastRet);
//				expectedModCount = modCount;
//			}
//			cursor = lastRet;
//			lastRet = -1;
//		}
//
//		final void checkForComodification() {
//			if (modCount != expectedModCount)
//				throw new ConcurrentModificationException();
//		}
//	}

	/**
	 * An optimized version of AbstractList.ListItr
	 */
//	final class ListItr extends Itr implements ListIterator<E> {
//		ListItr(int index) {
//			super();
//			cursor = index;
//		}
//
//		public boolean hasPrevious() {
//			return cursor != 0;
//		}
//
//		public int nextIndex() {
//			return cursor;
//		}
//
//		public int previousIndex() {
//			return cursor - 1;
//		}
//
//		public E previous() {
//			synchronized (Stack.this) {
//				checkForComodification();
//				int i = cursor - 1;
//				if (i < 0)
//					throw new NoSuchElementException();
//				cursor = i;
//				return elementData(lastRet = i);
//			}
//		}
//
//		public void set(E e) {
//			if (lastRet == -1)
//				throw new IllegalStateException();
//			synchronized (Stack.this) {
//				checkForComodification();
//				Stack.this.set(lastRet, e);
//			}
//		}
//
//		public void add(E e) {
//			int i = cursor;
//			synchronized (Stack.this) {
//				checkForComodification();
//				Stack.this.add(i, e);
//				expectedModCount = modCount;
//			}
//			cursor = i + 1;
//			lastRet = -1;
//		}
//	}

	/**
	 * Pushes an item onto the top of this stack. This has exactly
	 * the same effect as:
	 * <blockquote><pre>
	 * addElement(item)</pre></blockquote>
	 *
	 * @param   item   the item to be pushed onto this stack.
	 * @return  the <code>item</code> argument.
	 * @see     java.util.Vector#addElement
	 */
	public E push(E item) {
		addElement(item);

		return item;
	}

	/**
	 * Removes the object at the top of this stack and returns that
	 * object as the value of this function.
	 *
	 * @return     The object at the top of this stack (the last item
	 *             of the <tt>Vector</tt> object).
	 * @exception  EmptyStackException  if this stack is empty.
	 */
	public synchronized E pop() {
		E       obj;
		int     len = size();

		obj = peek();
		removeElementAt(len - 1);

		return obj;
	}

	/**
	 * Looks at the object at the top of this stack without removing it
	 * from the stack.
	 *
	 * @return     the object at the top of this stack (the last item
	 *             of the <tt>Vector</tt> object).
	 * @exception  EmptyStackException  if this stack is empty.
	 */
	public synchronized E peek() {
		int     len = size();

		if (len == 0)
			throw new EmptyStackException();
		return elementAt(len - 1);
	}

	/**
	 * Tests if this stack is empty.
	 *
	 * @return  <code>true</code> if and only if this stack contains
	 *          no items; <code>false</code> otherwise.
	 */
	public boolean empty() {
		return size() == 0;
	}

	/**
	 * Returns the 1-based position where an object is on this stack.
	 * If the object <tt>o</tt> occurs as an item in this stack, this
	 * method returns the distance from the top of the stack of the
	 * occurrence nearest the top of the stack; the topmost item on the
	 * stack is considered to be at distance <tt>1</tt>. The <tt>equals</tt>
	 * method is used to compare <tt>o</tt> to the
	 * items in this stack.
	 *
	 * @param   o   the desired object.
	 * @return  the 1-based position from the top of the stack where
	 *          the object is located; the return value <code>-1</code>
	 *          indicates that the object is not on the stack.
	 */
	public synchronized int search(Object o) {
		int i = lastIndexOf(o);

		if (i >= 0) {
			return size() - i;
		}
		return -1;
	}
	
	public Object[] toArray() {
		if (elementData == null) {
			return null;
		}
		return elementData.toArray();
	}
	
}