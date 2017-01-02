package org.msyu.parser.glr;

import org.msyu.javautil.cf.CopySet;

import java.util.Collection;
import java.util.Set;

public final class UnexpectedTokenException extends Exception {

	private final Set<Terminal> expected;

	UnexpectedTokenException(Collection<ItemStack> stacks) {
		this.expected = CopySet.immutableHash(stacks, stack -> (Terminal) stack.item.getExpectedNextSymbol());
	}

	public final Set<Terminal> getExpected() {
		return expected;
	}

}
