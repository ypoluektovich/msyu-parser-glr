package org.msyu.parser.glr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MultiState {

	private final Map<Object, State> stateByPosition;

	public static MultiState initializeFrom(Sapling sapling, Object position) {
		return new MultiState(sapling, position);
	}

	private MultiState(Sapling sapling, Object position) {
		stateByPosition = Collections.singletonMap(position, State.initializeFrom(sapling));
	}

	public final <T> MultiState advance(
			Map<T, Object> startByToken,
			GlrCallback<T> callback,
			Object end,
			Collection<Object> growingPositions
	) throws UnexpectedTokensException {
		List<State> endStates = new ArrayList<>();
		List<UnexpectedTokenException> exceptions = new ArrayList<>();
		for (Map.Entry<T, Object> tokenAndStart : startByToken.entrySet()) {
			T token = tokenAndStart.getKey();
			Object start = tokenAndStart.getValue();
			State state = stateByPosition.get(start);
			if (state == null) {
				throw new IllegalArgumentException("no state registered for position " + start);
			}
			try {
				endStates.add(state.advance(token, callback));
			} catch (UnexpectedTokenException e) {
				exceptions.add(e);
			}
		}
		if (!exceptions.isEmpty() && endStates.isEmpty()) {
			UnexpectedTokensException exception = new UnexpectedTokensException();
			for (UnexpectedTokenException e : exceptions) {
				exception.addSuppressed(e);
			}
			throw exception;
		}
		return new MultiState(this, endStates, end, growingPositions);
	}

	private MultiState(MultiState previousState, List<State> endStates, Object end, Collection<Object> growingPositions) {
		Map<Object, State> stateByPosition = new HashMap<>();
		for (Map.Entry<Object, State> positionAndState : previousState.stateByPosition.entrySet()) {
			Object position = positionAndState.getKey();
			if (growingPositions.contains(position)) {
				stateByPosition.put(position, positionAndState.getValue());
			}
		}
		if (!endStates.isEmpty()) {
			stateByPosition.put(end, State.join(endStates));
		}
		this.stateByPosition = Collections.unmodifiableMap(stateByPosition);
	}

}
