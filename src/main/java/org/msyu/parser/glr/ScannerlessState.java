package org.msyu.parser.glr;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;

public final class ScannerlessState {

	private final Long position;
	private final State state;
	private final Collection<Object> goals;

	public static ScannerlessState initializeFrom(Sapling sapling) {
		Long position = 0L;
		State state = State.initializeFrom(requireNonNull(sapling, "sapling"), position);
		return new ScannerlessState(position, state, emptyList());
	}

	private ScannerlessState(Long position, State state, Collection<Object> goals) {
		this.position = position;
		this.state = state;
		this.goals = goals;
	}

	public final <T> ScannerlessState advance(T token, GlrCallback<T> callback) throws UnexpectedTokenException {
		Long newPosition = position + 1;
		StateBuilder sb = state.startAdvance(newPosition);
		sb.advance(position, token, callback);
		return new ScannerlessState(
				newPosition,
				sb.finish(singleton(newPosition)),
				sb.viewGoalStacks().stream().map(ItemStackView::getId).collect(Collectors.toList())
		);
	}


	public final Set<Object> getUsedStackIds() {
		return state.getUsedStackIds();
	}

	public final Collection<Object> getGoalStackIds() {
		return goals;
	}

}
