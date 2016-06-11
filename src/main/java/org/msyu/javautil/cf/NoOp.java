package org.msyu.javautil.cf;

import java.util.function.Consumer;

public class NoOp {

	public static <T> Consumer<T> consumer() {
		return __ -> {};
	}

}
