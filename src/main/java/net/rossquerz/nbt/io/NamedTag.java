package net.rossquerz.nbt.io;

import net.rossquerz.nbt.tag.Tag;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class NamedTag {
	private static final Pattern NON_QUOTE_PATTERN = Pattern.compile("^[a-zA-Z0-9_.+\\-]+$");

	private String name;
	private Tag<?> tag;

	public NamedTag(String name, Tag<?> tag) {
		this.name = name;
		this.tag = tag;
	}

    public NamedTag(Map.Entry<String, Tag<?>> entry) {
		this(entry.getKey(), entry.getValue());
    }

    public void setName(String name) {
		this.name = name;
	}

	public void setTag(Tag<?> tag) {
		this.tag = tag;
	}

	public String getName() {
		return name;
	}

	public Tag<?> getTag() {
		return tag;
	}

	@SuppressWarnings("unchecked")
	public <T extends Tag<?>> T getTagAutoCast() {
		return (T) tag;
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
}
