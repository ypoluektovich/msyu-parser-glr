package org.msyu.parser.richgrammar;

import org.msyu.parser.glr.GlrCallback;

public interface AltReducer {

	Object reduce(GlrCallback<?> glrCallback, Object oldBranch, int alternativeIndex);

	AltReducer NOOP = (c, o, i) -> o;

}
