package org.msyu.parser.richgrammar;

import org.msyu.parser.glr.GlrCallback;

public interface BasicReducer {
	Object reduce(GlrCallback<?> glrCallback, Object oldBranch);
}
