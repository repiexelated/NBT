package net.querz.mca;

import net.querz.nbt.tag.CompoundTag;

public class EntitiesChunk extends ChunkBase {
    EntitiesChunk(int lastMCAUpdate) {
        super(lastMCAUpdate);
    }

    public EntitiesChunk(CompoundTag data) {
        super(data);
    }

    @Override
    protected void initReferences(long loadFlags) {

    }

    @Override
    public CompoundTag updateHandle() {
        return data;
    }
}
