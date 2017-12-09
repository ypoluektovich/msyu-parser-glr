package org.msyu.parser.glr.incubator;

import java.util.List;
import java.util.ListIterator;

public class ReverseListIterator<E> implements ListIterator<E> {

	private final ListIterator<E> back;
	private int size;

	public ReverseListIterator(List<E> list) {
		this(list.listIterator(list.size()), list.size());
	}

	public ReverseListIterator(ListIterator<E> back, int size) {
		this.back = back;
		this.size = size;
	}

	@Override
	public boolean hasNext() {
		return back.hasPrevious();
	}

	@Override
	public E next() {
		return back.previous();
	}

	@Override
	public boolean hasPrevious() {
		return back.hasNext();
	}

	@Override
	public E previous() {
		return back.next();
	}

	@Override
	public int nextIndex() {
		return size - back.nextIndex();
	}

	@Override
	public int previousIndex() {
		return nextIndex() - 1;
	}

	@Override
	public void remove() {
		back.remove();
		--size;
	}

	@Override
	public void set(E e) {
		back.set(e);
	}

	@Override
	public void add(E e) {
		back.add(e);
		back.previous();
		++size;
	}

}
