package org.msyu.parser.glr;

import java.util.Objects;

final class GreedMark {

	final Production production;
	final Object startPosition;

	GreedMark(Production production, Object startPosition) {
		this.production = production;
		this.startPosition = startPosition;
	}

	@Override
	public final boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || obj.getClass() != GreedMark.class) {
			return false;
		}
		GreedMark that = (GreedMark) obj;
		return production.equals(that.production) &&
				Objects.equals(startPosition, that.startPosition);
	}

	@Override
	public final int hashCode() {
		return Objects.hash(production, startPosition);
	}

}
