package org.msyu.parser.glr;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class ItemStack implements ItemStackView, ItemStackFrame {

	final Object id;
	private final int prependedEmptySymbols;
	final Item item;
	final ItemStack nextInStack;
	private volatile ItemStack copyWithNoId;

	ItemStack(Object id, int prependedEmptySymbols, Item item, ItemStack nextInStack) {
		this.id = id;
		this.prependedEmptySymbols = prependedEmptySymbols;
		this.item = item;
		this.nextInStack = nextInStack;
	}

	final <T> ItemStack shift(GlrCallback<T> callback, T token) {
		return new ItemStack(
				callback.shift(id, item.getCompletedSymbols(prependedEmptySymbols), token),
				0,
				item.shift(),
				nextInStack
		);
	}

	final ItemStack skipToEnd(GlrCallback<?> callback) {
		Production production = item.production;
		List<ASymbol> rhs = production.rhs;
		return new ItemStack(
				callback.skip(id, rhs.subList(item.position, rhs.size())),
				0,
				production.items.get(rhs.size()),
				nextInStack
		);
	}

	/**
	 * Called on the stack <em>being reduced</em>.
	 */
	final ItemStack finishBlindReduction(GlrCallback<?> callback, Object reducedBranchId, Item newItem) {
		return new ItemStack(
				callback.insert(reducedBranchId, newItem.getCompletedSymbols()),
				0,
				newItem.shift(),
				nextInStack
		);
	}

	/**
	 * Called on the stack <em>into</em> which the reduction happened.
	 */
	final ItemStack finishGuidedReduction(GlrCallback<?> callback, Object reducedBranchId) {
		return new ItemStack(
				callback.insert(reducedBranchId, item.getCompletedSymbols(prependedEmptySymbols)),
				0,
				item.shift(),
				nextInStack
		);
	}

	final ItemStack skipTo(Item item) {
		assert this.item.production == item.production : "ItemStack.skipTo() called with an item from another production";
		return new ItemStack(id, prependedEmptySymbols + item.position - this.item.position, item, nextInStack);
	}

	final ItemStack copyWithNoId() {
		ItemStack copyWithNoId = this.copyWithNoId;
		if (copyWithNoId == null) {
			// Even if we overwrite some other thread's value, it doesn't matter, so no locks or CAS are required.
			copyWithNoId = this.copyWithNoId = new ItemStack(null, prependedEmptySymbols, item, nextInStack);
		}
		return copyWithNoId;
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
				Objects.equals(nextInStack, that.nextInStack);
	}

	@Override
	public final int hashCode() {
		return Objects.hash(id, prependedEmptySymbols, item, nextInStack);
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
