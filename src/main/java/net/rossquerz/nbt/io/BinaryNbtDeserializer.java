package net.rossquerz.nbt.io;

import net.rossquerz.io.Deserializer;
import net.rossquerz.nbt.tag.Tag;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class BinaryNbtDeserializer implements Deserializer<NamedTag> {

	private boolean compressed, littleEndian;

	public BinaryNbtDeserializer() {
		this(true);
	}

	public BinaryNbtDeserializer(boolean compressed) {
		this.compressed = compressed;
	}

	public BinaryNbtDeserializer(boolean compressed, boolean littleEndian) {
		this.compressed = compressed;
		this.littleEndian = littleEndian;
	}

	@Override
	public NamedTag fromStream(InputStream stream) throws IOException {
		NbtInput nbtIn;
		InputStream input;
		if (compressed) {
			input = new GZIPInputStream(stream);
		} else {
			input = stream;
		}

		if (littleEndian) {
			nbtIn = new LittleEndianNbtInputStream(input);
		} else {
			nbtIn = new BinaryNbtInputStream(input);
		}
		return nbtIn.readTag(Tag.DEFAULT_MAX_DEPTH);
	}
}
