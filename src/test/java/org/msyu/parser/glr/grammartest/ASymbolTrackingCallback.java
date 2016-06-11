package org.msyu.parser.glr.grammartest;

import org.msyu.parser.glr.GlrCallback;
import org.msyu.parser.glr.State;
import org.msyu.parser.glr.Terminal;

public abstract class ASymbolTrackingCallback<T> implements GlrCallback {

	protected T token;

	public State advance(State state, T token) {
		this.token = token;
		state = state.advance(symbolByToken(token), this);
		this.token = null;
		return state;
	}

	protected abstract Terminal symbolByToken(T token);

}
