package eu.fbk.srf2json.logic;

import java.util.HashMap;
import java.util.function.Function;

@SuppressWarnings("serial")
public class DefaultDict<K, V> extends HashMap<K, V> {

	private Function<K, V> defaultValueSupplier;

	public DefaultDict(Function<K, V> defaultValueSupplier) {
		super();
		this.defaultValueSupplier = defaultValueSupplier;
	}
	
	@SuppressWarnings("unchecked")
	@Override
    public V get(Object key) {
		return computeIfAbsent((K) key, k -> defaultValueSupplier.apply(k));
	}
}
