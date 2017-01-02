package org.msyu.parser.glr;

import java.util.Collection;

public final class Lifeline {

	final Collection<GreedMark> greedMarks;
	final Collection<GreedMark> newGreedMarks;

	Lifeline(ItemStack stack) {
		this.greedMarks = stack.greedMarks;
		this.newGreedMarks = stack.newGreedMarks;
	}

	final boolean isCulledByMark(GreedMark mark) {
		return greedMarks.contains(mark) && !newGreedMarks.contains(mark);
	}

}
