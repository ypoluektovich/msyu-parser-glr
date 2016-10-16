package org.msyu.parser.richgrammar;

import org.msyu.parser.glr.GlrCallback;

public interface RptReducer {

	Object reduce(GlrCallback<?> glrCallback, Object oldBranch, int was, int increase);

	RptReducer NOOP = (c, o, w, i) -> o;

}
