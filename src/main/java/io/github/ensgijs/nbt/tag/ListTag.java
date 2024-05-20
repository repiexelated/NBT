package io.github.ensgijs.nbt.tag;

import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import io.github.ensgijs.nbt.io.MaxDepthIO;

/**
 * ListTag represents a typed List in the nbt structure.
 * An empty {@link ListTag} will be of type {@link EndTag} (unknown type).
 * The type of an empty untyped {@link ListTag} can be set by using any of the {@code add()}
 * methods or any of the {@code as...List()} methods.
 */
public class ListTag<T extends Tag<?>> extends Tag<List<T>> implements Iterable<T>, Comparable<ListTag<T>>, MaxDepthIO, Collection<T> {

	public static final byte ID = 9;

	private Class<?> typeClass = null;

	private ListTag(int initialCapacity) {
		super(createEmptyValue(initialCapacity));
	}

	/**
	 * Creates a new ListTag that uses the given list.
	 * @param usingList List instance to use to back this ListTag. Values are NOT cloned.
	 * @throws NullPointerException if usingList is null or contains null elements.
	 */
	@SuppressWarnings("unchecked")
	public ListTag(List<T> usingList) {
		super(usingList);
		if (usingList.stream().anyMatch(Objects::isNull))
			throw new NullPointerException("usingList must not contain nulls");
		this.typeClass = (Class<T>) ((ParameterizedType) usingList.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}

	/** {@inheritDoc} */
	@Override
	public byte getID() {
		return ID;
	}

	/**
	 * <p>Creates a non-type-safe ListTag. Its element type will be set after the first
	 * element was added.</p>
	 *
	 * <p>This is an internal helper method for cases where the element type is not known
	 * at construction time. Use {@link #ListTag(Class)} when the type is known.</p>
	 *
	 * @return A new non-type-safe ListTag
	 */
	public static ListTag<?> createUnchecked(Class<?> typeClass) {
		return createUnchecked(typeClass, 3);
	}

	/**
	 * <p>Creates a non-type-safe ListTag. Its element type will be set after the first
	 * element was added.</p>
	 *
	 * <p>This is an internal helper method for cases where the element type is not known
	 * at construction time. Use {@link #ListTag(Class)} when the type is known.</p>
	 *
	 * @return A new non-type-safe ListTag
	 */
	public static ListTag<?> createUnchecked(Class<?> typeClass, int initialCapacity) {
		ListTag<?> list = new ListTag<>(initialCapacity);
		list.typeClass = typeClass;
		return list;
	}

	/**
	 * <p>Creates an empty mutable list to be used as empty value of ListTags.</p>
	 *
	 * @param <T>             Type of the list elements
	 * @param initialCapacity The initial capacity of the returned List
	 * @return An instance of {@link java.util.List} with an initial capacity of 3
	 */
	private static <T> List<T> createEmptyValue(int initialCapacity) {
		return new ArrayList<>(initialCapacity);
	}

	/**
	 * @param typeClass The exact class of the elements
	 * @throws IllegalArgumentException When {@code typeClass} is {@link EndTag}{@code .class}
	 * @throws NullPointerException     When {@code typeClass} is {@code null}
	 */
	public ListTag(Class<? super T> typeClass) throws IllegalArgumentException, NullPointerException {
		this(typeClass, 3);
	}

	/**
	 * @param typeClass       The exact class of the elements
	 * @param initialCapacity Initial capacity of list
	 * @throws IllegalArgumentException When {@code typeClass} is {@link EndTag}{@code .class}
	 * @throws NullPointerException     When {@code typeClass} is {@code null}
	 */
	public ListTag(Class<? super T> typeClass, int initialCapacity) throws IllegalArgumentException, NullPointerException {
		super(createEmptyValue(initialCapacity));
		if (typeClass == EndTag.class) {
			throw new IllegalArgumentException("cannot create ListTag with EndTag elements");
		}
		this.typeClass = Objects.requireNonNull(typeClass);
	}

	public Class<?> getTypeClass() {
		return typeClass == null ? EndTag.class : typeClass;
	}

	/** {@inheritDoc} */
	@Override
	public int size() {
		return getValue().size();
	}

	/** {@inheritDoc} */
	@Override
	public boolean isEmpty() {
		return getValue().isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return getValue().contains(o);
	}

	public T remove(int index) {
		return getValue().remove(index);
	}

	@Override
	public void clear() {
		getValue().clear();
	}

	public boolean contains(T t) {
		return getValue().contains(t);
	}

	@Override
	public boolean containsAll(Collection<?> tags) {
		return getValue().containsAll(tags);
	}

	public void sort(Comparator<T> comparator) {
		getValue().sort(comparator);
	}

	@Override
	public Iterator<T> iterator() {
		return getValue().iterator();
	}

	@Override
	public Object[] toArray() {
		return getValue().toArray();
	}

	@Override
	public <T1> T1[] toArray(T1[] a) {
		return getValue().toArray(a);
	}

	@Override
	public Spliterator<T> spliterator() {
		return getValue().spliterator();
	}

	@Override
	public void forEach(Consumer<? super T> action) {
		getValue().forEach(action);
	}

	public Stream<T> stream() {
		return getValue().stream();
	}

	public T set(int index, T t) {
		return getValue().set(index, Objects.requireNonNull(t));
	}

	/**
	 * Adds a Tag to this ListTag after the last index.
	 *
	 * @param t The element to be added.
	 * @return true if this collection changed as a result of the call
	 */
	public boolean add(T t) {
		return getValue().add(t);
	}

	@Override
	public boolean remove(Object o) {
		return getValue().remove(o);
	}

	public void add(int index, T t) {
		Objects.requireNonNull(t);
		if (getTypeClass() == EndTag.class) {
			typeClass = t.getClass();
		} else if (typeClass != t.getClass()) {
			throw new ClassCastException(
					String.format("cannot add %s to ListTag<%s>",
							t.getClass().getSimpleName(),
							typeClass.getSimpleName()));
		}
		getValue().add(index, t);
	}

	/**
	 * Adds all the given Tags to this ListTag after the last index.
	 *
	 * @param t The element to be added.
	 * @return true if this collection changed as a result of the call
	 */
	@Override
	public boolean addAll(Collection<? extends T> t) {
		return getValue().addAll(t);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return getValue().removeAll(c);
	}

	@Override
	public boolean removeIf(Predicate<? super T> filter) {
		return getValue().removeIf(filter);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return getValue().retainAll(c);
	}

	public void addAll(int index, Collection<T> t) {
		int i = 0;
		for (T tt : t) {
			add(index + i, tt);
			i++;
		}
	}

	public void addBoolean(boolean value) {
		addUnchecked(new ByteTag(value));
	}

	public void addByte(byte value) {
		addUnchecked(new ByteTag(value));
	}

	public void addShort(short value) {
		addUnchecked(new ShortTag(value));
	}

	public void addInt(int value) {
		addUnchecked(new IntTag(value));
	}

	public void addLong(long value) {
		addUnchecked(new LongTag(value));
	}

	public void addFloat(float value) {
		addUnchecked(new FloatTag(value));
	}

	public void addDouble(double value) {
		addUnchecked(new DoubleTag(value));
	}

	public void addString(String value) {
		addUnchecked(new StringTag(value));
	}

	public void addByteArray(byte[] value) {
		addUnchecked(new ByteArrayTag(value));
	}

	public void addIntArray(int[] value) {
		addUnchecked(new IntArrayTag(value));
	}

	public void addLongArray(long[] value) {
		addUnchecked(new LongArrayTag(value));
	}

	public T get(int index) {
		return getValue().get(index);
	}

	public int indexOf(T t) {
		return getValue().indexOf(t);
	}

	@SuppressWarnings("unchecked")
	public <L extends Tag<?>> ListTag<L> asTypedList(Class<L> type) {
		checkTypeClass(type);
		return (ListTag<L>) this;
	}

	public ListTag<ByteTag> asByteTagList() {
		return asTypedList(ByteTag.class);
	}

	public ListTag<ShortTag> asShortTagList() {
		return asTypedList(ShortTag.class);
	}

	public ListTag<IntTag> asIntTagList() {
		return asTypedList(IntTag.class);
	}

	public ListTag<LongTag> asLongTagList() {
		return asTypedList(LongTag.class);
	}

	public ListTag<FloatTag> asFloatTagList() {
		return asTypedList(FloatTag.class);
	}

	public ListTag<DoubleTag> asDoubleTagList() {
		return asTypedList(DoubleTag.class);
	}

	public ListTag<StringTag> asStringTagList() {
		return asTypedList(StringTag.class);
	}

	public ListTag<ByteArrayTag> asByteArrayTagList() {
		return asTypedList(ByteArrayTag.class);
	}

	public ListTag<IntArrayTag> asIntArrayTagList() {
		return asTypedList(IntArrayTag.class);
	}

	public ListTag<LongArrayTag> asLongArrayTagList() {
		return asTypedList(LongArrayTag.class);
	}

	@SuppressWarnings("unchecked")
	public ListTag<ListTag<?>> asListTagList() {
		checkTypeClass(ListTag.class);
		typeClass = ListTag.class;
		return (ListTag<ListTag<?>>) this;
	}

	public ListTag<CompoundTag> asCompoundTagList() {
		return asTypedList(CompoundTag.class);
	}

	/** {@inheritDoc} */
	@Override
	public String valueToString(int maxDepth) {
		StringBuilder sb = new StringBuilder("{\"type\":\"").append(getTypeClass().getSimpleName()).append("\",\"list\":[");
		for (int i = 0; i < size(); i++) {
			sb.append(i > 0 ? "," : "").append(get(i).valueToString(decrementMaxDepth(maxDepth)));
		}
		sb.append("]}");
		return sb.toString();
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!super.equals(other) || size() != ((ListTag<?>) other).size() || getTypeClass() != ((ListTag<?>) other)
				.getTypeClass()) {
			return false;
		}
		for (int i = 0; i < size(); i++) {
			if (!get(i).equals(((ListTag<?>) other).get(i))) {
				return false;
			}
		}
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Objects.hash(getTypeClass().hashCode(), getValue().hashCode());
	}

	/** {@inheritDoc} */
	@Override
	public int compareTo(ListTag<T> o) {
		int k = Integer.compare(this.size(), o.size());
		if (k != 0) return k;
		k = this.typeClass == o.typeClass ? 0 : this.typeClass.getName().compareTo(o.typeClass.getName());
		for (int i = 0, len = size(); k == 0 && i < len; i++) {
			k = Tag.compare(this.get(i), o.get(i));
		}
		return k;
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public ListTag<T> clone() {
		ListTag<T> copy = new ListTag<>(this.size());
		// assure type safety for clone
		copy.typeClass = typeClass;
		for (T t : getValue()) {
			copy.add((T) t.clone());
		}
		return copy;
	}

	//TODO: make private
	@SuppressWarnings("unchecked")
	public void addUnchecked(Tag<?> tag) {
		if (getTypeClass() != EndTag.class && typeClass != tag.getClass()) {
			throw new IllegalArgumentException(String.format(
					"cannot add %s to ListTag<%s>",
					tag.getClass().getSimpleName(), typeClass.getSimpleName()));
		}
		add(size(), (T) tag);
	}

	private void checkTypeClass(Class<?> clazz) {
		if (getTypeClass() != EndTag.class && typeClass != clazz) {
			throw new ClassCastException(String.format(
					"cannot cast ListTag<%s> to ListTag<%s>",
					typeClass.getSimpleName(), clazz.getSimpleName()));
		}
	}
}
