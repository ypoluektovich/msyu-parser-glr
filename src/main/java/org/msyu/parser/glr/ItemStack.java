package org.msyu.parser.glr;

final class ItemStack {

	final Item item;
	final ItemStack nextInStack;

	ItemStack(Item item, ItemStack nextInStack) {
		this.item = item;
		this.nextInStack = nextInStack;
	}

}
