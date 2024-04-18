package net.rossquerz.mca;

import net.rossquerz.nbt.tag.CompoundTag;

public class PoiChunkTest extends PoiChunkBaseTest<PoiRecord, PoiChunk> {

    @Override
    protected PoiChunk createChunk(DataVersion dataVersion) {
        return new PoiChunk(dataVersion.id());
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
