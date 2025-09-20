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
 * - This solution is solidly 5-6x faster than using full Tag objects (takes 15% of the time!)<br/>
 *
 * <pre>
 * SAMPLES: 100,000
 * CONTROL: deserialize to Tag objects, sort and write to bin nbt
 * - Note all "times" are in nanoseconds unless otherwise denoted.
 * - "mean pXX" is the mean take over the fastest XX percent of samples.
 * - The most "useful" metrics to look at are the p90's as they throw out the majority of the noise.
 * - "u(p50)" is the percentage of candidate samples which were faster than the median control sample.
 *   Where uXXXXX is the control p50 value and = XXXX is the count of candidate samples under the uXXX value.
 * - Tested on AMD 7950X3D CPU.
 * - Yes, the code really does run faster with the "if (DEBUG**" logic commented out. 2-5% faster.
 *
 * [1_20_4/region/r.0.0/0299.11.9.snbt]
 *   Candidate Time.: 1.468 s
 *     mean....: 14682.619173808262
 *     mean p99: 14280.742424242424
 *     mean p90: 13028.333333333334
 *     low.....: 13200 (2, 63, 473)
 *     p50.....: 14200
 *     p90.....: 14900
 *     p95.....: 15300
 *     p99.....: 22800
 *     p999....: 74200
 *     p9999...: 125800
 *     high-2..: 1700000
 *     high-1..: 1730900
 *     high....: 3480600
 *   Control Time...: 10.05 s
 *     mean....: 100508.1269187308
 *     mean p99: 97015.96363636364
 *     mean p90: 87543.55858585858
 *     low.....: 90500 (2, 3, 6)
 *     p50.....: 96000
 *     p90.....: 100200
 *     p95.....: 102800
 *     p99.....: 168200
 *     p999....: 1476800
 *     p9999...: 1943000
 *     high-2..: 2247800
 *     high-1..: 2448700
 *     high....: 24608300
 *   Speedup:
 *     mean....: +149.015% (6.845x)
 *     mean p99: +148.675% (6.793x)
 *     mean p90: +148.183% (6.719x)
 *     low.....: +149.084% (6.856x)
 *     u(p50): u96000 = 99970 (99.969%)
 *
 * [text_nbt_samples/named_item.snbt]
 *   Candidate Time.: 57.72 ms
 *     mean....: 577.2362276377236
 *     mean p99: 561.9494949494949
 *     mean p90: 514.7212121212121
 *     low.....: 500 (48228, 92967, 96461)
 *     p50.....: 600
 *     p90.....: 600
 *     p95.....: 700
 *     p99.....: 1100
 *     p999....: 3000
 *     p9999...: 5200
 *     high-2..: 23400
 *     high-1..: 72200
 *     high....: 77600
 *   Control Time...: 317.6 ms
 *     mean....: 3175.580244197558
 *     mean p99: 3003.5343434343436
 *     mean p90: 2724.110101010101
 *     low.....: 2600 (2, 2071, 25516)
 *     p50.....: 2900
 *     p90.....: 3300
 *     p95.....: 3600
 *     p99.....: 5200
 *     p999....: 18600
 *     p9999...: 75600
 *     high-2..: 1538300
 *     high-1..: 1874700
 *     high....: 1941200
 *   Speedup:
 *     mean....: +138.474% (5.501x)
 *     mean p99: +136.957% (5.345x)
 *     mean p90: +136.431% (5.292x)
 *     low.....: +135.484% (5.200x)
 *     u(p50): u2900 = 99897 (99.896%)
 *
 * [text_nbt_samples/named_tag_sample-with_bom.snbt]
 *   Candidate Time.: 12.72 ms
 *     mean....: 127.15972840271597
 *     mean p99: 123.88383838383838
 *     mean p90: 122.55050505050505
 *     low.....: 100 (76619, 98972, 99412)
 *     p50.....: 100
 *     p90.....: 200
 *     p95.....: 200
 *     p99.....: 300
 *     p999....: 900
 *     p9999...: 2200
 *     high-2..: 5000
 *     high-1..: 12800
 *     high....: 45900
 *   Control Time...: 88.21 ms
 *     mean....: 882.1131788682113
 *     mean p99: 857.9555555555555
 *     mean p90: 778.4515151515152
 *     low.....: 700 (5313, 63993, 86307)
 *     p50.....: 800
 *     p90.....: 1000
 *     p95.....: 1100
 *     p99.....: 1800
 *     p999....: 5100
 *     p9999...: 18100
 *     high-2..: 71200
 *     high-1..: 87200
 *     high....: 100700
 *   Speedup:
 *     mean....: +149.603% (6.937x)
 *     mean p99: +149.530% (6.925x)
 *     mean p90: +145.594% (6.352x)
 *     low.....: +150.000% (7.000x)
 *     u(p50): u800 = 99875 (99.874%)
 *
 * [text_nbt_samples/unnamed_tag_sample.snbt]
 *   Candidate Time.: 14.23 ms
 *     mean....: 142.34757652423477
 *     mean p99: 122.72424242424242
 *     mean p90: 121.5030303030303
 *     low.....: 100 (77492, 98890, 99293)
 *     p50.....: 100
 *     p90.....: 200
 *     p95.....: 200
 *     p99.....: 300
 *     p999....: 1000
 *     p9999...: 1900
 *     high-2..: 5100
 *     high-1..: 10600
 *     high....: 1598500
 *   Control Time...: 83.05 ms
 *     mean....: 830.5146948530514
 *     mean p99: 807.4141414141415
 *     mean p90: 769.8010101010101
 *     low.....: 700 (14152, 86773, 95892)
 *     p50.....: 800
 *     p90.....: 900
 *     p95.....: 900
 *     p99.....: 1700
 *     p999....: 4300
 *     p9999...: 19900
 *     high-2..: 68100
 *     high-1..: 68400
 *     high....: 76000
 *   Speedup:
 *     mean....: +141.473% (5.834x)
 *     mean p99: +147.223% (6.579x)
 *     mean p90: +145.472% (6.336x)
 *     low.....: +150.000% (7.000x)
 *     u(p50): u800 = 99804 (99.803%)
 *
 * [mca_palettes/block_states-1.20.4-6entries.snbt]
 *   Candidate Time.: 55.01 ms
 *     mean....: 550.1254987450126
 *     mean p99: 513.6323232323232
 *     mean p90: 475.19191919191917
 *     low.....: 300 (863, 31333, 72913)
 *     p50.....: 500
 *     p90.....: 700
 *     p95.....: 800
 *     p99.....: 1000
 *     p999....: 2600
 *     p9999...: 6200
 *     high-2..: 74800
 *     high-1..: 87600
 *     high....: 2128400
 *   Control Time...: 743.7 ms
 *     mean....: 7437.342626573734
 *     mean p99: 7247.536363636364
 *     mean p90: 6504.152525252525
 *     low.....: 6400 (10, 213, 1595)
 *     p50.....: 7200
 *     p90.....: 7600
 *     p95.....: 8100
 *     p99.....: 10800
 *     p999....: 23800
 *     p9999...: 95300
 *     high-2..: 1409500
 *     high-1..: 1501500
 *     high....: 1508100
 *   Speedup:
 *     mean....: +172.451% (13.519x)
 *     mean p99: +173.528% (14.110x)
 *     mean p90: +172.766% (13.687x)
 *     low.....: +182.090% (21.333x)
 *     u(p50): u7200 = 99992 (99.991%)
 *
 * [text_nbt_samples/little_of_everything.snbt]
 *   Candidate Time.: 354.3 ms
 *     mean....: 3542.6475735242648
 *     mean p99: 3393.159595959596
 *     mean p90: 3124.9989898989897
 *     low.....: 3100 (860, 21460, 52572)
 *     p50.....: 3300
 *     p90.....: 3600
 *     p95.....: 3800
 *     p99.....: 5600
 *     p999....: 18600
 *     p9999...: 71500
 *     high-2..: 1527700
 *     high-1..: 1531100
 *     high....: 1543400
 *   Control Time...: 1.572 s
 *     mean....: 15715.421845781542
 *     mean p99: 15185.578787878789
 *     mean p90: 13582.733333333334
 *     low.....: 13400 (3, 11, 27)
 *     p50.....: 14900
 *     p90.....: 16200
 *     p95.....: 17700
 *     p99.....: 24500
 *     p999....: 88500
 *     p9999...: 1397400
 *     high-2..: 1621700
 *     high-1..: 1760100
 *     high....: 1780900
 *   Speedup:
 *     mean....: +126.417% (4.436x)
 *     mean p99: +126.945% (4.475x)
 *     mean p90: +125.184% (4.346x)
 *     low.....: +124.848% (4.323x)
 *     u(p50): u14900 = 99864 (99.863%)
 * </pre>
 */
public class BinaryNbtTagSorter {
//    private static final boolean DEBUG_LOG = false;
    public static final Stopwatch stopwatch = Stopwatch.createUnstarted();

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