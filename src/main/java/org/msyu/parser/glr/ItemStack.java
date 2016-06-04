package org.msyu.parser.glr;

final class ItemStack {

	final Object id;
	final int prependedEmptySymbols;
	final Item item;
	final ItemStack nextInStack;
	private volatile ItemStack copyWithNoId;

	ItemStack(Object id, int prependedEmptySymbols, Item item, ItemStack nextInStack) {
		this.id = id;
		this.prependedEmptySymbols = prependedEmptySymbols;
		this.item = item;
		this.nextInStack = nextInStack;
	}

	final ItemStack shift(Object branchId) {
		return new ItemStack(branchId, prependedEmptySymbols, item.shift(), nextInStack);
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

}
