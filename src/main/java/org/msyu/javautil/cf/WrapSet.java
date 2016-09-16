package org.msyu.javautil.cf;

import java.util.Collections;
import java.util.Set;

public class WrapSet {

	public static <T> Set<T> immutable(Set<? extends T> items) {
		return items.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(items);
	}

}
