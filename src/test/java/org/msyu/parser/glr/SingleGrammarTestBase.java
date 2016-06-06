package org.msyu.parser.glr;

public abstract class SingleGrammarTestBase extends GrammarAssertions {

	protected Grammar grammar;

	@Override
	protected final Grammar grammar() {
		return grammar;
	}

}
