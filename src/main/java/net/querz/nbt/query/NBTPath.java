package net.querz.nbt.query;

import net.querz.nbt.query.evaluator.*;
import net.querz.nbt.tag.*;
import net.querz.util.ArgValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NBTPath {
    private static final NBTPath IDENTITY_PATH = new NBTPath(Collections.emptyList());

    List<Evaluator> evalChain;

    protected NBTPath(List<Evaluator> evalChain) {
        this.evalChain = evalChain;
    }

    static String markLocation(String str, int pos, int width) {
        assert width >= 0;
        if (pos < 0) return "<>" + str;
        if (pos >= str.length()) return str + "<>";
        if (width <= 0) {
            return str.substring(0, pos) + "<>" + str.substring(pos);
        }
        return str.substring(0, pos) + "<" + str.substring(pos, pos + width) + ">" + str.substring(pos + width);
    }

    public static NBTPath of(String selector) {
        if (selector == null || selector.isEmpty() || selector.equals(".")) return IDENTITY_PATH;
        List<Evaluator> evalChain = new ArrayList<>();
        int partPos = 0;
        String[] parts = selector.split("[.]", -1);
        boolean allowEmpty = true;

        for (String part : parts) {
            if (part.isEmpty() && !allowEmpty) throw new IllegalArgumentException("empty name at: " + markLocation(selector, partPos, 0));
            int bracketOpenIdx = part.indexOf('[');
            int bracketCloseIdx = part.indexOf(']');
            String name;
            if (bracketOpenIdx < 0 || bracketCloseIdx < bracketOpenIdx) {
                if (bracketCloseIdx >= 0) throw new IllegalArgumentException("unexpected close bracket at: " + markLocation(selector, bracketCloseIdx + partPos, 1));
                name = part;
            } else {
                if (bracketCloseIdx < 0) throw new IllegalArgumentException("unclose bracket at: " + markLocation(selector, bracketOpenIdx + partPos, 1));
                name = part.substring(0, bracketOpenIdx);
            }

            if (!name.isEmpty()) {
                evalChain.add(new NameEvaluator(name));
            } else if (!evalChain.isEmpty() || !allowEmpty) {
                throw new IllegalArgumentException("expected map key name at: " + markLocation(selector, partPos, 0));
            }
            while (bracketOpenIdx >= 0) {
                if (bracketCloseIdx < 0)
                    throw new IllegalArgumentException("missing close bracket for: " + markLocation(selector, bracketOpenIdx + partPos, 1));
                String valStr = part.substring(bracketOpenIdx + 1, bracketCloseIdx);
                if (valStr.isEmpty() || !valStr.chars().allMatch(Character::isDigit))
                    throw new IllegalArgumentException("list index must be a positive number at: " + markLocation(selector, bracketOpenIdx + partPos + 1, bracketCloseIdx - bracketOpenIdx - 1));

                evalChain.add(new IndexEvaluator(Integer.parseInt(valStr)));
                bracketOpenIdx = part.indexOf('[', bracketCloseIdx + 1);
                if (bracketOpenIdx != -1 && bracketOpenIdx != bracketCloseIdx + 1) {
                    throw new IllegalArgumentException("invalid path string - error at: " + markLocation(selector, bracketCloseIdx + 1 + partPos, 0));
                }
                bracketCloseIdx = part.indexOf(']', bracketCloseIdx + 1);
            }
            if (bracketCloseIdx > 0)
                throw new IllegalArgumentException();
            partPos += name.length() + 1;
            allowEmpty = false;
        }
        return new NBTPath(evalChain);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean canDot = false;
        for (Evaluator evaluator : evalChain) {
            if (evaluator instanceof NameEvaluator) {
                if (canDot) sb.append('.');
            }
            sb.append(evaluator);
            canDot = true;
        }
        return sb.toString();
    }

    protected String makeErrorHint(int atIndex) {
        return makeErrorHint(evalChain.get(atIndex));
    }

    protected String makeErrorHint(Evaluator atEvaluator) {
        StringBuilder sb = new StringBuilder();
        boolean canDot = false;
        for (Evaluator evaluator : evalChain) {
            if (evaluator instanceof NameEvaluator) {
                if (canDot) sb.append('.');
            }
            if (evaluator == atEvaluator) sb.append("<");
            sb.append(evaluator);
            if (evaluator == atEvaluator) sb.append(">");
            canDot = true;
        }
        return sb.toString();
    }

    public <R> R get(Tag<?> root) {
        Object node = root;
        for (Evaluator evaluator : evalChain) {
            if (node == null) return null;
            if (!(node instanceof Tag)) throw new IllegalStateException("Expected TAG but was " + node.getClass().getTypeName() + "\n" + makeErrorHint(evaluator));
            node = evaluator.eval((Tag<?>) node);
        }
        return (R) node;
    }

    public <R extends Tag<?>> R getTag(Tag<?> root) {
        return get(root);
    }

    public String getString(Tag<?> root) {
        Object value = get(root);
        if (value instanceof StringTag) return ((StringTag) value).getValue();
        if (value instanceof String) return (String) value;
        if (value == null) return null;
        throw new ClassCastException("expected string but was " + value.getClass().getTypeName());
    }

    public boolean getBoolean(Tag<?> root) {
        Object value = get(root);
        return value instanceof ByteTag && ((ByteTag) value).asByte() > 0;
    }

    public byte getByte(Tag<?> root) {
        Object value = get(root);
        if (value instanceof NumberTag) {
            return ((NumberTag<?>) value).asByte();
        }
        if (value == null) return 0;
        return (byte) value;
    }

    public short getShort(Tag<?> root) {
        Object value = get(root);
        if (value instanceof NumberTag) {
            return ((NumberTag<?>) value).asShort();
        }
        if (value == null) return 0;
        return (short) value;
    }

    public int getInt(Tag<?> root) {
        Object value = get(root);
        if (value instanceof NumberTag) {
            return ((NumberTag<?>) value).asInt();
        }
        if (value == null) return 0;
        return (int) value;
    }

    public long getLong(Tag<?> root) {
        Object value = get(root);
        if (value instanceof NumberTag) {
            return ((NumberTag<?>) value).asLong();
        }
        if (value == null) return 0;
        return (long) value;
    }

    public float getFloat(Tag<?> root) {
        Object value = get(root);
        if (value instanceof NumberTag) {
            return ((NumberTag<?>) value).asFloat();
        }
        if (value == null) return 0;
        return (float) value;
    }

    public double getDouble(Tag<?> root) {
        Object value = get(root);
        if (value instanceof NumberTag) {
            return ((NumberTag<?>) value).asDouble();
        }
        if (value == null) return 0;
        return (double) value;
    }

    public <T extends Tag<?>> T putTag(Tag<?> root, Tag<?> value) {
        return putTag(root, value, false);
    }

    /**
     *
     * @param root root tag to evaluate this path from
     * @param value tag value to set, may be null to remove the tag
     * @param createParents when true CompoundTag's will be created as necessary. Will not create ListTag's or ArrayTag's.
     * @param <T> value tag type
     * @return the previous value associated with key, or null if there was no mapping for key.
     */
    @SuppressWarnings("unchecked")
    public <T extends Tag<?>> T putTag(Tag<?> root, Tag<?> value, boolean createParents) {
        ArgValidator.requireValue(root, "root");
        ArgValidator.check(evalChain.size() > 0);
        Tag<?> parent;
        Tag<?> node = root;
        for (int i = 0, l = evalChain.size() - 1; i < l; i ++) {
            parent = node;
            Evaluator evaluator = evalChain.get(i);
            node = (Tag<?>) evaluator.eval(node);
            if (node == null && createParents) {
                if (parent instanceof CompoundTag && evaluator instanceof NameEvaluator && evalChain.get(i + 1) instanceof NameEvaluator) {
                    node = new CompoundTag();
                    ((CompoundTag) parent).put(((NameEvaluator) evaluator).key(), node);
                } else {
                    throw new UnsupportedOperationException("cannot auto-create child tag at " + makeErrorHint(evaluator));
                }
            }

            if (node == null) {
                throw new IllegalArgumentException("Tag does not exist: " + makeErrorHint(evaluator));
            }
        }
        Evaluator last = evalChain.get(evalChain.size() - 1);
        if (node instanceof CompoundTag) {
            if (!(last instanceof NameEvaluator)) throw new IllegalArgumentException();
            if (value != null) {
                node = ((CompoundTag) node).put(((NameEvaluator) last).key(), value);
            } else {
                node = ((CompoundTag) node).remove(((NameEvaluator) last).key());
            }
        } else {
            throw new UnsupportedOperationException("expected CompoundTag but was " + node.getClass().getSimpleName() + " at: "
                    + makeErrorHint(last));
        }
        return (T) node;
    }


    public int size(Tag<?> root) {
        Tag<?> tag = getTag(root);
        if (tag instanceof CompoundTag) {
            return ((CompoundTag) tag).size();
        } else if (tag instanceof ListTag) {
            return ((ListTag<?>) tag).size();
        } else if (tag instanceof ArrayTag) {
            return ((ArrayTag<?>) tag).length();
        } else if (tag instanceof StringTag) {
            String str = ((StringTag) tag).getValue();
            return str != null ? str.length() : 0;
        }
        if (tag == null) return 0;
        throw new IllegalArgumentException("don't know how to get the size of " + tag.getClass().getTypeName());
    }

    public boolean exists(Tag<?> root) {
        return get(root) != null;
    }
}
