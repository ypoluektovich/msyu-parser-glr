package org.msyu.javautil.cf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class CopyList {

	public static <A, B> List<B> immutable(Collection<A> src, Function<? super A, ? extends B> mapper) {
		if (src.isEmpty()) {
			return Collections.emptyList();
		}
		List<B> dst = null;
		for (Iterator<A> iterator = src.iterator(); iterator.hasNext(); ) {
			A element = iterator.next();
			B newElement = mapper.apply(element);
			if (dst == null) {
				if (!iterator.hasNext()) {
					return Collections.singletonList(newElement);
				}
				dst = new ArrayList<>();
			}
			dst.add(newElement);
		}
		return Collections.unmodifiableList(dst);
	}

	public static <T> List<T> immutable(Collection<? extends T> src) {
		return immutable(src, Function.identity());
	}

}
