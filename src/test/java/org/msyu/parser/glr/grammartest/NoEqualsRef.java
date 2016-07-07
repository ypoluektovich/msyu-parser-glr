package org.msyu.parser.glr.grammartest;

import org.msyu.javautil.cf.CopyList;

import java.util.List;

final class NoEqualsRef {

	final Object ref;

	NoEqualsRef(Object ref) {
		this.ref = ref;
	}

	static Object unwrap(Object listOfRefs) {
		if (listOfRefs instanceof List) {
			return CopyList.immutable((List<?>) listOfRefs, NoEqualsRef::unwrap);
		} else if (listOfRefs instanceof NoEqualsRef) {
			return ((NoEqualsRef) listOfRefs).ref;
		} else {
			return listOfRefs;
		}
	}

}
