package org.msyu.parser.glr;

import java.util.Map;
import java.util.Set;

public final class UnexpectedTokensException extends Exception {

	final Map<Input<?>, UnexpectedTokenException> exceptionByInput;

	UnexpectedTokensException(Map<Input<?>, UnexpectedTokenException> exceptionByInput) {
		this.exceptionByInput = exceptionByInput;
	}

	public final Set<Terminal> getExpectedByInput(Input<?> input) {
		return exceptionByInput.get(input).getExpected();
	}

}
