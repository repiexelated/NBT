package net.rossquerz.nbt.query.evaluator;

import net.rossquerz.nbt.tag.CompoundTag;
import net.rossquerz.nbt.tag.Tag;

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
        return null;
    }

    @Override
    public String toString() {
        return key;
    }
}
