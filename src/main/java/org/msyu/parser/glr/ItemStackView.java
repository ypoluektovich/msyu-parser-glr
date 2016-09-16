package org.msyu.parser.glr;

import java.util.List;

public interface ItemStackView {

	Object getId();

	int getPrependedEmptySymbols();

	ItemStackFrame getFirstFrame();

	List<ItemStackFrame> getFrames();

}
