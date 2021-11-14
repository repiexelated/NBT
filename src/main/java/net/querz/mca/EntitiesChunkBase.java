package net.querz.mca;

import net.querz.mca.entities.EntityBase;
import net.querz.mca.entities.EntityFactory;
import net.querz.nbt.tag.CompoundTag;
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
 */
public abstract class EntitiesChunkBase<ET extends EntityBase> extends ChunkBase implements Iterable<ET> {
    protected List<ET> entities;

    @Override
    protected void initMembers() {
        entities = new ArrayList<>();
    }

    protected EntitiesChunkBase() { }

    public EntitiesChunkBase(CompoundTag data) {
        super(data);
    }

    public EntitiesChunkBase(CompoundTag data, long loadData) {
        super(data, loadData);
    }

    @Override
    protected void initReferences(long loadFlags) {
        if (getDataVersionEnum().hasEntitiesMca()) {
            int[] posXZ = data.getIntArray("Position");
            if (posXZ == null || posXZ.length != 2) {
                throw new IllegalArgumentException("Position tag missing or invalid");
            }
            chunkX = posXZ[0];
            chunkZ = posXZ[1];
        } else {
            // probably reading a "region" chunk as "entities"
            // TODO: extract chunk position information
        }
        if ((loadFlags & LoadFlags.ENTITIES) > 0) {
            ListTag<CompoundTag> entitiesTag = data.getListTag("Entities").asCompoundTagList();
            if (entitiesTag == null) {
                throw new IllegalArgumentException("Entities tag not found");
            }
            initEntities(entitiesTag, loadFlags);
        }
    }

    /**
     * Passed the entities tag from wherever it had to be found in the data tag.
     * @param entitiesTag not null
     */
    @SuppressWarnings("unchecked")
    protected void initEntities(ListTag<CompoundTag> entitiesTag, long loadFlags) {
        if (chunkX != NO_CHUNK_COORD_SENTINEL && chunkZ != NO_CHUNK_COORD_SENTINEL) {
            ChunkBoundingRectangle cbr = new ChunkBoundingRectangle(chunkX, chunkZ);
            for (CompoundTag entityTag : entitiesTag) {
                ET entity = (ET) EntityFactory.create(entityTag, dataVersion);
                if (!cbr.contains(entity.getX(), entity.getZ())) {
                    // TODO: consider reporting a warning... somehow... OR add a LoadFlag to control fix/fail/drop
                    entity.setX(cbr.relocateX(entity.getX()));
                    entity.setZ(cbr.relocateZ(entity.getZ()));
                }
                entities.add(entity);
            }
        } else {
            for (CompoundTag entityTag : entitiesTag) {
                entities.add((ET) EntityFactory.create(entityTag, dataVersion));
            }
        }
    }

    public List<ET> getEntities() {
        return entities;
    }

    public void setEntities(List<ET> entities) {
        ArgValidator.requireValue(entities);
        this.entities = entities;
    }

    // TODO: implement move chunk logic
    @Override
    public boolean moveChunkImplemented() {
        return false;
    }

    @Override
    public boolean moveChunkHasFullVersionSupport() {
        return false;
    }

    @Override
    public boolean moveChunk(int newChunkX, int newChunkZ, boolean force) {
        throw new UnsupportedOperationException();
    }


    @Override
    public Iterator<ET> iterator() {
        return entities.iterator();
    }
    @Override
    public void forEach(Consumer<? super ET> action) {
        entities.forEach(action);
    }

    @Override
    public Spliterator<ET> spliterator() {
        return entities.spliterator();
    }

    public Stream<ET> stream() {
        return entities.stream();
    }

    @Override
    public CompoundTag updateHandle() {
        if (raw) {
            return data;
        }
        super.updateHandle();
        if (chunkX != NO_CHUNK_COORD_SENTINEL && chunkZ != NO_CHUNK_COORD_SENTINEL) {
            // TODO: version support
            data.putIntArray("Position", new int[]{chunkX, chunkZ});
        }
        ListTag<CompoundTag> entitiesTag = new ListTag<>(CompoundTag.class, entities.size());
        data.put("Entities", entitiesTag);
        for (ET entity : entities) {
            entitiesTag.add(entity.updateHandle());
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
