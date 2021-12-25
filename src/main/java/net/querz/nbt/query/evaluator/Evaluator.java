package net.querz.nbt.query.evaluator;

import net.querz.nbt.tag.Tag;

@FunctionalInterface
public interface Evaluator {
    Object eval(Tag<?> tag);
}
