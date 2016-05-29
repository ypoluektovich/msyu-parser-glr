package org.msyu.parser.glr;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class GrammarBuilder extends GrammarSeed {

	private final Map<String, ASymbol> symbolByName = new HashMap<>();

	public GrammarBuilder() {
		super(new HashSet<>(), new HashMap<>());
	}

	public final Terminal addTerminal(String name) {
		checkDupSymbolName(name);
		Terminal symbol = new Terminal(name);
		terminals.add(symbol);
		symbolByName.put(name, symbol);
		return symbol;
	}

	public final NonTerminal addNonTerminal(String name) {
		checkDupSymbolName(name);
		NonTerminal symbol = new NonTerminal(name);
		nonTerminals.put(symbol, new HashSet<>());
		symbolByName.put(name, symbol);
		return symbol;
	}

	public final void addProduction(NonTerminal lhs, List<ASymbol> rhs) {
		if (!nonTerminals.containsKey(lhs)) {
			throw new IllegalArgumentException("production lhs must be a non-terminal from this grammar");
		}
		if (rhs == null) {
			throw new IllegalArgumentException("production rhs must be non-null");
		}
		checkCollection(
				rhs,
				"production rhs",
				true,
				sym -> {
					Collection<? extends ASymbol> collection;
					if (sym instanceof Terminal) {
						collection = terminals;
					} else if (sym instanceof NonTerminal) {
						collection = nonTerminals.keySet();
					} else {
						throw new IllegalArgumentException("production rhs must contain only terminals and non-terminals");
					}
					if (!collection.contains(sym)) {
						throw new IllegalArgumentException("production rhs must contain only symbols from this grammar");
					}
				}
		);
		nonTerminals.get(lhs).add(new Production(lhs, rhs));
	}

	public final void addProduction(NonTerminal lhs, ASymbol... rhs) {
		addProduction(lhs, rhs == null ? null : Arrays.asList(rhs));
	}

	public final Grammar build() {
		checkCollection(terminals, "terminals set", false, null);
		checkCollection(nonTerminals.entrySet(), "non-terminals set", false, entry -> {
			if (entry.getValue().isEmpty()) {
				throw new GrammarException("non-terminal " + entry.getKey().name + " does not have productions");
			}
		});
		return new Grammar(this);
	}


	private void checkDupSymbolName(String name) {
		if (symbolByName.containsKey(name)) {
			throw new GrammarException("grammar already contains a symbol named " + name);
		}
	}

	private static <T> void checkCollection(
			Collection<T> collection,
			String description,
			boolean allowEmpty,
			Consumer<T> customChecker
	) {
		if (!allowEmpty && collection.isEmpty()) {
			throw new GrammarException(description + " must not be empty");
		}
		for (T element : collection) {
			if (customChecker != null) {
				customChecker.accept(element);
			}
		}
	}

}
