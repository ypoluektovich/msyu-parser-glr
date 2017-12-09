package org.msyu.parser.glr.incubator;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class MapToSet<K, V> extends MapToCollection<K, V, Set<V>> {
	public MapToSet(Map<K, Set<V>> backingMap, Function<? super K, ? extends Set<V>> collectionCtor) {
		super(backingMap, collectionCtor);
	}
}
