package net.rossquerz.nbt.io;

import net.rossquerz.nbt.tag.Tag;
import net.rossquerz.util.ArgValidator;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class NamedTag implements Cloneable, Comparable<NamedTag> {
	private static final Pattern NON_QUOTE_PATTERN = Pattern.compile("^[a-zA-Z0-9_.+\\-]+$");

	private String name;
	private Tag<?> tag;

	protected NamedTag() {}

	/**
	 * Copy constructor. Performs a deep copy of the given other NamedTag (calls {@code other.getTag().clone()}).
	 * Other's name may be null but the tag may not be.
	 */
	public NamedTag(NamedTag other) {
		ArgValidator.requireValue(other, "other");
		ArgValidator.requireValue(other.tag, "other.tag");
		this.name = other.name;
		this.tag = other.getTag().clone();
	}

	/**
	 * Creates a NamedTag that references the given tag.
	 * @param name nullable name
	 * @param tag non-null tag
	 */
	public NamedTag(String name, Tag<?> tag) {
		ArgValidator.requireValue(tag, "tag");
		this.name = name;
		this.tag = tag;
	}

    public NamedTag(Map.Entry<String, Tag<?>> entry) {
		this(entry.getKey(), Objects.requireNonNull(entry.getValue()));
    }

	/** nullable */
    public void setName(String name) {
		this.name = name;
	}

	/** must be non-null */
	public void setTag(Tag<?> tag) {
		ArgValidator.requireValue(tag, "tag");
		this.tag = tag;
	}

	/** nullable */
	public String getName() {
		return name;
	}

	/** non-null */
	public Tag<?> getTag() {
		return tag;
	}

	/** non-null */
	@SuppressWarnings("unchecked")
	public <T extends Tag<?>> T getTagAutoCast() {
		return (T) getTag();
	}

	/**
	 * Wraps the name in quotes if it contains anything other than ascii letters (a-z), numbers, underscore, dash, or dot.
	 * If name is null, then null is returned.
	 */
	public String getEscapedName() {
		if (name != null && !NON_QUOTE_PATTERN.matcher(name).matches()) {
			StringBuilder sb = new StringBuilder();
			sb.append('"');
			for (int i = 0; i < name.length(); i++) {
				char c = name.charAt(i);
				if (c == '\\' || c == '"') {
					sb.append('\\');
				}
				sb.append(c);
			}
			sb.append('"');
			return sb.toString();
		}
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof NamedTag)) return false;
		NamedTag other = (NamedTag) o;
		return Objects.equals(this.name, other.name) &&
				Objects.equals(this.tag, other.tag);
	}

	public static int compare(NamedTag o1, NamedTag o2) {
		if (o1 == o2) return 0;
		if (o1 == null) return -1;
		if (o2 == null) return 1;
		return o1.name.compareTo(o2.name);
	}

	@Override
	public NamedTag clone() {
		return new NamedTag(this);
	}

	@Override
	public int compareTo(NamedTag o) {
		return compare(this, o);
	}
}
