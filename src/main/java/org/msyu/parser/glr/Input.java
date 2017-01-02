package org.msyu.parser.glr;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public final class Input<T> {

	public final Object startPosition;

	public final T token;

	public Input(Object startPosition, T token) {
		this.startPosition = startPosition;
		this.token = token;
	}

	@Override
	public final boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		Input<?> that = (Input<?>) obj;
		return Objects.equals(startPosition, that.startPosition) && Objects.equals(token, that.token);
	}

	@Override
	public final int hashCode() {
		return Objects.hash(startPosition, token);
	}


	public static <T> Collection<Input<T>> singleton(Object startPosition, T token) {
		return Collections.singleton(new Input<>(startPosition, token));
	}

}
