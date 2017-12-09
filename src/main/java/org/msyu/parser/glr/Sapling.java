package org.msyu.parser.glr;

import org.msyu.javautil.cf.CopyList;
import org.msyu.javautil.cf.CopySet;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Sapling {

	final Grammar grammar;
	final Set<NonTerminal> goals;
	final List<Item> initialItems;
	final Set<NonTerminal> allowedBlindReductionNonTerminals;

	Sapling(Grammar grammar, Collection<NonTerminal> goals) {
		this.grammar = grammar;
		this.goals = CopySet.immutableHash(goals);
		Set<Item> initialItems = new HashSet<>();
		Set<NonTerminal> allowedBlindReductionNonTerminals = new HashSet<>(this.goals);
		for (NonTerminal goal : this.goals) {
			initialItems.addAll(grammar.getAllInitializingItemsOf(goal));
			allowedBlindReductionNonTerminals.addAll(grammar.getAllInitializingNonTerminalsOf(goal));
		}
		this.initialItems = CopyList.immutable(initialItems);
		this.allowedBlindReductionNonTerminals = Collections.unmodifiableSet(allowedBlindReductionNonTerminals);
	}

}
