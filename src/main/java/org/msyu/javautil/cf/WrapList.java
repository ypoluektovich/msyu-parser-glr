package org.msyu.javautil.cf;

import java.util.Collections;
import java.util.List;

public class WrapList {

	public static <T> List<T> immutable(List<? extends T> items) {
		return items.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(items);
	}

}
