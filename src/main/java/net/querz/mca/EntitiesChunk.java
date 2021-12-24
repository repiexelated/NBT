package net.querz.mca;

import net.querz.mca.entities.EntityBase;
import net.querz.mca.entities.EntityFactory;
import net.querz.nbt.tag.CompoundTag;

/**
 * Thin default implementation of {@link EntitiesChunkBase}.
 *
 * @see EntitiesChunkBase
 * @see EntityFactory
 * @see MCAUtil#MCA_CREATORS
 */
public class EntitiesChunk extends EntitiesChunkBase<EntityBase> {

    protected EntitiesChunk(int dataVersion) {
        super(dataVersion);
    }

    public EntitiesChunk(CompoundTag data) {
        super(data);
    }

    public EntitiesChunk(CompoundTag data, long loadData) {
        super(data, loadData);
    }
}
