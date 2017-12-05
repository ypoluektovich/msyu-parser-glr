package org.msyu.parser.glr;

import org.msyu.javautil.cf.CopyList;

import java.util.List;

import static java.util.stream.Collectors.joining;

public final class Node {

	private final Production production;

	private final List<Object> elements;

	public Node(Production production, List<Object> elements) {
		this.production = production;
		this.elements = CopyList.immutable(elements);
	}

	public final Production getProduction() {
		return production;
	}

	public final List<Object> getElements() {
		return elements;
	}

	@Override
	public String toString() {
		return String.format("[%s:%s]", production, elements.stream().map(Object::toString).collect(joining(" ")));
	}

}
