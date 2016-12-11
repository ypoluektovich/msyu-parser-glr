package org.msyu.parser.glr;

import org.msyu.javautil.cf.CopyList;
import org.msyu.javautil.cf.CopySet;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

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
				CopyList.immutable(
						sapling.initialItems,
						item -> new ItemStack(null, item.position, item, null, initialPosition, emptySet(), emptyList())
				)
		);
	}

	public final <T> State advance(
			Map<T, ?> startByToken,
			GlrCallback<T> callback,
			Object end,
			Collection<?> growingPositions
	) throws UnexpectedTokensException {
		return new State(this, startByToken, callback, end, growingPositions);
	}

	private <T> State(
			State previousState,
			Map<T, ?> startByToken,
			GlrCallback<T> callback,
			Object end,
			Collection<?> growingPositions
	) throws UnexpectedTokensException {
		this.sapling = previousState.sapling;

		Collection<ItemStack> endStacks = new HashSet<>();
		List<UnexpectedTokenException> exceptions = new ArrayList<>();
		Set<GreedMark> marksToCull = new HashSet<>();
		for (Map.Entry<T, ?> tokenAndStart : startByToken.entrySet()) {
			T token = tokenAndStart.getKey();
			Object start = tokenAndStart.getValue();
			List<ItemStack> stacks = previousState.stacksByPosition.get(start);
			if (stacks == null) {
				throw new IllegalArgumentException("no stacks registered for position " + start);
			}
			try {
				endStacks.addAll(advance(stacks, token, callback, end, marksToCull));
			} catch (UnexpectedTokenException e) {
				exceptions.add(e);
			}
		}
		if (!exceptions.isEmpty() && endStacks.isEmpty()) {
			UnexpectedTokensException exception = new UnexpectedTokensException();
			for (UnexpectedTokenException e : exceptions) {
				exception.addSuppressed(e);
			}
			throw exception;
		}

		Map<Object, List<ItemStack>> stacksByPosition = new HashMap<>();
		for (Map.Entry<Object, List<ItemStack>> positionAndStacks : previousState.stacksByPosition.entrySet()) {
			Object position = positionAndStacks.getKey();
			if (growingPositions.contains(position)) {
				List<ItemStack> remainingStacks = cull(positionAndStacks.getValue(), marksToCull);
				if (!remainingStacks.isEmpty()) {
					stacksByPosition.put(position, remainingStacks);
				}
			}
		}
		endStacks = cull(endStacks, marksToCull);
		if (!endStacks.isEmpty()) {
			stacksByPosition.put(end, mergeMarks(endStacks));
		}
		this.stacksByPosition = Collections.unmodifiableMap(stacksByPosition);
	}

	private <T> Collection<ItemStack> advance(
			List<ItemStack> stacks,
			T token,
			GlrCallback<T> callback,
			Object endPosition,
			Set<GreedMark> marksToCull
	) throws UnexpectedTokenException {
		Queue<ItemStack> stacksQueue = new ArrayDeque<>();
		Set<ItemStack> stacksSet = new HashSet<>();

		shift(stacks, token, callback, stacksQueue);

		reduce(stacksQueue, stacksSet, callback, marksToCull);

		stacksQueue.addAll(stacksSet);
		stacksSet.clear();

		expand(stacksQueue, stacksSet, endPosition);

		return stacksSet;
	}

	private <T> void shift(List<ItemStack> stacks, T token, GlrCallback<T> callback, Collection<ItemStack> shiftedStacks) throws UnexpectedTokenException {
		Terminal terminal = callback.getSymbolOfToken(token);
		for (ItemStack oldStack : stacks) {
			if (oldStack.item.getExpectedNextSymbol().equals(terminal)) {
				shiftedStacks.add(oldStack.shift(callback, token));
			}
		}
		if (shiftedStacks.isEmpty()) {
			throw new UnexpectedTokenException(
					CopySet.immutableHash(stacks, stack -> (Terminal) stack.item.getExpectedNextSymbol()),
					terminal,
					token
			);
		}
	}

	private void reduce(
			Queue<ItemStack> reductionQueue,
			Collection<ItemStack> reducedStacks,
			GlrCallback<?> callback,
			Set<GreedMark> marksToCull
	) {
		for (ItemStack stack; (stack = reductionQueue.poll()) != null; ) {
			if (!stack.item.isFinished()) {
				if (sapling.grammar.isCompletable(stack.item)) {
					reductionQueue.add(stack.skipToEnd(callback));
				}
				reducedStacks.add(stack);
				continue;
			}
			ItemStack nextInStack = stack.nextInStack;
			Production completedProduction = stack.item.production;
			NonTerminal completedSymbol = completedProduction.lhs;

			Object reducedBranchId = callback.reduce(stack.id, completedProduction);

			if (completedProduction.isGreedy) {
				marksToCull.add(stack.getGreedMark());
			}

			for (Item newItem : sapling.grammar.getItemsInitializedBy(completedSymbol)) {
				if (itemIsGoodAsBlindReductionTarget(newItem, nextInStack)) {
					reductionQueue.add(stack.finishBlindReduction(callback, reducedBranchId, newItem));
				}
			}

			if (nextInStack != null && nextInStack.item.getExpectedNextSymbol().equals(completedSymbol)) {
				reductionQueue.add(nextInStack.finishGuidedReduction(stack, callback, reducedBranchId));
			}
		}
	}

	private void expand(Queue<ItemStack> expansionQueue, Collection<ItemStack> expandedStacks, Object position) {
		for (ItemStack stack; (stack = expansionQueue.poll()) != null; ) {
			Item item = stack.item;
			do {
				ASymbol nextSymbol = item.getExpectedNextSymbol();
				if (sapling.grammar.fillableSymbols.contains(nextSymbol)) {
					stack = stack.skipTo(item);
					if (nextSymbol instanceof Terminal) {
						expandedStacks.add(stack);
					} else if (nextSymbol instanceof NonTerminal) {
						NonTerminal nextNonTerminal = (NonTerminal) nextSymbol;
						for (Item nextItem : sapling.grammar.getAllInitializingItemsOf(nextNonTerminal)) {
							expandedStacks.add(new ItemStack(
									stack.id,
									nextItem.position,
									nextItem,
									stack.copyForChild(),
									position,
									stack.greedMarks,
									stack.newGreedMarks
							));
						}
					}
				}
				if (!sapling.grammar.skippableSymbols.contains(nextSymbol)) {
					break;
				}
				item = item.shift();
			} while (!item.isFinished());
		}
	}

	private boolean itemIsGoodAsBlindReductionTarget(Item item, ItemStack nextInStack) {
		boolean lhsFitsExpected = false;
		boolean skipSaplingCheck = false;
		if (nextInStack == null) {
			lhsFitsExpected = true;
		} else if (nextInStack.item.getExpectedNextSymbol() == item.production.lhs ||
				sapling.grammar
						.getAllInitializingNonTerminalsOf((NonTerminal) nextInStack.item.getExpectedNextSymbol())
						.contains(item.production.lhs)
		) {
			lhsFitsExpected = true;
			skipSaplingCheck = true;
		}
		return lhsFitsExpected &&
				(skipSaplingCheck || sapling.allowedBlindReductionNonTerminals.contains(item.production.lhs));
	}

	private static List<ItemStack> cull(Collection<ItemStack> stacks, Set<GreedMark> marksToCull) {
		List<ItemStack> remainingStacks = new ArrayList<>();
		for (ItemStack stack : stacks) {
			if (Collections.disjoint(stack.greedMarks, marksToCull)) {
				remainingStacks.add(stack);
			}
		}
		return unmodifiableList(remainingStacks);
	}

	private static List<ItemStack> mergeMarks(Collection<ItemStack> stacks) {
		List<ItemStack> merged = new ArrayList<>();
		for (ItemStack stack : stacks) {
			merged.add(stack.mergeMarks());
		}
		return unmodifiableList(merged);
	}


	private final Stream<ItemStack> streamStacks0() {
		return stacksByPosition.values().stream().flatMap(List::stream);
	}

	public final Stream<ItemStackView> streamStacks() {
		return streamStacks0().map(ItemStackView.class::cast);
	}

	public final List<? extends ItemStackView> getStacks() {
		return streamStacks().collect(toList());
	}

	public final Set<Object> getUsedStackIds() {
		return streamStacks0()
				.map(ItemStackView::getId)
				.collect(Collectors.toSet());
	}

	public final Set<Object> getUsedStartPositions() {
		return stacksByPosition.keySet();
	}

	public final Set<Terminal> getExpectedNextSymbols() {
		return streamStacks0()
				.map(stack -> {
					ASymbol expectedNextSymbol = stack.item.getExpectedNextSymbol();
					assert expectedNextSymbol instanceof Terminal : "expected next symbol is not a Terminal";
					return (Terminal) expectedNextSymbol;
				})
				.collect(Collectors.toSet());
	}

}
