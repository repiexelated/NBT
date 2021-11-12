package net.querz.mca;

import net.querz.nbt.io.NBTDeserializer;
import net.querz.nbt.io.NBTSerializer;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;

import java.io.*;
import java.util.Objects;

import static net.querz.mca.LoadFlags.ALL_DATA;
import static net.querz.mca.LoadFlags.RAW;

/**
 * Abstraction for the base of all chunk types which represent chunks composed of sub-chunks {@link SectionBase}.
 * <p>
 *     <b>Cautionary note to implementors - DO NOT USE INLINE MEMBER INITIALIZATION IN YOUR CLASSES</b><br/>
 *     Define all member initialization in {@link #initMembers()} or be very confused!
 * </p><p>
 *     Due to how Java initializes objects, this base class will call {@link #initReferences(long)} before any inline
 *     member initialization has occurred. The symptom of using in line member initialization is that you will get
 *     very confusing {@link NullPointerException}'s from within {@link #initReferences(long)} for members which
 *     are accessed by your {@link #initReferences(long)} implementation that you have defined inline initializers for
 *     because those initializers will not run until AFTER {@link #initReferences(long)} returns.
 * </p><p>
 *     It is however "safe" to use inline member initialization for any members which are not accessed from within
 *     {@link #initReferences(long)} - but unless you really fully understand the warning above and its full
 *     ramifications just don't do it.
 * </p>
 */
public abstract class ChunkBase implements VersionedDataContainer, TagWrapper {
	protected int dataVersion;
	protected boolean partial;
	protected boolean raw;
	protected int lastMCAUpdate;
	protected CompoundTag data;

	/**
	 * Due to how Java initializes objects and how this class hierarchy is setup it is ill advised to use inline member
	 * initialization because {@link #initReferences0(long)} may be called before members are initialized which WILL
	 * result in very confusing {@link NullPointerException}'s being thrown from within {@link #initReferences(long)}.
	 * This is not a problem that can be solved by moving initialization into your constructors, because you must call
	 * the super constructor as the first line of your child constructor!
	 * <p>So, to get around this hurdle, perform all member initialization you would normally inline in your
	 * class def, within this method instead. Implementers should never need to call this method themselves
	 * as ChunkBase will always call it, even from the default constructor.</p>
	 */
	protected void initMembers() { };

	protected ChunkBase() {
		dataVersion = DataVersion.latest().id();
		lastMCAUpdate = (int)(System.currentTimeMillis() / 1000);
		initMembers();
	}

	/**
	 * Create a new chunk based on raw base data from a region file.
	 * @param data The raw base data to be used.
	 */
	public ChunkBase(CompoundTag data) {
		this(data, ALL_DATA);
	}

	/**
	 * Create a new chunk based on raw base data from a region file.
	 * @param data The raw base data to be used.
	 * @param loadFlags Union of {@link LoadFlags} to process.
	 */
	public ChunkBase(CompoundTag data, long loadFlags) {
		this.data = data;
		initMembers();
		initReferences0(loadFlags);
	}

	private void initReferences0(long loadFlags) {
		Objects.requireNonNull(data, "data cannot be null");
		dataVersion = data.getInt("DataVersion");

		if ((loadFlags != ALL_DATA) && (loadFlags & RAW) != 0) {
			raw = true;
		} else {
			if (dataVersion == 0) {
				throw new IllegalArgumentException("data does not contain \"DataVersion\" tag");
			}

			initReferences(loadFlags);

			// If we haven't requested the full set of data we can drop the underlying raw data to let the GC handle it.
			if (loadFlags != ALL_DATA) {
				data = null;
				partial = true;
			}
		}
	}

	/**
	 * Child classes should not call this method directly, it will be called for them.
	 * Raw and partial data handling is taken care of, this method will not be called if {@code loadFlags} is
	 * {@link LoadFlags#RAW}.
	 */
	protected abstract void initReferences(final long loadFlags);

	/**
	 * {@inheritDoc}
	 */
	public int getDataVersion() {
		return dataVersion;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDataVersion(int dataVersion) {
		this.dataVersion = Math.max(0, dataVersion);
	}

	/**
	 * Serializes this chunk to a <code>RandomAccessFile</code>.
	 * @param raf The RandomAccessFile to be written to.
	 * @param xPos The x-coordinate of the chunk.
	 * @param zPos The z-coodrinate of the chunk.
	 * @return The amount of bytes written to the RandomAccessFile.
	 * @throws UnsupportedOperationException When something went wrong during writing.
	 * @throws IOException When something went wrong during writing.
	 */
	public int serialize(RandomAccessFile raf, int xPos, int zPos) throws IOException {
		if (partial) {
			throw new UnsupportedOperationException("Partially loaded chunks cannot be serialized");
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
		try (BufferedOutputStream nbtOut = new BufferedOutputStream(CompressionType.ZLIB.compress(baos))) {
			new NBTSerializer(false).toStream(new NamedTag(null, updateHandle(xPos, zPos)), nbtOut);
		}
		byte[] rawData = baos.toByteArray();
		raf.writeInt(rawData.length + 1); // including the byte to store the compression type
		raf.writeByte(CompressionType.ZLIB.getID());
		raf.write(rawData);
		return rawData.length + 5;
	}

	/**
	 * Reads chunk data from a RandomAccessFile. The RandomAccessFile must already be at the correct position.
	 * @param raf The RandomAccessFile to read the chunk data from.
	 * @throws IOException When something went wrong during reading.
	 */
	public void deserialize(RandomAccessFile raf) throws IOException {
		deserialize(raf, ALL_DATA);
	}

	/**
	 * Reads chunk data from a RandomAccessFile. The RandomAccessFile must already be at the correct position.
	 * @param raf The RandomAccessFile to read the chunk data from.
	 * @param loadFlags A logical or of {@link LoadFlags} constants indicating what data should be loaded
	 * @throws IOException When something went wrong during reading.
	 */
	public void deserialize(RandomAccessFile raf, long loadFlags) throws IOException {
		byte compressionTypeByte = raf.readByte();
		CompressionType compressionType = CompressionType.getFromID(compressionTypeByte);
		if (compressionType == null) {
			throw new IOException("invalid compression type " + compressionTypeByte);
		}
		BufferedInputStream dis = new BufferedInputStream(compressionType.decompress(new FileInputStream(raf.getFD())));
		NamedTag tag = new NBTDeserializer(false).fromStream(dis);
		if (tag != null && tag.getTag() instanceof CompoundTag) {
			data = (CompoundTag) tag.getTag();
			initReferences0(loadFlags);
		} else {
			throw new IOException("invalid data tag: " + (tag == null ? "null" : tag.getClass().getName()));
		}
	}

	/**
	 * @return The timestamp when this region file was last updated in seconds since 1970-01-01.
	 */
	public int getLastMCAUpdate() {
		return lastMCAUpdate;
	}

	/**
	 * Sets the timestamp when this region file was last updated in seconds since 1970-01-01.
	 * @param lastMCAUpdate The time in seconds since 1970-01-01.
	 */
	public void setLastMCAUpdate(int lastMCAUpdate) {
		checkRaw();
		this.lastMCAUpdate = lastMCAUpdate;
	}

	protected void checkRaw() {
		if (raw) {
			throw new UnsupportedOperationException("cannot update field when working with raw data");
		}
	}

	/**
	 * Provides a reference to the full chunk data.
	 * @return The full chunk data or null if there is none, e.g. when this chunk has only been loaded partially.
	 */
	public CompoundTag getHandle() {
		return data;
	}

	public CompoundTag updateHandle() {
		if (!raw) {
			data.putInt("DataVersion", dataVersion);
		}
		return data;
	}

	// Note: Not all chunk formats store xz in their NBT, but {@link MCAFileBase} will call this update method
	// to give them the chance to record them.
	public CompoundTag updateHandle(int xPos, int zPos) {
		return updateHandle();
	}
}
