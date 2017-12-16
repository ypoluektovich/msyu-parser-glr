package org.msyu.parser.glr;

import org.msyu.javautil.cf.CopyList;
import org.msyu.javautil.cf.CopySet;
import org.msyu.parser.glr.incubator.MapToSet;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

public final class StateBuilder {

	private final Sapling sapling;
	private final Map<Object, List<ItemStack>> oldStacksByPosition;
	private final Object end;

	private final Set<ItemStack> endStacks = new HashSet<>();
	private final Set<ItemStack> goalStacks = new HashSet<>();

	StateBuilder(
			Sapling sapling,
			Map<Object, List<ItemStack>> oldStacksByPosition,
			Object end
	) {
		this.sapling = sapling;
		this.oldStacksByPosition = oldStacksByPosition;
		this.end = end;
	}


	public final <T> void advance(Object start, T token, GlrCallback<T> callback) throws UnexpectedTokenException {
		List<ItemStack> startStacks = oldStacksByPosition.get(start);
		if (startStacks == null) {
			throw new IllegalArgumentException("no stacks registered for position " + start);
		}

		Queue<ItemStack> stacksQueue = new ArrayDeque<>();
		Set<ItemStack> stacksSet = new HashSet<>();
		MapToSet<ItemStack, ItemStack> ancestorsByStack = new MapToSet<>(new LinkedHashMap<>(), __ -> new HashSet<>());
		Set<Predicate<ItemStackView>> cullPredicates = new HashSet<>();
		// we need a set for the current advance so that unexpected token detector works properly
		Set<ItemStack> goalStacks = new HashSet<>();

		Terminal terminal = callback.getSymbolOfToken(token);
		Supplier<UnexpectedTokenException> exceptionMaker = () -> new UnexpectedTokenException(
				CopySet.immutableHash(startStacks, stack -> (Terminal) stack.item.getExpectedNextSymbol()),
				!cullPredicates.isEmpty(),
				terminal,
				token
		);

		shift(startStacks, terminal, token, callback, stacksQueue, ancestorsByStack, cullPredicates);

		cullAndMaybeThrow(stacksQueue, goalStacks, ancestorsByStack, cullPredicates, exceptionMaker);

		reduce(stacksQueue, stacksSet, goalStacks, callback, ancestorsByStack, cullPredicates);

		cullAndMaybeThrow(stacksSet, goalStacks, ancestorsByStack, cullPredicates, exceptionMaker);

		stacksQueue.addAll(stacksSet);
		stacksSet.clear();

		expand(stacksQueue, stacksSet);

		endStacks.addAll(stacksSet);
		this.goalStacks.addAll(goalStacks);
	}

	private void handleNewStack(
			ItemStack newStack,
			ItemStack oldStack,
			Collection<ItemStack> newStackSink,
			MapToSet<ItemStack, ItemStack> ancestorsByStack,
			GlrCallback<?> callback,
			Set<Predicate<ItemStackView>> cullPredicates
	) {
		newStackSink.add(newStack);
		ancestorsByStack.add(newStack, oldStack);

		Predicate<ItemStackView> predicate = callback.cull(newStack);
		if (predicate != null) {
			cullPredicates.add(predicate);
		}
	}

	private void cullAndMaybeThrow(
			Collection<ItemStack> unfinishedStacks,
			Set<ItemStack> goalStacks,
			MapToSet<ItemStack, ItemStack> ancestorsByStack,
			Set<Predicate<ItemStackView>> cullPredicates,
			Supplier<UnexpectedTokenException> exceptionMaker
	) throws UnexpectedTokenException {
		Set<ItemStack> culledStacks = new HashSet<>();
		for (Map.Entry<ItemStack, Set<ItemStack>> ancestorsAndStack : ancestorsByStack.entrySet()) {
			ItemStack stack = ancestorsAndStack.getKey();
			Set<ItemStack> ancestors = ancestorsAndStack.getValue();

			boolean cull = false;

			ancestors.removeAll(culledStacks);
			if (ancestors.isEmpty()) {
				cull = true;
			}

			if (!cull) {
				for (Predicate<ItemStackView> cullPredicate : cullPredicates) {
					if (cullPredicate.test(stack)) {
						cull = true;
						break;
					}
				}
			}

			if (cull) {
				culledStacks.add(stack);
			}
		}
		unfinishedStacks.removeAll(culledStacks);
		goalStacks.removeAll(culledStacks);
		if (unfinishedStacks.isEmpty() && goalStacks.isEmpty()) {
			throw exceptionMaker.get();
		}
	}

	private <T> void shift(
			List<ItemStack> stacks,
			Terminal terminal,
			T token,
			GlrCallback<T> callback,
			Collection<ItemStack> shiftedStacks,
			MapToSet<ItemStack, ItemStack> ancestorsByStack,
			Set<Predicate<ItemStackView>> cullPredicates
	) {
		for (ItemStack oldStack : stacks) {
			if (oldStack.item.getExpectedNextSymbol().equals(terminal)) {
				ItemStack newStack = oldStack.shift(callback, token);
				handleNewStack(newStack, oldStack, shiftedStacks, ancestorsByStack, callback, cullPredicates);
			}
		}
	}

	private void reduce(
			Queue<ItemStack> reductionQueue,
			Collection<ItemStack> reducedStacks,
			Set<ItemStack> goalStacks,
			GlrCallback<?> callback,
			MapToSet<ItemStack, ItemStack> ancestorsByStack,
			Set<Predicate<ItemStackView>> cullPredicates
	) {
		for (ItemStack stack; (stack = reductionQueue.poll()) != null; ) {
			if (!stack.item.isFinished()) {
				if (sapling.grammar.isCompletable(stack.item)) {
					ItemStack newStack = stack.skipToEnd(callback);
					handleNewStack(newStack, stack, reductionQueue, ancestorsByStack, callback, cullPredicates);
				}
				reducedStacks.add(stack);
				continue;
			}
			ItemStack nextInStack = stack.nextInStack;
			Production completedProduction = stack.item.production;
			NonTerminal completedSymbol = completedProduction.lhs;

			if (nextInStack == null && sapling.goals.contains(completedSymbol)) {
				goalStacks.add(stack);
			}

			Object reducedBranchId = callback.reduce(stack.id, completedProduction);

			for (Item newItem : sapling.grammar.getItemsInitializedBy(completedSymbol)) {
				if (itemIsGoodAsBlindReductionTarget(newItem, nextInStack)) {
					ItemStack newStack = stack.finishBlindReduction(callback, reducedBranchId, newItem);
					handleNewStack(newStack, stack, reductionQueue, ancestorsByStack, callback, cullPredicates);
				}
			}

			if (nextInStack != null && nextInStack.item.getExpectedNextSymbol().equals(completedSymbol)) {
				ItemStack newStack = nextInStack.finishGuidedReduction(callback, reducedBranchId);
				handleNewStack(newStack, stack, reductionQueue, ancestorsByStack, callback, cullPredicates);
			}
		}
	}

	private void expand(Queue<ItemStack> expansionQueue, Collection<ItemStack> expandedStacks) {
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
							expandedStacks.add(new ItemStack(stack.id, nextItem.position, nextItem, stack.copyWithNoId()));
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


	public final State finish(Collection<?> growingPositions) {
		Map<Object, List<ItemStack>> stacksByPosition = new HashMap<>();
		for (Map.Entry<Object, List<ItemStack>> positionAndStacks : oldStacksByPosition.entrySet()) {
			Object position = positionAndStacks.getKey();
			if (growingPositions.contains(position)) {
				stacksByPosition.put(position, positionAndStacks.getValue());
			}
		}
		if (!endStacks.isEmpty() && growingPositions.contains(end)) {
			stacksByPosition.put(end, CopyList.immutable(endStacks));
		}
		if (endStacks.isEmpty() && goalStacks.isEmpty() && stacksByPosition.isEmpty()) {
			throw new IllegalStateException("no goals reached or stacks remaining");
		}
		return new State(sapling, unmodifiableMap(stacksByPosition));
	}


	public final Collection<ItemStackView> viewGoalStacks() {
		return unmodifiableSet(goalStacks);
	}

}
