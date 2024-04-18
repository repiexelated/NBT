package net.rossquerz.nbt.io;

import net.rossquerz.io.Serializer;
import java.io.*;

public class TextNbtSerializer implements Serializer<NamedTag> {

	public void toWriter(NamedTag tag, Writer writer) throws IOException {
		TextNbtWriter.write(tag, writer);
	}

	public void toWriter(NamedTag tag, Writer writer, int maxDepth) throws IOException {
		TextNbtWriter.write(tag, writer, maxDepth);
	}

	public String toString(NamedTag object) throws IOException {
		Writer writer = new StringWriter();
		toWriter(object, writer);
		writer.flush();
		return writer.toString();
	}

	@Override
	public void toStream(NamedTag object, OutputStream stream) throws IOException {
		Writer writer = new OutputStreamWriter(stream);
		toWriter(object, writer);
		writer.flush();
	}

	@Override
	public void toFile(NamedTag object, File file) throws IOException {
		try (Writer writer = new FileWriter(file)) {
			toWriter(object, writer);
		}
	}
}
