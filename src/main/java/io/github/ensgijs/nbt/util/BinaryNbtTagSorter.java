package io.github.ensgijs.nbt.util;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Lean algorithm for sorting binary nbt data without full object deserialization.
 *
 * <p>
 * - This solution is solidly 4-5x faster than using full Tag objects when used on "real data".<br/>
 *
 * <pre>
 * CONTROL: deserialize to Tag objects, sort and write to bin nbt
 * - Note all "times" are in nanoseconds unless otherwise denoted.
 * - "mean pXX" is the mean take over the fastest XX percent of samples.
 * - The most "useful" metrics to look at are the p90's as they throw out the majority of the noise.
 * - "u(p50)" is the percentage of candidate samples which were faster than the median control sample.
 *   Where uXXXXX is the control p50 value and = XXXX is the count of candidate samples under the uXXX value.
 * - Tested on AMD 7950X3D CPU.
 * - Yes, the code really does run faster with the "if (DEBUG**" logic commented out. 2-5% faster.
 *
 * Benchmark                                       (filename) Score (ns)  Speedup
 * candidate               1_20_4/region/r.0.0/0299.11.9.snbt  13296.909   4.880x
 * control                 1_20_4/region/r.0.0/0299.11.9.snbt  64892.542
 *
 * candidate                 text_nbt_samples/named_item.snbt    418.780   4.459x
 * control                   text_nbt_samples/named_item.snbt   1867.330
 *
 * candidate  text_nbt_samples/named_tag_sample-with_bom.snbt     53.442  11.146x
 * control    text_nbt_samples/named_tag_sample-with_bom.snbt    595.680
 *
 * candidate         text_nbt_samples/unnamed_tag_sample.snbt     53.734  10.702x
 * control           text_nbt_samples/unnamed_tag_sample.snbt    575.070
 *
 * candidate   mca_palettes/block_states-1.20.4-6entries.snbt    278.948  15.503x
 * control     mca_palettes/block_states-1.20.4-6entries.snbt   4324.503
 *
 * candidate       text_nbt_samples/little_of_everything.snbt   3077.330   3.316x
 * control         text_nbt_samples/little_of_everything.snbt  10205.399
 * </pre>
 */
public class BinaryNbtTagSorter {
//    private static final boolean DEBUG_LOG = false;
//    public static final Stopwatch stopwatch = Stopwatch.createUnstarted();

    private static final byte END = (byte) 0;
    private static final byte BYTE = (byte) 1;
    private static final byte SHORT = (byte) 2;
    private static final byte INT = (byte) 3;
    private static final byte LONG = (byte) 4;
    private static final byte FLOAT = (byte) 5;
    private static final byte DOUBLE = (byte) 6;
    private static final byte BYTE_ARRAY = (byte) 7;
    private static final byte STRING = (byte) 8;
    private static final byte LIST = (byte) 9;
    private static final byte COMPOUND = (byte) 10;
    private static final byte INT_ARRAY = (byte) 11;
    private static final byte LONG_ARRAY = (byte) 12;

    private UnsafeFastByteArrayIO out;
    private TagDataComparator tagDataComparator = new TagDataComparator();

    public byte[] sort(byte[] nbtData) throws IOException {
        // if (DEBUG_LOG) System.out.printf("--- Starting sort for %d bytes ---\n", nbtData.length);
        UnsafeFastByteArrayIO in = new UnsafeFastByteArrayIO(nbtData);
        out = new UnsafeFastByteArrayIO(new byte[nbtData.length]);

        if (in.readByte() != COMPOUND) {
            throw new IOException("Root tag is not a CompoundTag.");
        }

        out.writeByte(COMPOUND);
        in.copyStringTo(out);
        // if (DEBUG_LOG) System.out.printf("Sorting root CompoundTag named '%s'...\n", in.debugReadString(1));
        scanCompound(in/*, 0*/);

        // if (DEBUG_LOG) System.out.printf("--- Sort finished. Output length: %d ---\n", out.length);
        byte[] ret = out.buffer;
        this.out = null;
        this.tagDataComparator = null;
        return ret;
    }

    private void scanCompound(UnsafeFastByteArrayIO tag/*, int level*/) throws IOException {
        // if (DEBUG_LOG) System.out.printf("  %s>> Scanning CompoundTag (tag.position=%d, tag.limit=%d)\n", "  ".repeat(level), tag.position, tag.limit);
        final List<NamedBinTag> childTags = new ArrayList<>();
        while (tag.position < tag.limit) {
            final byte type = tag.readByte();
            if (type == END) {
                break;
            }
            final int start = tag.position;
            tag.skipString();
            final int nameEndPos = tag.position;
            // if (DEBUG_LOG) System.out.printf("  %s- Reading tag '%s' (Type %d) at 0x%X\n", "  ".repeat(level), tag.debugReadString(start), type, start - 1);
            skipTagData(type, tag/*, level + 1*/);
            final int end = tag.position;
            childTags.add(new NamedBinTag(tag.buffer, type, start, nameEndPos, end));
        }

        childTags.sort(tagDataComparator);

        for (final NamedBinTag entryTag : childTags) {
            out.writeByte(entryTag.type);
            int pos = entryTag.position;
            entryTag.position = entryTag.start;
            entryTag.copyStringTo(out);
            entryTag.position = pos;

            if (entryTag.type == COMPOUND) {
                scanCompound(entryTag/*, level + 1*/);
            } else if (entryTag.type == LIST) {
                scanList(entryTag/*, level + 1*/);
            } else {
                // if (DEBUG_LOG) System.out.printf("  %s> Writing %d bytes of raw data from pos %d\n", "  ".repeat(level + 1), entryTag.remaining(), entryTag.position);
                entryTag.writeRemaining(out);
                // if (DEBUG_LOG) System.out.printf("  %s< Finished writing raw data\n", "  ".repeat(level + 1));
            }
        }
        out.writeByte(END);
        // if (DEBUG_LOG) System.out.printf("  %s<< Finished writing CompoundTag\n", "  ".repeat(level));
    }

    private void scanList(UnsafeFastByteArrayIO tag/*, int level*/) throws IOException {
        // if (DEBUG_LOG) System.out.printf("  %s>> Scanning ListTag (tag.position=%d, tag.limit=%d)\n", "  ".repeat(level), tag.position, tag.limit);
        final byte listType = tag.readByte();
        final int listLength = tag.readInt();

        out.writeByte(listType);
        out.writeInt(listLength);

        // if (DEBUG_LOG) System.out.printf("  %s- List type %d, length %d\n", "  ".repeat(level), listType, listLength);

        if (listType == COMPOUND || listType == LIST) {
            final List<UnsafeFastByteArrayIO> listItems = new ArrayList<>(listLength);
            for (int i = 0; i < listLength; i++) {
                final int start = tag.position;
                skipTagData(listType, tag/*, level + 1*/);
                final int end = tag.position;
                listItems.add(new UnsafeFastByteArrayIO(tag.buffer, start, end));
            }

            for (final UnsafeFastByteArrayIO entry : listItems) {
                if (listType == COMPOUND) {
                    scanCompound(entry/*, level + 1*/);
                } else {
                    scanList(entry/*, level + 1*/);
                }
            }
        } else {
            // if (DEBUG_LOG) System.out.printf("  %s> Writing %d bytes of raw data from pos %d\n", "  ".repeat(level + 1), tag.remaining(), tag.position);
            tag.writeRemaining(out);
            // if (DEBUG_LOG) System.out.printf("  %s< Finished writing raw data\n", "  ".repeat(level + 1));
        }
        // if (DEBUG_LOG) System.out.printf("  %s<< Finished writing ListTag\n", "  ".repeat(level));
    }

    private void skipTagData(final byte type, final UnsafeFastByteArrayIO tag/*, int level*/) throws IOException {
        // if (DEBUG_LOG) System.out.printf("  %s> Skipping tag data for type %d at pos %d\n", "  ".repeat(level), type, tag.position);
        switch (type) {
            case BYTE:
                tag.position ++;
                break;
            case SHORT:
                tag.position += 2;
                break;
            case INT, FLOAT:
                tag.position += 4;
                break;
            case LONG, DOUBLE:
                tag.position += 8;
                break;
            case STRING:
                tag.skip(tag.readUShort());
                break;
            case BYTE_ARRAY:
                tag.skip(tag.readInt());
                break;
            case INT_ARRAY:
                tag.skip(tag.readInt() * 4);
                break;
            case LONG_ARRAY:
                tag.skip(tag.readInt() * 8);
                break;
            case LIST:
                final byte listType = tag.readByte();
                final int listLength = tag.readInt();
                for (int i = 0; i < listLength; i++) {
                    skipTagData(listType, tag/*, level + 1*/);
                }
                break;
            case COMPOUND:
                while (tag.position < tag.limit) {
                    final byte childType = tag.readByte();
                    if (childType == END) {
                        break;
                    }
                    tag.skip(tag.readUShort());  // skip name string
                    skipTagData(childType, tag/*, level + 1*/);
                }
                break;
            default:
                throw new IOException("Unknown tag type: " + type + " at pos " + tag.position);
        }
        // if (DEBUG_LOG) System.out.printf("  %s< Finished skipping tag data for type %d at pos %d\n", "  ".repeat(level), type, tag.position);
    }

    // --- Helper Classes ---

    private static class UnsafeFastByteArrayIO {
//        private static final boolean DEBUG_IO = DEBUG_LOG;

        protected final byte[] buffer;
        protected final int start;
        protected final int limit;
        protected int position;

        /** aka "exclusive end position" which should not be read */
        public int length() {
            return limit - start;
        }

        public int remaining() {
            return limit - position;
        }

        public UnsafeFastByteArrayIO(byte[] buffer) {
            this.buffer = buffer;
            this.start = 0;
            this.position = 0;
            this.limit = buffer.length;
        }

        public UnsafeFastByteArrayIO(byte[] buffer, int start, int limit) {
            // if (DEBUG_IO && start < 0 || limit < start || limit >= buffer.length) throw new IndexOutOfBoundsException();
            this.buffer = buffer;
            this.start = start;
            this.limit = limit;
            this.position = start;
        }

        private void checkPosition(int requiredBytes) {
            if (position + requiredBytes > limit)
                throw new BufferOverflowException();
        }

        public void skip(int count) {
            // if (DEBUG_IO) checkPosition(count);
            position += count;
        }

        public byte readByte() {
            // if (DEBUG_IO) checkPosition(1);
            return buffer[position++];
        }

        public int readUShort() {
            // if (DEBUG_IO) checkPosition(2);
            return ((0xFF & buffer[position++]) << 8) | (0xFF & buffer[position++]);
        }

        public int peekUShort() {
            // if (DEBUG_IO) checkPosition(2);
            return ((0xFF & buffer[position]) << 8) | (0xFF & buffer[position+1]);
        }

        public int readInt() {
            // if (DEBUG_IO) checkPosition(4);
            return ((0xFF & buffer[position++]) << 24) | ((0xFF & buffer[position++]) << 16)
                    | ((0xFF & buffer[position++]) << 8) | (0xFF & buffer[position++]);
        }

        public void skipString() {
            int length = readUShort();
            // if (DEBUG_IO) checkPosition(length);
            position += length;
        }

        public String debugReadString(int position) {
            int length = ((0xFF & buffer[position]) << 8) | (0xFF & buffer[position+1]);
            return new String(buffer, position + 2, length, StandardCharsets.UTF_8);
        }

        @Override
        public String toString() {
            return String.format("start=%d; pos=%d; limit=%d; length=%d; remaining=%d",
                    start, position, limit, length(), remaining());
        }


        /**
         * Writes all remaining data from position to limit.
         */
        public void writeRemaining(UnsafeFastByteArrayIO out) {
            out.write(buffer, position, remaining());
        }

        private void write(byte[] source, int sourcePos, int length) {
            System.arraycopy(source, sourcePos, this.buffer, this.position, length);
            this.position += length;
        }

        public void writeByte(byte v) {
            // if (DEBUG_IO) checkPosition(1);
            buffer[position++] = v;
        }

        public void writeByte(int v) {
            // if (DEBUG_IO) checkPosition(1);
            buffer[position++] = (byte) v;
        }

        public void writeShort(int v) {
            // if (DEBUG_IO) checkPosition(2);
            buffer[position++] = (byte) (v >>> 8);
            buffer[position++] = (byte) v;
        }

        public void writeInt(int v) {
            // if (DEBUG_IO) checkPosition(4);
            buffer[position++] = (byte) (v >>> 24);
            buffer[position++] = (byte) (v >>> 16);
            buffer[position++] = (byte) (v >>> 8);
            buffer[position++] = (byte) v;
        }

        public void copyStringTo(UnsafeFastByteArrayIO destination) {
            int length = peekUShort() + 2;
            // if (DEBUG_IO) { checkPosition(length); destination.checkPosition(length); }
            System.arraycopy(buffer, position, destination.buffer, destination.position, length);
            position += length;
            destination.position += length;
        }
    }

    private static class NamedBinTag extends UnsafeFastByteArrayIO {
        final byte type;
        final int nameEndPos;

        /**
         * @param buffer binary nbt data
         * @param type tag type
         * @param start start position
         * @param nameEndPos exclusive ending position of the tag's name (use .position after {@link #skipString()})
         * @param limit an exclusive valid end position
         */
        public NamedBinTag(byte[] buffer, byte type, int start, int nameEndPos, int limit) {
            super(buffer, start, limit);
            this.type = type;
            this.nameEndPos = nameEndPos;
            this.position = nameEndPos;
        }

        // debugging function
        public String nameStr() {
            return new String(buffer, start + 2, nameEndPos - start - 2, StandardCharsets.UTF_8);
        }

        @Override
        public String toString() {
            return super.toString() + "; name='" + nameStr() + "'";
        }
    }

    private static class TagDataComparator implements Comparator<NamedBinTag> {
        @Override
        public int compare(NamedBinTag o1, NamedBinTag o2) {
            return Arrays.compare(
                    o1.buffer, o1.start + 2, o1.nameEndPos,
                    o2.buffer, o2.start + 2, o2.nameEndPos);
        }
    }
}