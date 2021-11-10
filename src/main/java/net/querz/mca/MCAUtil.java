package net.querz.mca;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Provides main and utility functions to read and write .mca files and
 * to convert block, chunk, and region coordinates.
 */
public final class MCAUtil {

	private MCAUtil() {}

	private static final Pattern mcaFilePattern = Pattern.compile("^.*\\.(?<regionX>-?\\d+)\\.(?<regionZ>-?\\d+)\\.mca$");

	/**
	 * This map controls the factory creation behavior of the various "auto" functions. When an auto function is
	 * given an mca file location the folder name which contains the .mca files is used to lookup the correct
	 * mca file creator from this map. For example, if an auto method were passed the path
	 * "foo/bar/creator_name/r.4.2.mca" it would call {@code MCA_CREATORS.get("creator_name").apply(4, 2)}.
	 * <p>By manipulating this map you can control the factory behavior to support new mca types or to specify
	 * that a custom creation method should be called which could even return a custom {@link MCAFileBase}
	 * implementation.</p>
	 */
	public static final Map<String, BiFunction<Integer, Integer, MCAFileBase<?>>> MCA_CREATORS = new HashMap<>();

	static {
		MCA_CREATORS.put("region", MCAFile::new);
		MCA_CREATORS.put("poi", PoiMCAFile::new);
		MCA_CREATORS.put("entities", EntitiesMCAFile::new);
	}

	//<editor-fold desc="Legacy Readers" defaultstate="collapsed">
	/**
	 * @see MCAUtil#read(File)
	 * @param file The file to read the data from.
	 * @return An in-memory representation of the MCA file with decompressed chunk data.
	 * @throws IOException if something during deserialization goes wrong.
	 * @deprecated switch to {@link #readAuto(String)}
	 */
	@Deprecated
	public static MCAFile read(String file) throws IOException {
		return read(new File(file), LoadFlags.ALL_DATA);
	}

	/**
	 * Reads an MCA file and loads all of its chunks.
	 * @param file The file to read the data from.
	 * @return An in-memory representation of the MCA file with decompressed chunk data.
	 * @throws IOException if something during deserialization goes wrong.
	 * @deprecated switch to {@link #readAuto(File)}
	 */
	@Deprecated
	public static MCAFile read(File file) throws IOException {
		return read(file, LoadFlags.ALL_DATA);
	}

	/**
	 * @see MCAUtil#read(File, long)
	 * @param file The file to read the data from.
	 * @return An in-memory representation of the MCA file with decompressed chunk data.
	 * @param loadFlags A logical or of {@link LoadFlags} constants indicating what data should be loaded
	 * @throws IOException if something during deserialization goes wrong.
	 * @deprecated switch to {@link #readAuto(String, long)}
	 */
	@Deprecated
	public static MCAFile read(String file, long loadFlags) throws IOException {
		return read(new File(file), loadFlags);
	}

	/**
	 * Reads an MCA file and loads all of its chunks.
	 * @param file The file to read the data from.
	 * @return An in-memory representation of the MCA file with decompressed chunk data
	 * @param loadFlags A logical or of {@link LoadFlags} constants indicating what data should be loaded
	 * @throws IOException if something during deserialization goes wrong.
	 * @deprecated switch to {@link #readAuto(File, long)}
	 */
	@Deprecated
	public static MCAFile read(File file, long loadFlags) throws IOException {
		MCAFile mcaFile = newMCAFile(file);
		try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
			mcaFile.deserialize(raf, loadFlags);
			return mcaFile;
		}
	}
	//</editor-fold>

	//<editor-fold desc="Auto MCA Readers">
	/**
	 * @see MCAUtil#readAuto(File)
	 * @param file The file to read the data from.
	 * @return An in-memory representation of the MCA file with decompressed chunk data.
	 * @throws IOException if something during deserialization goes wrong.
	 */
	public static <T extends MCAFileBase<?>> T readAuto(String file) throws IOException {
		return readAuto(new File(file), LoadFlags.ALL_DATA);
	}

	/**
	 * Reads an MCA file and loads all of its chunks.
	 * @param file The file to read the data from.
	 * @return An in-memory representation of the MCA file with decompressed chunk data.
	 * @throws IOException if something during deserialization goes wrong.
	 */
	public static <T extends MCAFileBase<?>> T readAuto(File file) throws IOException {
		return readAuto(file, LoadFlags.ALL_DATA);
	}

	/**
	 * @see MCAUtil#readAuto(File, long)
	 * @param file The file to read the data from.
	 * @return An in-memory representation of the MCA file with decompressed chunk data.
	 * @param loadFlags A logical or of {@link LoadFlags} constants indicating what data should be loaded
	 * @throws IOException if something during deserialization goes wrong.
	 */
	public static <T extends MCAFileBase<?>> T readAuto(String file, long loadFlags) throws IOException {
		return readAuto(new File(file), loadFlags);
	}

	/**
	 * Reads an MCA file and loads all of its chunks.
	 * @param file The file to read the data from.
	 * @return An in-memory representation of the MCA file with decompressed chunk data
	 * @param loadFlags A logical or of {@link LoadFlags} constants indicating what data should be loaded
	 * @throws IOException if something during deserialization goes wrong.
	 */
	public static <T extends MCAFileBase<?>> T readAuto(File file, long loadFlags) throws IOException {
		T mcaFile = autoMCAFile(file);
		try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
			mcaFile.deserialize(raf, loadFlags);
			return mcaFile;
		}
	}
	//</editor-fold>

	//<editor-fold desc="Writers">
	/**
	 * Calls {@link MCAUtil#write(MCAFileBase, File, boolean)} without changing the timestamps.
	 * @see MCAUtil#write(MCAFileBase, File, boolean)
	 * @param file The file to write to.
	 * @param mcaFile The data of the MCA file to write.
	 * @return The amount of chunks written to the file.
	 * @throws IOException If something goes wrong during serialization.
	 */
	public static int write(MCAFileBase<?> mcaFile, String file) throws IOException {
		return write(mcaFile, new File(file), false);
	}

	/**
	 * Calls {@link MCAUtil#write(MCAFileBase, File, boolean)} without changing the timestamps.
	 * @see MCAUtil#write(MCAFileBase, File, boolean)
	 * @param file The file to write to.
	 * @param mcaFile The data of the MCA file to write.
	 * @return The amount of chunks written to the file.
	 * @throws IOException If something goes wrong during serialization.
	 */
	public static int write(MCAFileBase<?> mcaFile, File file) throws IOException {
		return write(mcaFile, file, false);
	}

	/**
	 * @see MCAUtil#write(MCAFileBase, File, boolean)
	 * @param file The file to write to.
	 * @param mcaFile The data of the MCA file to write.
	 * @param changeLastUpdate Whether to adjust the timestamps of when the file was saved.
	 * @return The amount of chunks written to the file.
	 * @throws IOException If something goes wrong during serialization.
	 */
	public static int write(MCAFileBase<?> mcaFile, String file, boolean changeLastUpdate) throws IOException {
		return write(mcaFile, new File(file), changeLastUpdate);
	}

	/**
	 * Writes an {@code MCAFile} object to disk. It optionally adjusts the timestamps
	 * when the file was last saved to the current date and time or leaves them at
	 * the value set by either loading an already existing MCA file or setting them manually.<br>
	 * If the file already exists, it is completely overwritten by the new file (no modification).
	 * @param file The file to write to.
	 * @param mcaFile The data of the MCA file to write.
	 * @param changeLastUpdate Whether to adjust the timestamps of when the file was saved.
	 * @return The amount of chunks written to the file.
	 * @throws IOException If something goes wrong during serialization.
	 */
	public static int write(MCAFileBase<?> mcaFile, File file, boolean changeLastUpdate) throws IOException {
		File to = file;
		if (file.exists()) {
			to = File.createTempFile(to.getName(), null);
		}
		int chunks;
		try (RandomAccessFile raf = new RandomAccessFile(to, "rw")) {
			chunks = mcaFile.serialize(raf, changeLastUpdate);
		}

		if (chunks > 0 && to != file) {
			Files.move(to.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		return chunks;
	}
	//</editor-fold>

	//<editor-fold desc="Region File Name Generators">
	/**
	 * Turns the chunks coordinates into region coordinates and calls
	 * {@link MCAUtil#createNameFromRegionLocation(int, int)}
	 * @param chunkX The x-value of the location of the chunk.
	 * @param chunkZ The z-value of the location of the chunk.
	 * @return A mca filename in the format "r.{regionX}.{regionZ}.mca"
	 */
	public static String createNameFromChunkLocation(int chunkX, int chunkZ) {
		return createNameFromRegionLocation( chunkToRegion(chunkX), chunkToRegion(chunkZ));
	}

	/**
	 * Turns the block coordinates into region coordinates and calls
	 * {@link MCAUtil#createNameFromRegionLocation(int, int)}
	 * @param blockX The x-value of the location of the block.
	 * @param blockZ The z-value of the location of the block.
	 * @return A mca filename in the format "r.{regionX}.{regionZ}.mca"
	 */
	public static String createNameFromBlockLocation(int blockX, int blockZ) {
		return createNameFromRegionLocation(blockToRegion(blockX), blockToRegion(blockZ));
	}

	/**
	 * Creates a filename string from provided region coordinates.
	 * @param regionX The x-value of the location of the region.
	 * @param regionZ The z-value of the location of the region.
	 * @return A mca filename in the format "r.{regionX}.{regionZ}.mca"
	 */
	public static String createNameFromRegionLocation(int regionX, int regionZ) {
		return "r." + regionX + "." + regionZ + ".mca";
	}
	//</editor-fold>

	//<editor-fold desc="Coordinate Helpers">
	/**
	 * Turns a block coordinate value into a chunk coordinate value.
	 * @param block The block coordinate value.
	 * @return The chunk coordinate value.
	 */
	public static int blockToChunk(int block) {
		return block >> 4;
	}

	/**
	 * Turns a block coordinate value into a region coordinate value.
	 * @param block The block coordinate value.
	 * @return The region coordinate value.
	 */
	public static int blockToRegion(int block) {
		return block >> 9;
	}

	/**
	 * Turns a chunk coordinate value into a region coordinate value.
	 * @param chunk The chunk coordinate value.
	 * @return The region coordinate value.
	 */
	public static int chunkToRegion(int chunk) {
		return chunk >> 5;
	}

	/**
	 * Turns a region coordinate value into a chunk coordinate value.
	 * @param region The region coordinate value.
	 * @return The chunk coordinate value.
	 */
	public static int regionToChunk(int region) {
		return region << 5;
	}

	/**
	 * Turns a region coordinate value into a block coordinate value.
	 * @param region The region coordinate value.
	 * @return The block coordinate value.
	 */
	public static int regionToBlock(int region) {
		return region << 9;
	}

	/**
	 * Turns a chunk coordinate value into a block coordinate value.
	 * @param chunk The chunk coordinate value.
	 * @return The block coordinate value.
	 */
	public static int chunkToBlock(int chunk) {
		return chunk << 4;
	}
	//</editor-fold>

	/**
	 * Creates a REGION {@link MCAFile} initialized with its X and Z extracted from the given file name. The file
	 * does not need to exist.
	 * @deprecated Legacy helper, switch to {@link #autoMCAFile(Path)} for POI (1.14+) and ENTITIES (1.17+) mca support.
	 */
	@Deprecated
	public static MCAFile newMCAFile(File file) {
		final Matcher m = mcaFilePattern.matcher(file.getName());
		if (!m.find()) {
			throw new IllegalArgumentException("invalid mca file name (expect name match '*.<X>.<Z>.mca'): "
					+ file.getName());
		}
		return new MCAFile(Integer.parseInt(m.group("regionX")), Integer.parseInt(m.group("regionZ")));
	}


	private static void throwCannotDetermineMcaType(Exception cause) {
		throw new IllegalArgumentException(
				"Unable to determine mca file type. Expect the mca file to have a parent folder with one of the following names: "
						+ MCA_CREATORS.keySet().stream().sorted().collect(Collectors.joining(", ")), cause);
	}

	/**
	 * @see #autoMCAFile(Path)
	 */
	public static <T extends MCAFileBase<?>> T autoMCAFile(File file) {
		return autoMCAFile(file.toPath());
	}

	/**
	 * Detects and creates a concretion (implementer) of {@link MCAFileBase}. The actual type returned is determined by
	 * the name of the folder containing the .mca file.
	 * <p><b>Usage suggestion</b>  when the caller fully controls passed file name:
	 * <pre>{@code EntitiesMCAFile entitiesMca = MCAUtil.autoMCAFile(Paths.get("entities/r.0.0.mca"));}</pre>
	 * <p><b>Usage suggestion</b> when the caller expects a specific return type but does not control the passed file name:
	 * <pre>{@code try {
	 *   EntitiesMCAFile entitiesMca = MCAUtil.autoMCAFile(filename);
	 * } catch (ClassCastException expected) {
	 *   // got an unexpected type
	 * }}</pre>
	 * <p><b>Usage suggestion</b>  when the caller may not know what return type to expect:
	 * <pre>{@code MCAFileBase<?> mcaFile = MCAUtil.autoMCAFile(filename);
	 * if (mcaFile instanceof MCAFile) {
	 *   // process region mca file
	 *   MCAFile regionMca = (MCAFile) mcaFile;
	 * } else if (mcaFile instanceof PoiMCAFile) {
	 *   // process poi mca file
	 *   PoiMCAFile poiMca = (PoiMCAFile) mcaFile;
	 * } else if (mcaFile instanceof EntitiesMCAFile) {
	 *   // process entities mca file
	 *   EntitiesMCAFile entitiesMca = (EntitiesMCAFile) mcaFile;
	 * } else {
	 *   // unsupported type / don't care about this type, etc.
	 * }}</pre>
	 *
	 * @param path The file does not need to exist but the given path must have at least 2 parts.
	 *             Required parts: "mca_type/mca_file"
	 *             where mca_type (such as "region", "poi", "entities") is used to determine which
	 *             {@link #MCA_CREATORS} to call and mca_file is the .mca file such as "r.0.0.mca".
	 * @param <T> {@link MCAFileBase} type - do note that any {@link ClassCastException} errors will be thrown
	 *           at the location of assignment, not from within this call.
	 * @return Instantiated and initialized concretion of {@link MCAFileBase}. Never Null.
	 * @throws IllegalArgumentException Thrown when the mca type could not be determined from the path, when there
	 * is no {@link #MCA_CREATORS} mapped to the mca type, or when the regions X and Z locations could not be
	 * extracted from the filename.
	 * @throws NullPointerException Thrown when a custom creator did not produce a result.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends MCAFileBase<?>> T autoMCAFile(Path path) {
		BiFunction<Integer, Integer, MCAFileBase<?>> creator = null;
		String creatorName = null;
		try {
			creatorName = path.getParent().getFileName().toString();
			creator = MCA_CREATORS.get(creatorName);
			if (creator == null) throwCannotDetermineMcaType(null);
		} catch (Exception ex) {
			throwCannotDetermineMcaType(ex);
		}
		final Matcher m = mcaFilePattern.matcher(path.getFileName().toString());
		if (!m.find()) {
			throw new IllegalArgumentException("invalid mca file name (expect name match '*.<X>.<Z>.mca'): " + path);
		}
		final int x = Integer.parseInt(m.group("regionX"));
		final int z = Integer.parseInt(m.group("regionZ"));
		T mcaFile = (T) creator.apply(x, z);
		if (mcaFile == null) {
			throw new NullPointerException("Creator for " + creatorName + " did not produce a result for " + path);
		}
		return mcaFile;
	}
}
