package net.querz.nbt.query.evaluator;

import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.Tag;

public class NameEvaluator implements Evaluator {

    private final String key;

    public String key() {
        return key;
    }

    public NameEvaluator(String key) {
        this.key = key;
    }

    public Object eval(Tag<?> tag) {
        if (tag instanceof CompoundTag) {
            return ((CompoundTag) tag).get(key);
        }
        if (tag == null) return null;
        throw new IllegalArgumentException("expected CompoundTag but was " + tag.getClass().getTypeName());
    }

    @Override
    public String toString() {
        return key;
    }
}
