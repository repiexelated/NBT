package net.rossquerz.nbt.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

public enum CompressionType {
	NONE(0, t -> t, t -> t),
	/** Most used compression type for binary nbt data files. */
	GZIP(1, GZIPOutputStream::new, GZIPInputStream::new),
	/** Default compression type used by the vanilla jar to store chunks in mca files. */
	ZLIB(2, DeflaterOutputStream::new, InflaterInputStream::new);

	@FunctionalInterface
	private interface IOExceptionFunction<T, R> {
		R accept(T t) throws IOException;
	}

	private final byte id;
	private final IOExceptionFunction<OutputStream, ? extends OutputStream> compressor;
	private final IOExceptionFunction<InputStream, ? extends InputStream> decompressor;

	CompressionType(int id,
					IOExceptionFunction<OutputStream, ? extends OutputStream> compressor,
					IOExceptionFunction<InputStream, ? extends InputStream> decompressor) {
		this.id = (byte) id;
		this.compressor = compressor;
		this.decompressor = decompressor;
	}

	public byte getID() {
		return id;
	}

	public OutputStream compress(OutputStream out) throws IOException {
		return compressor.accept(out);
	}

	public InputStream decompress(InputStream in) throws IOException {
		return decompressor.accept(in);
	}

	/**
	 * Finishes writing compressed data to the output stream without closing it.
	 * @exception IOException if an I/O error has occurred
	 */
	public void finish(OutputStream out) throws IOException {
		if (out instanceof DeflaterOutputStream) {
			((DeflaterOutputStream) out).finish();
		}
	}

	public static CompressionType getFromID(byte id) {
		for (CompressionType c : CompressionType.values()) {
			if (c.id == id) {
				return c;
			}
		}
		return null;
	}
}
