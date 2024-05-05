package net.rossquerz.mca.io;

import net.rossquerz.io.PositionTrackingInputStream;
import net.rossquerz.mca.*;
import net.rossquerz.mca.util.ChunkIterator;
import net.rossquerz.mca.util.IntPointXZ;

import java.io.*;
import java.util.*;
import java.util.function.Supplier;

/**
 * Iterates over the chunks in an MCA file. Note iteration is in file-order, not index-order!
 * Chunks which do not exist in the file are skipped - {@link #next()} will never return null.
 * @see McaFileHelpers
 * @see McaFileStreamingWriter
 */
public class McaFileChunkIterator<T extends ChunkBase> implements ChunkIterator<T>, Closeable {
    private final Supplier<T> chunkCreator;
    private final IntPointXZ chunkAbsXzOffset;
    private final PositionTrackingInputStream in;
    private final long loadFlags;
    private final List<ChunkMetaInfo> chunkMetaInfos;
    private final Iterator<ChunkMetaInfo> iter;
    private final IntPointXZ regionXZ;
    private ChunkMetaInfo current;

    /**
     * This map controls the factory creation behavior of creating new chunk instances which then have their
     * {@link ChunkBase#deserialize(InputStream, long, int, int, int)} method called to initialize the chunk data.
     * <p>By manipulating this map you can control the factory behavior to support new chunk types not natively
     * supported by this library or to specify that a custom creation method should be called which could even
     * return a custom {@link ChunkBase} implementation.</p>
     * <p>The default mapping routes "region", "poi", and "entities" to {@link TerrainChunk#TerrainChunk()},
     * {@link PoiChunk#PoiChunk()}, and {@link EntitiesChunk#EntitiesChunk()}.</p>
     */
    public static final Map<String, Supplier<? extends ChunkBase>> DEFAULT_CHUNK_CREATORS = new HashMap<>();

    static {
        DEFAULT_CHUNK_CREATORS.put("region", TerrainChunk::new);
        DEFAULT_CHUNK_CREATORS.put("poi", PoiChunk::new);
        DEFAULT_CHUNK_CREATORS.put("entities", EntitiesChunk::new);
    }

    @SuppressWarnings("unchecked")
    public static <T extends ChunkBase> McaFileChunkIterator<T> iterate(File file, long loadFlags) throws IOException {
        Supplier<T> chunkCreator = (Supplier<T>) DEFAULT_CHUNK_CREATORS.get(file.getParentFile().getName());
        return new McaFileChunkIterator<>(
                chunkCreator,
                McaFileHelpers.regionXZFromFileName(file.getName()),
                new BufferedInputStream(new FileInputStream(file)),
                loadFlags
        );
    }

    public static <T extends ChunkBase> McaFileChunkIterator<T> iterate(File file, long loadFlags, Supplier<T> chunkCreator) throws IOException {
        return new McaFileChunkIterator<>(
                chunkCreator,
                McaFileHelpers.regionXZFromFileName(file.getName()),
                new BufferedInputStream(new FileInputStream(file)),
                loadFlags
        );
    }


    @SuppressWarnings("unchecked")
    public static <T extends ChunkBase> McaFileChunkIterator<T> iterate(InputStream stream, String fileName, long loadFlags) throws IOException {
        Supplier<T> chunkCreator = (Supplier<T>) DEFAULT_CHUNK_CREATORS.get(new File(fileName).getParentFile().getName());
        return new McaFileChunkIterator<>(
                chunkCreator,
                McaFileHelpers.regionXZFromFileName(fileName),
                stream,
                loadFlags
        );
    }

    /**
     * @param chunkCreator Supplies new instances of type {@link T}
     * @param regionXZ     Region XZ location, in region coordinates such as found a filename such as r.1.2.mca
     * @param stream       Stream to read MCA data from. The stream should be positioned at the start of the mca file.
     * @param loadFlags    {@link LoadFlags} to use when loading the chunks.
     * @throws IOException upon any read errors.
     * @see McaFileHelpers#regionXZFromFileName(String)
     */
    public McaFileChunkIterator(Supplier<T> chunkCreator, IntPointXZ regionXZ, InputStream stream, long loadFlags) throws IOException {
        this.chunkCreator = Objects.requireNonNull(chunkCreator);
        this.regionXZ = regionXZ;
        this.chunkAbsXzOffset = regionXZ.transformRegionToChunk();
        this.in = new PositionTrackingInputStream(stream);
        this.loadFlags = loadFlags;
        final int[] offsets = new int[1024];
        final int[] sectors = new int[1024];

        // read offsets
        int populatedChunks = 0;
        for (int i = 0; i < 1024; i++) {
            int offset = in.read() << 16;
            offset |= (in.read() & 0xFF) << 8;
            offset |= in.read() & 0xFF;
            int sector;
            if ((sector = in.read()) == 0) {
                continue;
            }
            offsets[i] = offset;
            sectors[i] = sector;
            populatedChunks++;
        }
        chunkMetaInfos = new ArrayList<>(populatedChunks);

        // read timestamps
        for (int i = 0; i < 1024; i++) {
            int ch1 = in.read();
            int ch2 = in.read();
            int ch3 = in.read();
            int ch4 = in.read();
            if (ch1 < 0 || ch2 < 0 || ch3 < 0 || ch4 < 0)
                throw new EOFException();
            int timestamp = (ch1 << 24) + (ch2 << 16) + (ch3 << 8) + ch4;
            if (offsets[i] > 0) {
                chunkMetaInfos.add(new ChunkMetaInfo(i, offsets[i], sectors[i], timestamp));
            }
        }
        chunkMetaInfos.sort(Comparator.comparingInt(e -> e.offset));
        iter = chunkMetaInfos.iterator();
    }

    public IntPointXZ chunkAbsXzOffset() {
        return chunkAbsXzOffset;
    }

    public IntPointXZ regionXZ() {
        return regionXZ;
    }

    @Override
    public boolean hasNext() {
        return iter.hasNext();
    }

    @Override
    public T next() {
        current = iter.next();
        try {
            in.setSoftEof(0);
            in.skipTo(4096L * current.offset + 4);  //+4 skip chunk byte count
            in.setSoftEof(4096L * (current.offset + current.sectors));
            T currentChunk = chunkCreator.get();
            currentChunk.deserialize(in, loadFlags, current.timestamp, currentAbsoluteX(), currentAbsoluteZ());
            return currentChunk;
        } catch (IOException ex) {
            throw new RuntimeException("Error processing " + current, ex);
        }
    }

    @Override
    public void set(T chunk) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int currentIndex() {
        if (current == null) throw new NoSuchElementException();
        return current.index;
    }

    @Override
    public int currentAbsoluteX() {
        if (current == null) throw new NoSuchElementException();
        return chunkAbsXzOffset.getX() + currentX();
    }

    @Override
    public int currentAbsoluteZ() {
        if (current == null) throw new NoSuchElementException();
        return chunkAbsXzOffset.getZ() + currentZ();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (ChunkMetaInfo cmi : chunkMetaInfos) {
            if (sb.length() > 0) sb.append('\n');
            sb.append(cmi);
        }
        return sb.toString();
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    private static class ChunkMetaInfo {
        public final int index;
        public final int offset;
        public final int sectors;
        public final int timestamp;

        public ChunkMetaInfo(int index, int offset, int sectors, int timestamp) {
            this.index = index;
            this.offset = offset;
            this.sectors = sectors;
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return "index: " + index + "; offset: " + offset + "; sectors: " + sectors + "; timestamp: " + timestamp;
        }
    }
}
