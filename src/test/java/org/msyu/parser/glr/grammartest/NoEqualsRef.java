package org.msyu.parser.glr.grammartest;

import org.msyu.javautil.cf.CopyList;

import java.util.List;

public final class NoEqualsRef {

	private final Object ref;

	public NoEqualsRef(Object ref) {
		this.ref = ref;
	}

	public static Object unwrap(Object listOfRefs) {
		if (listOfRefs instanceof List) {
			return CopyList.immutable((List<?>) listOfRefs, NoEqualsRef::unwrap);
		} else if (listOfRefs instanceof NoEqualsRef) {
			return ((NoEqualsRef) listOfRefs).ref;
		} else {
			return listOfRefs;
		}
	}

}
