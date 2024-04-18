package net.rossquerz.nbt.query.evaluator;

import net.rossquerz.nbt.tag.Tag;

@FunctionalInterface
public interface Evaluator {
    Object eval(Tag<?> tag);
}
