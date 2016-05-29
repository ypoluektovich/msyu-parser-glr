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
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public final class Grammar extends GrammarSeed {

	private final Map<NonTerminal, NonTerminalData> precomputedNonTerminalData;
	final Set<NonTerminal> skippableSymbols;

	Grammar(GrammarSeed seed) {
		super(
				CopySet.immutableHash(seed.terminals),
				CopyMap.immutableHashV(seed.nonTerminals, CopySet::immutableHash)
		);
		Set<NonTerminal> skippableSymbols = new HashSet<>();
		this.precomputedNonTerminalData = precomputeNonTerminalData(this, skippableSymbols);
		this.skippableSymbols = Collections.unmodifiableSet(skippableSymbols);
	}

	private static final class NonTerminalData {

		final Collection<Item> itemsInitializedByThis;
		final Set<Item> initialItems;
		final Set<NonTerminal> initialNonTerminals;

		NonTerminalData() {
			itemsInitializedByThis = new HashSet<>();
			initialItems = new HashSet<>();
			initialNonTerminals = new HashSet<>();
		}

		NonTerminalData(NonTerminalData other) {
			itemsInitializedByThis = CopyList.immutable(other.itemsInitializedByThis);
			initialItems = CopySet.immutableHash(other.initialItems);
			initialNonTerminals = CopySet.immutableHash(other.initialNonTerminals);
		}
	}

	private static Map<NonTerminal, NonTerminalData> precomputeNonTerminalData(GrammarSeed seed, Set<NonTerminal> skippableSymbols) {
		abstract class ACheck implements Runnable {
			final Item item;

			ACheck(Item item) {
				this.item = item;
			}
		}

		Map<ASymbol, Boolean> isSymbolSkippable = new HashMap<>();
		{
			Queue<ACheck> checksQueue = new ArrayDeque<>();
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

		Map<ASymbol, Boolean> isSymbolFillable = new HashMap<>();
		Set<Production> fillableProductions = new HashSet<>();
		{
			Queue<ACheck> checksQueue = new ArrayDeque<>();
			Map<ASymbol, List<ACheck>> waitingChecksByDependency = new HashMap<>();
			Map<NonTerminal, Set<Production>> remainingProductionsByLHS = new HashMap<>();

			final class FillableCheck extends ACheck {
				FillableCheck(Item item) {
					super(item);
				}

				@Override
				public final void run() {
					NonTerminal lhs = item.production.lhs;
					if (item.isFinished()) {
						if (item.production.rhs.stream().anyMatch(isSymbolFillable::containsKey)) {
							fillableProductions.add(item.production);
							isSymbolFillable.put(lhs, true);
							remainingProductionsByLHS.get(lhs).clear();
							requeue(lhs);
						} else {
							// production is not fillable - no fillable symbols, all skippable or empty
							failProduction();
						}
					} else {
						ASymbol nextSymbol = item.getExpectedNextSymbol();
						Boolean nextSymbolFillable = isSymbolFillable.get(nextSymbol);
						if (nextSymbolFillable == null) {
							waitingChecksByDependency.computeIfAbsent(nextSymbol, __ -> new ArrayList<ACheck>()).add(this);
						} else if (nextSymbolFillable || isSymbolSkippable.getOrDefault(nextSymbol, false)) {
							checksQueue.add(new FillableCheck(item.shift()));
						} else {
							// production is not fillable - can neither fill nor skip a symbol
							failProduction();
						}
					}
				}

				private void requeue(NonTerminal lhs) {
					List<ACheck> waitingChecks = waitingChecksByDependency.getOrDefault(lhs, Collections.emptyList());
					checksQueue.addAll(waitingChecks);
					waitingChecks.clear();
				}

				private void failProduction() {
					NonTerminal lhs = item.production.lhs;
					Set<Production> remainingProductions = remainingProductionsByLHS.get(lhs);
					remainingProductions.remove(item.production);
					if (remainingProductions.isEmpty()) {
						isSymbolFillable.putIfAbsent(lhs, false);
						requeue(lhs);
					}
				}
			}
			for (Terminal terminal : seed.terminals) {
				isSymbolFillable.put(terminal, true);
			}
			for (Map.Entry<NonTerminal, Set<Production>> symbolAndProductions : seed.nonTerminals.entrySet()) {
				remainingProductionsByLHS.put(symbolAndProductions.getKey(), new HashSet<>(symbolAndProductions.getValue()));
				for (Production production : symbolAndProductions.getValue()) {
					Item startingItem = production.items.get(0);
					checksQueue.add(new FillableCheck(startingItem));
				}
			}
			for (ACheck check; (check = checksQueue.poll()) != null; ) {
				check.run();
			}
		}

		// todo: warn about unfillable productions? warn about those we couldn't determine?

		Map<NonTerminal, NonTerminalData> buildingData = CopyMap.hashV(seed.nonTerminals, __ -> new NonTerminalData());
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
			Queue<Signal> signalQueue = new ArrayDeque<>();
			Map<NonTerminal, Set<NonTerminal>> symbolsInitializedByKey = CopyMap.hashV(seed.nonTerminals, __ -> new HashSet<>());
			for (Map.Entry<NonTerminal, Set<Production>> symbolAndProductions : seed.nonTerminals.entrySet()) {
				NonTerminal lhs = symbolAndProductions.getKey();
				for (Production production : symbolAndProductions.getValue()) {
					if (!fillableProductions.contains(production)) {
						continue;
					}
					List<ASymbol> rhs = production.rhs;
					if (rhs.isEmpty()) {
						continue;
					}
					for (int i = 0; i < rhs.size(); ++i) {
						ASymbol initial = rhs.get(i);
						Item item = production.items.get(i);
						if (initial instanceof Terminal) {
							final class InitialItemSignal extends Signal<Item> {
								InitialItemSignal(NonTerminal dst, Item payload) {
									super(dst, payload);
								}
								@Override
								final Collection<Item> getDestinationCollection(NonTerminalData dstData) {
									return dstData.initialItems;
								}
								@Override
								final InitialItemSignal propagate(NonTerminal newDst) {
									return new InitialItemSignal(newDst, payload);
								}
							}
							signalQueue.add(new InitialItemSignal(lhs, item));
						} else if (initial instanceof NonTerminal) {
							if (isSymbolFillable.getOrDefault(initial, false)) {
								final class InitialNonTerminalSignal extends Signal<NonTerminal> {
									InitialNonTerminalSignal(NonTerminal dst, NonTerminal payload) {
										super(dst, payload);
									}
									@Override
									final Collection<NonTerminal> getDestinationCollection(NonTerminalData dstData) {
										return dstData.initialNonTerminals;
									}
									@Override
									final InitialNonTerminalSignal propagate(NonTerminal newDst) {
										return new InitialNonTerminalSignal(newDst, payload);
									}
								}
								signalQueue.add(new InitialNonTerminalSignal(lhs, (NonTerminal) initial));
								buildingData.get(initial).itemsInitializedByThis.add(item);
								symbolsInitializedByKey.get(initial).add(lhs);
							}
						}
						if (!isSymbolSkippable.getOrDefault(initial, false)) {
							break;
						}
					}
				}
			}
			for (Signal<?> signal; (signal = signalQueue.poll()) != null; ) {
				if (signal.applyTo(buildingData.get(signal.dst))) {
					for (NonTerminal newDst : symbolsInitializedByKey.get(signal.dst)) {
						signalQueue.add(signal.propagate(newDst));
					}
				}
			}
		}
		return CopyMap.immutableHashV(buildingData, NonTerminalData::new);
	}


	final Collection<Item> getInitializingItemsOf(NonTerminal symbol) {
		return precomputedNonTerminalData.get(symbol).initialItems;
	}

	final Collection<NonTerminal> getInitializingNonTerminalsOf(NonTerminal symbol) {
		return precomputedNonTerminalData.get(symbol).initialNonTerminals;
	}

	final Collection<Item> getItemsInitializedBy(NonTerminal symbol) {
		return precomputedNonTerminalData.get(symbol).itemsInitializedByThis;
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
