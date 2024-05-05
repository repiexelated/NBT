package io.github.ensgijs.nbt.mca.util;

import io.github.ensgijs.nbt.tag.CompoundTag;

public interface TagWrapper {

    /**
     * Provides a reference to the wrapped data tag.
     * May be null for objects which support partial loading such as chunks.
     * @return A reference to the raw CompoundTag this object is based on.
     */
    CompoundTag getHandle();

    /**
     * Updates the data tag held by this wrapper and returns it.
     * @return A reference to the raw CompoundTag this object is based on.
     */
    CompoundTag updateHandle();
}
