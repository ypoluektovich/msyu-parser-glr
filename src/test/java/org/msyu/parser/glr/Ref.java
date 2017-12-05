package org.msyu.parser.glr;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.msyu.javautil.cf.Iterators.singletonIterator;

public class Ref<T> implements Consumer<T>, Supplier<T>, Iterable<T> {

	private T value;

	public Ref() {
	}

	public Ref(T value) {
		this.value = value;
	}

	@Override
	public void accept(T t) {
		value = t;
	}

	@Override
	public T get() {
		return value;
	}

	@Override
	public Iterator<T> iterator() {
		return singletonIterator(value);
	}

}
