package org.msyu.parser.glr;

final class ItemStack {

	final Item item;
	final ItemStack nextInStack;

	ItemStack(Item item, ItemStack nextInStack) {
		this.item = item;
		this.nextInStack = nextInStack;
	}

	@Override
	public final String toString() {
		StringBuilder sb = new StringBuilder();
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
		sb.append(item.toString());
	}

}
