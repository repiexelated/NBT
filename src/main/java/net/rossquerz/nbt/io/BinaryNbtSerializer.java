package net.rossquerz.nbt.io;

import net.rossquerz.io.Serializer;
import net.rossquerz.nbt.tag.Tag;
import java.io.IOException;
import java.io.OutputStream;

public class BinaryNbtSerializer implements Serializer<NamedTag> {
	private CompressionType compression;
	private boolean littleEndian;

	public BinaryNbtSerializer(CompressionType compression) {
		this(compression, false);
	}

	public BinaryNbtSerializer(CompressionType compression, boolean littleEndian) {
		this.compression = compression;
		this.littleEndian = littleEndian;
	}

	@Override
	public void toStream(NamedTag object, OutputStream out) throws IOException {
		NbtOutput nbtOut;
		OutputStream output = compression.compress(out);
		if (!littleEndian) {
			nbtOut = new BigEndianNbtOutputStream(output);
		} else {
			nbtOut = new LittleEndianNbtOutputStream(output);
		}
		nbtOut.writeTag(object, Tag.DEFAULT_MAX_DEPTH);
		compression.finish(output);
		nbtOut.flush();
	}
}
