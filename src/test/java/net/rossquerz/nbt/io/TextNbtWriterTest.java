package net.rossquerz.nbt.io;

import net.rossquerz.NbtTestCase;
import net.rossquerz.nbt.tag.*;

import java.util.LinkedHashMap;

public class TextNbtWriterTest extends NbtTestCase {

	public void testWrite() {

		// write number tags

		assertEquals("127b", assertThrowsNoException(() -> TextNbtHelpers.toTextNbt(new ByteTag(Byte.MAX_VALUE))));
		assertEquals("-32768s", assertThrowsNoException(() -> TextNbtHelpers.toTextNbt(new ShortTag(Short.MIN_VALUE))));
		assertEquals("-2147483648", assertThrowsNoException(() -> TextNbtHelpers.toTextNbt(new IntTag(Integer.MIN_VALUE))));
		assertEquals("-9223372036854775808l", assertThrowsNoException(() -> TextNbtHelpers.toTextNbt(new LongTag(Long.MIN_VALUE))));
		assertEquals("123.456f", assertThrowsNoException(() -> TextNbtHelpers.toTextNbt(new FloatTag(123.456F))));
		assertEquals("123.456d", assertThrowsNoException(() -> TextNbtHelpers.toTextNbt(new DoubleTag(123.456D))));

		// write array tags

		assertEquals("[B;-128,0,127]", assertThrowsNoException(() -> TextNbtHelpers.toTextNbt(new ByteArrayTag(new byte[]{Byte.MIN_VALUE, 0, Byte.MAX_VALUE}), false)));
		assertEquals("[I;-2147483648,0,2147483647]", assertThrowsNoException(() -> TextNbtHelpers.toTextNbt(new IntArrayTag(new int[]{Integer.MIN_VALUE, 0, Integer.MAX_VALUE}), false)));
		assertEquals("[L;-9223372036854775808,0,9223372036854775807]", assertThrowsNoException(() -> TextNbtHelpers.toTextNbt(new LongArrayTag(new long[]{Long.MIN_VALUE, 0, Long.MAX_VALUE}), false)));

		// write string tag

		assertEquals("abc", assertThrowsNoException(() -> TextNbtHelpers.toTextNbt(new StringTag("abc"))));
		assertEquals("\"123\"", assertThrowsNoException(() -> TextNbtHelpers.toTextNbt(new StringTag("123"))));
		assertEquals("\"123.456\"", assertThrowsNoException(() -> TextNbtHelpers.toTextNbt(new StringTag("123.456"))));
		assertEquals("\"-123\"", assertThrowsNoException(() -> TextNbtHelpers.toTextNbt(new StringTag("-123"))));
		assertEquals("\"-1.23e14\"", assertThrowsNoException(() -> TextNbtHelpers.toTextNbt(new StringTag("-1.23e14"))));
		assertEquals("\"äöü\\\\\"", assertThrowsNoException(() -> TextNbtHelpers.toTextNbt(new StringTag("äöü\\"))));

		// write list tag

		ListTag<StringTag> lt = new ListTag<>(StringTag.class);
		lt.addString("blah");
		lt.addString("blubb");
		lt.addString("123");
		assertEquals("[blah,blubb,\"123\"]", assertThrowsNoException(() -> TextNbtHelpers.toTextNbt(lt, false)));

		// write compound tag
		CompoundTag ct = new CompoundTag();
		invokeSetValue(ct, new LinkedHashMap<>());
		ct.putString("key", "value");
		ct.putByte("byte", Byte.MAX_VALUE);
		ct.putByteArray("array", new byte[]{Byte.MIN_VALUE, 0, Byte.MAX_VALUE});
		ListTag<StringTag> clt = new ListTag<>(StringTag.class);
		clt.addString("foo");
		clt.addString("bar");
		ct.put("list", clt);
		String ctExpected = "{key:value,byte:127b,array:[B;-128,0,127],list:[foo,bar]}";
		assertEquals(ctExpected, assertThrowsNoException(() -> TextNbtHelpers.toTextNbt(ct, false)));
	}
}
