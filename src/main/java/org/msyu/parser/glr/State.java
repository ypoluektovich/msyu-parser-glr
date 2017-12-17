package org.msyu.parser.glr;

import org.msyu.javautil.cf.CopyList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class State {

	private final Sapling sapling;
	private final Map<Object, List<ItemStack>> stacksByPosition;

	public static State initializeFrom(Sapling sapling, Object position) {
		return new State(sapling, position);
	}

	private State(Sapling sapling, Object initialPosition) {
		this.sapling = sapling;
		this.stacksByPosition = Collections.singletonMap(
				initialPosition,
				CopyList.immutable(sapling.initialItems, item -> new ItemStack(null, item.position, item, null))
		);
	}

	public final StateBuilder startAdvance(Object end) {
		return new StateBuilder(sapling, stacksByPosition, end);
	}

	State(Sapling sapling, Map<Object, List<ItemStack>> stacksByPosition) {
		this.sapling = sapling;
		this.stacksByPosition = stacksByPosition;
	}

	/**
	 * @deprecated
	 * This method is left here for compatibiity with old code.
	 * New code should use {@link #startAdvance(Object)} directly.
	 */
	@Deprecated
	public final <T> State advance(
			Map<T, ?> startByToken,
			GlrCallback<T> callback,
			Object end,
			Collection<?> growingPositions
	) throws UnexpectedTokensException {
		StateBuilder builder = startAdvance(end);
		List<UnexpectedTokenException> exceptions = new ArrayList<>();
		for (Map.Entry<T, ?> tokenAndStart : startByToken.entrySet()) {
			try {
				builder.advance(tokenAndStart.getValue(), tokenAndStart.getKey(), callback);
			} catch (UnexpectedTokenException e) {
				exceptions.add(e);
			}
		}
		try {
			return builder.finish(growingPositions);
		} catch (IllegalStateException ise) {
			UnexpectedTokensException exception = new UnexpectedTokensException();
			for (UnexpectedTokenException e : exceptions) {
				exception.addSuppressed(e);
			}
			throw exception;
		}
	}

	public final Collection<ItemStackView> getStacks() {
		return stacksByPosition.values().stream()
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	public final Collection<ItemStackView> getStacksAtPosition(Object position) {
		// We know that the list is unmodifiable, so this is okay.
		// It'd work without a cast if we were returning Collection<? extends ItemStackView>,
		// but that's just ugly.
		@SuppressWarnings("unchecked")
		Collection<ItemStackView> stacks = (List) stacksByPosition.get(position);
		return stacks;
	}

	public final Set<Object> getUsedStackIds() {
		return stacksByPosition.values().stream()
				.flatMap(Collection::stream)
				.map(ItemStackView::getId)
				.collect(Collectors.toSet());
	}

	public final Set<Terminal> getExpectedNextSymbols() {
		return stacksByPosition.values().stream()
				.flatMap(Collection::stream)
				.map(stack -> {
					ASymbol expectedNextSymbol = stack.item.getExpectedNextSymbol();
					assert expectedNextSymbol instanceof Terminal : "expected next symbol is not a Terminal";
					return (Terminal) expectedNextSymbol;
				})
				.collect(Collectors.toSet());
	}

	public final Set<Object> getGrowingPositions() {
		return stacksByPosition.keySet();
	}

}
