package org.msyu.parser.glr;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class GrammarSeed {

	final Set<Terminal> terminals;
	final Map<NonTerminal, Set<Production>> nonTerminals;

	GrammarSeed(Set<Terminal> terminals, Map<NonTerminal, Set<Production>> nonTerminals) {
		this.terminals = terminals;
		this.nonTerminals = nonTerminals;
	}

	public final Set<Terminal> viewTerminals() {
		return Collections.unmodifiableSet(terminals);
	}

	public final Set<NonTerminal> viewNonTerminals() {
		return Collections.unmodifiableSet(nonTerminals.keySet());
	}

	public final Set<Production> viewProductionsOf(NonTerminal nonTerminal) {
		Set<Production> productions = nonTerminals.get(nonTerminal);
		return productions == null ? null : Collections.unmodifiableSet(productions);
	}

}
