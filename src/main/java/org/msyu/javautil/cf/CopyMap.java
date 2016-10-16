package org.msyu.javautil.cf;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

public class CopyMap {

	public static <K1, K2, V1, V2> HashMap<K2, V2> hashKV(Map<K1, V1> src, Function<K1, K2> keyMapper, Function<V1, V2> valueMapper) {
		HashMap<K2, V2> dst = new HashMap<>();
		for (Map.Entry<K1, V1> entry : src.entrySet()) {
			K2 newKey = keyMapper.apply(entry.getKey());
			V2 newValue = valueMapper.apply(entry.getValue());
			dst.put(newKey, newValue);
		}
		return dst;
	}

	public static <K, V1, V2> HashMap<K, V2> hashV(Map<K, V1> src, Function<V1, V2> valueMapper) {
		return hashKV(src, Function.identity(), valueMapper);
	}


	public static <K1, K2, V1, V2> Map<K2, V2> immutableHashKV(Map<K1, V1> src, Function<K1, K2> keyMapper, Function<V1, V2> valueMapper) {
		if (src.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<K2, V2> dst = null;
		for (Iterator<Map.Entry<K1, V1>> iterator = src.entrySet().iterator(); iterator.hasNext(); ) {
			Map.Entry<K1, V1> entry = iterator.next();
			K2 newKey = keyMapper.apply(entry.getKey());
			V2 newValue = valueMapper.apply(entry.getValue());
			if (dst == null) {
				if (!iterator.hasNext()) {
					return Collections.singletonMap(newKey, newValue);
				}
				dst = new HashMap<>();
			}
			dst.put(newKey, newValue);
		}
		return Collections.unmodifiableMap(dst);
	}

	public static <K, V1, V2> Map<K, V2> immutableHashV(Map<K, V1> src, Function<V1, V2> valueMapper) {
		return immutableHashKV(src, Function.identity(), valueMapper);
	}

	public static <K, V> Map<K, V> immutableHash(Map<K, V> src) {
		return immutableHashKV(src, Function.identity(), Function.identity());
	}

}
