package net.rossquerz.nbt.io;

import net.rossquerz.NbtTestCase;
import net.rossquerz.nbt.tag.ByteTag;
import net.rossquerz.nbt.tag.ShortTag;

public class NamedTagTest extends NbtTestCase {

	public void testCreate() {
		ByteTag t = new ByteTag();
		NamedTag n = new NamedTag("name", t);
		assertEquals("name", n.getName());
		assertTrue(n.getTag() == t);
	}

	public void testSet() {
		ByteTag t = new ByteTag();
		NamedTag n = new NamedTag("name", t);
		n.setName("blah");
		assertEquals("blah", n.getName());
		ShortTag s = new ShortTag();
		n.setTag(s);
		assertTrue(n.getTag() == s);
	}
}
