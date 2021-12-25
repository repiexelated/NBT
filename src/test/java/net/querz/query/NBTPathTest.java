package net.querz.query;

import net.querz.NBTTestCase;
import net.querz.nbt.query.NBTPath;
import net.querz.nbt.tag.*;

public class NBTPathTest extends NBTTestCase {

    private void assertParse(String path) {
        assertEquals(path, NBTPath.of(path).toString());
    }

    private void assertParse(String expected, String path) {
        assertEquals(expected, NBTPath.of(path).toString());
    }

    private void assertParseThrows(String path) {
        assertThrowsIllegalArgumentException(() -> NBTPath.of(path));
    }

    public void testParsingAndToString() {
        assertParse("a");
        assertParse("a.b");
        assertParse("a[0].b");
        assertParse("a.b[7]");
        assertParse("a[0].b[2].c");
        assertParse("[42]");
        assertParse("[42].thing");
        assertParse("[42][19].thing.whatever.z[0].a.b");
        assertParse("4.baz");  // numbers can be names too

        assertParse("");
        // a leading dot is OK
        assertParse("", ".");
        assertParse("foo", ".foo");
        // but two leading dots aren't
        assertParseThrows("..");
        assertParseThrows("..b");
        assertParseThrows("..bob");

        assertParse("[1]");
        assertParseThrows(".[0]");
        assertParseThrows("a..b");
        assertParseThrows("foo.");
        assertParseThrows("foo.bar.");
        assertParseThrows("foo.bar.[0]");

        assertParseThrows("a[]");
        assertParseThrows("[]");
        assertParseThrows("[z]");
        assertParseThrows("[zap]");
        assertParseThrows("[-2]");
        assertParseThrows("[-]");
        assertParseThrows("a[0");
        assertParseThrows("a[0]b[1]");
        assertParseThrows("a]0");
        assertParseThrows("a]0[");
        assertParseThrows("a[0]]");
    }

    public void testUsageSanity() {
        CompoundTag root = new CompoundTag();
        ListTag<CompoundTag> teamTag = new ListTag<>(CompoundTag.class);

        CompoundTag tmp = new CompoundTag();
        tmp.putString("name", "Bob");
        tmp.putBoolean("boolT", true);
        tmp.putBoolean("boolF", false);
        tmp.putByte("byte", (byte) -42);
        tmp.putShort("short", (short) 12345);
        tmp.putInt("int", 1001);
        tmp.putLong("long", 99988765454785765L);
        tmp.putFloat("float", 0.35f);
        tmp.putDouble("double", 9.78d);
        tmp.putLongArray("history", new long[]{43L, 56L, 89L});
        teamTag.add(tmp);

        tmp = new CompoundTag();
        tmp.putString("name", "Jebbb");
        tmp.putLongArray("history", new long[]{12L, 11L, 15L});
        teamTag.add(tmp);

        root.put("team", teamTag);
        root.putInt("version", 1234);

        assertNull(NBTPath.of("xyz").get(root));  // doesnt exist
        assertNotNull(NBTPath.of("team").get(root));  // exists
        assertNull(NBTPath.of("team[6]").get(root));  // index doesn't exist

        assertSame(tmp, NBTPath.of("[1]").get(teamTag));  // can use a ListTag as root

        // value getters
        assertEquals("Bob", NBTPath.of("team[0].name").getString(root));
        assertTrue(NBTPath.of("team[0].boolT").getBoolean(root));
        assertFalse(NBTPath.of("team[0].boolF").getBoolean(root));
        assertEquals((byte) -42, NBTPath.of("team[0].byte").getByte(root));
        assertEquals((short) 12345, NBTPath.of("team[0].short").getShort(root));
        assertEquals(1001 , NBTPath.of("team[0].int").getInt(root));
        assertEquals(99988765454785765L , NBTPath.of("team[0].long").getLong(root));
        assertEquals(0.35f, NBTPath.of("team[0].float").getFloat(root), 1e-6);
        assertEquals(9.78d, NBTPath.of("team[0].double").getDouble(root), 1e-6);

        // missing value getters
        assertNull(NBTPath.of("team[0].whatever").getString(root));
        assertNull(NBTPath.of("team[0].whatever.something.else").getString(root));
        assertFalse(NBTPath.of("team[0].whatever").getBoolean(root));
        assertFalse(NBTPath.of("team[0].whatever.something.else").getBoolean(root));
        assertEquals((byte) 0, NBTPath.of("team[0].whatever").getByte(root));
        assertEquals((byte) 0, NBTPath.of("team[0].whatever.something.else").getByte(root));
        assertEquals((short) 0, NBTPath.of("team[0].whatever").getShort(root));
        assertEquals((short) 0, NBTPath.of("team[0].whatever.something.else").getShort(root));
        assertEquals(0, NBTPath.of("team[0].whatever").getInt(root));
        assertEquals(0, NBTPath.of("team[0].whatever.something.else").getInt(root));
        assertEquals(0L, NBTPath.of("team[0].whatever").getLong(root));
        assertEquals(0L, NBTPath.of("team[0].whatever.something.else").getLong(root));
        assertEquals(0f, NBTPath.of("team[0].whatever").getFloat(root));
        assertEquals(0f, NBTPath.of("team[0].whatever.something.else").getFloat(root));
        assertEquals(0d, NBTPath.of("team[0].whatever").getDouble(root));
        assertEquals(0d, NBTPath.of("team[0].whatever.something.else").getDouble(root));

        assertEquals(15L, NBTPath.of("team[1].history[2]").getLong(root));  // getting a raw long from a LongArray

        // get - throws when not the right type
        assertThrowsException(() -> NBTPath.of("team[0].long").getString(root), ClassCastException.class);
        assertThrowsException(() -> NBTPath.of("team[0].name").getByte(root), ClassCastException.class);
        assertThrowsException(() -> NBTPath.of("team[0].name").getShort(root), ClassCastException.class);
        assertThrowsException(() -> NBTPath.of("team[0].name").getInt(root), ClassCastException.class);
        assertThrowsException(() -> NBTPath.of("team[0].name").getLong(root), ClassCastException.class);
        assertThrowsException(() -> NBTPath.of("team[0].name").getFloat(root), ClassCastException.class);
        assertThrowsException(() -> NBTPath.of("team[0].name").getDouble(root), ClassCastException.class);


        // size function
        assertEquals(2, NBTPath.of("team").size(root));  // size of list tag
        assertEquals(3, NBTPath.of("team[1].history").size(root));  // size of long array
        assertEquals(0, NBTPath.of("meme").size(root));  // no-existing tag size
        assertEquals(5, NBTPath.of("team[1].name").size(root));  // string size
        assertEquals(2, NBTPath.of("").size(root));  // size of compound tag

        // can't get these sizes
        assertThrowsIllegalArgumentException(() -> NBTPath.of("team[0].byte").size(root));
        assertThrowsIllegalArgumentException(() -> NBTPath.of("team[0].short").size(root));
        assertThrowsIllegalArgumentException(() -> NBTPath.of("team[0].int").size(root));
        assertThrowsIllegalArgumentException(() -> NBTPath.of("team[0].long").size(root));
        assertThrowsIllegalArgumentException(() -> NBTPath.of("team[0].float").size(root));
        assertThrowsIllegalArgumentException(() -> NBTPath.of("team[0].double").size(root));

        // exists function
        assertTrue(NBTPath.of("team").exists(root));
        assertTrue(NBTPath.of("team[1].history").exists(root));
        assertFalse(NBTPath.of("meme").exists(root));
        assertTrue(NBTPath.of("").exists(root));
        assertTrue(NBTPath.of("team[1].history[0]").exists(root));
        assertFalse(NBTPath.of("team[1].history[99]").exists(root));

        assertFalse(NBTPath.of("team[1].abc").exists(root));
        assertFalse(NBTPath.of("team[100].abc").exists(root));
    }

    public void testPutTag() {
        // throws when create parents is FALSE
        assertThrowsIllegalArgumentException(() -> NBTPath.of("team.alpha").putTag(new CompoundTag(), new StringTag("STOP"), false));

        final CompoundTag root = new CompoundTag();
        // now try with create parents - and verify that no previous tag was replaced
        assertNull(NBTPath.of("team.alpha").putTag(root, new StringTag("GO GO"), true));
        assertEquals("GO GO", NBTPath.of("team.alpha").getString(root));
        // can't treat a compound as a list
        assertThrowsIllegalArgumentException(() -> NBTPath.of("team[0]").getTag(root));

        // check that the previous tag is returned - and the new value is set
        StringTag oldTag = NBTPath.of("team.alpha").putTag(root, new StringTag("Weeee"));
        assertNotNull(oldTag);
        assertEquals("GO GO", oldTag.getValue());
        assertEquals("Weeee", NBTPath.of("team.alpha").getString(root));

        // now test removal
        assertNull(NBTPath.of("team.beta").putTag(root, null));  // doesn't exist - should be FINE
        oldTag = NBTPath.of("team.alpha").putTag(root, null);
        assertNotNull(oldTag);
        assertEquals("Weeee", oldTag.getValue());
        assertNull(NBTPath.of("team.alpha").getTag(root));  // verify it's removed

        root.put("teams", new ListTag<>(CompoundTag.class));
        root.getListTag("teams").asCompoundTagList().add(new CompoundTag());
        // this works because we initialized list element [0] first
        NBTPath.of("teams[0].name").putTag(root, new StringTag("alpha"));
        assertEquals("alpha", NBTPath.of("teams[0].name").getString(root));

        // should create stats for us (not the first case - where we don't create parents)
        assertThrowsIllegalArgumentException(() -> NBTPath.of("teams[0].stats.wins").putTag(root, new IntTag(1)));
        NBTPath.of("teams[0].stats.wins").putTag(root, new IntTag(1), true);

        // list element [1] doesn't exist
        assertThrowsIllegalArgumentException(() -> NBTPath.of("teams[1].name").putTag(root, new StringTag("beta")));

        // cannot add tag list (at this time... i mean really, it doesn't make sense as the index would need to be
        // put in the path string - at that point trying to use this type of solution would just be bad practice)
        assertThrowsIllegalArgumentException(() -> NBTPath.of("zom.lom[0]").putTag(root, new CompoundTag()));

        // can't add a keyed field to a TagList (requires an index)
        assertThrowsUnsupportedOperationException(() -> NBTPath.of("teams.broken").putTag(root, new ByteTag(true)));
        assertThrowsUnsupportedOperationException(() -> NBTPath.of("teams.broken").putTag(root, new ByteTag(true), true));


        NBTPath.of("long").putTag(root, new LongTag(19));
        // can't get a child of a primitive tag
        assertThrowsIllegalArgumentException(() -> NBTPath.of("long.foo").getTag(root));
        // can't treat a primitive as a list
        assertThrowsIllegalArgumentException(() -> NBTPath.of("long[0]").getTag(root));

        // can't add a child to a primitive tag
        assertThrowsIllegalArgumentException(() -> NBTPath.of("long.foo.bar").putTag(root, new CompoundTag(), true));
    }
}
