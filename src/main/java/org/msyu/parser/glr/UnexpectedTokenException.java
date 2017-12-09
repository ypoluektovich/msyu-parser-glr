package org.msyu.parser.glr;

import java.util.Set;

public final class UnexpectedTokenException extends Exception {

	private final Set<Terminal> expected;
	private final boolean expectedWithRestrictions;
	private final Terminal actual;
	private final Object token;

	UnexpectedTokenException(Set<Terminal> expected, boolean expectedWithRestrictions, Terminal actual, Object token) {
		this.expected = expected;
		this.expectedWithRestrictions = expectedWithRestrictions;
		this.actual = actual;
		this.token = token;
	}

	public final Set<Terminal> getExpected() {
		return expected;
	}

	public boolean isExpectedWithRestrictions() {
		return expectedWithRestrictions;
	}

	public final Terminal getActual() {
		return actual;
	}

	public final Object getToken() {
		return token;
	}

	@Override
	public final String getMessage() {
		return String.format(
				"expected one of %s%s, got %s (%s)",
				expected,
				expectedWithRestrictions ? " (with restrictions)" : "",
				actual,
				token
		);
	}

}
