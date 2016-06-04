package org.msyu.parser.glr;

import org.msyu.javautil.cf.CopySet;
import org.msyu.javautil.cf.WrapList;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;
import java.util.Set;

import static java.util.Collections.emptyList;

public final class State {

	private final Sapling sapling;
	private final List<ItemStack> stacks;
	private final Set<Object> stackIds;

	public static State initializeFrom(Sapling sapling, GlrCallback callback) {
		return new State(sapling, callback);
	}

	private State(Sapling sapling, GlrCallback callback) {
		this.sapling = sapling;

		Object initialBranchId = callback.newBranchId();
		callback.shift(null, emptyList(), initialBranchId);
		List<ItemStack> stacks = new ArrayList<>();
		for (Item item : sapling.initialItems) {
			ItemStack stack = new ItemStack(initialBranchId, item.position, item, null);
			stacks.add(stack);
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
					Object shiftedBranchId = callback.newBranchId();
					callback.shift(oldStack.id, Collections.singletonList(terminal), shiftedBranchId);
					ItemStack newStack = new ItemStack(shiftedBranchId, oldStack.prependedEmptySymbols, oldStack.item.shift(), oldStack.nextInStack);
					shiftedStacks.add(newStack);
				}
			}
			reduce(shiftedStacks, stacks, callback);
		}
		this.stacks = WrapList.immutable(stacks);
		this.stackIds = CopySet.immutableHash(stacks, stack -> stack.id);
	}

	private void reduce(List<ItemStack> oldStacks, List<ItemStack> newStacks, GlrCallback callback) {
		Queue<ItemStack> stacksQueue = new ArrayDeque<>(oldStacks);
		for (ItemStack stack; (stack = stacksQueue.poll()) != null; ) {
			if (!stack.item.isFinished()) {
				newStacks.add(stack);
				continue;
			}
			ItemStack nextInStack = stack.nextInStack;
			Production completedProduction = stack.item.production;
			NonTerminal completedSymbol = completedProduction.lhs;

			Object reducedBranchId = callback.newBranchId();
			callback.reduce(stack.id, completedProduction, stack.prependedEmptySymbols, reducedBranchId);

			for (Item newItem : sapling.grammar.getItemsInitializedBy(completedSymbol)) {
				if (itemIsGoodAsBlindReductionTarget(newItem, nextInStack)) {
					Item shiftedNewItem = newItem.shift();
					ItemStack newStack = new ItemStack(reducedBranchId, newItem.position, shiftedNewItem, nextInStack);
					stacksQueue.add(newStack);
				}
			}

			if (nextInStack != null && nextInStack.item.getExpectedNextSymbol().equals(completedSymbol)) {
				ItemStack newStack = new ItemStack(reducedBranchId, nextInStack.prependedEmptySymbols, nextInStack.item.shift(), nextInStack.nextInStack);
				stacksQueue.add(newStack);
			}
		}

		for (ListIterator<ItemStack> iterator = newStacks.listIterator(); iterator.hasNext(); ) {
			ItemStack stack = iterator.next();
			Item item = stack.item;
			if (item.getExpectedNextSymbol() instanceof NonTerminal) {
				iterator.remove();
				NonTerminal nextSymbol = (NonTerminal) item.getExpectedNextSymbol();
				for (Item nextItem : sapling.grammar.getInitializingItemsOf(nextSymbol)) {
					iterator.add(new ItemStack(stack.id, nextItem.position, nextItem, stack));
				}
			}
		}
	}

	private boolean itemIsGoodAsBlindReductionTarget(Item item, ItemStack nextInStack) {
		boolean itemLhsInitializesNextInStack =
				nextInStack == null ||
						sapling.grammar
								.getInitializingNonTerminalsOf((NonTerminal) nextInStack.item.getExpectedNextSymbol())
								.contains(item.production.lhs);
		return itemLhsInitializesNextInStack &&
				sapling.allowedBlindReductionNonTerminals.contains(item.production.lhs);
	}


	public final Set<Object> getUsedStackIds() {
		return stackIds;
	}

}
