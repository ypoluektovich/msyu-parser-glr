package org.msyu.parser.glr;

import org.msyu.javautil.cf.CopyList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

final class Production {

	final NonTerminal lhs;
	final List<ASymbol> rhs;
	final List<Item> items;

	Production(NonTerminal lhs, List<ASymbol> rhs) {
		this.lhs = lhs;
		this.rhs = CopyList.immutable(rhs);
		this.items = Collections.unmodifiableList(
				IntStream.rangeClosed(0, this.rhs.size())
						.mapToObj(pos -> new Item(this, pos))
						.collect(Collectors.<Item, List<Item>>toCollection(ArrayList::new))
		);
	}

	@Override
	public final String toString() {
		return Item.toString(this, -1);
	}

}
