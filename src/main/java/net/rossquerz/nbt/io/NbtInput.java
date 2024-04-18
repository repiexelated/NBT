package net.rossquerz.nbt.io;

import net.rossquerz.nbt.tag.Tag;
import java.io.IOException;

public interface NbtInput {

	NamedTag readTag(int maxDepth) throws IOException;

	Tag<?> readRawTag(int maxDepth) throws IOException;
}
