package org.msyu.javautil.cf;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;

public class CopySet {

	public static <A, B> Set<B> immutableHash(Collection<A> src, Function<? super A, ? extends B> mapper) {
		if (src.isEmpty()) {
			return Collections.emptySet();
		}
		Set<B> dst = null;
		for (Iterator<A> iterator = src.iterator(); iterator.hasNext(); ) {
			A element = iterator.next();
			B newElement = mapper.apply(element);
			if (dst == null) {
				if (!iterator.hasNext()) {
					return Collections.singleton(newElement);
				}
				dst = new HashSet<>();
			}
			dst.add(newElement);
		}
		return Collections.unmodifiableSet(dst);
	}

	public static <T> Set<T> immutableHash(Collection<T> src) {
		return immutableHash(src, Function.identity());
	}

}
