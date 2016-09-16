package org.msyu.parser.glr;

import org.msyu.javautil.cf.CopyList;
import org.msyu.javautil.cf.CopySet;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

public final class State {

	private final Sapling sapling;
	private final List<ItemStack> stacks;

	public static State initializeFrom(Sapling sapling) {
		return new State(sapling);
	}

	private State(Sapling sapling) {
		this.sapling = sapling;
		this.stacks = CopyList.immutable(sapling.initialItems, item -> new ItemStack(null, item.position, item, null));
	}

	public final <T> State advance(T token, GlrCallback<T> callback) throws UnexpectedTokenException {
		return new State(this, token, callback);
	}

	private <T> State(State previousState, T token, GlrCallback<T> callback) throws UnexpectedTokenException {
		this.sapling = previousState.sapling;

		Queue<ItemStack> stacksQueue = new ArrayDeque<>();
		Set<ItemStack> stacksSet = new HashSet<>();

		shift(previousState, token, callback, stacksQueue);

		reduce(stacksQueue, stacksSet, callback);

		stacksQueue.addAll(stacksSet);
		stacksSet.clear();

		expand(stacksQueue, stacksSet);

		this.stacks = CopyList.immutable(stacksSet);
	}

	private <T> void shift(State previousState, T token, GlrCallback<T> callback, Collection<ItemStack> shiftedStacks) throws UnexpectedTokenException {
		Terminal terminal = callback.getSymbolOfToken(token);
		for (ItemStack oldStack : previousState.stacks) {
			if (oldStack.item.getExpectedNextSymbol().equals(terminal)) {
				shiftedStacks.add(oldStack.shift(callback, token));
			}
		}
		if (shiftedStacks.isEmpty()) {
			throw new UnexpectedTokenException(
					CopySet.immutableHash(previousState.stacks, stack -> (Terminal) stack.item.getExpectedNextSymbol()),
					terminal,
					token
			);
		}
	}

	private void reduce(Queue<ItemStack> reductionQueue, Collection<ItemStack> reducedStacks, GlrCallback<?> callback) {
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

			for (Item newItem : sapling.grammar.getItemsInitializedBy(completedSymbol)) {
				if (itemIsGoodAsBlindReductionTarget(newItem, nextInStack)) {
					reductionQueue.add(stack.finishBlindReduction(callback, reducedBranchId, newItem));
				}
			}

			if (nextInStack != null && nextInStack.item.getExpectedNextSymbol().equals(completedSymbol)) {
				reductionQueue.add(nextInStack.finishGuidedReduction(callback, reducedBranchId));
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
		} else if (nextInStack.item.getExpectedNextSymbol() == item.production.lhs) {
			lhsFitsExpected = true;
			skipSaplingCheck = true;
		} else if (sapling.grammar
				.getAllInitializingNonTerminalsOf((NonTerminal) nextInStack.item.getExpectedNextSymbol())
				.contains(item.production.lhs)
		) {
			lhsFitsExpected = true;
		}
		return lhsFitsExpected &&
				(skipSaplingCheck || sapling.allowedBlindReductionNonTerminals.contains(item.production.lhs));
	}

	public static State join(Iterable<State> states) {
		if (states == null) {
			throw new IllegalArgumentException("states collection must be non-null");
		}
		Sapling sapling = null;
		Set<ItemStack> stacks = new HashSet<>();
		for (State state : states) {
			if (sapling == null) {
				sapling = state.sapling;
			} else if (sapling != state.sapling) {
				throw new IllegalArgumentException("all joining states must grow from the same sapling");
			}
			stacks.addAll(state.stacks);
		}
		if (sapling == null) {
			throw new IllegalArgumentException("states collection must be non-empty");
		}
		return new State(sapling, stacks);
	}

	private State(Sapling sapling, Set<ItemStack> stackSet) {
		this.sapling = sapling;
		stacks = CopyList.immutable(stackSet);
	}


	public final List<? extends ItemStackView> viewStacks() {
		return stacks;
	}

	public final Set<Object> getUsedStackIds() {
		return viewStacks().stream().map(ItemStackView::getId).collect(Collectors.toSet());
	}

	public final Set<Terminal> getExpectedNextSymbols() {
		return stacks.stream()
				.map(stack -> {
					ASymbol expectedNextSymbol = stack.item.getExpectedNextSymbol();
					assert expectedNextSymbol instanceof Terminal : "expected next symbol is not a Terminal";
					return (Terminal) expectedNextSymbol;
				})
				.collect(Collectors.toSet());
	}

}
