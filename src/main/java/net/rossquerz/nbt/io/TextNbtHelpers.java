package net.rossquerz.nbt.io;

import net.rossquerz.nbt.tag.Tag;
import net.rossquerz.util.ArgValidator;
import net.rossquerz.util.JsonPrettyPrinter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utilities for converting {@link Tag}'s  and {@link NamedTag}'s to and from string NBT text data and files.
 * <p>NOTE: {@link #readTextNbtFile(File)}, and its variants, can read both uncompressed (plain text) and GZ
 * compressed files (usually ending in .gz file extension).</p>
 */
public final class TextNbtHelpers {
	private TextNbtHelpers() {}

	// <editor-fold desc="to/from string">
	public static String toTextNbt(NamedTag namedTag, boolean prettyPrint, boolean sortCompoundTagEntries) throws IOException {
		String snbt = new TextNbtSerializer(sortCompoundTagEntries).toString(namedTag);
		return !prettyPrint ? snbt : JsonPrettyPrinter.prettyPrintJson(snbt);
	}

	/** defaults to sortCompoundTagEntries=true */
	public static String toTextNbt(NamedTag namedTag, boolean prettyPrint) throws IOException {
		return toTextNbt(namedTag, prettyPrint, true);
	}

	/** defaults to sortCompoundTagEntries=false */
	public static String toTextNbtUnsorted(NamedTag namedTag, boolean prettyPrint) throws IOException {
		return toTextNbt(namedTag, prettyPrint, false);
	}

	/** defaults to prettyPrint=true, sortCompoundTagEntries=true */
	public static String toTextNbt(NamedTag namedTag) throws IOException {
		return toTextNbt(namedTag, true, true);
	}

	/** defaults to prettyPrint=true, sortCompoundTagEntries=false */
	public static String toTextNbtUnsorted(NamedTag namedTag) throws IOException {
		return toTextNbt(namedTag, true, false);
	}

	public static String toTextNbt(Tag<?> tag, boolean prettyPrint, boolean sortCompoundTagEntries) throws IOException {
		return toTextNbt(new NamedTag(null, tag), prettyPrint, sortCompoundTagEntries);
	}

	/** defaults to sortCompoundTagEntries=true */
	public static String toTextNbt(Tag<?> tag, boolean prettyPrint) throws IOException {
		return toTextNbt(new NamedTag(null, tag), prettyPrint, true);
	}

	/** defaults to sortCompoundTagEntries=false */
	public static String toTextNbtUnsorted(Tag<?> tag, boolean prettyPrint) throws IOException {
		return toTextNbt(new NamedTag(null, tag), prettyPrint, false);
	}

	/** defaults to prettyPrint=true, sortCompoundTagEntries=true */
	public static String toTextNbt(Tag<?> tag) throws IOException {
		return toTextNbt(new NamedTag(null, tag), true, true);
	}

	/** defaults to prettyPrint=true, sortCompoundTagEntries=false */
	public static String toTextNbtUnsorted(Tag<?> tag) throws IOException {
		return toTextNbt(new NamedTag(null, tag), true, false);
	}

	public static NamedTag fromTextNbt(String string) throws IOException {
		return new TextNbtDeserializer().fromString(string);
	}
	// </editor-fold>

	// <editor-fold desc="write to file">
	public static Path writeTextNbtFile(Path filePath, Tag<?> tag, boolean prettyPrint, boolean sortCompoundTagEntries) throws IOException {
		if (!filePath.getParent().toFile().exists()) {
			ArgValidator.check(filePath.getParent().toFile().mkdirs(),
					"Failed to create parent directory for " + filePath.toAbsolutePath());
		}
		Files.write(filePath, toTextNbt(tag, prettyPrint, sortCompoundTagEntries).getBytes(StandardCharsets.UTF_8));
		return filePath;
	}

	/** defaults to sortCompoundTagEntries=true */
	public static Path writeTextNbtFile(Path filePath, Tag<?> tag, boolean prettyPrint) throws IOException {
		return writeTextNbtFile(filePath, tag, prettyPrint, true);
	}

	/** defaults to sortCompoundTagEntries=true */
	public static Path writeTextNbtFile(File file, Tag<?> tag, boolean prettyPrint) throws IOException {
		return writeTextNbtFile(file.toPath(), tag, prettyPrint, true);
	}

	/** defaults to sortCompoundTagEntries=true */
	public static Path writeTextNbtFile(String file, Tag<?> tag, boolean prettyPrint) throws IOException {
		return writeTextNbtFile(Paths.get(file), tag, prettyPrint, true);
	}


	/** defaults to prettyPrint=true, sortCompoundTagEntries=true */
	public static Path writeTextNbtFile(Path filePath, Tag<?> tag) throws IOException {
		return writeTextNbtFile(filePath, tag, true, true);
	}

	/** defaults to prettyPrint=true, sortCompoundTagEntries=true */
	public static Path writeTextNbtFile(File file, Tag<?> tag) throws IOException {
		return writeTextNbtFile(file.toPath(), tag, true, true);
	}

	/** defaults to prettyPrint=true, sortCompoundTagEntries=true */
	public static Path writeTextNbtFile(String file, Tag<?> tag) throws IOException {
		return writeTextNbtFile(Paths.get(file), tag, true, true);
	}


	/** defaults to sortCompoundTagEntries=false */
	public static Path writeTextNbtFileUnsorted(Path filePath, Tag<?> tag, boolean prettyPrint) throws IOException {
		return writeTextNbtFile(filePath, tag, prettyPrint, false);
	}

	/** defaults to sortCompoundTagEntries=false */
	public static Path writeTextNbtFileUnsorted(File file, Tag<?> tag, boolean prettyPrint) throws IOException {
		return writeTextNbtFile(file.toPath(), tag, prettyPrint, false);
	}

	/** defaults to sortCompoundTagEntries=false */
	public static Path writeTextNbtFileUnsorted(String file, Tag<?> tag, boolean prettyPrint) throws IOException {
		return writeTextNbtFile(Paths.get(file), tag, prettyPrint, false);
	}


	/** defaults to prettyPrint=true, sortCompoundTagEntries=false */
	public static Path writeTextNbtFileUnsorted(Path filePath, Tag<?> tag) throws IOException {
		return writeTextNbtFile(filePath, tag, true, false);
	}

	/** defaults to prettyPrint=true, sortCompoundTagEntries=false */
	public static Path writeTextNbtFileUnsorted(File file, Tag<?> tag) throws IOException {
		return writeTextNbtFile(file.toPath(), tag, true, false);
	}

	/** defaults to prettyPrint=true, sortCompoundTagEntries=false */
	public static Path writeTextNbtFileUnsorted(String file, Tag<?> tag) throws IOException {
		return writeTextNbtFile(Paths.get(file), tag, true, false);
	}
	// </editor-fold>

	// <editor-fold desc="read from file">
	/** The given file can be either plain text (uncompressed) or gz compressed. */
	public static NamedTag readTextNbtFile(File file) throws IOException {
		try (DataInputStream dis = new DataInputStream(BinaryNbtHelpers.detectDecompression(new FileInputStream(file)))) {
			return new TextNbtDeserializer().fromStream(dis);
		}
	}

	/** The given file can be either plain text (uncompressed) or gz compressed. */
	public static NamedTag readTextNbtFile(String file) throws IOException {
		return readTextNbtFile(new File(file));
	}

	/** The given file can be either plain text (uncompressed) or gz compressed. */
	public static NamedTag readTextNbtFile(Path path) throws IOException {
		return readTextNbtFile(path.toFile());
	}
	// </editor-fold>
}
