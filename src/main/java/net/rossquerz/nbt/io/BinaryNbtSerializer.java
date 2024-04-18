package net.rossquerz.nbt.io;

import net.rossquerz.io.Serializer;
import net.rossquerz.nbt.tag.Tag;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public class BinaryNbtSerializer implements Serializer<NamedTag> {

	private boolean compressed, littleEndian;

	public BinaryNbtSerializer() {
		this(true);
	}

	public BinaryNbtSerializer(boolean compressed) {
		this.compressed = compressed;
	}

	public BinaryNbtSerializer(boolean compressed, boolean littleEndian) {
		this.compressed = compressed;
		this.littleEndian = littleEndian;
	}

	@Override
	public void toStream(NamedTag object, OutputStream out) throws IOException {
		NbtOutput nbtOut;
		OutputStream output;
		if (compressed) {
			output = new GZIPOutputStream(out, true);
		} else {
			output = out;
		}

		if (littleEndian) {
			nbtOut = new LittleEndianNbtOutputStream(output);
		} else {
			nbtOut = new BinaryNbtOutputStream(output);
		}
		nbtOut.writeTag(object, Tag.DEFAULT_MAX_DEPTH);
		nbtOut.flush();
	}
}
