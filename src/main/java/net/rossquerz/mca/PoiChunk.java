package net.rossquerz.mca;

import net.rossquerz.nbt.tag.CompoundTag;

public class PoiChunk extends PoiChunkBase<PoiRecord>{

    protected PoiChunk(int dataVersion) {
        super(dataVersion);
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
