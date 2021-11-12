package net.querz.mca;

import net.querz.nbt.tag.CompoundTag;

public class PoiChunkTest extends PoiChunkBaseTest<PoiChunk> {

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
    protected CompoundTag createTag(int dataVersion) {
        CompoundTag tag = super.createTag(dataVersion);
        tag.put("Sections", new CompoundTag());
        return tag;
    }

    @Override
    protected void validateAllDataConstructor() {

    }
}
