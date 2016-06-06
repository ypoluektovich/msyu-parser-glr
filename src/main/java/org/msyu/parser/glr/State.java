package org.msyu.parser.glr;

import org.msyu.javautil.cf.CopySet;
import org.msyu.javautil.cf.WrapList;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public final class State {

	private final Sapling sapling;
	private final List<ItemStack> stacks;
	private final Set<Object> stackIds;

	public static State initializeFrom(Sapling sapling, GlrCallback callback) {
		return new State(sapling, callback);
	}

	private State(Sapling sapling, GlrCallback callback) {
		this.sapling = sapling;
		Object initialBranchId = callback.shift(null, Collections.emptyList());
		List<ItemStack> stacks = new ArrayList<>();
		for (Item item : sapling.initialItems) {
			stacks.add(new ItemStack(initialBranchId, item.position, item, null));
		}
		this.stacks = WrapList.immutable(stacks);
		this.stackIds = CopySet.immutableHash(stacks, stack -> stack.id);
	}

	public final State advance(Terminal terminal, GlrCallback callback) {
		return new State(this, terminal, callback);
	}

	private State(State previousState, Terminal terminal, GlrCallback callback) {
		this.sapling = previousState.sapling;
		List<ItemStack> stacks = new ArrayList<>();
		{
			List<ItemStack> shiftedStacks = new ArrayList<>();
			for (ItemStack oldStack : previousState.stacks) {
				if (oldStack.item.getExpectedNextSymbol().equals(terminal)) {
					shiftedStacks.add(oldStack.shift(callback));
				}
			}
			reduce(shiftedStacks, stacks, callback);
		}
		this.stacks = WrapList.immutable(stacks);
		this.stackIds = CopySet.immutableHash(stacks, stack -> stack.id);
	}

	private void reduce(List<ItemStack> shiftedStacks, List<ItemStack> newStacks, GlrCallback callback) {
		Queue<ItemStack> reductionQueue = new ArrayDeque<>(shiftedStacks);
		Queue<ItemStack> expansionQueue = new ArrayDeque<>();

		for (ItemStack stack; (stack = reductionQueue.poll()) != null; ) {
			if (!stack.item.isFinished()) {
				if (sapling.grammar.isCompletable(stack.item)) {
					reductionQueue.add(stack.skipToEnd(callback));
				}
				expansionQueue.add(stack);
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

		for (ItemStack stack; (stack = expansionQueue.poll()) != null; ) {
			Item item = stack.item;
			do {
				ASymbol nextSymbol = item.getExpectedNextSymbol();
				if (sapling.grammar.fillableSymbols.contains(nextSymbol)) {
					stack = stack.skipTo(item);
					if (nextSymbol instanceof Terminal) {
						newStacks.add(stack);
					} else if (nextSymbol instanceof NonTerminal) {
						NonTerminal nextNonTerminal = (NonTerminal) nextSymbol;
						for (Item nextItem : sapling.grammar.getAllInitializingItemsOf(nextNonTerminal)) {
							newStacks.add(new ItemStack(stack.id, nextItem.position, nextItem, stack.copyWithNoId()));
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


	public final Set<Object> getUsedStackIds() {
		return stackIds;
	}

}
