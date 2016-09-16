package org.msyu.parser.glr;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

abstract class GrammarFruit extends GrammarSeed {

	final Map<String, ASymbol> symbolByName;

	GrammarFruit(Set<Terminal> terminals, Map<NonTerminal, Set<Production>> nonTerminals) {
		super(terminals, nonTerminals);
		symbolByName = new HashMap<>();
		for (Terminal terminal : terminals) {
			symbolByName.put(terminal.name, terminal);
		}
		for (NonTerminal nonTerminal : nonTerminals.keySet()) {
			symbolByName.put(nonTerminal.name, nonTerminal);
		}
	}

	public final Set<String> viewSymbolNames() {
		return Collections.unmodifiableSet(symbolByName.keySet());
	}

	public final ASymbol getSymbolByName(String name) {
		return symbolByName.get(name);
	}

}
