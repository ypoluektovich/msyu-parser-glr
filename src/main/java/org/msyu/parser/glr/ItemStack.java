package org.msyu.parser.glr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

final class ItemStack implements ItemStackView, ItemStackFrame {

	final Object id;
	private final int prependedEmptySymbols;
	final Item item;
	final ItemStack nextInStack;
	private final Object start;
	final Set<GreedMark> greedMarks;
	final List<GreedMark> newGreedMarks;
	private volatile ItemStack copyForChild;

	ItemStack(
			Object id,
			int prependedEmptySymbols,
			Item item,
			ItemStack nextInStack,
			Object start,
			Set<GreedMark> greedMarks,
			List<GreedMark> newGreedMarks
	) {
		this.id = id;
		this.prependedEmptySymbols = prependedEmptySymbols;
		this.item = item;
		this.nextInStack = nextInStack;
		this.start = start;
		this.greedMarks = greedMarks;
		this.newGreedMarks = newGreedMarks;
	}

	final <T> ItemStack shift(GlrCallback<T> callback, T token) {
		return new ItemStack(
				callback.shift(id, item.getCompletedSymbols(prependedEmptySymbols), token),
				0,
				item.shift(),
				nextInStack,
				start,
				greedMarks,
				emptyList()
		);
	}

	final ItemStack skipToEnd(GlrCallback<?> callback) {
		Production production = item.production;
		List<ASymbol> rhs = production.rhs;
		return new ItemStack(
				callback.skip(id, rhs.subList(item.position, rhs.size())),
				0,
				production.items.get(rhs.size()),
				nextInStack,
				start,
				greedMarks,
				newGreedMarks
		);
	}

	final ItemStack maybeAddGreedMark(Collection<GreedMark> marksToCull) {
		GreedMark greedMark = item.production.getGreedMark(start);
		if (greedMark == null) {
			return this;
		}
		marksToCull.add(greedMark);
		return new ItemStack(
				id,
				prependedEmptySymbols,
				item,
				nextInStack,
				start,
				greedMarks,
				addGreedMark(newGreedMarks, greedMark)
		);
	}

	private static List<GreedMark> addGreedMark(List<GreedMark> src, GreedMark myMark) {
		List<GreedMark> copy = new ArrayList<>(src.size() + 1);
		copy.addAll(src);
		copy.add(myMark);
		return copy;
	}

	/**
	 * Called on the stack <em>being reduced</em>.
	 */
	final ItemStack finishBlindReduction(GlrCallback<?> callback, Object reducedBranchId, Item newItem) {
		return new ItemStack(
				callback.insert(reducedBranchId, newItem.getCompletedSymbols()),
				0,
				newItem.shift(),
				nextInStack,
				start,
				greedMarks,
				newGreedMarks
		);
	}

	/**
	 * Called on the stack <em>into</em> which the reduction happened.
	 */
	final ItemStack finishGuidedReduction(ItemStack completedStack, GlrCallback<?> callback, Object reducedBranchId) {
		return new ItemStack(
				callback.insert(reducedBranchId, item.getCompletedSymbols(prependedEmptySymbols)),
				0,
				item.shift(),
				nextInStack,
				start,
				completedStack.greedMarks,
				newGreedMarks
		);
	}

	final ItemStack skipTo(Item item) {
		assert this.item.production == item.production : "ItemStack.skipTo() called with an item from another production";
		return new ItemStack(id, prependedEmptySymbols + item.position - this.item.position, item, nextInStack, start, greedMarks, newGreedMarks);
	}

	final ItemStack copyForChild() {
		ItemStack copyForChild = this.copyForChild;
		if (copyForChild == null) {
			// Even if we overwrite some other thread's value, it doesn't matter, so no locks or CAS are required.
			copyForChild = this.copyForChild = new ItemStack(null, prependedEmptySymbols, item, nextInStack, start, emptySet(), emptyList());
		}
		return copyForChild;
	}

	final boolean isCulledByMark(GreedMark mark) {
		return greedMarks.contains(mark) && !newGreedMarks.contains(mark);
	}

	final ItemStack forgetAndMergeMarks(Collection<Object> witheredPositions) {
		Set<GreedMark> mergedMarks = new HashSet<>(newGreedMarks);
		for (GreedMark mark : greedMarks) {
			if (!witheredPositions.contains(mark.startPosition)) {
				mergedMarks.add(mark);
			}
		}
		return new ItemStack(id, prependedEmptySymbols, item, nextInStack, start, unmodifiableSet(mergedMarks), emptyList());
	}


	@Override
	public final boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ItemStack that = (ItemStack) obj;
		return prependedEmptySymbols == that.prependedEmptySymbols &&
				Objects.equals(id, that.id) &&
				item.equals(that.item) &&
				Objects.equals(nextInStack, that.nextInStack) &&
				Objects.equals(start, that.start) &&
				greedMarks.equals(that.greedMarks) &&
				newGreedMarks.equals(that.newGreedMarks);
	}

	@Override
	public final int hashCode() {
		return Objects.hash(id, prependedEmptySymbols, item, nextInStack, start, greedMarks, newGreedMarks);
	}

	@Override
	public final String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(id);
		sb.append('[');
		describeTo(sb);
		sb.append(']');
		return sb.toString();
	}

	private void describeTo(StringBuilder sb) {
		if (nextInStack != null) {
			nextInStack.describeTo(sb);
			sb.append(", ");
		}
		sb.append(prependedEmptySymbols);
		sb.append('(');
		sb.append(item.toString());
		sb.append(')');
	}

	@Override
	public final Object getId() {
		return id;
	}

	@Override
	public final int getPrependedEmptySymbols() {
		return prependedEmptySymbols;
	}

	@Override
	public final ItemStackFrame getFirstFrame() {
		return this;
	}

	@Override
	public final List<ItemStackFrame> getFrames() {
		List<ItemStackFrame> frames = new ArrayList<>();
		ItemStack frame = this;
		do {
			frames.add(frame);
		} while ((frame = frame.nextInStack) != null);
		return frames;
	}

	@Override
	public final Item getItem() {
		return item;
	}

}
