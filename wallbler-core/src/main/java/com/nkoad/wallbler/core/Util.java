package com.nkoad.wallbler.core;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class Util {

	public static <K, V> Dictionary<K, V> mapToDictionary(Map<K, V> map) {
		Dictionary<K, V> result = new Hashtable<>();
		if (map == null || map.isEmpty()) {
			return result;
		}
		for (Map.Entry<K, V> entry : map.entrySet()) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	public static <K, V> Map<K, V> dictionaryToMap(Dictionary<K, V> properties) {
		if (properties == null || properties.isEmpty()) {
			return new HashMap<>();
		}
		List<K> keys = Collections.list(properties.keys());
		return keys.stream().collect(Collectors.toMap(Function.identity(), properties::get));
	}

	public static <K, V> boolean areEqual(Map<K, V> first, Map<K, V> second) {
		if (first.size() != second.size()) {
			return false;
		}
		return first.entrySet().stream()
				.allMatch(e -> e.getValue().equals(second.get(e.getKey())));
	}
}
