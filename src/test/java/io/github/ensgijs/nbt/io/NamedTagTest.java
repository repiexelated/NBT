package io.github.ensgijs.nbt.io;

import io.github.ensgijs.nbt.tag.ByteTag;
import io.github.ensgijs.nbt.tag.ShortTag;
import io.github.ensgijs.nbt.NbtTestCase;

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
