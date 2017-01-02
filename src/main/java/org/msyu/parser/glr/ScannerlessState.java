package org.msyu.parser.glr;

import java.util.Set;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonMap;
import static java.util.Objects.requireNonNull;

public final class ScannerlessState {

	private final Long position;
	public final State state;

	public static ScannerlessState initializeFrom(Sapling sapling) {
		return new ScannerlessState(sapling);
	}

	private ScannerlessState(Sapling sapling) {
		position = 0L;
		state = State.initializeFrom(requireNonNull(sapling, "sapling"), position);
	}

	public final <T> ScannerlessState advance(T token, GlrCallback<T> callback) throws UnexpectedTokenException {
		return new ScannerlessState(this, token, callback);
	}

	private <T> ScannerlessState(ScannerlessState prevState, T token, GlrCallback<T> callback) throws UnexpectedTokenException {
		position = prevState.position + 1;
		try {
			state = prevState.state.advance(singletonMap(token, prevState.position), callback, position, singleton(position));
		} catch (UnexpectedTokensException e) {
			throw (UnexpectedTokenException) e.getSuppressed()[0];
		}
	}


	public final Set<Object> getUsedStackIds() {
		return state.getUsedStackIds();
	}

}
