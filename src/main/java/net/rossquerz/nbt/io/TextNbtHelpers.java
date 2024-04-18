package net.rossquerz.nbt.io;

import net.rossquerz.nbt.tag.Tag;
import net.rossquerz.util.ArgValidator;
import net.rossquerz.util.JsonPrettyPrinter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Utilities for converting {@link Tag}'s to and from string NBT text data. */
public final class TextNbtHelpers {
	private TextNbtHelpers() {}

	// <editor-fold desc="to/from string">
	public static String toTextNbt(Tag<?> tag, boolean prettyPrint) throws IOException {
		String snbt = new TextNbtSerializer().toString(tag);
		return !prettyPrint ? snbt : JsonPrettyPrinter.prettyPrintJson(snbt);
	}

	public static String toTextNbt(Tag<?> tag) throws IOException {
		return toTextNbt(tag, false);
	}

	public static Tag<?> fromTextNbt(String string) throws IOException {
		return new TextNbtDeserializer().fromString(string);
	}

	public static Tag<?> fromTextNbt(String string, boolean lenient) throws IOException {
		return new TextNbtParser(string).parse(Tag.DEFAULT_MAX_DEPTH, lenient);
	}
	// </editor-fold>

	// <editor-fold desc="to/from file">
	public static Path writeTextNbtFile(Path filePath, Tag<?> tag, boolean prettyPrint) throws IOException {
		if (!filePath.getParent().toFile().exists()) {
			ArgValidator.check(filePath.getParent().toFile().mkdirs(), "Failed to create parent directory for " + filePath.toAbsolutePath());
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
	// </editor-fold>
}
