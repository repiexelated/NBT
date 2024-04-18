package net.rossquerz.mca;

import net.rossquerz.nbt.query.NbtPath;
import net.rossquerz.nbt.tag.*;
import net.rossquerz.mca.util.VersionAware;

import java.util.Arrays;

import static net.rossquerz.mca.DataVersion.*;
import static net.rossquerz.mca.LoadFlags.*;

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

	protected int[] biomes;
	protected static final VersionAware<NbtPath> BIOMES_PATH = new VersionAware<NbtPath>()
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

	// PRIVATE - this field is only used when we didn't load section data, prefer #getYPos() to reading this value
	/** @since 1.18 */
	private int yPos;
	protected static final VersionAware<Integer> DEFAULT_Y_POS = new VersionAware<Integer>()
			.register(0, 0)
			.register(JAVA_1_18_XS1.id(), -4);  // IDK if they actually enabled deep worlds here or not...
	protected static final VersionAware<NbtPath> Y_POS_PATH = new VersionAware<NbtPath>()
			.register(JAVA_1_18_21W43A.id(), NbtPath.of("yPos"));

	/** @since 1.18 */
	protected CompoundTag belowZeroRetrogen;
	protected static final VersionAware<NbtPath> BELOW_ZERO_RETROGEN_PATH = new VersionAware<NbtPath>()
			.register(JAVA_1_18_21W43A.id(), NbtPath.of("below_zero_retrogen"));

	/** @since 1.18 */
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
		yPos = DEFAULT_Y_POS.get(dataVersion);
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
				biomes = getTagValue(BIOMES_PATH, IntArrayTag::getValue);
			} else {
				byte[] byteBiomes = getTagValue(BIOMES_PATH, ByteArrayTag::getValue);
				biomes = new int[byteBiomes.length];
				for (int i = 0; i < biomes.length; i++) {
					biomes[i] = byteBiomes[i];
				}
			}
			if (biomes != null && biomes.length == 0) biomes = null;
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

		boolean loadSections = (loadFlags & (BLOCK_LIGHTS|BLOCK_STATES|SKY_LIGHT)) != 0;
		loadSections |= dataVersion >= JAVA_1_18_21W39A.id()
				&& ((loadFlags & BIOMES) != 0);
		if (loadSections) {
			ListTag<CompoundTag> sections = getTag(SECTIONS_PATH);
			if (sections != null) {
				for (CompoundTag section : sections) {
					T newSection = createSection(section, dataVersion, loadFlags);
					putSection(newSection.getHeight(), newSection, false);
				}
			}
		} else {
			yPos = getTagValue(Y_POS_PATH, IntTag::asInt, 0);
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
	 * @deprecated Use {@link #getBiomeAt(int, int, int)} instead for 1.15 and beyond
	 */
	@Deprecated
	public int getBiomeAt(int blockX, int blockZ) {
		if (dataVersion < JAVA_1_15_19W36A.id()) {
			if (biomes == null || biomes.length != 256) {
				return -1;
			}
			return biomes[getBlockIndex(blockX, blockZ)];
		} else {
			throw new IllegalStateException("cannot get biome using Chunk#getBiomeAt(int,int) from biome data with DataVersion of 2203 or higher (1.15+), use Chunk#getBiomeAt(int,int,int) instead");
		}
	}

	/**
	 * Fetches a biome id at a specific block in this chunk.
	 * The coordinates can be absolute coordinates or relative to the region or chunk.
	 * @param blockX The x-coordinate of the block.
	 * @param blockY The y-coordinate of the block.
	 * @param blockZ The z-coordinate of the block.
	 * @return The biome id or -1 if the biomes are not correctly initialized.
	 */
	public int getBiomeAt(int blockX, int blockY, int blockZ) {
		if (dataVersion >= JAVA_1_15_19W36A.id()) {
			if (biomes == null || biomes.length != 1024) {
				return -1;
			}
			int biomeX = (blockX & 0xF) >> 2;
			int biomeY = (blockY & 0xF) >> 2;
			int biomeZ = (blockZ & 0xF) >> 2;

			return biomes[getBiomeIndex(biomeX, biomeY, biomeZ)];
		} else {
			return getBiomeAt(blockX, blockZ);
		}
	}

	/**
	 * Should only be used for data versions LT 2203 which includes all of 1.14
	 * and up until 19w36a (a 1.15 weekly snapshot).
	 * @deprecated Use {@link #setBiomeAt(int, int, int, int)} instead for 1.15 and beyond
	 */
	@Deprecated
	public void setBiomeAt(int blockX, int blockZ, int biomeID) {
		checkRaw();
		if (dataVersion < JAVA_1_15_19W36A.id()) {
			if (biomes == null || biomes.length != 256) {
				biomes = new int[256];
				Arrays.fill(biomes, -1);
			}
			biomes[getBlockIndex(blockX, blockZ)] = biomeID;
		} else {
			if (biomes == null || biomes.length != 1024) {
				biomes = new int[1024];
				Arrays.fill(biomes, -1);
			}

			int biomeX = (blockX & 0xF) >> 2;
			int biomeZ = (blockZ & 0xF) >> 2;

			for (int y = 0; y < 64; y++) {
				biomes[getBiomeIndex(biomeX, y, biomeZ)] = biomeID;
			}
		}
	}


	public void setBiomeAt(int blockX, int blockY, int blockZ, String biomeName) {
		throw new UnsupportedOperationException("not yet implemented");
	}

	/**
	  * Sets a biome id at a specific block.
	  * The coordinates can be absolute coordinates or relative to the region or chunk.
	  *
	  * <h2>data version &lt; JAVA_1_15_19W36A (non-3D biomes)</h2>
	  * The blockY value has no effect and the biome is set for the entire column, filling a cuboid of 1x256x1.
	  *
	  * <h2>data version &gt;= JAVA_1_15_19W36A (3D biomes)</h2>
	  * 3D biomes occupy a 4x4x4 cuboid so setting the biome for a single block within a cuboid sets it for all
	  * blocks within the same cuboid.
	  *
	  * <h2>data version &gt;= JAVA_1_18_21W39A (palette based 3D biomes)</h2>
	  * This method is NOT supported for data versions &gt;= JAVA_1_18_21W39A, use
	  * {@link #setBiomeAt(int, int, int, String)} instead.
	  *
	  * @param blockX The x-coordinate of the block column.
	  * @param blockY The y-coordinate of the block column.
	  * @param blockZ The z-coordinate of the block column.
	  * @param biomeID The biome id to be set.
	  *                When set to a negative number, Minecraft will replace it with the block column's default biome.
	  */
	public void setBiomeAt(int blockX, int blockY, int blockZ, int biomeID) {
		checkRaw();
		// TODO: see about finding an id to string mapping somewhere
		if (dataVersion >= JAVA_1_18_21W39A.id())
			throw new UnsupportedOperationException(
					"JAVA_1_18_21W39A and above no longer use biomeID ints. " +
					"You must use #setBiomeAt(int, int, int, String) instead.");
		if (dataVersion >= JAVA_1_15_19W36A.id()) {
			if (biomes == null || biomes.length != 1024) {
				biomes = new int[1024];
				Arrays.fill(biomes, -1);
			}
			int biomeX = (blockX & 0xF) >> 2;
			int biomeZ = (blockZ & 0xF) >> 2;
			biomes[getBiomeIndex(biomeX, blockY, biomeZ)] = biomeID;
		} else {
			if (biomes == null || biomes.length != 256) {
				biomes = new int[256];
				Arrays.fill(biomes, -1);
			}
			biomes[getBlockIndex(blockX, blockZ)] = biomeID;
		}
	}

	int getBiomeIndex(int biomeX, int biomeY, int biomeZ) {
		return biomeY * 16 + biomeZ * 4 + biomeX;
	}

	public CompoundTag getBlockStateAt(int blockX, int blockY, int blockZ) {
		T section = getSection(McaFileHelpers.blockToChunk(blockY));
		if (section == null) {
			return null;
		}
		return section.getBlockStateAt(blockX, blockY, blockZ);
	}

	/**
	 * Sets a block state at a specific location.
	 * The block coordinates can be absolute or relative to the region or chunk.
	 * @param blockX The x-coordinate of the block.
	 * @param blockY The y-coordinate of the block.
	 * @param blockZ The z-coordinate of the block.
	 * @param state The block state to be set.
	 * @param cleanup When <code>true</code>, it will cleanup all palettes of this chunk.
	 *                This option should only be used moderately to avoid unnecessary recalculation of the palette indices.
	 *                Recalculating the Palette should only be executed once right before saving the Chunk to file.
	 */
	public void setBlockStateAt(int blockX, int blockY, int blockZ, CompoundTag state, boolean cleanup) {
		checkRaw();
		int sectionIndex = McaFileHelpers.blockToChunk(blockY);
		T section = getSection(sectionIndex);
		if (section == null) {
			putSection(sectionIndex, section = createSection(sectionIndex), false);
			section.syncDataVersion(dataVersion);
		}
		section.setBlockStateAt(blockX, blockY, blockZ, state, cleanup);
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
	public int[] getBiomes() {
		return biomes;
	}

	/**
	 * Sets the biome IDs for this chunk.
	 * @param biomes The biome ID matrix of this chunk. Must have a length of {@code 1024} for 1.15+ or {@code 256}
	 *                  for prior versions.
	 * @throws IllegalArgumentException When the biome matrix is {@code null} or does not have a version appropriate length.
	 */
	public void setBiomes(int[] biomes) {
		checkRaw();
		if (biomes != null) {
			final int requiredSize = dataVersion <= 0 || dataVersion >= JAVA_1_15_19W36A.id() ? 1024 : 256;
			if (biomes.length != requiredSize) {
				throw new IllegalArgumentException("biomes array must have a length of " + requiredSize);
			}
		}
		this.biomes = biomes;
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

	int getBlockIndex(int blockX, int blockZ) {
		return (blockZ & 0xF) * 16 + (blockX & 0xF);
	}

	public void cleanupPalettesAndBlockStates() {
		checkRaw();
		for (T section : this) {
			if (section != null) {
				section.cleanupPaletteAndBlockStates();
			}
		}
	}

	/**
	 * Gets the minimum section y position in the chunk.
	 * @since 1.18
	 */
	public int getYPos() {
		if (hasSections()) return getMinSectionY();
		else return yPos;
	}

	/**
	 * @since 1.18
	 */
	public CompoundTag getBelowZeroRetrogen() {
		return belowZeroRetrogen;
	}

	/**
	 * @since 1.18
	 */
	public CompoundTag getBlendingData() {
		return blendingData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CompoundTag updateHandle() {
		if (raw) {
			return data;
		}
		super.updateHandle();
		setTag(LAST_UPDATE_PATH, new LongTag(lastUpdate));
		setTag(INHABITED_TIME_PATH, new LongTag(inhabitedTime));
		if (biomes != null && dataVersion < JAVA_1_18_21W39A.id()) {
			final int requiredSize = dataVersion <= 0 || dataVersion >= JAVA_1_15_19W36A.id() ? 1024 : 256;
			if (biomes.length != requiredSize)
				throw new IllegalStateException(
						String.format("Biomes array must be %d bytes for version %d, array size is %d",
								requiredSize, dataVersion, biomes.length));

			if (dataVersion >= DataVersion.JAVA_1_13_0.id()) {
				setTag(BIOMES_PATH, new IntArrayTag(biomes));
			} else {
				byte[] byteBiomes = new byte[biomes.length];
				for (int i = 0; i < biomes.length; i++) {
					byteBiomes[i] = (byte) biomes[i];
				}
				setTag(BIOMES_PATH, new ByteArrayTag(byteBiomes));
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
		// 		 simply "fixing" it may break consumers...
		ListTag<CompoundTag> sections = new ListTag<>(CompoundTag.class);
		for (T section : this) {
			if (section != null) {
				sections.add(section.updateHandle());  // contract of iterator assures correctness of "height" aka section-y
			}
		}
		setTag(SECTIONS_PATH, sections);

		if (dataVersion >= JAVA_1_18_21W43A.id()) {
			setTag(Y_POS_PATH, new IntTag(getMinSectionY()));
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
		updateHandle();
		setTag(X_POS_PATH, new IntTag(xPos));
		setTag(Z_POS_PATH, new IntTag(zPos));
		return data;
	}
}
