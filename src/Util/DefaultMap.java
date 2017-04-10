package Util;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Gig on 4/1/2017.
 * A HashMap that returns a default value
 * useful little class
 */
public class DefaultMap<K, V> extends HashMap<K, V> {
    private V defaultValue;
    public DefaultMap(V defV) {
        defaultValue = defV;
    }
    @Override
    public V get(Object key) {
        if (!containsKey(key))
            return defaultValue;
        return super.get(key);
    }
}
