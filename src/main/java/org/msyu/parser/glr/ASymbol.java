package org.msyu.parser.glr;

import java.util.Objects;

public abstract class ASymbol {

	final String name;

	/**
	 * @throws IllegalArgumentException if name is null or empty.
	 */
	ASymbol(String name) {
		if (name == null) {
			throw new IllegalArgumentException("symbol name must be non-null");
		}
		if (name.isEmpty()) {
			throw new IllegalArgumentException("symbol name must not be empty");
		}
		this.name = name;
	}

	@Override
	public final String toString() {
		return name;
	}

	@Override
	public final boolean equals(Object obj) {
		return this == obj ||
				obj != null && getClass() == obj.getClass() &&
						Objects.equals(name, ((ASymbol) obj).name);
	}

	@Override
	public final int hashCode() {
		return Objects.hashCode(name);
	}

}
