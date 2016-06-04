package org.msyu.parser.glr;

final class ItemStack {

	final Object id;
	final int prependedEmptySymbols;
	final Item item;
	final ItemStack nextInStack;

	ItemStack(Object id, int prependedEmptySymbols, Item item, ItemStack nextInStack) {
		this.id = id;
		this.prependedEmptySymbols = prependedEmptySymbols;
		this.item = item;
		this.nextInStack = nextInStack;
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
