package net.querz.mca;

import net.querz.nbt.tag.CompoundTag;

public class PoiChunk extends PoiChunkBase<PoiRecord>{

    protected PoiChunk(int dataVersion) {
        super(dataVersion);
    }

    public PoiChunk(int dataVersion, int chunkX, int chunkZ) {
        super(dataVersion, chunkX, chunkZ);
    }

    public PoiChunk(CompoundTag data) {
        super(data);
    }

    public PoiChunk(CompoundTag data, long loadData) {
        super(data, loadData);
    }

    @Override
    protected PoiRecord createPoiRecord(CompoundTag recordTag) {
        return new PoiRecord(recordTag);
    }

}
