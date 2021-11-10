package net.querz.mca;

import java.io.IOException;
import java.io.RandomAccessFile;

public class EntitiesMCAFile extends MCAFileBase<EntitiesChunk> {
    public EntitiesMCAFile(int regionX, int regionZ) {
        super(regionX, regionZ);
    }

    public EntitiesMCAFile(int regionX, int regionZ, int defaultDataVersion) {
        super(regionX, regionZ, defaultDataVersion);
    }

    public EntitiesMCAFile(int regionX, int regionZ, DataVersion defaultDataVersion) {
        super(regionX, regionZ, defaultDataVersion);
    }

    @Override
    public Class<EntitiesChunk> chunkClass() {
        return EntitiesChunk.class;
    }

    @Override
    public EntitiesChunk createChunk() {
        EntitiesChunk chunk = new EntitiesChunk(0);
        chunk.setDataVersion(getDefaultChunkDataVersion());
        return chunk;
    }
}
