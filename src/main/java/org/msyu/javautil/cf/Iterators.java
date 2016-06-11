package org.msyu.javautil.cf;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

import static java.util.Arrays.asList;

public class Iterators {

	public static <E> Iterator<E> singletonIterator(E element) {
		return new Iterator<E>() {
			private boolean hasNext = true;

			@Override
			public final boolean hasNext() {
				return hasNext;
			}

			@Override
			public final E next() {
				if (!hasNext) {
					throw new NoSuchElementException();
				}
				hasNext = false;
				return element;
			}
		};
	}

	public static <E> Iterator<E> concat(List<Iterator<E>> iterators) {
		return new Iterator<E>() {
			private final Queue<Iterator<E>> queue = new ArrayDeque<>(iterators);
			private Iterator<E> current = queue.poll();

			@Override
			public final boolean hasNext() {
				if (current == null) {
					return false;
				}
				while (!current.hasNext()) {
					current = queue.poll();
					if (current == null) {
						return false;
					}
				}
				return true;
			}

			@Override
			public final E next() {
				if (hasNext()) {
					return current.next();
				} else {
					throw new NoSuchElementException();
				}
			}
		};
	}

	@SafeVarargs
	public static <E> Iterator<E> concat(Iterator<E>... iterators) {
		return concat(asList(iterators));
	}

}
