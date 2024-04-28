package net.rossquerz.mca;

import net.rossquerz.mca.util.ChunkBoundingRectangle;
import net.rossquerz.mca.util.IntPointXZ;
import net.rossquerz.nbt.query.NbtPath;
import net.rossquerz.nbt.tag.*;
import net.rossquerz.mca.util.VersionAware;

import java.util.*;

import static net.rossquerz.mca.DataVersion.*;
import static net.rossquerz.mca.io.LoadFlags.*;

/**
 * Represents a Terrain data mca chunk. Terrain chunks are composed of a set of {@link TerrainSection} where any empty/null
 * section is filled with air blocks by the game. When altering existing chunks for MC 1.14+, be sure to have read and
 * understood the documentation on {@link PoiRecord} to avoid problems with villagers, nether portal linking,
 * lodestones, bees, and probably more as Minecraft continues to evolve.
 */
public abstract class TerrainChunkBase<T extends TerrainSectionBase> extends SectionedChunkBase<T> {

	protected long lastUpdate;
	protected static final VersionAware<NbtPath> LAST_UPDATE_PATH = new VersionAware<NbtPath>()
			.register(0, NbtPath.of("Level.LastUpdate"))
			.register(JAVA_1_18_21W43A.id(), NbtPath.of("LastUpdate"));

	protected long inhabitedTime;
	protected static final VersionAware<NbtPath> INHABITED_TIME_PATH = new VersionAware<NbtPath>()
			.register(0, NbtPath.of("Level.InhabitedTime"))
			.register(JAVA_1_18_21W43A.id(), NbtPath.of("InhabitedTime"));

	/**
	 * Only populated for data versions < JAVA_1_18_21W39A. For later data versions use
	 * {@link net.rossquerz.mca.util.PalettizedCuboid} and load biomes from
	 * @see <a href=https://minecraft.fandom.com/wiki/Biome/IDs_before_1.13>minecraft.fandom.com/wiki/Biome/IDs_before_1.13</a>
	 * @see <a href=https://minecraft.fandom.com/wiki/Biome/ID>minecraft.fandom.com/wiki/Biome/ID</a>
	 */
	protected int[] legacyBiomes;
	protected static final VersionAware<NbtPath> LEGACY_BIOMES_PATH = new VersionAware<NbtPath>()
			.register(0, NbtPath.of("Level.Biomes"))
			.register(JAVA_1_18_21W39A.id(), null);  // biomes are now paletted and live in a similar container structure in sections[].biomes

	protected IntArrayTag legacyHeightMap;
	protected static final VersionAware<NbtPath> LEGACY_HEIGHT_MAP_PATH = new VersionAware<NbtPath>()
			.register(0, NbtPath.of("Level.HeightMap"));

	protected CompoundTag heightMaps;
	protected static final VersionAware<NbtPath> HEIGHT_MAPS_PATH = new VersionAware<NbtPath>()
			.register(0, NbtPath.of("Level.Heightmaps"))  // TODO: find when this was introduced - it was sometime before 1.13.0
			.register(JAVA_1_18_21W43A.id(), NbtPath.of("Heightmaps"));

	// TODO(1.18): WTF - change notes say Level.CarvingMasks[] is now long[] instead of byte[] ... but this is a CompoundTag!
	protected CompoundTag carvingMasks;
	protected static final VersionAware<NbtPath> CARVING_MASKS_PATH = new VersionAware<NbtPath>()
			.register(0, NbtPath.of("Level.CarvingMasks"))
			.register(JAVA_1_18_21W43A.id(), NbtPath.of("CarvingMasks"));

	protected ListTag<CompoundTag> entities;  // usage changed for chunk versions >= 2724 (1.17) after which entities are only stored in terrain chunks during world generation.
	protected static final VersionAware<NbtPath> ENTITIES_PATH = new VersionAware<NbtPath>()
			.register(0, NbtPath.of("Level.Entities"))
			.register(JAVA_1_18_21W43A.id(), NbtPath.of("entities"));

	protected ListTag<CompoundTag> tileEntities;
	protected static final VersionAware<NbtPath> TILE_ENTITIES_PATH = new VersionAware<NbtPath>()
			.register(0, NbtPath.of("Level.TileEntities"))
			.register(JAVA_1_18_21W43A.id(), NbtPath.of("block_entities"));

	protected ListTag<CompoundTag> tileTicks;
	protected static final VersionAware<NbtPath> TILE_TICKS_PATH = new VersionAware<NbtPath>()
			.register(0, NbtPath.of("Level.TileTicks"))
			.register(JAVA_1_18_21W43A.id(), NbtPath.of("block_ticks"));

	protected ListTag<ListTag<?>> toBeTicked;
	protected static final VersionAware<NbtPath> TO_BE_TICKED_PATH = new VersionAware<NbtPath>()
			.register(0, NbtPath.of("Level.ToBeTicked"))
			.register(JAVA_1_18_21W43A.id(), null);  // unsure when this was removed - but notes on this version say it was also "moved to block_ticks"

	protected ListTag<CompoundTag> liquidTicks;
	protected static final VersionAware<NbtPath> LIQUID_TICKS_PATH = new VersionAware<NbtPath>()
			.register(0, NbtPath.of("Level.LiquidTicks"))
			.register(JAVA_1_18_21W43A.id(), NbtPath.of("fluid_ticks"));

	protected ListTag<ListTag<?>> liquidsToBeTicked;
	protected static final VersionAware<NbtPath> LIQUIDS_TO_BE_TICKED_PATH = new VersionAware<NbtPath>()
			.register(0, NbtPath.of("Level.LiquidsToBeTicked"))
			.register(JAVA_1_18_21W43A.id(), null);  // unsure when this was removed - but notes on this version say it was also "moved to fluid_ticks"

	protected ListTag<ListTag<?>> lights;
	protected static final VersionAware<NbtPath> LIGHTS_PATH = new VersionAware<NbtPath>()
			.register(0, NbtPath.of("Level.Lights"))
			.register(JAVA_1_18_21W43A.id(), NbtPath.of("Lights"));

	protected ListTag<ListTag<?>> postProcessing;
	protected static final VersionAware<NbtPath> POST_PROCESSING_PATH = new VersionAware<NbtPath>()
			.register(0, NbtPath.of("Level.PostProcessing"))
			.register(JAVA_1_18_21W43A.id(), NbtPath.of("PostProcessing"));

	protected String status;
	protected static final VersionAware<NbtPath> STATUS_PATH = new VersionAware<NbtPath>()
			.register(0, NbtPath.of("Level.Status"))
			.register(JAVA_1_18_21W43A.id(), NbtPath.of("Status"));

	protected CompoundTag structures;
	protected static final VersionAware<NbtPath> STRUCTURES_PATH = new VersionAware<NbtPath>()
			.register(0, NbtPath.of("Level.Structures"))
			.register(JAVA_1_18_21W43A.id(), NbtPath.of("structures"));

	// null if the chunk data tag didn't contain a value
	protected Boolean isLightOn;
	protected static final VersionAware<NbtPath> IS_LIGHT_ON_PATH = new VersionAware<NbtPath>()
			.register(0, NbtPath.of("Level.LightPopulated"))
			.register(JAVA_1_13_18W06A.id(), NbtPath.of("Level.isLightOn"))
			.register(JAVA_1_18_21W43A.id(), NbtPath.of("isLightOn"));

	// null if the chunk data tag didn't contain a value
	protected Boolean isTerrainPopulated;
	protected static final VersionAware<NbtPath> TERRAIN_POPULATED_PATH = new VersionAware<NbtPath>()
			.register(0, NbtPath.of("Level.TerrainPopulated"))
			.register(JAVA_1_18_21W43A.id(), NbtPath.of("TerrainPopulated"));

	protected Boolean hasLegacyStructureData;
	protected static final VersionAware<NbtPath> HAS_LEGACY_STRUCTURE_DATA_PATH = new VersionAware<NbtPath>()
			.register(0, NbtPath.of("Level.hasLegacyStructureData"));

	protected CompoundTag upgradeData;
	protected static final VersionAware<NbtPath> UPGRADE_DATA_PATH = new VersionAware<NbtPath>()
			.register(0, NbtPath.of("Level.UpgradeData"))
			.register(JAVA_1_18_21W43A.id(), NbtPath.of("UpgradeData"));

	protected static final VersionAware<NbtPath> SECTIONS_PATH = new VersionAware<NbtPath>()
			.register(0, NbtPath.of("Level.Sections"))
			.register(JAVA_1_18_21W43A.id(), NbtPath.of("sections"));

	protected static final VersionAware<NbtPath> X_POS_PATH = new VersionAware<NbtPath>()
			.register(0, NbtPath.of("Level.xPos"))
			.register(JAVA_1_18_21W43A.id(), NbtPath.of("xPos"));

	protected static final VersionAware<NbtPath> Z_POS_PATH = new VersionAware<NbtPath>()
			.register(0, NbtPath.of("Level.zPos"))
			.register(JAVA_1_18_21W43A.id(), NbtPath.of("zPos"));

	/**
	 * Represents world bottom - note there may exist a dummy chunk -1 below this depending on MC flavor and current chunk state.
	 * @since {@link DataVersion#JAVA_1_18_21W43A}
	 */
	protected int yPos = NO_CHUNK_COORD_SENTINEL;
	protected static final VersionAware<NbtPath> Y_POS_PATH = new VersionAware<NbtPath>()
			.register(JAVA_1_18_21W43A.id(), NbtPath.of("yPos"));
	protected static final VersionAware<Integer> DEFAULT_WORLD_BOTTOM_Y_POS = new VersionAware<Integer>()
			.register(0, 0)
			.register(JAVA_1_18_XS1.id(), -4);  // IDK if they actually enabled deep worlds here or not...

	/** @since {@link DataVersion#JAVA_1_18_21W43A} */
	protected CompoundTag belowZeroRetrogen;
	protected static final VersionAware<NbtPath> BELOW_ZERO_RETROGEN_PATH = new VersionAware<NbtPath>()
			.register(JAVA_1_18_21W43A.id(), NbtPath.of("below_zero_retrogen"));

	/** @since {@link DataVersion#JAVA_1_18_21W43A} */
	protected CompoundTag blendingData;
	protected static final VersionAware<NbtPath> BLENDING_DATA_PATH = new VersionAware<NbtPath>()
			.register(JAVA_1_18_21W43A.id(), NbtPath.of("blending_data"));


	protected TerrainChunkBase(int dataVersion) {
		super(dataVersion);
	}

	/**
	 * Create a new chunk based on raw base data from a Terrain region file.
	 * @param data The raw base data to be used.
	 */
	public TerrainChunkBase(CompoundTag data) {
		super(data);
	}

	public TerrainChunkBase(CompoundTag data, long loadFlags) {
		super(data, loadFlags);
	}

	@Override
	protected void initMembers() {
		// give this a reasonable default... better than nothing...
		yPos = DEFAULT_WORLD_BOTTOM_Y_POS.get(dataVersion);
	}

	@Override
	protected void initReferences(final long loadFlags) {
		if (dataVersion < JAVA_1_18_21W39A.id()) {
			if (data.getCompoundTag("Level") == null) {
				throw new IllegalArgumentException("data does not contain \"Level\" tag");
			}
		}

		inhabitedTime = getTagValue(INHABITED_TIME_PATH, LongTag::asLong, 0L);
		lastUpdate = getTagValue(LAST_UPDATE_PATH, LongTag::asLong, 0L);
		if (dataVersion < JAVA_1_18_21W39A.id() && (loadFlags & BIOMES) != 0) {
			if (dataVersion >= DataVersion.JAVA_1_13_0.id()) {
				legacyBiomes = getTagValue(LEGACY_BIOMES_PATH, IntArrayTag::getValue);
			} else {
				byte[] byteBiomes = getTagValue(LEGACY_BIOMES_PATH, ByteArrayTag::getValue);
				legacyBiomes = new int[byteBiomes.length];
				for (int i = 0; i < legacyBiomes.length; i++) {
					legacyBiomes[i] = byteBiomes[i];
				}
			}
			if (legacyBiomes != null && legacyBiomes.length == 0) legacyBiomes = null;
		} else {
			// palette biomes

		}
		if ((loadFlags & HEIGHTMAPS) != 0) {
			legacyHeightMap = getTag(LEGACY_HEIGHT_MAP_PATH);
			heightMaps = getTag(HEIGHT_MAPS_PATH);
		}
		if ((loadFlags & CARVING_MASKS) != 0) {
			carvingMasks = getTag(CARVING_MASKS_PATH);
		}
		if ((loadFlags & ENTITIES) != 0) {
			entities = getTag(ENTITIES_PATH);
		}
		if ((loadFlags & TILE_ENTITIES) != 0) {
			tileEntities = getTag(TILE_ENTITIES_PATH);
		}
		if ((loadFlags & TILE_TICKS) != 0) {
			tileTicks = getTag(TILE_TICKS_PATH);
		}
		if ((loadFlags & TO_BE_TICKED) != 0) {
			toBeTicked = getTag(TO_BE_TICKED_PATH);
		}
		if ((loadFlags & LIGHTS) != 0) {
			lights = getTag(LIGHTS_PATH);
		}
		if ((loadFlags & LIQUID_TICKS) != 0) {
			liquidTicks = getTag(LIQUID_TICKS_PATH);
		}
		if ((loadFlags & LIQUIDS_TO_BE_TICKED) != 0) {
			liquidsToBeTicked = getTag(LIQUIDS_TO_BE_TICKED_PATH);
		}
		if ((loadFlags & POST_PROCESSING) != 0) {
			postProcessing = getTag(POST_PROCESSING_PATH);
		}

		status = getTagValue(STATUS_PATH, StringTag::getValue);
		isLightOn = getTagValue(IS_LIGHT_ON_PATH, ByteTag::asBoolean);
		isTerrainPopulated = getTagValue(TERRAIN_POPULATED_PATH, ByteTag::asBoolean);

		// TODO: add load flag for this
		upgradeData = getTag(UPGRADE_DATA_PATH);

		if ((loadFlags & STRUCTURES) != 0) {
			structures = getTag(STRUCTURES_PATH);
			hasLegacyStructureData = getTagValue(HAS_LEGACY_STRUCTURE_DATA_PATH, ByteTag::asBoolean);
		}

		// chunkXZ may be pre-populated with a solid guess so don't overwrite that guess if we don't have values.
		if (X_POS_PATH.get(dataVersion).exists(data)) {
			chunkX = getTagValue(X_POS_PATH, IntTag::asInt);
		}
		if (Z_POS_PATH.get(dataVersion).exists(data)) {
			chunkZ = getTagValue(Z_POS_PATH, IntTag::asInt);
		}

		yPos = getTagValue(Y_POS_PATH, IntTag::asInt, DEFAULT_WORLD_BOTTOM_Y_POS.get(dataVersion));

		boolean loadSections = ((loadFlags & (BLOCK_LIGHTS|BLOCK_STATES|SKY_LIGHT)) != 0)
				|| (dataVersion >= JAVA_1_18_21W39A.id() && ((loadFlags & BIOMES) != 0));
		if (loadSections) {
			ListTag<CompoundTag> sections = getTag(SECTIONS_PATH);
			if (sections != null) {
				for (CompoundTag section : sections) {
					T newSection = createSection(section, dataVersion, loadFlags);
					putSection(newSection.getHeight(), newSection, false);
				}
			}
		}
		if ((loadFlags & WORLD_UPGRADE_HINTS) != 0) {
			belowZeroRetrogen = getTag(BELOW_ZERO_RETROGEN_PATH);
			blendingData = getTag(BLENDING_DATA_PATH);
		}
	}

	protected abstract T createSection(CompoundTag section, int dataVersion, long loadFlags);

	/**
	 * May only be used for data versions LT 2203 which includes all of 1.14
	 * and up until 19w36a (a 1.15 weekly snapshot).
	 * @deprecated unsupported after JAVA_1_15_19W35A use {@link #getBiomeAt(int, int, int)} instead for 1.15 and beyond
	 */
	@Deprecated
	public int getBiomeAt(int blockX, int blockZ) {
		if (dataVersion > JAVA_1_15_19W35A.id())
			throw new VersionLacksSupportException(dataVersion, null, JAVA_1_15_19W35A,
					"cannot get biome using Chunk#getBiomeAt(int,int) from biome data with DataVersion of 2203 or higher (1.15+), use Chunk#getBiomeAt(int,int,int) instead");
		if (legacyBiomes == null || legacyBiomes.length != 256) {
			return -1;
		}
		return legacyBiomes[getBlockIndex(blockX, blockZ)];
	}

	/**
	 * Fetches a biome id at a specific block in this chunk.
	 * The coordinates can be absolute coordinates or relative to the region or chunk.
	 * @param blockX The x-coordinate of the block.
	 * @param blockY The y-coordinate of the block.
	 * @param blockZ The z-coordinate of the block.
	 * @return The biome id or -1 if the biomes are not correctly initialized.
	 * @deprecated unsupported after JAVA_1_18_21W38A
	 */
	public int getBiomeAt(int blockX, int blockY, int blockZ) {
		if (dataVersion > JAVA_1_18_21W38A.id())
			throw new VersionLacksSupportException(dataVersion, null, JAVA_1_18_21W38A, "legacy biomes");
		if (dataVersion >= JAVA_1_15_19W36A.id()) {  // 3D biomes
			if (legacyBiomes == null || legacyBiomes.length != 1024) {
				return -1;
			}
			int biomeX = (blockX & 0xF) >> 2;
			int biomeY = (blockY & 0xF) >> 2;
			int biomeZ = (blockZ & 0xF) >> 2;

			return legacyBiomes[getBiomeIndex(biomeX, biomeY, biomeZ)];
		} else {  // 2D biomes
			return getBiomeAt(blockX, blockZ);
		}
	}

	/**
	 * Should only be used for data versions LT 2203 which includes all of 1.14
	 * and up until 19w36a (a 1.15 weekly snapshot).
	 * @deprecated unsupported after JAVA_1_18_21W38A
	 */
	@Deprecated
	public void setBiomeAt(int blockX, int blockZ, int biomeID) {
		checkRaw();
		if (dataVersion > JAVA_1_18_21W38A.id())
			throw new VersionLacksSupportException(dataVersion, null, JAVA_1_18_21W38A, "2D legacy biomes");
		if (dataVersion < JAVA_1_15_19W36A.id()) {  // 2D biomes
			if (legacyBiomes == null || legacyBiomes.length != 256) {
				legacyBiomes = new int[256];
				Arrays.fill(legacyBiomes, -1);
			}
			legacyBiomes[getBlockIndex(blockX, blockZ)] = biomeID;
		} else {  // 3D biomes
			if (legacyBiomes == null || legacyBiomes.length != 1024) {
				legacyBiomes = new int[1024];
				Arrays.fill(legacyBiomes, -1);
			}

			int biomeX = (blockX & 0xF) >> 2;
			int biomeZ = (blockZ & 0xF) >> 2;

			for (int y = 0; y < 64; y++) {
				legacyBiomes[getBiomeIndex(biomeX, y, biomeZ)] = biomeID;
			}
		}
	}

	public void setBiomeAt(int blockX, int blockY, int blockZ, int biomeID) {
		if (dataVersion < JAVA_1_15_19W36A.id() || dataVersion > JAVA_1_18_21W38A.id())
			throw new VersionLacksSupportException(dataVersion, JAVA_1_15_19W36A, JAVA_1_18_21W38A, "3D legacy biomes");
		if (legacyBiomes == null || legacyBiomes.length != 1024) {
			legacyBiomes = new int[1024];
			Arrays.fill(legacyBiomes, -1);
		}

		int biomeX = (blockX & 0x0F) >> 2;
		int biomeY = blockY >> 2;
		int biomeZ = (blockZ & 0x0F) >> 2;
		legacyBiomes[getBiomeIndex(biomeX, biomeY, biomeZ)] = biomeID;
	}

	protected int getBiomeIndex(int biomeX, int biomeY, int biomeZ) {
		return biomeY * 16 + biomeZ * 4 + biomeX;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDataVersion(int dataVersion) {
		super.setDataVersion(dataVersion);
		for (T section : this) {
			if (section != null) {
				section.syncDataVersion(dataVersion);
			}
		}
	}

	/**
	 * @return The generation station of this chunk.
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Sets the generation status of this chunk.
	 * @param status The generation status of this chunk.
	 */
	public void setStatus(String status) {
		checkRaw();
		this.status = status;
	}

	// TODO(javadoc)
	public Boolean getLightOn() {
		return isLightOn;
	}

	// TODO(javadoc)
	public void setLightOn(Boolean lightOn) {
		isLightOn = lightOn;
	}

	// TODO(javadoc)
	public Boolean getTerrainPopulated() {
		return isTerrainPopulated;
	}

	// TODO(javadoc)
	public void setTerrainPopulated(Boolean terrainPopulated) {
		isTerrainPopulated = terrainPopulated;
	}

	// TODO(javadoc)
	public Boolean getHasLegacyStructureData() {
		return hasLegacyStructureData;
	}

	// TODO(javadoc)
	public void setHasLegacyStructureData(Boolean hasLegacyStructureData) {
		this.hasLegacyStructureData = hasLegacyStructureData;
	}

	// TODO(javadoc)
	public CompoundTag getUpgradeData() {
		return upgradeData;
	}

	// TODO(javadoc)
	public void setUpgradeData(CompoundTag upgradeData) {
		this.upgradeData = upgradeData;
	}

	// 2048 bytes recording the amount of block-emitted light in each block. Makes load times faster compared to recomputing at load time. 4 bits per block.

	/**
	 * @return The timestamp when this chunk was last updated as a UNIX timestamp.
	 */
	public long getLastUpdate() {
		return lastUpdate;
	}

	/**
	 * Sets the time when this chunk was last updated as a UNIX timestamp.
	 * @param lastUpdate The UNIX timestamp.
	 */
	public void setLastUpdate(long lastUpdate) {
		checkRaw();
		this.lastUpdate = lastUpdate;
	}

	/**
	 * @return The cumulative amount of time players have spent in this chunk in ticks.
	 */
	public long getInhabitedTime() {
		return inhabitedTime;
	}

	/**
	 * Sets the cumulative amount of time players have spent in this chunk in ticks.
	 * @param inhabitedTime The time in ticks.
	 */
	public void setInhabitedTime(long inhabitedTime) {
		checkRaw();
		this.inhabitedTime = inhabitedTime;
	}

	/**
	 * @return A matrix of biome IDs for all block columns in this chunk.
	 */
	public int[] getLegacyBiomes() {
		return legacyBiomes;
	}

	/**
	 * Sets the biome IDs for this chunk.
	 * @param legacyBiomes The biome ID matrix of this chunk. Must have a length of {@code 1024} for 1.15+ or {@code 256}
	 *                  for prior versions.
	 * @throws IllegalArgumentException When the biome matrix is {@code null} or does not have a version appropriate length.
	 */
	public void setLegacyBiomes(int[] legacyBiomes) {
		checkRaw();
		if (legacyBiomes != null) {
			final int requiredSize = dataVersion <= 0 || dataVersion >= JAVA_1_15_19W36A.id() ? 1024 : 256;
			if (legacyBiomes.length != requiredSize) {
				throw new IllegalArgumentException("biomes array must have a length of " + requiredSize);
			}
		}
		this.legacyBiomes = legacyBiomes;
	}

	/**
	 * @return The height maps of this chunk.
	 */
	public CompoundTag getHeightMaps() {
		return heightMaps;
	}

	/**
	 * Sets the height maps of this chunk.
	 * @param heightMaps The height maps.
	 */
	public void setHeightMaps(CompoundTag heightMaps) {
		checkRaw();
		this.heightMaps = heightMaps;
	}

	public IntArrayTag getLegacyHeightMap() {
		return legacyHeightMap;
	}

	public void setLegacyHeightMap(IntArrayTag legacyHeightMap) {
		this.legacyHeightMap = legacyHeightMap;
	}

	/**
	 * @return The carving masks of this chunk.
	 */
	public CompoundTag getCarvingMasks() {
		return carvingMasks;
	}

	/**
	 * Sets the carving masks of this chunk.
	 * @param carvingMasks The carving masks.
	 */
	public void setCarvingMasks(CompoundTag carvingMasks) {
		checkRaw();
		this.carvingMasks = carvingMasks;
	}

	/**
	 * @return The entities of this chunk. May be null.
	 */
	public ListTag<CompoundTag> getEntities() {
		return entities;
	}

	/**
	 * Sets the entities of this chunk.
	 * @param entities The entities.
	 */
	public void setEntities(ListTag<CompoundTag> entities) {
		checkRaw();
		this.entities = entities;
	}

	/**
	 * @return The tile entities of this chunk.
	 */
	public ListTag<CompoundTag> getTileEntities() {
		return tileEntities;
	}

	/**
	 * Sets the tile entities of this chunk.
	 * @param tileEntities The tile entities of this chunk.
	 */
	public void setTileEntities(ListTag<CompoundTag> tileEntities) {
		checkRaw();
		this.tileEntities = tileEntities;
	}

	/**
	 * @return The tile ticks of this chunk.
	 */
	public ListTag<CompoundTag> getTileTicks() {
		return tileTicks;
	}

	/**
	 * Sets the tile ticks of this chunk.
	 * @param tileTicks Thee tile ticks.
	 */
	public void setTileTicks(ListTag<CompoundTag> tileTicks) {
		checkRaw();
		this.tileTicks = tileTicks;
	}

	/**
	 * @return The liquid ticks of this chunk.
	 */
	public ListTag<CompoundTag> getLiquidTicks() {
		return liquidTicks;
	}

	/**
	 * Sets the liquid ticks of this chunk.
	 * @param liquidTicks The liquid ticks.
	 */
	public void setLiquidTicks(ListTag<CompoundTag> liquidTicks) {
		checkRaw();
		this.liquidTicks = liquidTicks;
	}

	/**
	 * @return The light sources in this chunk.
	 */
	public ListTag<ListTag<?>> getLights() {
		return lights;
	}

	/**
	 * Sets the light sources in this chunk.
	 * @param lights The light sources.
	 */
	public void setLights(ListTag<ListTag<?>> lights) {
		checkRaw();
		this.lights = lights;
	}

	/**
	 * @return The liquids to be ticked in this chunk.
	 */
	public ListTag<ListTag<?>> getLiquidsToBeTicked() {
		return liquidsToBeTicked;
	}

	/**
	 * Sets the liquids to be ticked in this chunk.
	 * @param liquidsToBeTicked The liquids to be ticked.
	 */
	public void setLiquidsToBeTicked(ListTag<ListTag<?>> liquidsToBeTicked) {
		checkRaw();
		this.liquidsToBeTicked = liquidsToBeTicked;
	}

	/**
	 * @return Stuff to be ticked in this chunk.
	 */
	public ListTag<ListTag<?>> getToBeTicked() {
		return toBeTicked;
	}

	/**
	 * Sets stuff to be ticked in this chunk.
	 * @param toBeTicked The stuff to be ticked.
	 */
	public void setToBeTicked(ListTag<ListTag<?>> toBeTicked) {
		checkRaw();
		this.toBeTicked = toBeTicked;
	}

	/**
	 * @return Things that are in post processing in this chunk.
	 */
	public ListTag<ListTag<?>> getPostProcessing() {
		return postProcessing;
	}

	/**
	 * Sets things to be post processed in this chunk.
	 * @param postProcessing The things to be post processed.
	 */
	public void setPostProcessing(ListTag<ListTag<?>> postProcessing) {
		checkRaw();
		this.postProcessing = postProcessing;
	}

	/**
	 * @return Data about structures in this chunk.
	 */
	public CompoundTag getStructures() {
		return structures;
	}

	/**
	 * Sets data about structures in this chunk.
	 * @param structures The data about structures.
	 */
	public void setStructures(CompoundTag structures) {
		checkRaw();
		this.structures = structures;
	}

	protected int getBlockIndex(int blockX, int blockZ) {
		return (blockZ & 0xF) * 16 + (blockX & 0xF);
	}

	/**
	 * Gets the world-bottom section y in the chunk.
	 */
	public int getChunkY() {
		if (yPos != NO_CHUNK_COORD_SENTINEL) return yPos;
		return DEFAULT_WORLD_BOTTOM_Y_POS.get(dataVersion);
	}

	/**
	 * @since {@link DataVersion#JAVA_1_18_21W43A}
	 */
	public CompoundTag getBelowZeroRetrogen() {
		return belowZeroRetrogen;
	}

	/**
	 * @since {@link DataVersion#JAVA_1_18_21W43A}
	 */
	public CompoundTag getBlendingData() {
		return blendingData;
	}


	/** {@inheritDoc} */
	@Override
	public boolean moveChunkImplemented() {
		return raw || ((this.chunkX != NO_CHUNK_COORD_SENTINEL && this.chunkZ != NO_CHUNK_COORD_SENTINEL) &&
				(data != null || (getStructures() != null && getTileEntities() != null && getTileTicks() != null && getLiquidTicks() != null)));
	}

	/** {@inheritDoc} */
	@Override
	public boolean moveChunkHasFullVersionSupport() {
		// TODO: this can probably safely be dropped to 1.18.0 or lower - need to investigate
		return dataVersion >= JAVA_1_20_0.id();
	}

	// For RAW support
	private ListTag<CompoundTag> tagOrFetch(ListTag<CompoundTag> tag, VersionAware<NbtPath> path) {
		if (tag != null) return tag;
		return getTag(path);
	}
	private CompoundTag tagOrFetch(CompoundTag tag, VersionAware<NbtPath> path) {
		if (tag != null) return tag;
		return getTag(path);
	}

	@Override
	public boolean moveChunk(int newChunkX, int newChunkZ, boolean force) {
		if (!moveChunkImplemented())
			throw new UnsupportedOperationException("Missing the data required to move this chunk!");
		if (this.chunkX == newChunkX && this.chunkZ == newChunkZ) return false;

		IntPointXZ deltaXZ;
		if (raw) {
			this.chunkX = getTagValue(X_POS_PATH, IntTag::asInt);
			this.chunkZ = getTagValue(Z_POS_PATH, IntTag::asInt);
			setTag(X_POS_PATH, new IntTag(newChunkX));
			setTag(Z_POS_PATH, new IntTag(newChunkZ));
		}
		deltaXZ = new IntPointXZ(newChunkX - chunkX, newChunkZ - chunkZ);
		this.chunkX = newChunkX;
		this.chunkZ = newChunkZ;
		deltaXZ = deltaXZ.multiply(16);  // scale to block cords

		boolean changed = false;
		ChunkBoundingRectangle cbr = new ChunkBoundingRectangle(chunkX, chunkZ);
		changed |= fixTileLocations(cbr, tagOrFetch(getTileEntities(), TILE_ENTITIES_PATH));
		changed |= fixTileLocations(cbr, tagOrFetch(getTileTicks(), TILE_TICKS_PATH));
		changed |= fixTileLocations(cbr, tagOrFetch(getLiquidTicks(), LIQUID_TICKS_PATH));
		changed |= moveStructures(tagOrFetch(getStructures(), STRUCTURES_PATH), deltaXZ);
		// TODO: move legacy entities (and maybe those in partially generated terrain chunks?) and poi's
		return changed;

	}

	protected boolean fixTileLocations(ChunkBoundingRectangle cbr, ListTag<CompoundTag> tagList) {
		boolean changed = false;
		for (CompoundTag tag : tagList) {
			int x = tag.getInt("x");
			int z = tag.getInt("z");
			if (!cbr.contains(x, z)) {
				changed = true;
				tag.putInt("x", cbr.relocateX(x));
				tag.putInt("z", cbr.relocateX(z));
			}
		}
		return changed;
	}

	protected boolean moveStructures(CompoundTag structuresTag, IntPointXZ deltaXZ) {
		CompoundTag references = structuresTag.getCompoundTag("References");
		boolean changed = false;
		if (references != null && !references.isEmpty()) {
			for (Tag<?> tag : references.values()) {
				long[] longs = ((LongArrayTag) tag).getValue();
				for (int i = 0; i < longs.length; i++) {
					longs[i] = IntPointXZ.pack(IntPointXZ.unpack(longs[i]).add(deltaXZ));
				}
			}
			changed = true;
		}
		return changed | deepMoveAll(structuresTag.getCompoundTag("starts"), deltaXZ);

//		CompoundTag starts = structuresTag.getCompoundTag("starts");
//		if (starts != null && !starts.isEmpty()) {
//			for (Tag<?> tag : starts.values()) {
//				CompoundTag ct = (CompoundTag) tag;
//				if (ct.containsKey("ChunkX")) ct.putInt("ChunkX", this.chunkX);
//				if (ct.containsKey("ChunkZ")) ct.putInt("ChunkZ", this.chunkZ);
//				ListTag<CompoundTag> children = ct.getCompoundList("Children");
//				if (children != null) {
//					for (CompoundTag child :  children) {
//						moveBoundingBox(child.getIntArrayTag("BB"), deltaXZ);
//					}
//				}
//			}
//		}
//		return changed;
	}

	private void moveBoundingBox(IntArrayTag boundsTag, IntPointXZ deltaXZ) {
		if (boundsTag != null) {
			int[] bounds = boundsTag.getValue();
			bounds[0] = bounds[0] + deltaXZ.getX();
			bounds[2] = bounds[2] + deltaXZ.getZ();
			bounds[3] = bounds[3] + deltaXZ.getX();
			bounds[5] = bounds[5] + deltaXZ.getZ();
		}
	}

	// sinks into every compound and list tag looking for "BB" bounding boxes an any IntTag that has a name ending in x or z.
	@SuppressWarnings("unchecked")
	private boolean deepMoveAll(CompoundTag root, IntPointXZ deltaXZ) {
		if (root == null || root.isEmpty()) return false;
		Deque<Object> stack = new LinkedList<>();
		stack.push(root);
		boolean changed = false;
		while (!stack.isEmpty()) {
			Object o = stack.pop();
			if (o instanceof CompoundTag) {
				CompoundTag tag = (CompoundTag) o;
				for (Map.Entry<String, Tag<?>> e : tag) {
					final String key = e.getKey().toLowerCase();
					if (key.equals("bb")) {
						moveBoundingBox((IntArrayTag) e.getValue(), deltaXZ);
						changed = true;
					} else if (key.equals("entrances")) {
						ListTag<IntArrayTag> list = (ListTag<IntArrayTag>) e.getValue();
						for (IntArrayTag bb : list) {
							moveBoundingBox(bb, deltaXZ);
							changed = true;
						}
					} else if (e.getValue() instanceof IntTag) {
						IntTag intTag = (IntTag) e.getValue();
						if (key.endsWith("x")) {
							if (!key.toLowerCase().startsWith("chunk")) {
								intTag.setValue(intTag.asInt() + deltaXZ.getX());
							} else {
								intTag.setValue(chunkX);
							}
							changed = true;
						} else if (key.endsWith("z")) {
							if (!key.toLowerCase().startsWith("chunk")) {
								intTag.setValue(intTag.asInt() + deltaXZ.getZ());
							} else {
								intTag.setValue(chunkZ);
							}
							changed = true;
						}
					} else if (e.getValue() instanceof CompoundTag || e.getValue() instanceof ListTag<?>) {
						stack.push(e.getValue());
					}
				}
			} else if (o instanceof ListTag) {
				ListTag<?> tag = (ListTag<?>) o;
				for (Tag<?> t : tag) {
					if (t instanceof CompoundTag || t instanceof ListTag<?>) {
						stack.push(t);
					}
				}
			}
		}
		return changed;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public CompoundTag updateHandle() {
		if (raw) {
			return data;
		}
		this.data = super.updateHandle();
		setTag(LAST_UPDATE_PATH, new LongTag(lastUpdate));
		setTag(INHABITED_TIME_PATH, new LongTag(inhabitedTime));
		if (legacyBiomes != null && dataVersion < JAVA_1_18_21W39A.id()) {
			final int requiredSize = dataVersion <= 0 || dataVersion >= JAVA_1_15_19W36A.id() ? 1024 : 256;
			if (legacyBiomes.length != requiredSize)
				throw new IllegalStateException(
						String.format("Biomes array must be %d bytes for version %d, array size is %d",
								requiredSize, dataVersion, legacyBiomes.length));

			if (dataVersion >= DataVersion.JAVA_1_13_0.id()) {
				setTag(LEGACY_BIOMES_PATH, new IntArrayTag(legacyBiomes));
			} else {
				byte[] byteBiomes = new byte[legacyBiomes.length];
				for (int i = 0; i < legacyBiomes.length; i++) {
					byteBiomes[i] = (byte) legacyBiomes[i];
				}
				setTag(LEGACY_BIOMES_PATH, new ByteArrayTag(byteBiomes));
			}
		}
		setTagIfNotNull(LEGACY_HEIGHT_MAP_PATH, legacyHeightMap);
		setTagIfNotNull(HEIGHT_MAPS_PATH, heightMaps);
		setTagIfNotNull(CARVING_MASKS_PATH, carvingMasks);
		setTagIfNotNull(ENTITIES_PATH, entities);
		setTagIfNotNull(TILE_ENTITIES_PATH, tileEntities);
		setTagIfNotNull(TILE_TICKS_PATH, tileTicks);
		setTagIfNotNull(LIQUID_TICKS_PATH, liquidTicks);
		setTagIfNotNull(LIGHTS_PATH, lights);
		setTagIfNotNull(LIQUIDS_TO_BE_TICKED_PATH, liquidsToBeTicked);
		setTagIfNotNull(TO_BE_TICKED_PATH, toBeTicked);
		setTagIfNotNull(POST_PROCESSING_PATH, postProcessing);
		if (status != null) setTag(STATUS_PATH, new StringTag(status));
		if (isLightOn != null) setTag(IS_LIGHT_ON_PATH, new ByteTag(isLightOn));
		if (isTerrainPopulated != null) setTag(TERRAIN_POPULATED_PATH, new ByteTag(isTerrainPopulated));
		setTagIfNotNull(STRUCTURES_PATH, structures);
		if (hasLegacyStructureData != null) setTag(HAS_LEGACY_STRUCTURE_DATA_PATH, new ByteTag(hasLegacyStructureData));

		// TODO: This logic does not respect original load flags! However, this is a long standing bug so
		// 		 simply "fixing" it may break consumers... I no longer care about existing consumers and
		//       need to figure out what that "fix" I was referring to was -.-
		ListTag<CompoundTag> sections = new ListTag<>(CompoundTag.class);
		for (T section : this) {
			if (section != null) {
				sections.add(section.updateHandle());  // contract of iterator assures correctness of "height" aka section-y
			}
		}
		setTag(SECTIONS_PATH, sections);

		setTag(X_POS_PATH, new IntTag(getChunkX()));
		setTag(Z_POS_PATH, new IntTag(getChunkZ()));
		if (dataVersion >= JAVA_1_18_21W43A.id()) {
			setTag(Y_POS_PATH, new IntTag(getChunkY()));
			setTagIfNotNull(BELOW_ZERO_RETROGEN_PATH, belowZeroRetrogen);
			setTagIfNotNull(BLENDING_DATA_PATH, blendingData);
		}
		return data;
	}

	@Override
	public CompoundTag updateHandle(int xPos, int zPos) {
		if (raw) {
			return data;
		}
		// TODO: moveChunk or die if given xPos != chunkX and same for z?
		updateHandle();
		if (xPos != NO_CHUNK_COORD_SENTINEL)
			setTag(X_POS_PATH, new IntTag(chunkX = xPos));
		// Y_POS_PATH is set in updateHandle() - was added in 1.18
		if (zPos != NO_CHUNK_COORD_SENTINEL)
			setTag(Z_POS_PATH, new IntTag(chunkZ = zPos));
		return data;
	}
}
