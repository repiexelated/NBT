package net.rossquerz.nbt.io;

import net.rossquerz.io.ExceptionTriConsumer;
import net.rossquerz.io.MaxDepthIO;
import net.rossquerz.nbt.tag.ByteArrayTag;
import net.rossquerz.nbt.tag.ByteTag;
import net.rossquerz.nbt.tag.CompoundTag;
import net.rossquerz.nbt.tag.DoubleTag;
import net.rossquerz.nbt.tag.EndTag;
import net.rossquerz.nbt.tag.FloatTag;
import net.rossquerz.nbt.tag.IntArrayTag;
import net.rossquerz.nbt.tag.IntTag;
import net.rossquerz.nbt.tag.ListTag;
import net.rossquerz.nbt.tag.LongArrayTag;
import net.rossquerz.nbt.tag.LongTag;
import net.rossquerz.nbt.tag.ShortTag;
import net.rossquerz.nbt.tag.StringTag;
import net.rossquerz.nbt.tag.Tag;
import java.io.Closeable;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/** Use for Minecraft Bedrock edition data. */
public class LittleEndianNbtOutputStream implements DataOutput, NbtOutput, MaxDepthIO, Closeable {

	private final DataOutputStream output;

	private static Map<Byte, ExceptionTriConsumer<LittleEndianNbtOutputStream, Tag<?>, Integer, IOException>> writers = new HashMap<>();
	private static Map<Class<?>, Byte> classIdMapping = new HashMap<>();

	static {
		put(EndTag.ID, (o, t, d) -> {}, EndTag.class);
		put(ByteTag.ID, (o, t, d) -> writeByte(o, t), ByteTag.class);
		put(ShortTag.ID, (o, t, d) -> writeShort(o, t), ShortTag.class);
		put(IntTag.ID, (o, t, d) -> writeInt(o, t), IntTag.class);
		put(LongTag.ID, (o, t, d) -> writeLong(o, t), LongTag.class);
		put(FloatTag.ID, (o, t, d) -> writeFloat(o, t), FloatTag.class);
		put(DoubleTag.ID, (o, t, d) -> writeDouble(o, t), DoubleTag.class);
		put(ByteArrayTag.ID, (o, t, d) -> writeByteArray(o, t), ByteArrayTag.class);
		put(StringTag.ID, (o, t, d) -> writeString(o, t), StringTag.class);
		put(ListTag.ID, LittleEndianNbtOutputStream::writeList, ListTag.class);
		put(CompoundTag.ID, LittleEndianNbtOutputStream::writeCompound, CompoundTag.class);
		put(IntArrayTag.ID, (o, t, d) -> writeIntArray(o, t), IntArrayTag.class);
		put(LongArrayTag.ID, (o, t, d) -> writeLongArray(o, t), LongArrayTag.class);
	}

	private static void put(byte id, ExceptionTriConsumer<LittleEndianNbtOutputStream, Tag<?>, Integer, IOException> f, Class<?> clazz) {
		writers.put(id, f);
		classIdMapping.put(clazz, id);
	}

	public LittleEndianNbtOutputStream(OutputStream out) {
		output = new DataOutputStream(out);
	}

	public LittleEndianNbtOutputStream(DataOutputStream out) {
		output = out;
	}

	public void writeTag(NamedTag tag, int maxDepth) throws IOException {
		writeByte(tag.getTag().getID());
		if (tag.getTag().getID() != 0) {
			writeUTF(tag.getName() == null ? "" : tag.getName());
		}
		writeRawTag(tag.getTag(), maxDepth);
	}

	public void writeTag(Tag<?> tag, int maxDepth) throws IOException {
		writeByte(tag.getID());
		if (tag.getID() != 0) {
			writeUTF("");
		}
		writeRawTag(tag, maxDepth);
	}

	public void writeRawTag(Tag<?> tag, int maxDepth) throws IOException {
		ExceptionTriConsumer<LittleEndianNbtOutputStream, Tag<?>, Integer, IOException> f;
		if ((f = writers.get(tag.getID())) == null) {
			throw new IOException("invalid tag \"" + tag.getID() + "\"");
		}
		f.accept(this, tag, maxDepth);
	}

	static byte idFromClass(Class<?> clazz) {
		Byte id = classIdMapping.get(clazz);
		if (id == null) {
			throw new IllegalArgumentException("unknown Tag class " + clazz.getName());
		}
		return id;
	}

	private static void writeByte(LittleEndianNbtOutputStream out, Tag<?> tag) throws IOException {
		out.writeByte(((ByteTag) tag).asByte());
	}
	
	private static void writeShort(LittleEndianNbtOutputStream out, Tag<?> tag) throws IOException {
		out.writeShort(((ShortTag) tag).asShort());
	}
	
	private static void writeInt(LittleEndianNbtOutputStream out, Tag<?> tag) throws IOException {
		out.writeInt(((IntTag) tag).asInt());
	}

	private static void writeLong(LittleEndianNbtOutputStream out, Tag<?> tag) throws IOException {
		out.writeLong(((LongTag) tag).asLong());
	}

	private static void writeFloat(LittleEndianNbtOutputStream out, Tag<?> tag) throws IOException {
		out.writeFloat(((FloatTag) tag).asFloat());
	}

	private static void writeDouble(LittleEndianNbtOutputStream out, Tag<?> tag) throws IOException {
		out.writeDouble(((DoubleTag) tag).asDouble());
	}

	private static void writeString(LittleEndianNbtOutputStream out, Tag<?> tag) throws IOException {
		out.writeUTF(((StringTag) tag).getValue());
	}

	private static void writeByteArray(LittleEndianNbtOutputStream out, Tag<?> tag) throws IOException {
		out.writeInt(((ByteArrayTag) tag).length());
		out.write(((ByteArrayTag) tag).getValue());
	}

	private static void writeIntArray(LittleEndianNbtOutputStream out, Tag<?> tag) throws IOException {
		out.writeInt(((IntArrayTag) tag).length());
		for (int i : ((IntArrayTag) tag).getValue()) {
			out.writeInt(i);
		}
	}

	private static void writeLongArray(LittleEndianNbtOutputStream out, Tag<?> tag) throws IOException {
		out.writeInt(((LongArrayTag) tag).length());
		for (long l : ((LongArrayTag) tag).getValue()) {
			out.writeLong(l);
		}
	}

	private static void writeList(LittleEndianNbtOutputStream out, Tag<?> tag, int maxDepth) throws IOException {
		out.writeByte(idFromClass(((ListTag<?>) tag).getTypeClass()));
		out.writeInt(((ListTag<?>) tag).size());
		for (Tag<?> t : ((ListTag<?>) tag)) {
			out.writeRawTag(t, out.decrementMaxDepth(maxDepth));
		}
	}

	private static void writeCompound(LittleEndianNbtOutputStream out, Tag<?> tag, int maxDepth) throws IOException {
		for (NamedTag entry : (CompoundTag) tag) {
			if (entry.getTag().getID() == 0) {
				throw new IOException("end tag not allowed");
			}
			out.writeByte(entry.getTag().getID());
			out.writeUTF(entry.getName());
			out.writeRawTag(entry.getTag(), out.decrementMaxDepth(maxDepth));
		}
		out.writeByte(0);
	}

	@Override
	public void close() throws IOException {
		output.close();
	}

	@Override
	public void flush() throws IOException {
		output.flush();
	}

	@Override
	public void write(int b) throws IOException {
		output.write(b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		output.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		output.write(b, off, len);
	}

	@Override
	public void writeBoolean(boolean v) throws IOException {
		output.writeBoolean(v);
	}

	@Override
	public void writeByte(int v) throws IOException {
		output.writeByte(v);
	}

	@Override
	public void writeShort(int v) throws IOException {
		output.writeShort(Short.reverseBytes((short) v));
	}

	@Override
	public void writeChar(int v) throws IOException {
		output.writeChar(Character.reverseBytes((char) v));
	}

	@Override
	public void writeInt(int v) throws IOException {
		output.writeInt(Integer.reverseBytes(v));
	}

	@Override
	public void writeLong(long v) throws IOException {
		output.writeLong(Long.reverseBytes(v));
	}

	@Override
	public void writeFloat(float v) throws IOException {
		output.writeInt(Integer.reverseBytes(Float.floatToIntBits(v)));
	}

	@Override
	public void writeDouble(double v) throws IOException {
		output.writeLong(Long.reverseBytes(Double.doubleToLongBits(v)));
	}

	@Override
	public void writeBytes(String s) throws IOException {
		output.writeBytes(s);
	}

	@Override
	public void writeChars(String s) throws IOException {
		output.writeChars(s);
	}

	@Override
	public void writeUTF(String s) throws IOException {
		byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
		writeShort(bytes.length);
		write(bytes);
	}
}
