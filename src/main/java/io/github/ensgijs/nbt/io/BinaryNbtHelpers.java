package io.github.ensgijs.nbt.io;

import io.github.ensgijs.nbt.tag.Tag;

import java.io.*;
import java.nio.file.Path;

/** Utilities for reading and writing {@link Tag}'s to and from binary NBT data. */
public final class BinaryNbtHelpers {
	private BinaryNbtHelpers() {}

	// <editor-fold desc="Big Endian read/write (MC Java)">
	public static Path write(NamedTag tag, File file, CompressionType compression) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(file)) {
			new BinaryNbtSerializer(compression).toStream(tag, fos);
		}
		return file.toPath();
	}

	public static Path write(NamedTag tag, String file, CompressionType compression) throws IOException {
		return write(tag, new File(file), compression);
	}

	public static Path write(NamedTag tag, Path path, CompressionType compression) throws IOException {
		return write(tag, path.toFile(), compression);
	}

	public static Path write(Tag<?> tag, File file, CompressionType compression) throws IOException {
		return write(new NamedTag(null, tag), file, compression);
	}

	public static Path write(Tag<?> tag, String file, CompressionType compression) throws IOException {
		return write(new NamedTag(null, tag), new File(file), compression);
	}

	public static Path write(Tag<?> tag, Path path, CompressionType compression) throws IOException {
		return write(new NamedTag(null, tag), path.toFile(), compression);
	}

	public static NamedTag read(File file, CompressionType compression) throws IOException {
		try (FileInputStream fis = new FileInputStream(file)) {
			return new BinaryNbtDeserializer(compression).fromStream(fis);
		}
	}

	public static NamedTag read(String file, CompressionType compression) throws IOException {
		return read(new File(file), compression);
	}

	public static NamedTag read(Path path, CompressionType compression) throws IOException {
		return read(path.toFile(), compression);
	}
	// </editor-fold>

	// <editor-fold desc="Little Endian read/write (MC Bedrock)">
	public static Path writeLittleEndian(NamedTag tag, File file, CompressionType compression) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(file)) {
			new BinaryNbtSerializer(compression, true).toStream(tag, fos);
		}
		return file.toPath();
	}

	public static Path writeLittleEndian(NamedTag tag, String file, CompressionType compression) throws IOException {
		return writeLittleEndian(tag, new File(file), compression);
	}

	public static Path writeLittleEndian(NamedTag tag, Path path, CompressionType compression) throws IOException {
		return writeLittleEndian(tag, path.toFile(), compression);
	}

	public static Path writeLittleEndian(Tag<?> tag, File file, CompressionType compression) throws IOException {
		return writeLittleEndian(new NamedTag(null, tag), file, compression);
	}

	public static Path writeLittleEndian(Tag<?> tag, String file, CompressionType compression) throws IOException {
		return writeLittleEndian(new NamedTag(null, tag), new File(file), compression);
	}

	public static Path writeLittleEndian(Tag<?> tag, Path path, CompressionType compression) throws IOException {
		return writeLittleEndian(new NamedTag(null, tag), path.toFile(), compression);
	}

	public static NamedTag readLittleEndian(File file, CompressionType compression) throws IOException {
		try (FileInputStream fis = new FileInputStream(file)) {
			return new BinaryNbtDeserializer(compression, true).fromStream(fis);
		}
	}

	public static NamedTag readLittleEndian(String file, CompressionType compression) throws IOException {
		return readLittleEndian(new File(file), compression);
	}

	public static NamedTag readLittleEndian(Path path, CompressionType compression) throws IOException {
		return readLittleEndian(path.toFile(), compression);
	}
	// </editor-fold>
}
