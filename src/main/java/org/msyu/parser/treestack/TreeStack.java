package org.msyu.parser.treestack;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Consumer;

public final class TreeStack<E> {

	final Map<Object, Branch<E>> branchById;

	public TreeStack() {
		branchById = new HashMap<>();
	}

	public final Object push(Object oldId, Iterator<E> elements) {
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

	public final Object pop(Object oldId, int count, Consumer<E> sink) {
		if (oldId == null) {
			if (count != 0) {
				throw new IllegalArgumentException("asked to pop non-zero elements from empty stack");
			}
			return null;
		}
		Branch<E> branch = branchById.get(oldId);
		if (branch == null) {
			throw new IllegalArgumentException("asked to pop from nonexistent branch");
		}
		if (branch.fullSize < count) {
			throw new IllegalArgumentException("underflow");
		}
		while (count > 0) {
			int branchSize = branch.elements.size();
			for (int i = branchSize - 1, m = Math.max(0, branchSize - count); i >= m; --i) {
				sink.accept(branch.elements.get(i));
			}
			if (count < branchSize) {
				return split(branch, branchSize - count);
			}
			count -= branchSize;
			branch = branch.parent;
		}
		return branch;
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

	public final void retain(Collection<Object> retainIds) {
		Queue<Branch<E>> removalQueue = new ArrayDeque<>();
		for (Branch<E> branch : branchById.values()) {
			if (branch.joint.isEmpty() && !retainIds.contains(branch)) {
				removalQueue.add(branch);
			}
		}
		for (Branch<E> branch; (branch = removalQueue.poll()) != null; ) {
			branchById.remove(branch);
			Branch<E> parent = branch.parent;
			if (parent != null) {
				parent.joint.remove(branch.elements.get(0));
				if (parent.joint.isEmpty() && !retainIds.contains(parent)) {
					removalQueue.add(parent);
				}
			}
		}
	}

	public final void merge(Collection<Object> retainIds) {
		for (Iterator<Branch<E>> iterator = branchById.values().iterator(); iterator.hasNext(); ) {
			Branch<E> branch = iterator.next();
			if (branch.joint.size() != 1 || retainIds.contains(branch)) {
				continue;
			}
			Branch<E> nextBranch = branch.joint.values().iterator().next();
			nextBranch.elements.addAll(0, branch.elements);
			nextBranch.parent = branch.parent;
			if (branch.parent != null) {
				branch.parent.joint.put(branch.elements.get(0), nextBranch);
			}
			iterator.remove();
		}
	}

	public final void enumerate(Object id, Consumer<E> sink) {
		Objects.requireNonNull(sink, "asked to enumerate into null sink");
		if (id == null) {
			return;
		}
		Branch<E> branch = branchById.get(id);
		if (branch == null) {
			throw new IllegalArgumentException("asked to enumerate nonexistent branch");
		}
		Deque<Branch<E>> branches = new ArrayDeque<>();
		while (branch != null) {
			branches.addLast(branch);
			branch = branch.parent;
		}
		while ((branch = branches.pollLast()) != null) {
			for (E element : branch.elements) {
				sink.accept(element);
			}
		}
	}

}
