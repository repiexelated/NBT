package net.querz.mca;

import java.io.IOException;
import java.io.RandomAccessFile;

public class PoiMCAFile extends MCAFileBase<PoiChunk> {
    public PoiMCAFile(int regionX, int regionZ) {
        super(regionX, regionZ);
    }

    public PoiMCAFile(int regionX, int regionZ, int defaultDataVersion) {
        super(regionX, regionZ, defaultDataVersion);
    }

    public PoiMCAFile(int regionX, int regionZ, DataVersion defaultDataVersion) {
        super(regionX, regionZ, defaultDataVersion);
    }

    @Override
    public Class<PoiChunk> chunkClass() {
        return null;
    }

    @Override
    public PoiChunk createChunk() {
        return null;
    }

    @Override
    protected PoiChunk deserializeChunk(RandomAccessFile raf, long loadFlags, int timestamp) throws IOException {
        return null;
    }
}
