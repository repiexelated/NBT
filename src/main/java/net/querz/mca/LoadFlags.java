package net.querz.mca;

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
	public static final long RAW                  = 0x0001_0000;
	public static final long POI_RECORDS          = 0x0002_0000;
	// For fields such as below_zero_retrogen and blending_data which were added to support chunk migration to 1.18
	public static final long WORLD_UPGRADE_HINTS  = 0x0004_0000;

	public static final long ALL_DATA             = 0xffffffffffffffffL;

}
