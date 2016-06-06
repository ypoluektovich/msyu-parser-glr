package org.msyu.parser.glr;

import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

public abstract class GrammarAssertions {

	protected abstract Grammar grammar();

	@Test
	public final void terminalsAreNotSkippable() {
		Grammar grammar = grammar();
		for (Terminal symbol : grammar.terminals) {
			assertThat(grammar.skippableSymbols, not(hasItem(symbol)));
		}
	}

	@Test
	public final void terminalsAreFillable() {
		Grammar grammar = grammar();
		for (Terminal symbol : grammar.terminals) {
			assertThat(grammar.fillableSymbols, hasItem(symbol));
		}
	}

	public static void runAllAssertionsFor(Grammar grammar) {
		GrammarAssertions ga = new GrammarAssertions() {
			@Override
			protected final Grammar grammar() {
				return grammar;
			}
		};
		ga.terminalsAreNotSkippable();
		ga.terminalsAreFillable();
	}

}
