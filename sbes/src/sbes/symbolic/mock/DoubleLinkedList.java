package sbes.symbolic.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Vector;

import jbse.meta.Analysis;
import sbes.symbolic.CorrespondenceHandler;


/**
 * Linked list implementation of the <tt>List</tt> interface. Implements all
 * optional list operations, and permits all elements (including <tt>null</tt>).
 * In addition to implementing the <tt>List</tt> interface, the
 * <tt>DoubleLinkedList</tt> class provides uniformly named methods to
 * <tt>get</tt>, <tt>remove</tt> and <tt>insert</tt> an element at the beginning
 * and end of the list. These operations allow linked lists to be used as a
 * stack, queue, or double-ended queue (deque).
 * <p>
 *
 * All of the stack/queue/deque operations could be easily recast in terms of
 * the standard list operations. They're included here primarily for
 * convenience, though they may run slightly faster than the equivalent List
 * operations.
 * <p>
 *
 * All of the operations perform as could be expected for a doubly-linked list.
 * Operations that index into the list will traverse the list from the begining
 * or the end, whichever is closer to the specified index.
 * <p>
 *
 * <b>Note that this implementation is not synchronized.</b> If multiple threads
 * access a list concurrently, and at least one of the threads modifies the list
 * structurally, it <i>must</i> be synchronized externally. (A structural
 * modification is any operation that adds or deletes one or more elements;
 * merely setting the value of an element is not a structural modification.)
 * This is typically accomplished by synchronizing on some object that naturally
 * encapsulates the list. If no such object exists, the list should be "wrapped"
 * using the Collections.synchronizedList method. This is best done at creation
 * time, to prevent accidental unsynchronized access to the list:
 *
 * <pre>
 *     List list = Collections.synchronizedList(new DoubleLinkedList(...));
 * </pre>
 * <p>
 *
 * The iterators returned by the this class's <tt>iterator</tt> and
 * <tt>listIterator</tt> methods are <i>fail-fast</i>: if the list is
 * structurally modified at any time after the iterator is created, in any way
 * except through the Iterator's own <tt>remove</tt> or <tt>add</tt> methods,
 * the iterator will throw a <tt>ConcurrentModificationException</tt>. Thus, in
 * the face of concurrent modification, the iterator fails quickly and cleanly,
 * rather than risking arbitrary, non-deterministic behavior at an undetermined
 * time in the future.
 *
 * <p>
 * Note that the fail-fast behavior of an iterator cannot be guaranteed as it
 * is, generally speaking, impossible to make any hard guarantees in the
 * presence of unsynchronized concurrent modification. Fail-fast iterators throw
 * <tt>ConcurrentModificationException</tt> on a best-effort basis. Therefore,
 * it would be wrong to write a program that depended on this exception for its
 * correctness: <i>the fail-fast behavior of iterators should be used only to
 * detect bugs.</i>
 * <p>
 *
 * This class is a member of the <a href="{@docRoot}
 * /../guide/collections/index.html"> Java Collections Framework</a>.
 *
 * @author Josh Bloch
 * @version 1.46, 01/23/03
 * @see List
 * @see ArrayList
 * @see Vector
 * @see Collections#synchronizedList(List)
 * @since 1.2
 */

public class DoubleLinkedList  extends CorrespondenceHandler {
	//@invariant repOk();
	
	protected transient int modCount = 0;
//INSTRUMENTATION BEGIN
    //private transient Entry header = new Entry(null, null, null);
    private transient Entry header = new Entry(null, null, null, this);
//INSTRUMENTATION END
    private transient int size = 0;
//INSTRUMENTATION BEGIN
    /*
     * Shadow fields for initial values 
     */
    private int 	_initialSize;
    
	/*
     * Other instrumentation variables
     */
    private int _minSize;
    
    /*
     * Triggers
     */

    @SuppressWarnings("unused")
    private static void _got_DoublyLinkedList_LICS(DoubleLinkedList l) {
    	l._initialSize = l.size;
    	//rep invariant for LinkedList.size
    	l._minSize = 0;
    	Analysis.assume(l._initialSize >= l._minSize); 
    }
//INSTRUMENTATION END

    /**
     * Constructs an empty list.
     */
    public DoubleLinkedList() {
        header.next = header.previous = header;
//INSTRUMENTATION BEGIN
        //initializates instrumentation fields for concrete objects
        _initialSize = this.size;
        _minSize = 0;
//INSTRUMENTATION END
    }

    /**
     * Constructs a list containing the elements of the specified collection, in
     * the order they are returned by the collection's iterator.
     *
     * @param c
     *            the collection whose elements are to be placed into this list.
     * @throws NullPointerException
     *             if the specified collection is null.
     */
    public DoubleLinkedList(DoubleLinkedList c) {
        this();
        addAll(c);
    }

//STUB BEGIN
    public
//STUB END
    boolean repOK() {
        if (header == null) {
            return false;
        }
        Entry tmp = header;
        int i = 0;
        do {
            if (!tmp.nonNullPointers() || !tmp.repOK()) {
                return false;
            }
            tmp = tmp.next;
            if (tmp != header) {
                ++i;
//STUB BEGIN
                //acceleration
                if (i > size) { 
                	return false;
                }
//STUB END
            }
        } while (tmp != header);
        tmp = header;
        return i == size;
    }
     
     public boolean test(DoubleLinkedList dll, Object o) {
         dll.add(o);
         return dll.getLast() == o;
     }

     boolean inList(Entry e) {
         if (header == e) {
             return true;
         }
         Entry tmp = header.next;
         while (tmp != header) {
             if (tmp == e) {
                 return true;
             }
             tmp = tmp.next;
         }
         return false;
     }

     /**
      * Returns the first element in this list.
      *
      * @return the first element in this list.
      * @throws NoSuchElementException
      *             if this list is empty.
      */
     //@ requires size > 0;
     public Object getFirst() {
         if (size == 0)
             throw new NoSuchElementException();

         return header.next.element;
     }

     /**
      * Returns the last element in this list.
      *
      * @return the last element in this list.
      * @throws NoSuchElementException
      *             if this list is empty.
      */
     public Object getLast() {
         if (size == 0)
             throw new NoSuchElementException();

         return header.previous.element;
     }

     /**
      * Removes and returns the first element from this list.
      *
      * @return the first element from this list.
      * @throws NoSuchElementException
      *             if this list is empty.
      */
     public Object removeFirst() {
         Object first = header.next.element;
         remove(header.next);
         return first;
     }

     /**
      * Removes and returns the last element from this list.
      *
      * @return the last element from this list.
      * @throws NoSuchElementException
      *             if this list is empty.
      */
     public Object removeLast() {
         Object last = header.previous.element;
         remove(header.previous);
         return last;
     }

     /**
      * Inserts the given element at the beginning of this list.
      *
      * @param o
      *            the element to be inserted at the beginning of this list.
      */
     public void addFirst(Object o) {
         addBefore(o, header.next);
     }

     /**
      * Appends the given element to the end of this list. (Identical in function
      * to the <tt>add</tt> method; included only for consistency.)
      *
      * @param o
      *            the element to be inserted at the end of this list.
      */
     public void addLast(Object o) {
         addBefore(o, header);
     }

     /**
      * Returns <tt>true</tt> if this list contains the specified element. More
      * formally, returns <tt>true</tt> if and only if this list contains at
      * least one element <tt>e</tt> such that <tt>(o==null ? e==null
      * : o.equals(e))</tt>.
      *
      * @param o
      *            element whose presence in this list is to be tested.
      * @return <tt>true</tt> if this list contains the specified element.
      */
     public boolean contains(Object o) {
         return indexOf(o) != -1;
     }

     /**
      * Returns the number of elements in this list.
      *
      * @return the number of elements in this list.
      */
     public int size() {
         return size;
     }

     /**
      * Appends the specified element to the end of this list.
      *
      * @param o
      *            element to be appended to this list.
      * @return <tt>true</tt> (as per the general contract of
      *         <tt>Collection.add</tt>).
      */
     public boolean add(Object o) {
         addBefore(o, header);
         return true;
     }

     /**
      * Removes the first occurrence of the specified element in this list. If
      * the list does not contain the element, it is unchanged. More formally,
      * removes the element with the lowest index <tt>i</tt> such that
      * <tt>(o==null ? get(i)==null : o.equals(get(i)))</tt> (if such an element
      * exists).
      *
      * @param o
      *            element to be removed from this list, if present.
      * @return <tt>true</tt> if the list contained the specified element.
      */
     public boolean remove(Object o) {
         if (o == null) {
             for (Entry e = header.next; e != header; e = e.next) {
                 if (e.element == null) {
                     remove(e);
                     return true;
                 }
             }
         } else {
             for (Entry e = header.next; e != header; e = e.next) {
                 if (o == e.element) {
                     // if (o.equals(e.element)) {
                     remove(e);
                     return true;
                 }
             }
         }
         return false;
     }

     /**
      * Appends all of the elements in the specified collection to the end of
      * this list, in the order that they are returned by the specified
      * collection's iterator. The behavior of this operation is undefined if the
      * specified collection is modified while the operation is in progress.
      * (This implies that the behavior of this call is undefined if the
      * specified Collection is this list, and this list is nonempty.)
      *
      * @param c
      *            the elements to be inserted into this list.
      * @return <tt>true</tt> if this list changed as a result of the call.
      * @throws NullPointerException
      *             if the specified collection is null.
      */
//STUB BEGIN
     //public boolean addAll(Collection c) {
     public boolean addAll(DoubleLinkedList c) {
//STUB END
         return addAll(size, c);
     }

     /**
      * Inserts all of the elements in the specified collection into this list,
      * starting at the specified position. Shifts the element currently at that
      * position (if any) and any subsequent elements to the right (increases
      * their indices). The new elements will appear in the list in the order
      * that they are returned by the specified collection's iterator.
      *
      * @param index
      *            index at which to insert first element from the specified
      *            collection.
      * @param c
      *            elements to be inserted into this list.
      * @return <tt>true</tt> if this list changed as a result of the call.
      * @throws IndexOutOfBoundsException
      *             if the specified index is out of range (
      *             <tt>index &lt; 0 || index &gt; size()</tt>).
      * @throws NullPointerException
      *             if the specified collection is null.
      */
//STUB BEGIN
     //public boolean addAll(int index, Collection c) {
     public boolean addAll(int index, DoubleLinkedList c) {
//STUB END
         Object[] a = c.toArray();
         int numNew = a.length;
         if (numNew == 0)
             return false;
         modCount++;

         Entry successor = (index == size ? header : entry(index));
         Entry predecessor = successor.previous;
         for (int i = 0; i < numNew; i++) {
//INSTRUMENTATION BEGIN
             //Entry e = new Entry(a[i], successor, predecessor);
             Entry e = new Entry(a[i], successor, predecessor, this);
//INSTRUMENTATION END
             predecessor.next = e;
             predecessor = e;
         }
         successor.previous = predecessor;

         size += numNew;
         return true;
     }

     /**
      * Removes all of the elements from this list.
      */
     //@ ensures size == 0;
     public void clear() {
         modCount++;
         header.next = header.previous = header;
         size = 0;
     }
     
     public void retainAll(DoubleLinkedList c) {
    	 modCount++;
    	 for (int i = 0; i < size();) {
    		 if (c.contains(get(i))) {
    			 i++;
    		 }
    		 else {
    			 remove(i);
    		 }
    	 }
    	 if (size() == 0) {
    		 Analysis.assume(header.next == header);
    		 Analysis.assume(header.previous == header);
    	 }
     }

    // Positional Access Operations

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index index of element to return.
     * @return the element at the specified position in this list.
     * 
     * @throws IndexOutOfBoundsException if the specified index is is out of
     * range (<tt>index &lt; 0 || index &gt;= size()</tt>).
     */
    public Object get(int index) {
        return entry(index).element;
    }

    /**
     * Replaces the element at the specified position in this list with the
     * specified element.
     *
     * @param index index of element to replace.
     * @param element element to be stored at the specified position.
     * @return the element previously at the specified position.
     * @throws IndexOutOfBoundsException if the specified index is out of
     *		  range (<tt>index &lt; 0 || index &gt;= size()</tt>).
     */
    public Object set(int index, Object element) {
        Entry e = entry(index);
        Object oldVal = e.element;
        e.element = element;
        return oldVal;
    }

    /**
     * Inserts the specified element at the specified position in this list.
     * Shifts the element currently at that position (if any) and any
     * subsequent elements to the right (adds one to their indices).
     *
     * @param index index at which the specified element is to be inserted.
     * @param element element to be inserted.
     * 
     * @throws IndexOutOfBoundsException if the specified index is out of
     *		  range (<tt>index &lt; 0 || index &gt; size()</tt>).
     */
    public void add(int index, Object element) {
        addBefore(element, (index == size ? header : entry(index)));
    }

    /**
     * Removes the element at the specified position in this list.  Shifts any
     * subsequent elements to the left (subtracts one from their indices).
     * Returns the element that was removed from the list.
     *
     * @param index the index of the element to removed.
     * @return the element previously at the specified position.
     * 
     * @throws IndexOutOfBoundsException if the specified index is out of
     * 		  range (<tt>index &lt; 0 || index &gt;= size()</tt>).
     */
    public Object remove(int index) {
        Entry e = entry(index);
        remove(e);
        return e.element;
    }

    /**
     * Return the indexed entry.
     */
    private Entry entry(int index) {
        if (index < 0 || index >= size)
//STUB BEGIN
//JBSE cannot handle concatenation of strings and symbolic integers
            /*throw new IndexOutOfBoundsException("Index: " + index + ", Size: "
                                                + size);*/
        	throw new IndexOutOfBoundsException();
//STUB END
        Entry e = header;
        // if (index < (size >> 1)) { // Kiasan can not handle bit shifting
        // currently.
//		if (index < (size / 2)) {
			for (int i = 0; i <= index; i++)
				e = e.next;
//		} else {
//			for (int i = size; i > index; i--)
//				e = e.previous;
//		}
 
        return e;
    }

    // Search Operations

    /**
     * Returns the index in this list of the first occurrence of the specified
     * element, or -1 if the List does not contain this element. More formally,
     * returns the lowest index i such that
     * <tt>(o==null ? get(i)==null : o.equals(get(i)))</tt>, or -1 if there is
     * no such index.
     *
     * @param o
     *            element to search for.
     * @return the index in this list of the first occurrence of the specified
     *         element, or -1 if the list does not contain this element.
     */
    public int indexOf(Object o) {
        int index = 0;
        if (o == null) {
            for (Entry e = header.next; e != header; e = e.next) {
                if (e.element == null)
                    return index;
                index++;
            }
        } else {
            for (Entry e = header.next; e != header; e = e.next) {
                if (o.equals(e.element))
                    return index;
                index++;
            }
        }
        return -1;
    }

    /**
     * Returns the index in this list of the last occurrence of the specified
     * element, or -1 if the list does not contain this element. More formally,
     * returns the highest index i such that
     * <tt>(o==null ? get(i)==null : o.equals(get(i)))</tt>, or -1 if there is
     * no such index.
     *
     * @param o
     *            element to search for.
     * @return the index in this list of the last occurrence of the specified
     *         element, or -1 if the list does not contain this element.
     */
    public int lastIndexOf(Object o) {
        int index = size;
        if (o == null) {
            for (Entry e = header.previous; e != header; e = e.previous) {
                index--;
                if (e.element == null)
                    return index;
            }
        } else {
            for (Entry e = header.previous; e != header; e = e.previous) {
                index--;
                if (o.equals(e.element))
                    return index;
            }
        }
        return -1;
    }

    /**
     * Returns a list-iterator of the elements in this list (in proper
     * sequence), starting at the specified position in the list. Obeys the
     * general contract of <tt>List.listIterator(int)</tt>.
     * <p>
     *
     * The list-iterator is <i>fail-fast</i>: if the list is structurally
     * modified at any time after the Iterator is created, in any way except
     * through the list-iterator's own <tt>remove</tt> or <tt>add</tt> methods,
     * the list-iterator will throw a <tt>ConcurrentModificationException</tt>.
     * Thus, in the face of concurrent modification, the iterator fails quickly
     * and cleanly, rather than risking arbitrary, non-deterministic behavior at
     * an undetermined time in the future.
     *
     * @param index
     *            index of first element to be returned from the list-iterator
     *            (by a call to <tt>next</tt>).
     * @return a ListIterator of the elements in this list (in proper sequence),
     *         starting at the specified position in the list.
     * @throws IndexOutOfBoundsException
     *             if index is out of range (
     *             <tt>index &lt; 0 || index &gt; size()</tt>).
     * @see List#listIterator(int)
     */
    @SuppressWarnings("rawtypes")
    public ListIterator listIterator(int index) {
        return new ListItr(index);
    }

    @SuppressWarnings("rawtypes")
    private class ListItr implements ListIterator {
        private Entry lastReturned = header;
        private Entry next;
        private int nextIndex;
        private int expectedModCount = modCount;

        ListItr(int index) {
//STUB BEGIN
		if (index < 0 || index > size)
		/*	throw new IndexOutOfBoundsException("Index: "+index+
				    ", Size: "+size);*/ //JBSE cannot handle concatenation of strings and symbols
		throw new IndexOutOfBoundsException();
		//if (index < (size >> 1)) {  //JBSE hardly handles shift operators
		next = header.next;
		for (nextIndex=0; nextIndex<index; nextIndex++)
			next = next.next;
		/*  //see above (JBSE hardly handles shift operators)
	    } else {
		next = header;
		for (nextIndex=size; nextIndex>index; nextIndex--)
		    next = next.previous;
	    }*/

//STUB END
	}

        public boolean hasNext() {
            return nextIndex != size;
        }

        public Object next() {
            checkForComodification();
            if (nextIndex == size)
                throw new NoSuchElementException();

            lastReturned = next;
            next = next.next;
            nextIndex++;
            return lastReturned.element;
        }

        public boolean hasPrevious() {
            return nextIndex != 0;
        }

        public Object previous() {
            if (nextIndex == 0)
                throw new NoSuchElementException();

            lastReturned = next = next.previous;
            nextIndex--;
            checkForComodification();
            return lastReturned.element;
        }

        public int nextIndex() {
            return nextIndex;
        }

        public int previousIndex() {
            return nextIndex - 1;
        }

        public void remove() {
            checkForComodification();
            try {
                DoubleLinkedList.this.remove(lastReturned);
            } catch (NoSuchElementException e) {
                throw new IllegalStateException();
            }
            if (next == lastReturned)
                next = lastReturned.next;
            else
                nextIndex--;
            lastReturned = header;
            expectedModCount++;
        }

        public void set(Object o) {
            if (lastReturned == header)
                throw new IllegalStateException();
            checkForComodification();
            lastReturned.element = o;
        }

        public void add(Object o) {
            checkForComodification();
            lastReturned = header;
            addBefore(o, next);
            nextIndex++;
            expectedModCount++;
        }

        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

    private static class Entry extends CorrespondenceHandler {
        Object element;
        Entry next;
        Entry previous;
//INSTRUMENTATION BEGIN

        /*
         * Other instrumentation fields
         */
        DoubleLinkedList _owner;

        /*
         * Triggers.
         */
        private static void _got_DoubleLinkedList_Entry_LICS_any(DoubleLinkedList.Entry e) {
        	//_owner is resolved (always by alias!) by LICS rules upon first access below
        	++e._owner._minSize;
        	Analysis.assume(e._owner._initialSize >= e._owner._minSize);
        }
        
        @SuppressWarnings("unused")
        private static void _got_DoubleLinkedList_Entry_LICS_nonroot_previous(DoubleLinkedList.Entry e) {
        	_got_DoubleLinkedList_Entry_LICS_any(e.previous);
        }
        @SuppressWarnings("unused")
        private static void _got_DoubleLinkedList_Entry_LICS_nonroot_next(DoubleLinkedList.Entry e) {
        	_got_DoubleLinkedList_Entry_LICS_any(e.next);
        }

        @SuppressWarnings("unused")
        private static void _handleListClosure_next(DoubleLinkedList.Entry e) {
            Analysis.assume(e._owner._initialSize == e._owner._minSize);
        }

        @SuppressWarnings("unused")
        private static void _handleListClosure_previous(DoubleLinkedList.Entry e) {
            Analysis.assume(e._owner._initialSize == e._owner._minSize);
        }

//INSTRUMENTATION END
	
//INSTRUMENTATION BEGIN
        //Entry(Object element, Entry next, Entry previous) {
        Entry(Object element, Entry next, Entry previous, DoubleLinkedList _owner) {
//INSTRUMENTATION END
            this.element = element;
            this.next = next;
            this.previous = previous;
//INSTRUMENTATION BEGIN
            //initializates instrumentation fields for concrete objects
            this._owner = _owner;
//INSTRUMENTATION END
        }

        boolean nonNullPointers() {
        	return next != null && previous !=null;
        }
        boolean repOK() {
            return next.previous == this;
        }
		
        public static boolean mirrorEachOtherAtEnd(Entry entry1, Entry entry2) {
 
        	if(!entry1.mustVisitDuringAssert()) return true;

        	boolean ok;
        	
			if (Analysis.isResolved(entry1, "element") || Analysis.isResolved(entry2, "element")) {
	        	if (entry1.element == null ^ entry2.element == null)
	        		return false;
	        	else if (entry1.element != null && entry2.element != null) {
	        		ok = IntegerMock.mirrorEachOtherAtEnd((IntegerMock)entry1.element, (IntegerMock)entry2.element);
	        		if(!ok) return false;
	        	}
			}

			if (Analysis.isResolved(entry1, "next") || Analysis.isResolved(entry2, "next")) {
	        	if (entry1.next == null ^ entry2.next == null)
	        		return false;
	        	else if (entry1.next != null && entry2.next != null) {
	        		ok = Entry.mirrorEachOtherAtEnd(entry1.next, entry2.next);
	        		if(!ok) return false;
	        	}
			}

			if (Analysis.isResolved(entry1, "previous") || Analysis.isResolved(entry2, "previous")) {
	        	if (entry1.previous == null ^ entry2.previous == null) 
	        		return false;
	        	else if (entry1.previous != null && entry2.previous != null) {
	        		ok = Entry.mirrorEachOtherAtEnd(entry1.previous, entry2.previous);
	        		if(!ok) return false;
	        	}
			}

        	return true;		
        }
        
        public static boolean mirrorEachOtherInitially_conservative(Entry entry1, Entry entry2) {
			boolean ok = CorrespondenceHandler.doOrMayCorrespondInInitialState(entry1, entry2);
			if(!ok) return false;			
			
			if(!entry1.mustVisitDuringAssume()) return true;
			
			ok = CorrespondenceHandler.setAsCorrespondingInInitialState(entry1, entry2);
			if(!ok) return false;
			
			if (Analysis.isResolved(entry1, "element") || Analysis.isResolved(entry2, "element")) {
				if (entry1.element == null)  
					ok = entry2.element == null;
				else if (entry2.element == null)
					ok = false;
				else 
					ok = IntegerMock.mirrorEachOtherInitially_conservative((IntegerMock)entry1.element, (IntegerMock)entry2.element);
				if(!ok) return false;
	     	}

			if (Analysis.isResolved(entry1, "next") || Analysis.isResolved(entry2, "next")) {
				if (entry1.next == null)  
					ok = entry2.next == null;
				else if (entry2.next == null)
					ok = false;
				else 
					ok = Entry.mirrorEachOtherInitially_conservative(entry1.next, entry2.next);
				if(!ok) return false;
	     	}

			if (Analysis.isResolved(entry1, "previous") || Analysis.isResolved(entry2, "previous")) {
				if (entry1.previous == null)  
					ok = entry2.previous == null;
				else if (entry2.previous == null)
					ok = false;
				else 
					ok = Entry.mirrorEachOtherInitially_conservative(entry1.previous, entry2.previous);
				if(!ok) return false;
	     	}

			return true;		
		}
        
    }
    
    //@ requires inList(e);
    private Entry addBefore(Object o, Entry e) {
//INSTRUMENTATION BEGIN
        //Entry newEntry = new Entry(o, e, e.previous);
        Entry newEntry = new Entry(o, e, e.previous, this);
//INSTRUMENTATION END
        newEntry.previous.next = newEntry;
        newEntry.next.previous = newEntry;
        size++;
        modCount++;
        return newEntry;
    }

    private void remove(Entry e) {
        if (e == header)
            throw new NoSuchElementException();

        e.previous.next = e.next;
        e.next.previous = e.previous;
        size--;
        modCount++;
    }

    /**
     * Returns a shallow copy of this <tt>DoubleLinkedList</tt>. (The elements
     * themselves are not cloned.)
     *
     * @return a shallow copy of this <tt>DoubleLinkedList</tt> instance.
     */
    public Object clone() {
//STUB BEGIN
    	/*
        DoubleLinkedList clone = null;
        try { 
        	clone = (DoubleLinkedList) super.clone();
        } catch (CloneNotSupportedException e) { 
        	throw new InternalError();
        }*/ //JBSE still does not implement Object.clone
    	DoubleLinkedList clone = new DoubleLinkedList();
//STUB END

        // Put clone into "virgin" state
//INSTRUMENTATION BEGIN
        //clone.header = new Entry(null, null, null);
        clone.header = new Entry(null, null, null, clone);
//INSTRUMENTATION END
        clone.header.next = clone.header.previous = clone.header;
        clone.size = 0;
        clone.modCount = 0;

        // Initialize clone with our elements
        for (Entry e = header.next; e != header; e = e.next)
            clone.add(e.element);

        return clone;
    }

    /**
     * Returns an array containing all of the elements in this list in the
     * correct order.
     *
     * @return an array containing all of the elements in this list in the
     *         correct order.
     */
    //@ ensures \result != null && \result.length == size;
    public Object[] toArray() {
    	Object[] resultForward = new Object[size];
    	boolean closedForward = true;
    	int iForward = 0;
    	for (Entry e = header.next; e != header && iForward < size; e = e.next) {
    		if (e == null) {
    			closedForward = false;
    			break;
    		}
    		resultForward[iForward++] = e.element;
    	}

    	Object[] resultBackward = new Object[size];
    	boolean closedBackward = true;
    	int iBackward = size;
    	for (Entry e = header.previous; e != header && iBackward < size; e = e.previous) {
    		if (e == null) {
    			closedBackward = false;
    			break;
    		}
    		resultBackward[--iBackward] = e.element;
    	}

    	if (closedForward || (!closedForward && iBackward == size)) {
    		return resultForward;
    	}
    	else if (closedBackward || (!closedBackward && iForward == 0)) {
    		return resultBackward;
    	}
    	else {
    		Object[] result = new Object[size];
    		List<?> l = Arrays.asList(resultBackward);
    		Collections.reverse(l);
    		resultBackward = l.toArray();
    		System.arraycopy(resultForward, 0, result, 0, iForward);
    		if (iForward < size)
    			System.arraycopy(resultBackward, 0, result, iForward, (size - iBackward));
    		return result;
    	}
    }

    boolean containsAllInOrder(Object[] objs) {
        for (int i = 0; i < size; i++) {
            if (get(i) != objs[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns an array containing all of the elements in this list in the
     * correct order; the runtime type of the returned array is that of the
     * specified array. If the list fits in the specified array, it is returned
     * therein. Otherwise, a new array is allocated with the runtime type of the
     * specified array and the size of this list.
     * <p>
     *
     * If the list fits in the specified array with room to spare (i.e., the
     * array has more elements than the list), the element in the array
     * immediately following the end of the collection is set to null. This is
     * useful in determining the length of the list <i>only</i> if the caller
     * knows that the list does not contain any null elements.
     *
     * @param a
     *            the array into which the elements of the list are to be
     *            stored, if it is big enough; otherwise, a new array of the
     *            same runtime type is allocated for this purpose.
     * @return an array containing the elements of the list.
     * @throws ArrayStoreException
     *             if the runtime type of a is not a supertype of the runtime
     *             type of every element in this list.
     * @throws NullPointerException
     *             if the specified array is null.
     */
    public Object[] toArray(Object a[]) {
        if (a.length < size)
//STUB BEGIN
            /*a = (Object[])java.lang.reflect.Array.newInstance(
                                a.getClass().getComponentType(), size);*/
        	a = new Object[size];
//STUB END
        int i = 0;
        for (Entry e = header.next; e != header; e = e.next)
            a[i++] = e.element;

        if (a.length > size)
            a[size] = null;

        return a;
    }

	/**
     * Save the state of this <tt>DoubleLinkedList</tt> instance to a stream
     * (that is, serialize it).
     *
     * @serialData The size of the list (the number of elements it contains) is
     *             emitted (int), followed by all of its elements (each an
     *             Object) in the proper order.
     */
    private void writeObject(java.io.ObjectOutputStream s)
    throws java.io.IOException {
        // Write out any hidden serialization magic
        s.defaultWriteObject();

        // Write out size
        s.writeInt(size);

        // Write out all elements in the proper order.
        for (Entry e = header.next; e != header; e = e.next)
            s.writeObject(e.element);
    }

    /**
     * Reconstitute this <tt>DoubleLinkedList</tt> instance from a stream (that
     * is deserialize it).
     */
    private void readObject(java.io.ObjectInputStream s)
    throws java.io.IOException, ClassNotFoundException {
        // Read in any hidden serialization magic
        s.defaultReadObject();

        // Read in size
        int size = s.readInt();

        // Initialize header
//INSTRUMENTATION BEGIN
        //header = new Entry(null, null, null);
        header = new Entry(null, null, null, this);
//INSTRUMENTATION END
        header.next = header.previous = header;

        // Read in all elements in the proper order.
        for (int i = 0; i < size; i++)
            add(s.readObject());
    }
	
    public static boolean mirrorEachOtherInitially_conservative(DoubleLinkedList list1, DoubleLinkedList list2) {
		boolean ok = CorrespondenceHandler.doOrMayCorrespondInInitialState(list1, list2);
		if(!ok) return false;			

		if(!list1.mustVisitDuringAssume()) return true;

		ok = CorrespondenceHandler.setAsCorrespondingInInitialState(list1, list2);
		if(!ok) return false;

		ok = list1.size == list2.size;
		if(!ok) return false;

		if (Analysis.isResolved(list1, "header") || Analysis.isResolved(list2, "header")) {
			if (list1.header == null)  
				ok = list2.header == null;
			else if (list2.header == null)
				ok = false;
			else 
				ok = Entry.mirrorEachOtherInitially_conservative(list1.header, list2.header);
			if(!ok) return false;
		}

		return true;	
	}
    
    public static boolean mirrorEachOtherAtEnd(DoubleLinkedList list1, DoubleLinkedList list2) {
    	if(!list1.mustVisitDuringAssert()) return true;
    	
		boolean ok = list1.size == list2.size;
		if(!ok) return false;

		if (Analysis.isResolved(list1, "header") || Analysis.isResolved(list2, "header")) {
			if (list1.header == null ^ list2.header == null)
				return false;
			else if (list1.header != null && list2.header != null) {
				ok = Entry.mirrorEachOtherAtEnd(list1.header, list2.header);
				if(!ok) return false;
			}
		}

		return true;
    }

}
