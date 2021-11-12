package net.querz.mca;

import net.querz.nbt.tag.CompoundTag;

public class PoiChunkTest extends PoiChunkBaseTest<PoiRecord, PoiChunk> {

    @Override
    protected PoiChunk createChunk() {
        return new PoiChunk();
    }

    @Override
    protected PoiChunk createChunk(CompoundTag tag) {
        return new PoiChunk(tag);
    }

    @Override
    protected PoiChunk createChunk(CompoundTag tag, long loadData) {
        return new PoiChunk(tag, loadData);
    }

    @Override
    protected PoiRecord createPoiRecord(int x, int y, int z, String type) {
        return new PoiRecord(x, y, z, type);
    }
}
