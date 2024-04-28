package net.rossquerz.mca.io;

import net.rossquerz.mca.ChunkBase;
import net.rossquerz.nbt.io.CompressionType;
import net.rossquerz.util.ArgValidator;

import java.io.*;
import java.nio.file.Path;

/**
 * Provides a streaming data sink for writing a region file. Chunks can be written in any order.
 * Attempting to write a chunk (XZ) that has already been written will throw {@link IOException}.
 */
public class McaFileStreamingWriter<T extends ChunkBase> implements Closeable {
    private final int[] chunkSectors = new int[1024];
    private final int[] chunkTimestamps = new int[1024];

    private final RandomAccessFile raf;

    public McaFileStreamingWriter(RandomAccessFile raf) throws IOException {
        ArgValidator.requireValue(raf);
        raf.setLength(0);
        raf.seek(0);
        // zero out the chunk sector and timestamp tables
        for (int i = 0; i < 2 * 4096; i++) {
            raf.write(0);
        }
        this.raf = raf;
    }
    public McaFileStreamingWriter(File file) throws IOException {
        this(new RandomAccessFile(file, "rw"));
    }
    public McaFileStreamingWriter(String file) throws IOException {
        this(new File(file));
    }
    public McaFileStreamingWriter(Path path) throws IOException {
        this(path.toFile());
    }

    public void write(T chunk) throws IOException {
        ArgValidator.requireValue(chunk);
        if (chunk.getChunkX() == ChunkBase.NO_CHUNK_COORD_SENTINEL || chunk.getChunkZ() == ChunkBase.NO_CHUNK_COORD_SENTINEL) {
            throw new IllegalArgumentException("Chunk XZ must be set!");
        }
        final int index = ((chunk.getChunkZ() & 0x1F) << 5) + (chunk.getChunkX() & 0x1F);
        if (chunkSectors[index] != 0)
            throw new IOException("Chunk " + chunk.getChunkXZ() + " (index: " + index + ") has already been written!");

        if (raf.getFilePointer() % 4096 != 0)
            throw new IllegalStateException();
        final int startSector = (int) (raf.getFilePointer() >> 12);
        int bytesWritten = chunk.serialize(raf, chunk.getChunkX(), chunk.getChunkZ(), CompressionType.ZLIB, true);
        // compute the count of 4kb sectors the chunk data occupies
        int sectors = (bytesWritten >> 12) + (bytesWritten % 4096 == 0 ? 0 : 1);
        if (sectors > 255) throw new IOException("Chunk " + chunk.getChunkXZ() + " to large! 1MB maximum");
        long roundedEof = ((long) (startSector + sectors) << 12);
        while (roundedEof > raf.getFilePointer()) {
            raf.write(0);
        }
        if (raf.getFilePointer() % 4096 != 0)
            throw new IllegalStateException();
        chunkSectors[index] = (startSector << 8) | sectors;
        chunkTimestamps[index] = chunk.getLastMCAUpdate();
    }

    @Override
    public void close() throws IOException {
        raf.seek(0);
        for (int i : chunkSectors) {
//            raf.write(i >>> 24);
//            raf.write(i >>> 16 & 0xFF);
//            raf.write(i >>> 8 & 0xFF);
//            raf.write(i & 0xFF);
            raf.writeInt(i);
        }
        for (int i : chunkTimestamps) {
//            raf.write((i >>> 24) & 0xFF);
//            raf.write((i >>> 16) & 0xFF);
//            raf.write((i >>>  8) & 0xFF);
//            raf.write((i >>>  0) & 0xFF);
            raf.writeInt(i);
        }
        raf.close();
    }
}
