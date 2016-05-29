package org.msyu.parser.glr;

import org.msyu.javautil.cf.WrapList;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;
import java.util.stream.Collectors;

public final class State {

	private final Sapling sapling;
	private final List<ItemStack> stacks;
	/** todo: encapsulate */
	public final List<NonTerminal> completedGoals;

	public static State initializeFrom(Sapling sapling) {
		return new State(sapling);
	}

	private State(Sapling sapling) {
		this.sapling = sapling;

		List<ItemStack> stacks = new ArrayList<>();
		for (Item item : sapling.initialItems) {
			stacks.add(new ItemStack(item, null));
		}
		this.stacks = WrapList.immutable(stacks);

		List<NonTerminal> completedGoals = new ArrayList<>();
		for (NonTerminal goal : sapling.goals) {
			if (sapling.grammar.skippableSymbols.contains(goal)) {
				completedGoals.add(goal);
			}
		}
		this.completedGoals = WrapList.immutable(completedGoals);
	}

	public final State advance(Terminal terminal) {
		return new State(this, terminal);
	}

	private State(State previousState, Terminal terminal) {
		this.sapling = previousState.sapling;
		List<ItemStack> stacks = new ArrayList<>();
		List<NonTerminal> completedGoals = new ArrayList<>();
		reduce(
				previousState.stacks.stream()
						.filter(stack -> stack.item.getExpectedNextSymbol().equals(terminal))
						.map(stack -> new ItemStack(stack.item.shift(), stack.nextInStack))
						.collect(Collectors.toList()),
				stacks,
				completedGoals
		);
		this.stacks = WrapList.immutable(stacks);
		this.completedGoals = WrapList.immutable(completedGoals);
	}

	private void reduce(List<ItemStack> oldStacks, List<ItemStack> newStacks, List<NonTerminal> completedGoals) {
		Queue<ItemStack> stacksQueue = new ArrayDeque<>(oldStacks);
		for (ItemStack stack; (stack = stacksQueue.poll()) != null; ) {
			if (!stack.item.isFinished()) {
				newStacks.add(stack);
				continue;
			}
			ItemStack nextInStack = stack.nextInStack;
			NonTerminal completedSymbol = stack.item.production.lhs;
			if (sapling.goals.contains(completedSymbol)) {
				// todo: supply more info than the completed symbol
				completedGoals.add(completedSymbol);
			}

			sapling.grammar.getItemsInitializedBy(completedSymbol).stream()
					.filter(item ->
							nextInStack == null ||
									sapling.grammar
											.getInitializingNonTerminalsOf((NonTerminal) nextInStack.item.getExpectedNextSymbol())
											.contains(item.production.lhs)
					)
					.filter(item -> sapling.allowedBlindReductionNonTerminals.contains(item.production.lhs))
					.map(Item::shift)
					.distinct()
					.map(item -> new ItemStack(item, nextInStack))
					.forEach(stacksQueue::add);

			if (nextInStack != null && nextInStack.item.getExpectedNextSymbol().equals(completedSymbol)) {
				stacksQueue.add(new ItemStack(nextInStack.item.shift(), nextInStack.nextInStack));
			}
		}

		for (ListIterator<ItemStack> iterator = newStacks.listIterator(); iterator.hasNext(); ) {
			ItemStack stack = iterator.next();
			Item item = stack.item;
			if (item.getExpectedNextSymbol() instanceof NonTerminal) {
				iterator.remove();
				NonTerminal nextSymbol = (NonTerminal) item.getExpectedNextSymbol();
				for (Item nextItem : sapling.grammar.getInitializingItemsOf(nextSymbol)) {
					iterator.add(new ItemStack(nextItem, stack));
				}
			}
		}
	}

}
