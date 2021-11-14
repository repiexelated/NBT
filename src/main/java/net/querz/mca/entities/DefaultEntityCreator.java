package net.querz.mca.entities;

import net.querz.nbt.tag.CompoundTag;

public class DefaultEntityCreator implements EntityCreator<EntityBase> {

    /** @see EntityCreator#create(String, CompoundTag, int) */
    @Override
    public EntityBase create(String normalizedId, CompoundTag tag, int dataVersion) {
        return new EntityBaseImpl(tag, dataVersion);
    }
}
