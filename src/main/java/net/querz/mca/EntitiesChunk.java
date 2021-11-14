package net.querz.mca;

import net.querz.mca.entities.EntityBase;
import net.querz.nbt.tag.CompoundTag;

public class EntitiesChunk extends EntitiesChunkBase<EntityBase> {

    protected EntitiesChunk() {}

    public EntitiesChunk(CompoundTag data) {
        super(data);
    }

    public EntitiesChunk(CompoundTag data, long loadData) {
        super(data, loadData);
    }
}
