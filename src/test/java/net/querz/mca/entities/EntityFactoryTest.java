package net.querz.mca.entities;

import net.querz.mca.DataVersion;
import net.querz.mca.MCATestCase;
import net.querz.nbt.tag.CompoundTag;

import java.util.List;

public class EntityFactoryTest extends MCATestCase {

    // <editor-fold desc="Test Stubs" defaultstate="collapsed">
    private static class EntityStub extends EntityBaseImpl {
        final String givenNormalizedId;
        final EntityCreatorStub creator;

        public EntityStub(EntityCreatorStub creator, String givenNormalizedId, CompoundTag data, int dataVersion) {
            super(dataVersion);
            this.creator = creator;
            this.givenNormalizedId = givenNormalizedId;
            this.data = data;
            this.dataVersion = dataVersion;
        }
    }

    private static class EntityCreatorStub implements EntityCreator<EntityStub> {
        final String name;
        boolean returnNull;
        RuntimeException throwMe;
        public EntityCreatorStub(String name) {
            this.name = name;
        }

        @Override
        public EntityStub create(String normalizedId, CompoundTag tag, int dataVersion) {
            if (throwMe != null) throw throwMe;
            return returnNull ? null : new EntityStub(this, normalizedId, tag, dataVersion);
        }

        @Override
        public String toString() {
            return name;
        }
    }
    // </editor-fold>

    final EntityCreatorStub defaultedCreator = new EntityCreatorStub("TEST DEFAULT CREATOR");

    // TODO: IDK why @Before isn't working, resolve that and run this as @Before someday
    public void reset() {
        // Test should not rely on defaults put in place for user convenience!
        EntityFactory.clearCreators();
        EntityFactory.clearEntityIdRemap();
        EntityFactory.setDefaultEntityCreator(defaultedCreator);
//        System.out.println("RUNNING BEFORE");
    }

    public void testNormalizeId() {
        reset();
        assertThrowsIllegalArgumentException(() -> EntityFactory.normalizeId(null));
        assertThrowsNoException(() -> EntityFactory.normalizeId("not_null"));
        assertThrowsIllegalArgumentException(() -> EntityFactory.normalizeId("minecraft:"));

        assertEquals("PIG_ZIG", EntityFactory.normalizeId("minecraft:Pig_zig"));
        assertEquals("PIGZIG", EntityFactory.normalizeId("minecraft:PigZig"));
    }

    public void testNormalizeAndRemapId() {
        reset();
        assertThrowsIllegalArgumentException(() -> EntityFactory.normalizeAndRemapId(null));
        assertThrowsNoException(() -> EntityFactory.normalizeAndRemapId("not_null"));
        assertThrowsIllegalArgumentException(() -> EntityFactory.normalizeAndRemapId("minecraft:"));
        // not mapped
        assertEquals("PIGZIG", EntityFactory.normalizeAndRemapId("minecraft:PigZig"));
        // remap
        EntityFactory.registerIdRemap("pigzig", "pig_zig");
        assertEquals("PIG_ZIG", EntityFactory.normalizeAndRemapId("minecraft:PigZig"));
    }

    public void testDefaultCreatorNotNull() {
        reset();
        assertNotNull(EntityFactory.getDefaultEntityCreator());
    }

    public void testSetDefaultCreator() {
        reset();
        assertNotNull(EntityFactory.getDefaultEntityCreator());
        assertThrowsIllegalArgumentException(() -> EntityFactory.setDefaultEntityCreator(null));
        EntityCreator<?> ec = new EntityCreatorStub(getName());
        EntityFactory.setDefaultEntityCreator(ec);
        assertSame(ec, EntityFactory.getDefaultEntityCreator());
    }

    public void testRegisterCreator_basic() {
        reset();
        EntityCreator<?> ec = new EntityCreatorStub(getName());
        assertNull(EntityFactory.getCreatorById("FOO"));
        EntityFactory.registerCreator(ec, "foo");
        assertSame(ec, EntityFactory.getCreatorById("FOO"));
        assertNull(EntityFactory.getCreatorById("BAR"));
    }

    public void testRegisterCreator_multiple() {
        reset();
        EntityCreator<?> ec = new EntityCreatorStub(getName());
        EntityFactory.registerCreator(ec, "foo", "minecraft:bar");
        assertSame(ec, EntityFactory.getCreatorById("FOO"));
        assertSame(ec, EntityFactory.getCreatorById("BAR"));
        assertNull(EntityFactory.getCreatorById("BAZ"));
    }

    public void testRegisterCreator_throwsOnNullId() {
        reset();
        EntityCreator<?> ec = new EntityCreatorStub(getName());
        assertThrowsIllegalArgumentException(() -> EntityFactory.registerCreator(ec, (String) null));
        assertThrowsIllegalArgumentException(() -> EntityFactory.registerCreator(ec, "foo", null, "minecraft:bar"));
    }

    public void testRegisterCreator_afterRegisteringRemapping() {
        reset();
        EntityCreator<?> fooEc = new EntityCreatorStub(getName());
        EntityFactory.registerIdRemap("bar", "foo");
        EntityFactory.registerIdRemap("baz", "foo");
        EntityFactory.registerCreator(fooEc, "foo");
        assertSame(fooEc, EntityFactory.getCreatorById("FOO"));
        assertSame(fooEc, EntityFactory.getCreatorById("BAR"));
        assertSame(fooEc, EntityFactory.getCreatorById("BAZ"));
        assertNull(EntityFactory.getCreatorById("ZAP"));
    }

    public void testRegisteringRemapping_afterRegisterCreator() {
        reset();
        EntityCreator<?> fooEc = new EntityCreatorStub(getName());
        EntityFactory.registerCreator(fooEc, "foo");
        EntityFactory.registerIdRemap("bar", "foo");
        EntityFactory.registerIdRemap("baz", "foo");
        assertSame(fooEc, EntityFactory.getCreatorById("FOO"));
        assertSame(fooEc, EntityFactory.getCreatorById("BAR"));
        assertSame(fooEc, EntityFactory.getCreatorById("BAZ"));
        assertNull(EntityFactory.getCreatorById("ZAP"));
    }

    public void testReverseIdRemap() {
        reset();
        List<String> rev = EntityFactory.reverseIdRemap("foo");
        assertNotNull(rev);
        assertTrue(rev.isEmpty());

        EntityFactory.registerIdRemap("bar", "foo");

        // this isn't a reverse lookup, it would be forward
        rev = EntityFactory.reverseIdRemap("bar");
        assertNotNull(rev);
        assertTrue(rev.isEmpty());

        // one reverse match
        rev = EntityFactory.reverseIdRemap("foo");
        assertNotNull(rev);
        assertEquals(1, rev.size());


        EntityFactory.registerIdRemap("oof", "foo");
        rev = EntityFactory.reverseIdRemap("foo");
        assertNotNull(rev);
        assertEquals(2, rev.size());
    }

    public void testGetRegisteredCreatorIdKeys() {
        reset();
        assertTrue(EntityFactory.getRegisteredCreatorIdKeys().isEmpty());
        EntityFactory.registerCreator(new EntityCreatorStub("A"), "FOO", "BAR");
        EntityFactory.registerCreator(new EntityCreatorStub("B"), "ZOO");
        assertEquals(3, EntityFactory.getRegisteredCreatorIdKeys().size());
        assertTrue(EntityFactory.getRegisteredCreatorIdKeys().contains("FOO"));
        assertTrue(EntityFactory.getRegisteredCreatorIdKeys().contains("BAR"));
        assertTrue(EntityFactory.getRegisteredCreatorIdKeys().contains("ZOO"));
    }

    public void testCreate() {
        reset();
        CompoundTag tag = new CompoundTag();
        tag.putString("id", "whatever");
        EntityStub entityStub = EntityFactory.createAutoCast(tag, DataVersion.latest().id());
        assertNotNull(entityStub);
        assertSame(defaultedCreator, entityStub.creator);
        assertEquals("WHATEVER", entityStub.givenNormalizedId);
        assertSame(tag, entityStub.getHandle());

        EntityCreatorStub ec = new EntityCreatorStub(getName());
        EntityFactory.registerIdRemap("Muggle", "non_wizard");
        EntityFactory.registerCreator(ec, "non_wizard");

        // check that the default is still used
        entityStub = EntityFactory.createAutoCast(tag, DataVersion.latest().id());
        assertNotNull(entityStub);
        assertSame(defaultedCreator, entityStub.creator);
        assertEquals("WHATEVER", entityStub.givenNormalizedId);
        assertSame(tag, entityStub.getHandle());

        // check behavior with use of preferred name
        tag = new CompoundTag();
        tag.putString("id", "non_wizard");
        entityStub = EntityFactory.createAutoCast(tag, DataVersion.latest().id());
        assertNotNull(entityStub);
        assertSame(ec, entityStub.creator);
        assertEquals("NON_WIZARD", entityStub.givenNormalizedId);
        assertSame(tag, entityStub.getHandle());

        // check behavior with use of legacy name
        tag = new CompoundTag();
        tag.putString("id", "Muggle");
        entityStub = EntityFactory.createAutoCast(tag, DataVersion.latest().id());
        assertNotNull(entityStub);
        assertSame(ec, entityStub.creator);
        assertEquals("NON_WIZARD", entityStub.givenNormalizedId);
        assertSame(tag, entityStub.getHandle());
    }

    public void testCreate_throwsIllegalEntityTagException_whenCreatorReturnsNull() {
        reset();
        CompoundTag tag = new CompoundTag();
        tag.putString("id", "whatever");
        defaultedCreator.returnNull = true;
        assertThrowsException(() -> EntityFactory.createAutoCast(tag, DataVersion.latest().id()), IllegalEntityTagException.class);
    }
}
