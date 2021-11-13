package net.querz.nbt.tag;

import java.util.*;
import java.util.function.BiConsumer;

import net.querz.io.MaxDepthIO;

public class CompoundTag extends Tag<Map<String, Tag<?>>>
		implements Iterable<Map.Entry<String, Tag<?>>>, Comparable<CompoundTag>, MaxDepthIO {

	public static final byte ID = 10;

	public CompoundTag() {
		super(createEmptyValue());
	}

	public CompoundTag(int initialCapacity) {
		super(new HashMap<>(initialCapacity));
	}

	@Override
	public byte getID() {
		return ID;
	}

	private static Map<String, Tag<?>> createEmptyValue() {
		return new HashMap<>(8);
	}

	public int size() {
		return getValue().size();
	}

	public boolean isEmpty() {
		return getValue().isEmpty();
	}

	public Tag<?> remove(String key) {
		return getValue().remove(key);
	}

	public void clear() {
		getValue().clear();
	}

	public boolean containsKey(String key) {
		return getValue().containsKey(key);
	}

	public boolean containsValue(Tag<?> value) {
		return getValue().containsValue(value);
	}

	public Collection<Tag<?>> values() {
		return getValue().values();
	}

	public Set<String> keySet() {
		return getValue().keySet();
	}

	public Set<Map.Entry<String, Tag<?>>> entrySet() {
		return new NonNullEntrySet<>(getValue().entrySet());
	}

	@Override
	public Iterator<Map.Entry<String, Tag<?>>> iterator() {
		return entrySet().iterator();
	}

	public void forEach(BiConsumer<String, Tag<?>> action) {
		getValue().forEach(action);
	}

	public <C extends Tag<?>> C get(String key, Class<C> type) {
		Tag<?> t = getValue().get(key);
		if (t != null) {
			return type.cast(t);
		}
		return null;
	}

	public Tag<?> get(String key) {
		return getValue().get(key);
	}

	public NumberTag<?> getNumberTag(String key) {
		return (NumberTag<?>) getValue().get(key);
	}

	public Number getNumber(String key) {
		return getNumberTag(key).getValue();
	}

	public ByteTag getByteTag(String key) {
		return get(key, ByteTag.class);
	}

	public ShortTag getShortTag(String key) {
		return get(key, ShortTag.class);
	}

	public IntTag getIntTag(String key) {
		return get(key, IntTag.class);
	}

	public LongTag getLongTag(String key) {
		return get(key, LongTag.class);
	}

	public FloatTag getFloatTag(String key) {
		return get(key, FloatTag.class);
	}

	public DoubleTag getDoubleTag(String key) {
		return get(key, DoubleTag.class);
	}

	public StringTag getStringTag(String key) {
		return get(key, StringTag.class);
	}

	public ByteArrayTag getByteArrayTag(String key) {
		return get(key, ByteArrayTag.class);
	}

	public IntArrayTag getIntArrayTag(String key) {
		return get(key, IntArrayTag.class);
	}

	public LongArrayTag getLongArrayTag(String key) {
		return get(key, LongArrayTag.class);
	}

	public ListTag<?> getListTag(String key) {
		return get(key, ListTag.class);
	}

	@SuppressWarnings("unchecked")
	public <R extends ListTag<?>> R getListTagAutoCast(String key) {
		return (R) get(key, ListTag.class);
	}

	public CompoundTag getCompoundTag(String key) {
		return get(key, CompoundTag.class);
	}

	public boolean getBoolean(String key) {
		Tag<?> t = get(key);
		return t instanceof ByteTag && ((ByteTag) t).asByte() > 0;
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		Tag<?> t = get(key);
		return t instanceof ByteTag ? ((ByteTag) t).asByte() > 0 : defaultValue;
	}

	public byte getByte(String key) {
		ByteTag t = getByteTag(key);
		return t == null ? ByteTag.ZERO_VALUE : t.asByte();
	}

	public byte getByte(String key, byte defaultValue) {
		ByteTag t = getByteTag(key);
		return t == null ? defaultValue : t.asByte();
	}

	public short getShort(String key) {
		ShortTag t = getShortTag(key);
		return t == null ? ShortTag.ZERO_VALUE : t.asShort();
	}

	public short getShort(String key, short defaultValue) {
		ShortTag t = getShortTag(key);
		return t == null ? defaultValue: t.asShort();
	}

	public int getInt(String key) {
		IntTag t = getIntTag(key);
		return t == null ? IntTag.ZERO_VALUE : t.asInt();
	}

	public int getInt(String key, int defaultValue) {
		IntTag t = getIntTag(key);
		return t == null ? defaultValue : t.asInt();
	}

	public long getLong(String key) {
		LongTag t = getLongTag(key);
		return t == null ? LongTag.ZERO_VALUE : t.asLong();
	}

	public long getLong(String key, long defaultValue) {
		LongTag t = getLongTag(key);
		return t == null ? defaultValue : t.asLong();
	}

	public float getFloat(String key) {
		FloatTag t = getFloatTag(key);
		return t == null ? FloatTag.ZERO_VALUE : t.asFloat();
	}

	public float getFloat(String key, float defaultValue) {
		FloatTag t = getFloatTag(key);
		return t == null ? defaultValue : t.asFloat();
	}

	public double getDouble(String key) {
		DoubleTag t = getDoubleTag(key);
		return t == null ? DoubleTag.ZERO_VALUE : t.asDouble();
	}

	public double getDouble(String key, double defaultValue) {
		DoubleTag t = getDoubleTag(key);
		return t == null ? defaultValue: t.asDouble();
	}

	public String getString(String key) {
		StringTag t = getStringTag(key);
		return t == null ? StringTag.ZERO_VALUE : t.getValue();
	}

	public String getString(String key, String defaultValue) {
		StringTag t = getStringTag(key);
		return t == null ? defaultValue: t.getValue();
	}

	public byte[] getByteArray(String key) {
		ByteArrayTag t = getByteArrayTag(key);
		return t == null ? ByteArrayTag.ZERO_VALUE : t.getValue();
	}

	public int[] getIntArray(String key) {
		IntArrayTag t = getIntArrayTag(key);
		return t == null ? IntArrayTag.ZERO_VALUE : t.getValue();
	}

	public long[] getLongArray(String key) {
		LongArrayTag t = getLongArrayTag(key);
		return t == null ? LongArrayTag.ZERO_VALUE : t.getValue();
	}

	/**
	 * Convenience function to get a ListTag&lt;FloatTag&gt; as an array of floats.
	 * @param key name of the ListTag
	 * @return null if key does not exist; empty array if key exists but list was empty; array of values otherwise
	 */
	public float[] getFloatTagListAsArray(String key) {
		ListTag<FloatTag> t = getListTagAutoCast(key);
		if (t == null) return null;
		List<FloatTag> floatTagList = t.getValue();
		float[] floats = new float[floatTagList.size()];
		for (int i = 0; i < floats.length; i++) {
			floats[i] = floatTagList.get(i).asFloat();
		}
		return floats;
	}

	/**
	 * Convenience function to get a ListTag&lt;DoubleTag&gt; as an array of doubles.
	 * @param key name of the ListTag
	 * @return null if key does not exist; empty array if key exists but list was empty; array of values otherwise
	 */
	public double[] getDoubleTagListAsArray(String key) {
		ListTag<DoubleTag> t = getListTagAutoCast(key);
		if (t == null) return null;
		List<DoubleTag> doubleTagList = t.getValue();
		double[] doubles = new double[doubleTagList.size()];
		for (int i = 0; i < doubles.length; i++) {
			doubles[i] = doubleTagList.get(i).asDouble();
		}
		return doubles;
	}

	public Tag<?> put(String key, Tag<?> tag) {
		return getValue().put(Objects.requireNonNull(key), Objects.requireNonNull(tag));
	}

	public Tag<?> putIfNotNull(String key, Tag<?> tag) {
		if (tag == null) {
			return this;
		}
		return put(key, tag);
	}

	public Tag<?> putBoolean(String key, boolean value) {
		return put(key, new ByteTag(value));
	}

	public Tag<?> putByte(String key, byte value) {
		return put(key, new ByteTag(value));
	}

	public Tag<?> putShort(String key, short value) {
		return put(key, new ShortTag(value));
	}

	public Tag<?> putInt(String key, int value) {
		return put(key, new IntTag(value));
	}

	public Tag<?> putLong(String key, long value) {
		return put(key, new LongTag(value));
	}

	public Tag<?> putFloat(String key, float value) {
		return put(key, new FloatTag(value));
	}

	public Tag<?> putDouble(String key, double value) {
		return put(key, new DoubleTag(value));
	}

	public Tag<?> putString(String key, String value) {
		return put(key, new StringTag(value));
	}

	public Tag<?> putByteArray(String key, byte[] value) {
		return put(key, new ByteArrayTag(value));
	}

	public Tag<?> putIntArray(String key, int[] value) {
		return put(key, new IntArrayTag(value));
	}

	public Tag<?> putLongArray(String key, long[] value) {
		return put(key, new LongArrayTag(value));
	}

	/**
	 * Convenience function to set a ListTag&lt;FloatTag&gt; from an array. If values is null then
	 * the specified key is REMOVED. Provide an empty array to indicate that an empty ListTag is desired.
	 * @param key name of ListTag
	 * @param values values to set
	 * @return new ListTag, or null if values was null
	 */
	public Tag<?> putFloatArrayAsTagList(String key, float[] values) {
		if (values == null) {
			remove(key);
			return null;
		}
		ListTag<FloatTag> listTag = new ListTag<>(FloatTag.class, values.length);
		for (float v : values) {
			listTag.addFloat(v);
		}
		return put(key, listTag);
	}

	/**
	 * Convenience function to set a ListTag&lt;DoubleTag&gt; from an array. If values is null then
	 * the specified key is REMOVED. Provide an empty array to indicate that an empty ListTag is desired.
	 * @param key name of ListTag
	 * @param values values to set
	 * @return new ListTag, or null if values was null
	 */
	public Tag<?> putDoubleArrayAsTagList(String key, double[] values) {
		if (values == null) {
			remove(key);
			return null;
		}
		ListTag<DoubleTag> listTag = new ListTag<>(DoubleTag.class, values.length);
		for (double v : values) {
			listTag.addDouble(v);
		}
		return put(key, listTag);
	}

	@Override
	public String valueToString(int maxDepth) {
		StringBuilder sb = new StringBuilder("{");
		boolean first = true;
		for (Map.Entry<String, Tag<?>> e : getValue().entrySet()) {
			sb.append(first ? "" : ",")
					.append(escapeString(e.getKey(), false)).append(":")
					.append(e.getValue().toString(decrementMaxDepth(maxDepth)));
			first = false;
		}
		sb.append("}");
		return sb.toString();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!super.equals(other) || size() != ((CompoundTag) other).size()) {
			return false;
		}
		for (Map.Entry<String, Tag<?>> e : getValue().entrySet()) {
			Tag<?> v;
			if ((v = ((CompoundTag) other).get(e.getKey())) == null || !e.getValue().equals(v)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int compareTo(CompoundTag o) {
		return Integer.compare(size(), o.getValue().size());
	}

	@Override
	public CompoundTag clone() {
		// Choose initial capacity based on default load factor (0.75) so all entries fit in map without resizing
		CompoundTag copy = new CompoundTag((int) Math.ceil(getValue().size() / 0.75f));
		for (Map.Entry<String, Tag<?>> e : getValue().entrySet()) {
			copy.put(e.getKey(), e.getValue().clone());
		}
		return copy;
	}
}
