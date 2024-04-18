package net.rossquerz.mca;

import net.rossquerz.nbt.tag.CompoundTag;
import java.util.*;

/**
 * Sections can be thought of as "sub-chunks" which are 16x16x16 block cubes
 * stacked atop each other to create a "chunk".
 */
public abstract class SectionBase<T extends SectionBase<?>> implements Comparable<T>, TagWrapper {
	public static final int NO_HEIGHT_SENTINEL = Integer.MIN_VALUE;
	/** for internal use only - must be kept in sync with chunk data version */
	protected int dataVersion;
	protected final CompoundTag data;
	protected int height = NO_HEIGHT_SENTINEL;

	protected SectionBase(CompoundTag sectionRoot, int dataVersion) {
		Objects.requireNonNull(sectionRoot, "sectionRoot must not be null");
		this.data = sectionRoot;
		this.dataVersion = dataVersion;
	}

	protected SectionBase(int dataVersion) {
		data = new CompoundTag();
		this.dataVersion = dataVersion;
	}

	/** section data version must be kept in sync with chunk data version */
	protected void syncDataVersion(int newDataVersion) {
		if (newDataVersion <= 0) {
			throw new IllegalArgumentException("Invalid data version - must be GT 0");
		}
		this.dataVersion = newDataVersion;
	}

	@Override
	public int compareTo(T o) {
		if (o == null) {
			return -1;
		}
		return Integer.compare(height, o.height);
	}

	/**
	 * Checks whether the data of this Section is empty.
	 * @return true if empty
	 */
	public boolean isEmpty() {
		return data.isEmpty();
	}

	/**
	 * Gets the height of the bottom of this section relative to Y0 as a section-y value, each 1 section-y is equal to
	 * 16 blocks.
	 * This library (as a whole) will attempt to keep the value returned by this function in sync with the actual
	 * location it has been placed within its chunk.
	 * <p>The value returned may be unreliable if this section is placed in multiple chunks at different heights
	 * or if this section is an instance of {@link TerrainSection} and user code calls {@link TerrainSection#setHeight(int)}
	 * on a section which is referenced by any chunk.</p>
	 * <p>Prefer using {@code chunk.getSectionY(section)} which will always be accurate within the context of the
	 * chunk.</p>
	 * @return The Y value of this section.
	 */
	public int getHeight() {
		return height;
	}

	protected void syncHeight(int height) {
		this.height = height;
	}

	protected void checkY(int y) {
		if (y == NO_HEIGHT_SENTINEL) {
			throw new IndexOutOfBoundsException("section height not set");
		}
		if (y < Byte.MIN_VALUE | y > Byte.MAX_VALUE) {
			throw new IndexOutOfBoundsException("section height (aka section-y) must be in range of BYTE [-128..127] was: " + y);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public CompoundTag getHandle() {
		return data;
	}
}
