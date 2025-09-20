package io.github.ensgijs.nbt;


import io.github.ensgijs.nbt.io.BinaryNbtHelpers;
import io.github.ensgijs.nbt.io.CompressionType;
import io.github.ensgijs.nbt.io.NamedTag;
import io.github.ensgijs.nbt.io.TextNbtHelpers;

import java.io.IOException;
import java.io.InputStream;

public abstract class BenchmarkBase {

    protected byte[] load(String resourceName) throws IOException {
        NamedTag tag;
        try (InputStream is = getResourceStream(resourceName)) {
            if (!resourceName.endsWith(".snbt") && !resourceName.endsWith(".snbt.gz")) {
                is.mark(2);
                CompressionType compression = CompressionType.detect(is.readNBytes(2));
                is.reset();
                tag = BinaryNbtHelpers.read(is, compression);
            } else {
                tag = TextNbtHelpers.readTextNbt(is);
            }
        }
        return BinaryNbtHelpers.serializeAsBytes(tag, CompressionType.NONE);
    }

    protected InputStream getResourceStream(String name) {
        // Use the class loader to get an InputStream
        InputStream is = getClass().getClassLoader().getResourceAsStream(name);
        if (is == null) {
            throw new IllegalArgumentException("Resource not found: " + name);
        }
        return is;
    }
}
