package net.rossquerz.mca;

import net.rossquerz.mca.entities.EntityBase;
import net.rossquerz.mca.entities.EntityFactory;
import net.rossquerz.nbt.tag.CompoundTag;

/**
 * Thin default implementation of {@link EntitiesChunkBase}.
 *
 * @see EntitiesChunkBase
 * @see EntityFactory
 * @see McaFileHelpers#MCA_CREATORS
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

    public EntitiesChunk() {
        super(DataVersion.latest().id());
    }
}
