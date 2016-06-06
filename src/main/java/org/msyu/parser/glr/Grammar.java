package org.msyu.parser.glr;

import org.msyu.javautil.cf.CopyList;
import org.msyu.javautil.cf.CopyMap;
import org.msyu.javautil.cf.CopySet;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public final class Grammar extends GrammarSeed {

	private final Map<NonTerminal, NonTerminalData> precomputedNonTerminalData;
	final Set<ASymbol> skippableSymbols;
	final Set<ASymbol> fillableSymbols;
	private final Set<Item> completableItems;

	Grammar(GrammarSeed seed) {
		super(
				CopySet.immutableHash(seed.terminals),
				CopyMap.immutableHashV(seed.nonTerminals, CopySet::immutableHash)
		);
		Map<NonTerminal, NonTerminalData> precomputedNonTerminalData = CopyMap.hashV(nonTerminals, __ -> new NonTerminalData());
		Set<ASymbol> skippableSymbols = new HashSet<>();
		Set<ASymbol> fillableSymbols = new HashSet<>();
		Set<Item> completableItems = new HashSet<>();
		precomputeNonTerminalData(this, precomputedNonTerminalData, skippableSymbols, fillableSymbols, completableItems);
		this.precomputedNonTerminalData = CopyMap.immutableHashV(precomputedNonTerminalData, NonTerminalData::new);
		this.skippableSymbols = Collections.unmodifiableSet(skippableSymbols);
		this.fillableSymbols = Collections.unmodifiableSet(fillableSymbols);
		this.completableItems = Collections.unmodifiableSet(completableItems);
	}

	private static final class NonTerminalData {

		final Collection<Item> itemsInitializedByThis;
		final Set<Item> allInitialItems;
		final Set<NonTerminal> allInitialNonTerminals;

		NonTerminalData() {
			itemsInitializedByThis = new HashSet<>();
			allInitialItems = new HashSet<>();
			allInitialNonTerminals = new HashSet<>();
		}

		NonTerminalData(NonTerminalData other) {
			itemsInitializedByThis = CopyList.immutable(other.itemsInitializedByThis);
			allInitialItems = CopySet.immutableHash(other.allInitialItems);
			allInitialNonTerminals = CopySet.immutableHash(other.allInitialNonTerminals);
		}
	}

	private static void precomputeNonTerminalData(
			GrammarSeed seed,
			Map<NonTerminal, NonTerminalData> precomputedNonTerminalData,
			Set<ASymbol> skippableSymbols,
			Set<ASymbol> fillableSymbols,
			Set<Item> completableItems
	) {
		{
			abstract class ACheck implements Runnable {
				final Item item;
				ACheck(Item item) {
					this.item = item;
				}
			}
			Queue<ACheck> checksQueue = new ArrayDeque<>();
			Map<ASymbol, Boolean> isSymbolSkippable = new HashMap<>();
			Map<ASymbol, List<ACheck>> waitingChecksByDependency = new HashMap<>();
			Map<NonTerminal, Set<Production>> remainingProductionsByLHS = new HashMap<>();

			final class SkippableCheck extends ACheck {
				SkippableCheck(Item item) {
					super(item);
				}

				@Override
				public final void run() {
					NonTerminal lhs = item.production.lhs;
					// If we're here, the symbols in the producion before the item position are skippable.
					if (item.isFinished()) {
						isSymbolSkippable.put(lhs, true);
						skippableSymbols.add(lhs);
						remainingProductionsByLHS.get(lhs).clear();
						requeue(lhs);
					} else {
						ASymbol nextSymbol = item.getExpectedNextSymbol();
						Boolean nextSymbolSkippable = isSymbolSkippable.get(nextSymbol);
						if (nextSymbolSkippable == null) {
							waitingChecksByDependency.computeIfAbsent(nextSymbol, __ -> new ArrayList<ACheck>()).add(this);
						} else if (nextSymbolSkippable) {
							checksQueue.add(new SkippableCheck(item.shift()));
						} else {
							Set<Production> remainingProductions = remainingProductionsByLHS.get(lhs);
							remainingProductions.remove(item.production);
							if (remainingProductions.isEmpty()) {
								isSymbolSkippable.putIfAbsent(lhs, false);
								requeue(lhs);
							}
						}
					}
				}

				private void requeue(NonTerminal lhs) {
					List<ACheck> waitingChecks = waitingChecksByDependency.getOrDefault(lhs, Collections.emptyList());
					checksQueue.addAll(waitingChecks);
					waitingChecks.clear();
				}
			}
			for (Terminal terminal : seed.terminals) {
				isSymbolSkippable.put(terminal, false);
			}
			for (Map.Entry<NonTerminal, Set<Production>> symbolAndProductions : seed.nonTerminals.entrySet()) {
				remainingProductionsByLHS.put(symbolAndProductions.getKey(), new HashSet<>(symbolAndProductions.getValue()));
				for (Production production : symbolAndProductions.getValue()) {
					Item startingItem = production.items.get(0);
					checksQueue.add(new SkippableCheck(startingItem));
				}
			}
			for (ACheck check; (check = checksQueue.poll()) != null; ) {
				check.run();
			}
		}

		Set<Production> fillableProductions = new HashSet<>();
		{
			Queue<Production> checksQueue = new ArrayDeque<>();
			Map<ASymbol, Boolean> isSymbolFillable = new HashMap<>();
			Map<ASymbol, Set<Production>> waitingChecksByDependency = new HashMap<>();
			Map<NonTerminal, Set<Production>> remainingProductionsByLHS = new HashMap<>();

			final class CheckState implements Runnable {
				private final Production production;
				private final List<ASymbol> remainingSymbols;
				private boolean hasDefinitelyFillableSymbols = false;
				private boolean answerFound = false;

				CheckState(Production production) {
					this.production = production;
					remainingSymbols = new ArrayList<>(production.rhs);
				}

				@Override
				public final void run() {
					if (answerFound) {
						return;
					}
					for (Iterator<ASymbol> iterator = remainingSymbols.iterator(); iterator.hasNext(); ) {
						ASymbol symbol = iterator.next();
						Boolean fillable = isSymbolFillable.get(symbol);
						if (fillable == Boolean.TRUE) {
							hasDefinitelyFillableSymbols = true;
							iterator.remove();
							continue;
						}
						boolean skippable = skippableSymbols.contains(symbol);
						if (fillable == Boolean.FALSE) {
							if (skippable) {
								iterator.remove();
							} else {
								failProduction();
								return;
							}
						} else {
							waitingChecksByDependency.computeIfAbsent(symbol, __ -> new HashSet<>()).add(production);
						}
					}
					if (remainingSymbols.isEmpty() && !hasDefinitelyFillableSymbols) {
						failProduction();
						return;
					}
					if (hasDefinitelyFillableSymbols && remainingSymbols.stream().allMatch(skippableSymbols::contains)) {
						answerFound = true;
						NonTerminal lhs = production.lhs;
						fillableProductions.add(production);
						isSymbolFillable.put(lhs, true);
						fillableSymbols.add(lhs);
						remainingProductionsByLHS.get(lhs).clear();
						requeue(lhs);
					}
				}

				private void requeue(NonTerminal lhs) {
					Set<Production> waitingChecks = waitingChecksByDependency.getOrDefault(lhs, Collections.emptySet());
					checksQueue.addAll(waitingChecks);
					waitingChecks.clear();
				}

				private void failProduction() {
					answerFound = true;
					NonTerminal lhs = production.lhs;
					Set<Production> remainingProductions = remainingProductionsByLHS.get(lhs);
					remainingProductions.remove(production);
					if (remainingProductions.isEmpty()) {
						isSymbolFillable.putIfAbsent(lhs, false);
						requeue(lhs);
					}
				}
			}
			for (Terminal terminal : seed.terminals) {
				isSymbolFillable.put(terminal, true);
				fillableSymbols.add(terminal);
			}
			Map<Production, CheckState> stateByProduction = new HashMap<>();
			for (Map.Entry<NonTerminal, Set<Production>> symbolAndProductions : seed.nonTerminals.entrySet()) {
				Set<Production> productions = symbolAndProductions.getValue();
				remainingProductionsByLHS.put(symbolAndProductions.getKey(), new HashSet<>(productions));
				for (Production production : productions) {
					stateByProduction.put(production, new CheckState(production));
					checksQueue.add(production);
				}
			}
			for (Production production; (production = checksQueue.poll()) != null; ) {
				stateByProduction.get(production).run();
			}
		}

		// todo: warn about unfillable productions? warn about those we couldn't determine?

		{
			abstract class Signal<P> {
				final NonTerminal dst;
				final P payload;
				Signal(NonTerminal dst, P payload) {
					this.dst = dst;
					this.payload = payload;
				}

				final boolean applyTo(NonTerminalData data) {
					return getDestinationCollection(data).add(payload);
				}
				abstract Collection<P> getDestinationCollection(NonTerminalData dstData);
				abstract Signal<P> propagate(NonTerminal newDst);
			}
			final class InitialItemSignal extends Signal<Item> {
				InitialItemSignal(NonTerminal dst, Item payload) {
					super(dst, payload);
				}
				@Override
				final Collection<Item> getDestinationCollection(NonTerminalData dstData) {
					return dstData.allInitialItems;
				}
				@Override
				final InitialItemSignal propagate(NonTerminal newDst) {
					return new InitialItemSignal(newDst, payload);
				}
			}
			final class InitialNonTerminalSignal extends Signal<NonTerminal> {
				InitialNonTerminalSignal(NonTerminal dst, NonTerminal payload) {
					super(dst, payload);
				}
				@Override
				final Collection<NonTerminal> getDestinationCollection(NonTerminalData dstData) {
					return dstData.allInitialNonTerminals;
				}
				@Override
				final InitialNonTerminalSignal propagate(NonTerminal newDst) {
					return new InitialNonTerminalSignal(newDst, payload);
				}
			}
			Queue<Signal> signalQueue = new ArrayDeque<>();
			Map<NonTerminal, Set<NonTerminal>> symbolsInitializedByKey = CopyMap.hashV(seed.nonTerminals, __ -> new HashSet<>());
			for (Map.Entry<NonTerminal, Set<Production>> symbolAndProductions : seed.nonTerminals.entrySet()) {
				NonTerminal lhs = symbolAndProductions.getKey();
				for (Production production : symbolAndProductions.getValue()) {
					if (!fillableProductions.contains(production)) {
						continue;
					}

					List<ASymbol> rhs = production.rhs;

					{
						Item item = production.items.get(rhs.size());
						do {
							completableItems.add(item);
							if (item.position == 0) {
								break;
							}
							item = production.items.get(item.position - 1);
						} while (skippableSymbols.contains(item.getExpectedNextSymbol()));
					}

					for (int i = 0; i < rhs.size(); ++i) {
						ASymbol initial = rhs.get(i);
						Item item = production.items.get(i);
						if (initial instanceof Terminal) {
							signalQueue.add(new InitialItemSignal(lhs, item));
						} else if (initial instanceof NonTerminal) {
							if (fillableSymbols.contains(initial)) {
								signalQueue.add(new InitialNonTerminalSignal(lhs, (NonTerminal) initial));
								precomputedNonTerminalData.get(initial).itemsInitializedByThis.add(item);
								symbolsInitializedByKey.get(initial).add(lhs);
							}
						}
						if (!skippableSymbols.contains(initial)) {
							break;
						}
					}
				}
			}
			for (Signal<?> signal; (signal = signalQueue.poll()) != null; ) {
				if (signal.applyTo(precomputedNonTerminalData.get(signal.dst))) {
					for (NonTerminal newDst : symbolsInitializedByKey.get(signal.dst)) {
						signalQueue.add(signal.propagate(newDst));
					}
				}
			}
		}
	}


	final Collection<Item> getAllInitializingItemsOf(NonTerminal symbol) {
		return precomputedNonTerminalData.get(symbol).allInitialItems;
	}

	final Collection<NonTerminal> getAllInitializingNonTerminalsOf(NonTerminal symbol) {
		return precomputedNonTerminalData.get(symbol).allInitialNonTerminals;
	}

	final Collection<Item> getItemsInitializedBy(NonTerminal symbol) {
		return precomputedNonTerminalData.get(symbol).itemsInitializedByThis;
	}

	final boolean isCompletable(Item item) {
		return completableItems.contains(item);
	}


	public final Sapling newSapling(Collection<NonTerminal> goals) {
		return new Sapling(this, goals);
	}

	public final Sapling newSapling(NonTerminal... goals) {
		return new Sapling(this, Arrays.asList(goals));
	}

	public final Sapling newSapling(NonTerminal goal) {
		return new Sapling(this, Collections.singleton(goal));
	}


}
