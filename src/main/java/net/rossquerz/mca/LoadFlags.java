package net.rossquerz.mca;

public final class LoadFlags {
	private LoadFlags() {}

	public static final long BIOMES               = 0x0000_0001;
	public static final long HEIGHTMAPS           = 0x0000_0002;
	public static final long CARVING_MASKS        = 0x0000_0004;
	public static final long ENTITIES             = 0x0000_0008;
	public static final long TILE_ENTITIES        = 0x0000_0010;
	public static final long TILE_TICKS           = 0x0000_0040;
	public static final long LIQUID_TICKS         = 0x0000_0080;
	public static final long TO_BE_TICKED         = 0x0000_0100;
	public static final long POST_PROCESSING      = 0x0000_0200;
	public static final long STRUCTURES           = 0x0000_0400;
	public static final long BLOCK_LIGHTS         = 0x0000_0800;
	public static final long BLOCK_STATES         = 0x0000_1000;
	public static final long SKY_LIGHT            = 0x0000_2000;
	public static final long LIGHTS               = 0x0000_4000;
	public static final long LIQUIDS_TO_BE_TICKED = 0x0000_8000;
	public static final long POI_RECORDS          = 0x0002_0000;
	// For fields such as below_zero_retrogen and blending_data which were added to support chunk migration to 1.18
	public static final long WORLD_UPGRADE_HINTS  = 0x0004_0000;

	// high byte reserved for behavioral flags that follow
	public static final long LOAD_ALL_DATA = 0x00FF_FFFF_FFFF_FFFFL;

	/**
	 * When set {@link ChunkBase#data} will be nulled out after {@link ChunkBase#initReferences} has completed.
	 * This will allow garbage collection the chance to free memory you're only interested in biome data for example.
	 * <p>However, even if you have specified {@link #LOAD_ALL_DATA}, this will also cause non-vanilla tags
	 * (or very new vanilla tags this library doesn't yet support) to also be discarded should you wish to write
	 * the data back out. Also if you did not specify {@link #LOAD_ALL_DATA} and you release the chunk data tag
	 * and you write the chunk back out you will get a very reduced, incomplete, output containing only data
	 * as specified by the given load flags.</p>
	 * <p>{@link TerrainSectionBase} also honors this flag.</p>
	 * <p>Note that if {@link #RAW} is specified setting this flag has no effect!</p>
	 * <p>Note if you set this flag you will not be able to call {@link ChunkBase#updateHandle()}.
	 * This behavior may change in the future but for now it's the safe option to prevent overwriting
	 * an mca file with partial contents.</p>
	 */
	public static final long RELEASE_CHUNK_DATA_TAG = 0x4000_0000_0000_0000L;

	/**
	 * Setting the RAW bit causes all other flag settings to be ignored and for only {@link ChunkBase#data}
	 * and {@link ChunkBase#dataVersion} to be populated. {@link ChunkBase#initReferences(long)} will NOT be
	 * called and therefore any child classes of {@link ChunkBase} will also not have a chance to perform
	 * any tag processing.
	 */
	public static final long RAW                    = 0x8000_0000_0000_0000L;
}
