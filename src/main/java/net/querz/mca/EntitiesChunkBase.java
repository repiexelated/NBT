package net.querz.mca;

import net.querz.mca.entities.EntityBase;
import net.querz.mca.entities.EntityFactory;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.DoubleTag;
import net.querz.nbt.tag.ListTag;
import net.querz.util.ArgValidator;
import net.querz.util.ChunkBoundingRectangle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Provides all the basic functionality necessary for this type of chunk with abstraction hooks
 * making it easy to extend this class and modify the factory behavior of {@link MCAUtil} to create
 * instances of your custom class.
 * <p>
 *     It is possible to use this class to load chunk tags from versions prior to the introduction of /entities/*.mca
 *     files (introduced in 1.17). See this classes unit tests for an example, but it's really as simple as
 *     passing the chunk's {@link CompoundTag} to the constructor of a concrete implementation - such as
 *     {@link EntitiesChunk}.
 * </p>
 * @see EntitiesChunk
 * @see EntityFactory
 * @see MCAUtil#MCA_CREATORS
 */
public abstract class EntitiesChunkBase<ET extends EntityBase> extends ChunkBase implements Iterable<ET> {
    // Private to keep child classes clean (and well behaved) - child classes should access this via getEntities()
    // Not populated until getEntities() is called.
    private List<ET> entities;
    // Not populated if loaded in RAW mode or if load flags did not include ENTITIES
    protected ListTag<CompoundTag> entitiesTag;

    protected EntitiesChunkBase(int dataVersion) {
        super(dataVersion);
    }

    public EntitiesChunkBase(int dataVersion, int chunkX, int chunkZ) {
        super(dataVersion, chunkX, chunkZ);
    }

    public EntitiesChunkBase(CompoundTag data) {
        super(data);
    }

    public EntitiesChunkBase(CompoundTag data, long loadData) {
        super(data, loadData);
    }

    @Override
    protected void initReferences(long loadFlags) {
        // remember: this isn't called when loaded in RAW mode, see base class

        if (dataVersion >= DataVersion.JAVA_1_17_20W45A.id()) {
            int[] posXZ = data.getIntArray("Position");
            if (posXZ == null || posXZ.length != 2) {
                throw new IllegalArgumentException("Position tag missing or invalid");
            }
            chunkX = posXZ[0];
            chunkZ = posXZ[1];
        } else {
            // not reading an entities mca file, this must be a pre 1.17 terrain mca file
            CompoundTag level = data.getCompoundTag("Level");
            ArgValidator.check(level != null && level.containsKey("xPos") && level.containsKey("zPos"),
                    "Level/xPos/zPos tag missing");
            chunkX = level.getInt("xPos", NO_CHUNK_COORD_SENTINEL);
            chunkZ = level.getInt("zPos", NO_CHUNK_COORD_SENTINEL);
        }

        if ((loadFlags & LoadFlags.ENTITIES) > 0) {
            if (dataVersion >= DataVersion.JAVA_1_17_20W45A.id()) {
                entitiesTag = data.getListTagAutoCast("Entities");
            } else {
                // not reading an entities mca file, this must be a region mca file - pre 1.17 chunk
                if (data.containsKey("Level")) {
                    entitiesTag = data.getCompoundTag("Level").getListTagAutoCast("Entities");
                }
            }
            ArgValidator.check(entitiesTag != null, "\"Entities\" tag not found");
            // Don't call initEntities() here, let getEntities do this lazily to keep things lean
        }
    }

    /**
     * Called to initialize entity wrappers - implementers should respect the {@code raw} setting and DO NOTHING
     * if called when raw is set.
     */
    @SuppressWarnings("unchecked")
    protected void initEntities() {
        if (raw) return;
        if (entitiesTag == null) {
            // This branch should not be reachable in any typical usage scenario. The only way
            // this state should happen is if there is a bug in the implementers of this class.
            throw new IllegalStateException("Entities nbt tag was not loaded for this chunk");
        }
        entities = new ArrayList<>();
        for (CompoundTag entityTag : entitiesTag) {
            entities.add((ET) EntityFactory.create(entityTag, dataVersion));
        }
    }

    /**
     * Gets the list of entity object instances representing all entities in this chunk.
     * This list is lazy-instantiated to avoid the memory and compute costs of populating it if it's unused.
     * Translation, calling this for the first time will be slower than making successive calls.
     * <p>If performance is everything for you, but you would still like to work with higher level objects
     * than nbt tags, you can use {@link #getEntitiesTag()}, find an entity record you want to manipulate
     * and use {@link EntityFactory#create(CompoundTag, int)} to get an entity instance then call
     * {@link EntityBase#updateHandle()} to apply your changes all the way back to the entities tag held
     * for this chunk.</p>
     */
    public List<ET> getEntities() {
        checkRaw();
        if (entities == null) initEntities();
        return entities;
    }

    /**
     * Gets an indication of if the result of {@link #getEntities()} has been computed, or if calling it
     * will trigger lazy instantiation.
     * <p></p>
     * @return true if the result of {@link #getEntities()} is already computed; false if calling {@link #getEntities()}
     * will trigger creation of wrapper objects.
     */
    public boolean areWrappedEntitiesGenerated() {
        return entities != null;
    }

    /**
     * Sets the entities in this chunk. You should probably follow this call with a call to
     * {@link #fixEntityLocations()} unless you are sure all of the given entities are already
     * within the chunks bounds.
     * <p>Does not trigger a handle update. The result of calling {@link #getEntitiesTag()}
     * will not change until {@link #updateHandle()} has been called.</p>
     * @param entities Entities to set, not null, may be empty.
     * @throws UnsupportedOperationException if loaded in raw mode
     * @see #clearEntities()
     */
    public void setEntities(List<ET> entities) {
        checkRaw();
        ArgValidator.requireValue(entities);
        this.entities = entities;
    }

    /**
     * Gets the entities nbt tag by reference.
     * Result may be null if chunk was loaded with a LoadFlags that excluded Entities.
     * @throws UnsupportedOperationException if loaded in raw mode
     */
    public ListTag<CompoundTag> getEntitiesTag() {
        checkRaw();
        return entitiesTag;
    }

    /**
     * Sets the entities tag and causes the next call to {@link #getEntities()} to recreate wrapped entities.
     * The given tag is also set as the entities tag in the underlying CompoundTag handle in the version appropriate
     * location. I.e. calling this method or modifying the tag passed after calling this method will affect the
     * value returned by {@link #getHandle()}.
     * <p><b>Raw mode behavior: supported!</b><br />
     * Sets the given tag in the held nbt data handle in its version correct place. Does not make calling
     * {@link #getEntitiesTag()} or {@link #getEntities()} legal for chunks loaded in raw mode.
     * </p>
     *
     * @param entitiesTag Not null. If you want to clear the entities tag use {@link #clearEntities()} instead or if
     *                    operating in raw mode you can pass a new empty tag to take advantage of this classes
     *                    version awareness to place the tag in the correct location within the nbt data tag
     *                    as returned by {@link #getHandle()} and {@link #updateHandle()}
     */
    public void setEntitiesTag(ListTag<CompoundTag> entitiesTag) {
        checkPartial();
        ArgValidator.requireValue(entitiesTag);
        setEntitiesTagInternal(entitiesTag);
    }

    protected void setEntitiesTagInternal(ListTag<CompoundTag> entitiesTag) {
        if (data != null) {  // only sync the data tag if we have it - data will be null if chunk was partially loaded
            if (dataVersion >= DataVersion.JAVA_1_17_20W45A.id()) {
                data.put("Entities", entitiesTag);
            } else {
                // not reading an entities mca file, this must be a region mca file - pre 1.17 chunk
                CompoundTag levelTag = data.getCompoundTag("Level");
                if (levelTag == null) {
                    throw new IllegalStateException("Expected \"Level\" tag to exist in data handle for chunk version " +
                            getDataVersionEnum().toString());
                }
                levelTag.put("Entities", entitiesTag);
            }
        }
        if (!raw) {
            this.entitiesTag = entitiesTag;
        }
        // respect lazy loading and cause the next call to getEntities() to rebuild the wrapped entities
        entities = null;
    }

    /**
     * Clears the entities known to this chunk. If you have previously retrieved the list of entities from
     * {@link #getEntities()} that list is unaffected by this call.
     * Likewise a new entities tag is also created and any result previously returned from {@link #getEntitiesTag()}
     * is also unaffected by this call.
     * @throws UnsupportedOperationException if loaded in raw mode
     */
    public void clearEntities() {
        checkRaw();
        setEntitiesTagInternal(new ListTag<>(CompoundTag.class));
    }

    /** {@inheritDoc} */
    @Override
    public boolean moveChunkImplemented() {
        return !raw;
    }

    /** {@inheritDoc} */
    @Override
    public boolean moveChunkHasFullVersionSupport() {
        return !raw;
    }

    /**
     * Sets this chunks absolute XZ and calls {@link #fixEntityLocations()} returning its result.
     *
     * @param chunkX new absolute chunk-x
     * @param chunkZ new absolute chunk-z
     * @param force unused
     * @return true if any data was changed as a result of this call
     * @throws UnsupportedOperationException if loaded in raw mode
     */
    @Override
    public boolean moveChunk(int chunkX, int chunkZ, boolean force) {
        checkRaw();
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        return fixEntityLocations();
    }

    /**
     * Scans all entities and moves any which are outside this chunks bounds into it preserving their
     * relative location from their source chunk.
     * @return true if any entity locations were changed; false if no changes were made.
     * @throws UnsupportedOperationException if loaded in raw mode
     */
    public boolean fixEntityLocations() {
        checkRaw();
        if (this.chunkX == NO_CHUNK_COORD_SENTINEL || this.chunkZ == NO_CHUNK_COORD_SENTINEL) {
            throw new IllegalStateException("Chunk XZ not known");
        }
        boolean changed = false;
        if (entities != null) {
            final ChunkBoundingRectangle cbr = new ChunkBoundingRectangle(chunkX, chunkZ);
            for (ET entity : entities) {
                if (!cbr.contains(entity.getX(), entity.getZ())) {
                    entity.setX(cbr.relocateX(entity.getX()));
                    entity.setZ(cbr.relocateZ(entity.getZ()));
                    changed = true;
                }
            }
        } else {
            changed = fixEntityLocations(entitiesTag, new ChunkBoundingRectangle(chunkX, chunkZ));
        }
        return changed;
    }

    private boolean fixEntityLocations(ListTag<CompoundTag> entityTags, ChunkBoundingRectangle cbr) {
        boolean changed = false;
        for (CompoundTag entityTag : entityTags) {
            ListTag<DoubleTag> posTag = entityTag.getListTag("Pos").asDoubleTagList();
            double x = posTag.get(0).asDouble();
            double z = posTag.get(2).asDouble();
            if (!cbr.contains(x, z)) {
                posTag.set(0, new DoubleTag(cbr.relocateX(x)));
                posTag.set(2, new DoubleTag(cbr.relocateZ(z)));
                changed = true;
            }
            // This is correct even for boats visually straddling a chunk boarder, the passengers share the boat
            // location and the order of the passengers apparently controls their visual offset in game.
            // Example (trimmed down) F3+I capture of such a boat:
            // /summon minecraft:boat -1002.50 63.00 -672.01 {Type:"acacia",
            //    Passengers:[
            //      {id:"minecraft:cow",Pos:[-1002.5d,63.04d,-672.01d]},
            //      {id:"minecraft:pig",Pos:[-1002.5d,63.04d,-672.01d]}
            //    ],Rotation:[-180.0f,0.0f]}
            if (entityTag.containsKey("Passengers")) {
                changed |= fixEntityLocations(data.getListTag("Passengers").asCompoundTagList(), cbr);
            }
        }
        return changed;
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<ET> iterator() {
        return getEntities().iterator();
    }

    /** {@inheritDoc} */
    @Override
    public void forEach(Consumer<? super ET> action) {
        getEntities().forEach(action);
    }

    /** {@inheritDoc} */
    @Override
    public Spliterator<ET> spliterator() {
        return getEntities().spliterator();
    }

    /** {@inheritDoc} */
    public Stream<ET> stream() {
        return getEntities().stream();
    }

    /** {@inheritDoc} */
    @Override
    public void setDataVersion(int dataVersion) {
        DataVersion.JAVA_1_17_20W45A.throwUnsupportedVersionChangeIfCrossed(this.dataVersion, dataVersion);
        super.setDataVersion(dataVersion);
    }

    @Override
    public CompoundTag updateHandle() {
        checkPartial();
        if (!raw) {
            super.updateHandle();
            if (dataVersion >= DataVersion.JAVA_1_17_20W45A.id()) {
                if (chunkX != NO_CHUNK_COORD_SENTINEL && chunkZ != NO_CHUNK_COORD_SENTINEL) {
                    data.putIntArray("Position", new int[]{chunkX, chunkZ});
                }
            } else if (data.containsKey("Level")) {
                CompoundTag levelTag = data.getCompoundTag("Level");
                levelTag.putInt("xPos", chunkX);
                levelTag.putInt("zPos", chunkZ);
            }

            // if getEntities() was never called then don't rebuild entitiesTag
            if (entities != null) {
                // WARN: If this chunk was loaded without the ENTITIES LoadData flag but 'entities' is not null
                // this indicates the user called setEntities() which initialized entitiesTag
                // so no NPE risk here - assuming someone didn't extend this class and break the contract of
                // setEntities
                entitiesTag.clear();
                for (ET entity : entities) {
                    entitiesTag.add(entity.updateHandle());
                }
            }
        }
        return data;
    }

    @Override
    public CompoundTag updateHandle(int xPos, int zPos) {
        if (!raw) {
            if (chunkX == NO_CHUNK_COORD_SENTINEL) chunkX = xPos;
            if (chunkZ == NO_CHUNK_COORD_SENTINEL) chunkZ = zPos;
            ArgValidator.check(xPos == chunkX && zPos == chunkZ,
                    "Attempted to write chunk with incorrect chunk XZ. Chunk must be moved with moveChunk(..) first.");
            updateHandle();
        }
        return data;
    }
}
