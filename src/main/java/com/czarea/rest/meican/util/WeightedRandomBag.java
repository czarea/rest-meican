package com.czarea.rest.meican.util;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 权重计算器
 *
 * @author zhouzx
 */
public class WeightedRandomBag<T extends Object> {

    private class Entry {

        double accumulatedWeight;
        T object;
    }

    private List<Entry> entries = new CopyOnWriteArrayList<>();
    private double accumulatedWeight;
    private Random rand = new Random();

    public void addEntry(T object, double weight) {
        accumulatedWeight += weight;
        Entry e = new Entry();
        e.object = object;
        e.accumulatedWeight = accumulatedWeight;
        entries.add(e);
    }

    public T getRandom() {
        double r = rand.nextDouble() * accumulatedWeight;

        for (Entry entry : entries) {
            if (entry.accumulatedWeight >= r) {
                return entry.object;
            }
        }
        return null;
    }
}
