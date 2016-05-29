package org.msyu.parser.glr;

import java.util.Map;
import java.util.Set;

public class GrammarSeed {

	final Set<Terminal> terminals;

	final Map<NonTerminal, Set<Production>> nonTerminals;

	GrammarSeed(Set<Terminal> terminals, Map<NonTerminal, Set<Production>> nonTerminals) {
		this.terminals = terminals;
		this.nonTerminals = nonTerminals;
	}

}
