package com.jh.user.util;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class ValueComparator implements Comparator<Long> {

    Map<Long, Double> base;

    public ValueComparator(Map<Long, Double> base) {
        this.base = base;
    }

    @Override
    public int compare(Long a, Long b) {
        if (base.get(a).doubleValue() >= base.get(b).doubleValue()) {
            return -1;
        } else {
            return 1;
        }
    }

    public static <K, V extends Comparable<V>> Map<K, V> sortByValues(
            final Map<K, V> map) {
        Comparator<K> valueComparator = new Comparator<K>() {
            public int compare(K k1, K k2) {
                int compare = map.get(k2).compareTo(map.get(k1));
                if (compare == 0)
                    return 1;
                else
                    return compare;
            }
        };
        Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
        sortedByValues.putAll(map);
        return sortedByValues;
    }

}
