package org.msyu.parser.glr.incubator;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class CutoffIterator<E> implements Iterator<E> {

	private final Iterator<E> back;
	private int remainingCount;

	private E next;
	private boolean hasNext;

	public CutoffIterator(Iterator<E> back, int count) {
		this.back = back;
		this.remainingCount = count;
		updateNext();
	}

	private void updateNext() {
		if (remainingCount == 0) {
			hasNext = false;
			next = null;
			return;
		}
		if (back.hasNext()) {
			hasNext = true;
			next = back.next();
			--remainingCount;
		} else {
			hasNext = false;
			next = null;
		}
	}

	@Override
	public boolean hasNext() {
		return hasNext;
	}

	@Override
	public E next() {
		if (!hasNext) {
			throw new NoSuchElementException();
		}
		E elementToReturn = next;
		updateNext();
		return elementToReturn;
	}

}
