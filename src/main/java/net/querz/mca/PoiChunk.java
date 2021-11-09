package net.querz.mca;

import net.querz.nbt.tag.CompoundTag;

public class PoiChunk extends ChunkBase {
    PoiChunk(int lastMCAUpdate) {
        super(lastMCAUpdate);
    }

    public PoiChunk(CompoundTag data) {
        super(data);
    }

    @Override
    protected void initReferences(long loadFlags) {

    }
}
