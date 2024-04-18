package net.rossquerz.nbt.io;

import net.rossquerz.nbt.tag.Tag;
import net.rossquerz.util.ArgValidator;
import net.rossquerz.util.JsonPrettyPrinter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Utilities for converting {@link Tag}'s  and {@link NamedTag}'s to and from string NBT text data and files. */
public final class TextNbtHelpers {
	private TextNbtHelpers() {}

	// <editor-fold desc="to/from string">
	public static String toTextNbt(NamedTag namedTag, boolean prettyPrint) throws IOException {
		String snbt = new TextNbtSerializer().toString(namedTag);
		return !prettyPrint ? snbt : JsonPrettyPrinter.prettyPrintJson(snbt);
	}

	/** defaults to prettyPrint=true */
	public static String toTextNbt(NamedTag namedTag) throws IOException {
		return toTextNbt(namedTag, true);
	}

	public static String toTextNbt(Tag<?> tag, boolean prettyPrint) throws IOException {
		return toTextNbt(new NamedTag(null, tag), prettyPrint);
	}

	/** defaults to prettyPrint=true */
	public static String toTextNbt(Tag<?> tag) throws IOException {
		return toTextNbt(new NamedTag(null, tag), true);
	}

	public static NamedTag fromTextNbt(String string) throws IOException {
		return new TextNbtDeserializer().fromString(string);
	}
	// </editor-fold>

	// <editor-fold desc="write to file">
	public static Path writeTextNbtFile(Path filePath, Tag<?> tag, boolean prettyPrint) throws IOException {
		if (!filePath.getParent().toFile().exists()) {
			ArgValidator.check(filePath.getParent().toFile().mkdirs(),
					"Failed to create parent directory for " + filePath.toAbsolutePath());
		}
		Files.write(filePath, toTextNbt(tag, prettyPrint).getBytes(StandardCharsets.UTF_8));
		return filePath;
	}

	public static File writeTextNbtFile(File file, Tag<?> tag, boolean prettyPrint) throws IOException {
		return writeTextNbtFile(file.toPath(), tag, prettyPrint).toFile();
	}

	public static Path writeTextNbtFile(String file, Tag<?> tag, boolean prettyPrint) throws IOException {
		return writeTextNbtFile(Paths.get(file), tag, prettyPrint);
	}

	/** defaults to prettyPrint=true */
	public static Path writeTextNbtFile(Path filePath, Tag<?> tag) throws IOException {
		return writeTextNbtFile(filePath, tag, true);
	}

	/** defaults to prettyPrint=true */
	public static File writeTextNbtFile(File file, Tag<?> tag) throws IOException {
		return writeTextNbtFile(file.toPath(), tag, true).toFile();
	}

	/** defaults to prettyPrint=true */
	public static Path writeTextNbtFile(String file, Tag<?> tag) throws IOException {
		return writeTextNbtFile(Paths.get(file), tag, true);
	}
	// </editor-fold>

	// <editor-fold desc="read from file">
	public static NamedTag readTextNbtFile(File file) throws IOException {
		try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
			return new TextNbtDeserializer().fromStream(dis);
		}
	}

	public static NamedTag readTextNbtFile(String file) throws IOException {
		return readTextNbtFile(new File(file));
	}

	public static NamedTag readTextNbtFile(Path path) throws IOException {
		return readTextNbtFile(path.toFile());
	}
	// </editor-fold>
}
