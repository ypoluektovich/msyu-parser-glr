package org.msyu.parser.treestack;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public final class TreeStack<E> {

	final Map<Object, Branch<E>> branchById;

	TreeStack() {
		branchById = new HashMap<>();
	}

	final Object push(Object oldId, Iterator<E> elements) {
		if (!elements.hasNext()) {
			return oldId;
		}
		final class Context {
			private Branch<E> branch;
			private E element;

			final boolean advance() {
				int position = 1;
				while (true) {
					boolean endOfBranch = position == branch.elements.size();
					if (!elements.hasNext()) {
						branch = endOfBranch ? branch : split(branch, position);
						return true;
					}
					if (endOfBranch) {
						element = elements.next();
						return false;
					}
					element = elements.next();
					if (!Objects.equals(branch.elements.get(position), element)) {
						branch = grow(split(branch, position), element, elements);
						return true;
					}
					++position;
				}
			}
		}
		Context c = new Context();
		c.element = elements.next();
		if (oldId == null) {
			for (Branch<E> aBranch : branchById.values()) {
				if (aBranch.parent == null && Objects.equals(aBranch.elements.get(0), c.element)) {
					c.branch = aBranch;
					break;
				}
			}
			if (c.branch == null) {
				return register(new Branch<>(null, c.element, elements));
			}
			if (c.advance()) {
				return c.branch;
			}
		} else {
			c.branch = branchById.get(oldId);
			if (c.branch == null) {
				throw new IllegalArgumentException("asked to push to nonexistent branch");
			}
		}
		while (true) {
			Branch<E> nextBranch = c.branch.joint.get(c.element);
			if (nextBranch == null) {
				return grow(c.branch, c.element, elements);
			}
			c.branch = nextBranch;
			if (c.advance()) {
				return c.branch;
			}
		}
	}

	private Branch<E> register(Branch<E> branch) {
		branchById.put(branch, branch);
		return branch;
	}

	private Branch<E> grow(Branch<E> branch, E element, Iterator<E> elements) {
		return register(branch.grow(element, elements));
	}

	private Branch<E> split(Branch<E> branch, int position) {
		return register(branch.splitAt(position));
	}

}
