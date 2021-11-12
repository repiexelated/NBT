package net.querz.mca;

import net.querz.nbt.tag.CompoundTag;

/**
 * Represents a REGION data mca chunk. Region chunks are composed of a set of {@link Section} where any empty/null
 * section is filled with air blocks by the game. When altering existing chunks for MC 1.14+, be sure to have read and
 * understood the documentation on {@link PoiRecord} to avoid problems with villagers, nether portal linking,
 * lodestones, bees, and probably more as Minecraft continues to evolve.
 */
public class Chunk extends RegionChunkBase<Section> {
	/**
	 * The default chunk data version used when no custom version is supplied.
	 * @deprecated Use {@code DataVersion.latest().id()} instead.
	 */
	@Deprecated
	public static final int DEFAULT_DATA_VERSION = DataVersion.latest().id();

	@Deprecated
	protected Chunk(int lastMCAUpdate) {
		setLastMCAUpdate(lastMCAUpdate);
	}

	public Chunk(CompoundTag data) {
		super(data);
	}

	public Chunk(CompoundTag data, long loadFlags) {
		super(data, loadFlags);
	}


	@Override
	protected Section createSection(CompoundTag section, int dataVersion, long loadFlags) {
		return new Section(section, dataVersion, loadFlags);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Section createSection(int sectionY) throws IllegalArgumentException {
		if (containsSection(sectionY)) throw new IllegalArgumentException("section already exists at section-y " + sectionY);
		Section section = new Section(dataVersion);
		if (sectionY != SectionBase.NO_HEIGHT_SENTINEL) {
			putSection(sectionY, section);  // sets section height & validates range
		}
		return section;
	}

	public Section createSection() {
		return createSection(SectionBase.NO_HEIGHT_SENTINEL);
	}

	/**
	 * @deprecated Dangerous - assumes latest full release data version defined by {@link DataVersion}
	 * prefer using {@link MCAFileBase#createChunk()} or {@link MCAFileBase#createChunkIfMissing(int, int)}.
	 */
	@Deprecated
	public static Chunk newChunk() {
		return newChunk(DataVersion.latest().id());
	}

	public static Chunk newChunk(int dataVersion) {
		Chunk c = new Chunk(0);
		c.dataVersion = dataVersion;
		c.data = new CompoundTag();
		c.data.put("Level", new CompoundTag());
		c.status = "mobs_spawned";
		return c;
	}
}
