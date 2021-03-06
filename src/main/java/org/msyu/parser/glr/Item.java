package org.msyu.parser.glr;

import java.util.List;

public final class Item {

	private static final String SPACE_ARROW = " \u2192";
	private static final String SPACE_EPSILON = " \u03B5";
	private static final String SPACE_BULLET = " \u2022";

	public final Production production;
	public final int position;

	/**
	 * USE ONLY IN PRODUCTION CONSTRUCTOR!
	 */
	Item(Production production, int position) {
		this.production = production;
		this.position = position;
	}

	public final boolean isFinished() {
		return position == production.rhs.size();
	}

	final List<ASymbol> getCompletedSymbols() {
		return getCompletedSymbols(position);
	}

	final List<ASymbol> getCompletedSymbols(int count) {
		return production.rhs.subList(position - count, position);
	}

	public final ASymbol getExpectedNextSymbol() {
		return production.rhs.get(position);
	}

	final Item shift() {
		return production.items.get(position + 1);
	}


	@Override
	public final String toString() {
		return toString(production, position);
	}

	static String toString(Production production, int position) {
		StringBuilder sb = new StringBuilder();
		sb.append(production.lhs.name);
		sb.append(SPACE_ARROW);
		if (production.rhs.isEmpty()) {
			sb.append(SPACE_EPSILON);
		} else {
			for (int i = 0; i < production.rhs.size(); ++i) {
				if (position == i) {
					sb.append(SPACE_BULLET);
				}
				sb.append(' ');
				sb.append(production.rhs.get(i).name);
			}
		}
		if (position == production.rhs.size()) {
			sb.append(SPACE_BULLET);
		}
		return sb.toString();
	}

}
