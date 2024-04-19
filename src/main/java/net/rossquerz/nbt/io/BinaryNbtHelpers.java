package net.rossquerz.nbt.io;

import net.rossquerz.nbt.tag.Tag;

import java.io.*;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

/** Utilities for reading and writing {@link Tag}'s to and from binary NBT data. */
public final class BinaryNbtHelpers {
	private BinaryNbtHelpers() {}

	// <editor-fold desc="Big Endian read/write (MC Java)">
	public static Path write(NamedTag tag, File file, boolean compressed) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(file)) {
			new BinaryNbtSerializer(compressed).toStream(tag, fos);
		}
		return file.toPath();
	}

	public static Path write(NamedTag tag, String file, boolean compressed) throws IOException {
		return write(tag, new File(file), compressed);
	}

	public static Path write(NamedTag tag, Path path, boolean compressed) throws IOException {
		return write(tag, path.toFile(), compressed);
	}

	public static Path write(NamedTag tag, File file) throws IOException {
		return write(tag, file, true);
	}

	public static Path write(NamedTag tag, String file) throws IOException {
		return write(tag, new File(file), true);
	}

	public static Path write(NamedTag tag, Path path) throws IOException {
		return write(tag, path.toFile(), true);
	}

	public static Path write(Tag<?> tag, File file, boolean compressed) throws IOException {
		return write(new NamedTag(null, tag), file, compressed);
	}

	public static Path write(Tag<?> tag, String file, boolean compressed) throws IOException {
		return write(new NamedTag(null, tag), new File(file), compressed);
	}

	public static Path write(Tag<?> tag, Path path, boolean compressed) throws IOException {
		return write(new NamedTag(null, tag), path.toFile(), compressed);
	}

	public static Path write(Tag<?> tag, File file) throws IOException {
		return write(new NamedTag(null, tag), file, true);
	}

	public static Path write(Tag<?> tag, String file) throws IOException {
		return write(new NamedTag(null, tag), new File(file), true);
	}

	public static Path write(Tag<?> tag, Path path) throws IOException {
		return write(new NamedTag(null, tag), path.toFile(), true);
	}

	public static NamedTag read(File file, boolean compressed) throws IOException {
		try (FileInputStream fis = new FileInputStream(file)) {
			return new BinaryNbtDeserializer(compressed).fromStream(fis);
		}
	}

	public static NamedTag read(String file, boolean compressed) throws IOException {
		return read(new File(file), compressed);
	}

	public static NamedTag read(Path path, boolean compressed) throws IOException {
		return read(path.toFile(), compressed);
	}

	public static NamedTag read(File file) throws IOException {
		try (FileInputStream fis = new FileInputStream(file)) {
			return new BinaryNbtDeserializer(false).fromStream(detectDecompression(fis));
		}
	}

	public static NamedTag read(String file) throws IOException {
		return read(new File(file));
	}

	public static NamedTag read(Path path) throws IOException {
		return read(path.toFile());
	}
	// </editor-fold>

	// <editor-fold desc="Little Endian read/write (MC Bedrock)">
	public static Path writeLittleEndian(NamedTag tag, File file, boolean compressed) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(file)) {
			new BinaryNbtSerializer(compressed, true).toStream(tag, fos);
		}
		return file.toPath();
	}

	public static Path writeLittleEndian(NamedTag tag, String file, boolean compressed) throws IOException {
		return writeLittleEndian(tag, new File(file), compressed);
	}

	public static Path writeLittleEndian(NamedTag tag, Path path, boolean compressed) throws IOException {
		return writeLittleEndian(tag, path.toFile(), compressed);
	}

	public static Path writeLittleEndian(NamedTag tag, File file) throws IOException {
		return writeLittleEndian(tag, file, true);
	}

	public static Path writeLittleEndian(NamedTag tag, String file) throws IOException {
		return writeLittleEndian(tag, new File(file), true);
	}

	public static Path writeLittleEndian(NamedTag tag, Path path) throws IOException {
		return writeLittleEndian(tag, path.toFile(), true);
	}

	public static Path writeLittleEndian(Tag<?> tag, File file, boolean compressed) throws IOException {
		return writeLittleEndian(new NamedTag(null, tag), file, compressed);
	}

	public static Path writeLittleEndian(Tag<?> tag, String file, boolean compressed) throws IOException {
		return writeLittleEndian(new NamedTag(null, tag), new File(file), compressed);
	}

	public static Path writeLittleEndian(Tag<?> tag, Path path, boolean compressed) throws IOException {
		return writeLittleEndian(new NamedTag(null, tag), path.toFile(), compressed);
	}

	public static Path writeLittleEndian(Tag<?> tag, File file) throws IOException {
		return writeLittleEndian(new NamedTag(null, tag), file, true);
	}

	public static Path writeLittleEndian(Tag<?> tag, String file) throws IOException {
		return writeLittleEndian(new NamedTag(null, tag), new File(file), true);
	}

	public static Path writeLittleEndian(Tag<?> tag, Path path) throws IOException {
		return writeLittleEndian(new NamedTag(null, tag), path.toFile(), true);
	}

	public static NamedTag readLittleEndian(File file, boolean compressed) throws IOException {
		try (FileInputStream fis = new FileInputStream(file)) {
			return new BinaryNbtDeserializer(compressed, true).fromStream(fis);
		}
	}

	public static NamedTag readLittleEndian(String file, boolean compressed) throws IOException {
		return readLittleEndian(new File(file), compressed);
	}

	public static NamedTag readLittleEndian(Path path, boolean compressed) throws IOException {
		return readLittleEndian(path.toFile(), compressed);
	}

	public static NamedTag readLittleEndian(File file) throws IOException {
		try (FileInputStream fis = new FileInputStream(file)) {
			return new BinaryNbtDeserializer(false, true).fromStream(detectDecompression(fis));
		}
	}

	public static NamedTag readLittleEndian(String file) throws IOException {
		return readLittleEndian(new File(file));
	}

	public static NamedTag readLittleEndian(Path path) throws IOException {
		return readLittleEndian(path.toFile());
	}
	// </editor-fold>
	
	static InputStream detectDecompression(InputStream is) throws IOException {
		PushbackInputStream pbis = new PushbackInputStream(is, 2);
		int b0 = pbis.read();
		int b1 = pbis.read();
		int signature = (b0 & 0xFF) | (b1 << 8);
		if (b1 >= 0) pbis.unread(b1);
		if (b0 >= 0) pbis.unread(b0);
		if (signature == GZIPInputStream.GZIP_MAGIC) {
			return new GZIPInputStream(pbis);
		}
		return pbis;
	}
}
