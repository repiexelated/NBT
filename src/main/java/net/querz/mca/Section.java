package net.querz.mca;

import net.querz.nbt.tag.CompoundTag;

/**
 * Represents a REGION data chunk section.
 */
public class Section extends RegionSectionBase {

	public Section(CompoundTag sectionRoot, int dataVersion) {
		super(sectionRoot, dataVersion);
	}

	public Section(CompoundTag sectionRoot, int dataVersion, long loadFlags) {
		super(sectionRoot, dataVersion, loadFlags);
	}

	public Section(int dataVersion) {
		super(dataVersion);
	}

	/**
	 * @return An empty Section initialized <b>using the latest full release data version</b>.
	 * @deprecated Dangerous - prefer using {@link Chunk#createSection(int)} or using the
	 * {@link #Section(int)} constructor instead.
	 */
	@Deprecated
	public static Section newSection() {
		return new Section(DataVersion.latest().id());
	}

	/**
	 * This method should only be used for building sections prior to adding to a chunk where you want to use this
	 * section height property for the convenience of not having to track the value separately.
	 *
	 * @deprecated To set section height (aka section-y) use
	 * {@code chunk.putSection(int, SectionBase, boolean)} instead of this function. Setting the section height
	 * by calling this function WILL NOT have any affect upon the sections height in the Chunk or or MCA data when
	 * serialized.
	 */
	@Deprecated
	public void setHeight(int height) {
		syncHeight(height);
	}

	/**
	 * Updates the raw CompoundTag that this Section is based on.
	 *
	 * @param y The Y-value of this Section to include in the returned tag.
	 *             DOES NOT update this sections height value permanently.
	 * @return A reference to the raw CompoundTag this Section is based on
	 * @deprecated The holding chunk is the authority on this sections y / height and takes care of all updates to it.
	 */
	@Deprecated
	public CompoundTag updateHandle(int y) {
		final int oldY = height;
		try {
			height = y;
			return updateHandle();
		} finally {
			height = oldY;
		}
	}
}
