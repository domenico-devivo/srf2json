package eu.fbk.srf2json.logic;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class IdSequence {
    private final Set<Integer> usedIds;
    public final int MAX_VALUE = 255;
    private int current_value;

    public IdSequence(Collection<Integer> usedIds) {
        super();

        this.usedIds = usedIds == null ? new HashSet<>() : new HashSet<>(usedIds);
        this.current_value = 1;
    }

    public int next() {
        while (current_value <= MAX_VALUE && usedIds.contains(current_value)) {
            current_value++;
        }

        if (current_value > MAX_VALUE) {
            throw new IllegalStateException("Attribute ID overflow: " + current_value);
        }

        int res = current_value;

        current_value++;

        return res;
    }
}
