package net.rossquerz.mca.io;


public final class MoveChunkFlags {
    private MoveChunkFlags() {}

    public static final long MOVE_CHUNK_NO_FLAGS = 0;

    /** Entity UUID's will be randomized to avoid UUID collisions. */
    public static final long RANDOMIZE_ENTITY_UUID                       = 0x0000_0001;
    /** If the (terrain) chunk contains references to structures outside the new chunk's region file
     * those references will be removed. If the chunk contains structure 'start' data, all bounding
     * box's and other volumes will be restricted to values which fall inside the new region file. */
    public static final long DISCARD_STRUCTURE_REFERENCES_OUTSIDE_REGION = 0x0000_0002;


    /** When set all structure references and start tags will be discarded. Any structures which have already
     * been generated will still exist, but they won't behave any differently than if they were player built.
     * E.g. structure spawns won't happen. */
    public static final long DISCARD_STRUCTURE_DATA = 0x0000_0002_0000_0000L;

    public static final long MOVE_CHUNK_DEFAULT_FLAGS                    = 0x0000_0000_FFFF_FFFFL;
}
