package net.rossquerz.nbt.io;

import net.rossquerz.io.StringSerializer;
import net.rossquerz.nbt.tag.Tag;
import java.io.IOException;
import java.io.Writer;

public class TextNbtSerializer implements StringSerializer<Tag<?>> {

	@Override
	public void toWriter(Tag<?> tag, Writer writer) throws IOException {
		TextNbtWriter.write(tag, writer);
	}

	public void toWriter(Tag<?> tag, Writer writer, int maxDepth) throws IOException {
		TextNbtWriter.write(tag, writer, maxDepth);
	}
}
