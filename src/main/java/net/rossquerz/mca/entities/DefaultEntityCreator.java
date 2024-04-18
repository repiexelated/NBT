package net.rossquerz.mca.entities;

import net.rossquerz.nbt.tag.CompoundTag;

/**
 * The default {@link EntityCreator} which creates {@link EntityBaseImpl} that can represent any, and all, entities
 * in vanilla Minecraft MCA files.
 */
public class DefaultEntityCreator implements EntityCreator<EntityBase> {

    /** @see EntityCreator#create(String, CompoundTag, int) */
    @Override
    public EntityBase create(String normalizedId, CompoundTag tag, int dataVersion) {
        return new EntityBaseImpl(tag, dataVersion);
    }
}
