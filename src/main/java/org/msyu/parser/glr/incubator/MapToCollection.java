package org.msyu.parser.glr.incubator;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class MapToCollection<K, V, C extends Collection<V>> {

	private final Map<K, C> backingMap;
	private final Function<? super K, ? extends C> collectionCtor;

	public MapToCollection(Map<K, C> backingMap, Function<? super K, ? extends C> collectionCtor) {
		this.backingMap = backingMap;
		this.collectionCtor = collectionCtor;
	}

	public boolean add(K key, V value) {
		return backingMap.computeIfAbsent(key, collectionCtor).add(value);
	}

	public C remove(K key) {
		return backingMap.remove(key);
	}

	public Set<Map.Entry<K, C>> entrySet() {
		return backingMap.entrySet();
	}

}
