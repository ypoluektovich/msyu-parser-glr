package org.msyu.parser.treestack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

final class Branch<E> {

	Branch<E> parent;
	final List<E> elements;
	final Map<E, Branch<E>> joint;

	Branch(Branch<E> parent, E firstElement, Iterator<E> otherElements) {
		this.parent = parent;
		elements = new ArrayList<>();
		elements.add(firstElement);
		while (otherElements.hasNext()) {
			elements.add(otherElements.next());
		}
		joint = new HashMap<>();
	}

	final Branch<E> grow(E firstElement, Iterator<E> otherElements) {
		assert !joint.containsKey(firstElement);

		Branch<E> newBranch = new Branch<>(this, firstElement, otherElements);
		joint.put(firstElement, newBranch);
		return newBranch;
	}

	final Branch<E> splitAt(int position) {
		assert position >= 1;
		assert position < elements.size();

		Branch<E> firstPart = new Branch<>(parent, elements.get(0), elements.subList(1, position).iterator());
		elements.subList(0, position).clear();
		firstPart.joint.put(elements.get(0), this);
		if (parent != null) {
			parent.joint.put(firstPart.elements.get(0), firstPart);
		}
		parent = firstPart;
		return firstPart;
	}

}
