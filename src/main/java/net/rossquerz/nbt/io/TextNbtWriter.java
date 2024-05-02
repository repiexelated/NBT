package net.rossquerz.nbt.io;

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
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * TextNbtWriter creates a text NBT String.
 */
public final class TextNbtWriter implements MaxDepthIO {

	private static final Pattern NON_QUOTE_PATTERN = Pattern.compile("^[a-zA-Z_.+\\-]+$");

	private Writer writer;

	private TextNbtWriter(Writer writer) {
		this.writer = writer;
	}

	public static void write(NamedTag tag, Writer writer, boolean sortCompoundTagEntries, int maxDepth) throws IOException {
		new TextNbtWriter(writer).writeAnything(tag, sortCompoundTagEntries, maxDepth);
	}

	public static void write(NamedTag tag, Writer writer, int maxDepth) throws IOException {
		new TextNbtWriter(writer).writeAnything(tag, false, maxDepth);
	}

	public static void write(NamedTag tag, Writer writer) throws IOException {
		write(tag, writer, Tag.DEFAULT_MAX_DEPTH);
	}

	public static void write(Tag<?> tag, Writer writer, int maxDepth) throws IOException {
		new TextNbtWriter(writer).writeAnything(tag, false, maxDepth);
	}

	public static void write(Tag<?> tag, Writer writer) throws IOException {
		write(tag, writer, Tag.DEFAULT_MAX_DEPTH);
	}

	private void writeAnything(NamedTag tag, boolean sortCompoundTagEntries, int maxDepth) throws IOException {
		// note to future self: if you're ever compelled not write an empty name be sure to
		// consider what that means for TextNbtParser#readTag(int)
		if (tag.getName() != null) {
			writer.write(tag.getEscapedName());
			writer.write(':');
		}
		writeAnything(tag.getTag(), sortCompoundTagEntries, maxDepth);
	}

	private void writeAnything(Tag<?> tag, boolean sortCompoundTagEntries, int maxDepth) throws IOException {
		switch (tag.getID()) {
		case EndTag.ID:
			//do nothing
			break;
		case ByteTag.ID:
			writer.append(Byte.toString(((ByteTag) tag).asByte())).write('b');
			break;
		case ShortTag.ID:
			writer.append(Short.toString(((ShortTag) tag).asShort())).write('s');
			break;
		case IntTag.ID:
			writer.write(Integer.toString(((IntTag) tag).asInt()));
			break;
		case LongTag.ID:
			writer.append(Long.toString(((LongTag) tag).asLong())).write('l');
			break;
		case FloatTag.ID:
			writer.append(Float.toString(((FloatTag) tag).asFloat())).write('f');
			break;
		case DoubleTag.ID:
			writer.append(Double.toString(((DoubleTag) tag).asDouble())).write('d');
			break;
		case ByteArrayTag.ID:
			writeArray(((ByteArrayTag) tag).getValue(), ((ByteArrayTag) tag).length(), "B");
			break;
		case StringTag.ID:
			writer.write(escapeString(((StringTag) tag).getValue()));
			break;
		case ListTag.ID:
			writer.write('[');
			for (int i = 0; i < ((ListTag<?>) tag).size(); i++) {
				writer.write(i == 0 ? "" : ",");
				writeAnything(((ListTag<?>) tag).get(i), sortCompoundTagEntries, decrementMaxDepth(maxDepth));
			}
			writer.write(']');
			break;
		case CompoundTag.ID:
			writer.write('{');
			boolean first = true;
			Iterator<NamedTag> iter;
			if (sortCompoundTagEntries) iter = ((CompoundTag) tag).stream().sorted(NamedTag::compare).iterator();
			else iter = ((CompoundTag) tag).iterator();
			while (iter.hasNext()) {
				NamedTag entry = iter.next();
				writer.write(first ? "" : ",");
				writer.append(escapeString(entry.getName())).write(':');
				writeAnything(entry.getTag(), sortCompoundTagEntries, decrementMaxDepth(maxDepth));
				first = false;
			}
			writer.write('}');
			break;
		case IntArrayTag.ID:
			writeArray(((IntArrayTag) tag).getValue(), ((IntArrayTag) tag).length(), "I");
			break;
		case LongArrayTag.ID:
			writeArray(((LongArrayTag) tag).getValue(), ((LongArrayTag) tag).length(), "L");
			break;
		default:
			throw new IOException("unknown tag with id \"" + tag.getID() + "\"");
		}
	}

	private void writeArray(Object array, int length, String prefix) throws IOException {
		writer.append('[').append(prefix).write(';');
		for (int i = 0; i < length; i++) {
			writer.append(i == 0 ? "" : ",").write(Array.get(array, i).toString());
		}
		writer.write(']');
	}

	public static String escapeString(String s) {
		if (!NON_QUOTE_PATTERN.matcher(s).matches()) {
			StringBuilder sb = new StringBuilder();
			sb.append('"');
			for (int i = 0; i < s.length(); i++) {
				char c = s.charAt(i);
				if (c == '\\' || c == '"') {
					sb.append('\\');
				}
				sb.append(c);
			}
			sb.append('"');
			return sb.toString();
		}
		return s;
	}
}
