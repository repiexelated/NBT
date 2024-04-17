package net.querz.nbt.io;

import net.querz.nbt.tag.Tag;
import net.querz.util.ArgValidator;
import net.querz.util.JsonPrettyPrinter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Utilities for converting {@link Tag}'s to and from string NBT text data. */
public class SNBTUtil {

	public static String toSNBT(Tag<?> tag) throws IOException {
		return toSNBT(tag, false);
	}

	public static String toSNBT(Tag<?> tag, boolean prettyPrint) throws IOException {
		String snbt = new SNBTSerializer().toString(tag);
		return !prettyPrint ? snbt : JsonPrettyPrinter.prettyPrintJson(snbt);
	}


	public static Path writeSnbtTextFile(Path filePath, Tag<?> tag, boolean prettyPrint) throws IOException {
		if (!filePath.getParent().toFile().exists()) {
			ArgValidator.check(filePath.getParent().toFile().mkdirs(), "Failed to create parent directory for " + filePath.toAbsolutePath());
		}
		Files.write(filePath, toSNBT(tag, prettyPrint).getBytes(StandardCharsets.UTF_8));
		return filePath;
	}

	public static File writeSnbtTextFile(File file, Tag<?> tag, boolean prettyPrint) throws IOException {
		return writeSnbtTextFile(file.toPath(), tag, prettyPrint).toFile();
	}

	public static Path writeSnbtTextFile(String file, Tag<?> tag, boolean prettyPrint) throws IOException {
		return writeSnbtTextFile(Paths.get(file), tag, prettyPrint);
	}

	public static Tag<?> fromSNBT(String string) throws IOException {
		return new SNBTDeserializer().fromString(string);
	}

	public static Tag<?> fromSNBT(String string, boolean lenient) throws IOException {
		return new SNBTParser(string).parse(Tag.DEFAULT_MAX_DEPTH, lenient);
	}
}
