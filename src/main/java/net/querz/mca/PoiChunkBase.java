package net.querz.mca;

import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;
import net.querz.nbt.tag.Tag;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static net.querz.mca.LoadFlags.*;

public abstract class PoiChunkBase<T extends PoiRecord> extends ChunkBase implements Iterable<T> {
    protected List<T> records = new ArrayList<>();
    protected Map<Integer, Boolean> poiSectionValidity = new HashMap<>();

    protected PoiChunkBase() { }

    public PoiChunkBase(CompoundTag data) {
        super(data);
    }

    public PoiChunkBase(CompoundTag data, long loadData) {
        super(data, loadData);
    }

    @Override
    protected void initReferences(long loadFlags) {
        if ((loadFlags & POI_RECORDS) != 0) {
            CompoundTag sectionsTag = data.getCompoundTag("Sections");
            if (sectionsTag == null) {
                throw new IllegalArgumentException("Sections tag not found!");
            }
            for (Map.Entry<String, Tag<?>> sectionTag : sectionsTag.entrySet()) {
                int sectionY = Integer.parseInt(sectionTag.getKey());
                boolean valid = ((CompoundTag) sectionTag.getValue()).getBoolean("Valid");
                poiSectionValidity.put(sectionY, valid);
                ListTag<CompoundTag> recordTags = ((CompoundTag) sectionTag.getValue()).getListTagAutoCast("Records");
                for (CompoundTag recordTag : recordTags) {
                    T record = createPoiRecord(recordTag);
                    records.add(record);
                }
            }
        }
    }

    /**
     * Called from {@link #initReferences(long)}. Exists to provide a hook for custom implementations to override to
     * add support for modded poi's, etc. without having to implement {@link #initReferences(long)} logic fully.
     */
    protected abstract T createPoiRecord(CompoundTag recordTag);

    public void add(T record) {
        if (record == null || record.getType() == null || record.getType().isEmpty()) {
            throw new IllegalArgumentException(
                    "record must not be null and must have a type specified");
        }
        records.add(record);
    }

    /**
     * Removes the given record from ths poi chunk both by reference and by equality.
     * @param record record to remove
     * @return true if any record was removed
     */
    public boolean remove(T record) {
        if (record == null || record.getType() == null || record.getType().isEmpty()) {
            return false;
        }
        return records.removeIf(r -> r == record || r.equals(record));
    }

    /**
     * Removes the FIRST PoiRecord at the given xyz.
     * @param x world block x
     * @param y world block y
     * @param z world block z
     * @return Removed PoiRecord or null if no such record
     */
    public T removeFirst(final int x, final int y, final int z) {
        Iterator<T> iter = records.iterator();
        while (iter.hasNext()) {
            T record = iter.next();
            if (record.matches(x, y, z)) {
                iter.remove();
                return record;
            }
        }
        return null;
    }

    /**
     * Removes all records at the given xyz.
     * @param x world block x
     * @param y world block y
     * @param z world block z
     * @return True if any records were removed
     */
    public boolean removeAll(final int x, final int y, final int z) {
        return records.removeIf(r -> r.matches(x, y, z));
    }

    /**
     * Removes all PoiRecords with the given type.
     * @param poiType poi type to remove
     * @return true if any records were removed
     */
    public boolean removeAll(final String poiType) {
        return records.removeIf(r -> r.matches(poiType));
    }

    /**
     * Gets the first poi record found with the exact xyz given
     * @param x world block x
     * @param y world block y
     * @param z world block z
     * @return poi record if found, otherwise null
     */
    public T getFirst(final int x, final int y, final int z) {
        return records.stream().filter(r -> r.matches(x, y, z)).findFirst().orElse(null);
    }

    /**
     * Gets all poi record found with the exact xyz given. Really there should be only one - but nothing is stopping you
     * from messing it up.
     * @param x world block x
     * @param y world block y
     * @param z world block z
     * @return list of poi records at the given xyz
     */
    public List<T> getAll(final int x, final int y, final int z) {
        return records.stream().filter(r -> r.matches(x, y, z)).collect(Collectors.toList());
    }

    /**
     * Gets all poi records of the given type
     * @param poiType poi type
     * @return list of poi records matching the given poi type
     */
    public List<T> getAll(final String poiType) {
        return records.stream().filter(r -> r.matches(poiType)).collect(Collectors.toList());
    }

    /**
     * Marks the given subchunk invalid so that Minecraft will recompute POI for it when loaded.
     * @param sectionY subchunk section-y to invalidate
     */
    public void invalidateSection(int sectionY) {
        if (sectionY < Byte.MIN_VALUE || sectionY > Byte.MAX_VALUE)
            throw new IllegalArgumentException("sectionY must be in range [-128..127]");
        poiSectionValidity.put(sectionY, false);
    }

    /**
     * Checks if the given section has been marked invalid either by calling {@link #invalidateSection(int)} or if
     * it was already invalidated in the poi mca file.
     */
    public boolean isPoiSectionValid(int sectionY) {
        return poiSectionValidity.getOrDefault(sectionY, true);
    }

    /**
     * Checks if the given poi record resides in a section that has been marked invalid either by calling
     * {@link #invalidateSection(int)} or was already invalidated in the poi mca file.
     */
    public boolean isPoiSectionValid(PoiRecord record) {
        return record == null || poiSectionValidity.getOrDefault(record.getSectionY(), true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompoundTag updateHandle() {
        if (raw) return data;
        super.updateHandle();
        Map<Integer, List<T>> sectionedLists = records.stream().collect(Collectors.groupingBy(PoiRecord::getSectionY));
        // ensure that all invalidated sections are in sectionedLists so we can just do one processing pass
        for (int sectionY : poiSectionValidity.keySet()) {
            if (!sectionedLists.containsKey(sectionY)) {
                sectionedLists.put(sectionY, Collections.emptyList());
            }
        }
        CompoundTag sectionsTag = new CompoundTag(sectionedLists.size());
        data.put("Sections", sectionsTag);
        for (Map.Entry<Integer, List<T>> sectionList : sectionedLists.entrySet()) {
            CompoundTag sectionTag = new CompoundTag();
            sectionsTag.put(Integer.toString(sectionList.getKey()), sectionsTag);
            ListTag<CompoundTag> recordsTag = new ListTag<>(CompoundTag.class, sectionList.getValue().size());
            sectionTag.put("Records", recordsTag);
            for (PoiRecord record : sectionList.getValue()) {
                recordsTag.add(record.updateHandle());
            }
            sectionTag.putBoolean("Valid", poiSectionValidity.getOrDefault(sectionList.getKey(), true));
        }
        return data;
    }

    @Override
    public Iterator<T> iterator() {
        return records.iterator();
    }

    /**
     * Provides an iterator over poi records with the given type. This is a convenience function and does not provide
     * any real optimization v.s. iterating over all elements.
     * @param poiType poi type
     * @return Never null, but may be empty
     */
    public Iterator<T> iterator(final String poiType) {
        return records.stream().filter(r -> r.matches(poiType)).iterator();
    }

    public Stream<T> stream() {
        return records.stream();
    }
}
