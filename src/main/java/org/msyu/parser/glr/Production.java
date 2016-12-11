package org.msyu.parser.glr;

import org.msyu.javautil.cf.CopyList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class Production {

	public final NonTerminal lhs;
	public final List<ASymbol> rhs;
	public final boolean isGreedy;
	public final List<Item> items;

	Production(NonTerminal lhs, List<ASymbol> rhs, boolean isGreedy) {
		this.lhs = lhs;
		this.rhs = CopyList.immutable(rhs);
		this.isGreedy = isGreedy;
		this.items = Collections.unmodifiableList(
				IntStream.rangeClosed(0, this.rhs.size())
						.mapToObj(pos -> new Item(this, pos))
						.collect(Collectors.<Item, List<Item>>toCollection(ArrayList::new))
		);
	}

	final GreedMark getGreedMark(Object start) {
		return isGreedy ? new GreedMark(this, start) : null;
	}


	@Override
	public final String toString() {
		return Item.toString(this, -1);
	}

	@Override
	public final boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || obj.getClass() != Production.class) {
			return false;
		}
		Production that = (Production) obj;
		return lhs.equals(that.lhs) && rhs.equals(that.rhs);
	}

	@Override
	public final int hashCode() {
		return Objects.hash(lhs, rhs);
	}

}
