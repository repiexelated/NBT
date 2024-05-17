# NBT
<!-- [![Build Status](https://travis-ci.org/Querz/NBT.svg?branch=master)](https://travis-ci.org/Querz/NBT) [![Coverage Status](https://img.shields.io/coveralls/github/Querz/NBT/master.svg)](https://coveralls.io/github/Querz/NBT?branch=master) [![Release](https://jitpack.io/v/Querz/NBT.svg)](https://jitpack.io/#Querz/NBT) -->
A java implementation of the [NBT protocol](https://minecraft.gamepedia.com/NBT_format) for Minecraft Java Edition.

**!!! THIS FORK IS UNDER HEAVY DEVELOPMENT - May 2024!!!**

But, it is ready for early adopters!

This library includes a rich [NBT](https://minecraft.gamepedia.com/NBT_format) experience and powerful tools
for working with .mca files. The NBT portion is largely stable while the mca library is still going through heavy
iteration.

#### Highlights of the NBT library
* [NbtPath](src/main/java/io/github/ensgijs/nbt/query/NbtPath.java) Allows you to easily get and put nested tags.
* [TextNbtHelpers](src/main/java/io/github/ensgijs/nbt/io/TextNbtHelpers.java) Utilities for reading and writing text nbt data - with pretty printing! Yes, pretty printed text nbt data is also parseable.
* [BinaryNbtHelpers](src/main/java/io/github/ensgijs/nbt/io/BinaryNbtHelpers.java) Utilities for working with binary nbt files, compressed or uncompressed.

#### Highlights of the MCA library
* Version support from Minecraft Java 1.9.0 to 1.20.5 (no Bedrock support currently or planed at this time).
  * [DataVersion](src/main/java/io/github/ensgijs/nbt/mca/DataVersion.java) nearly complete data version to minecraft version mapping and detection back to 1.9.0.
* Rich javadocs on most classes and methods.
* Excellent code coverage and extensive unit testing as well as integration testing for multiple Minecraft versions see [test resources](src/test/resources)
  * _There's always room for improvement of the richness of integration data samples - some of the samples are a little less interesting than I would prefer._
* Supports editing of non-vanilla world files without data loss.
* Support for terrain (region), entities, and poi mca files - the various chunk class types are due for a heavy refactor, and I plan to add an abstraction layer to wrap all 3 mca/chunk types to provide a more seamless processing / editing experience.
* Safely relocate (move) chunks - all chunk implementations fully support being moved to a new location. All contents are updated to exist in the new chunk location. This feature is the primary reason I dusted off this project after 3 years and is well tested and very ready for consumption.
  * [RegionFileRelocator](src/main/java/io/github/ensgijs/nbt/mca/io/RegionFileRelocator.java) relocate entire region files.
* Multiple options for reading and writing chunk data to .mca files using one of the following classes.
  * [RandomAccessMcaFile](src/main/java/io/github/ensgijs/nbt/mca/io/RandomAccessMcaFile.java) read and write chunks with minimal memory overhead.
  * [McaFileChunkIterator](src/main/java/io/github/ensgijs/nbt/mca/io/McaFileChunkIterator.java) iterate through the chunks in an mca file one after the other, again keeping memory overhead down.
  * [McaFileStreamingWriter](src/main/java/io/github/ensgijs/nbt/mca/io/McaFileStreamingWriter.java) write an entire mca file one chunk at a time.

#### Powerful Utilities
* [LongArrayTagPackedIntegers](src/main/java/io/github/ensgijs/nbt/mca/util/LongArrayTagPackedIntegers.java) a comprehensive solution to working with all long[] packed values (block palettes, biome palettes, Heightmaps) across all DataVersions.
* [PalettizedCuboid](src/main/java/io/github/ensgijs/nbt/mca/util/PalettizedCuboid.java) powerful class for working with block and biome palettes. This class currently only supports MC 1.17 and above palettes; I plan to add block and biome child classes to improve usability.

#### How to get started with 0.1-SNAPSHOT
This package is not yet published to a public repository - but using it from a local build is easy!

Download the source, open the project in your IDE of choice (Intellij, etc), run the gradle rule
`gradle publishToMavenLocal` to build the source and stash the 0.1-SNAPSHOT in your local maven cache.

Gradle:
```
dependencies {
	...
	implementation 'io.github.ensgijs:ens-nbt:0.1-SNAPSHOT'
}
```

Maven:
```
<dependency>
    <groupId>io.github.ensgijs</groupId>
    <artifactId>ens-nbt</artifactId>
    <version>0.1-SNAPSHOT</version>
    <scope>compile</scope>
</dependency>
```


---
### NBT Specification
According to the [specification](https://minecraft.gamepedia.com/NBT_format), there are currently 13 different types of tags:

| Tag class    | Superclass | ID | Payload |
| ---------    | ---------- | -- | ----------- |
| [EndTag](src/main/java/io/github/ensgijs/nbt/tag/EndTag.java)             | [Tag](src/main/java/io/github/ensgijs/nbt/tag/Tag.java)               | 0  | None |
| [ByteTag](src/main/java/io/github/ensgijs/nbt/tag/ByteTag.java)           | [NumberTag](src/main/java/io/github/ensgijs/nbt/tag/NumberTag.java)   | 1  | 1 byte / 8 bits, signed |
| [ShortTag](src/main/java/io/github/ensgijs/nbt/tag/ShortTag.java)         | [NumberTag](src/main/java/io/github/ensgijs/nbt/tag/NumberTag.java)   | 2  | 2 bytes / 16 bits, signed, big endian |
| [IntTag](src/main/java/io/github/ensgijs/nbt/tag/IntTag.java)             | [NumberTag](src/main/java/io/github/ensgijs/nbt/tag/NumberTag.java)   | 3  | 4 bytes / 32 bits, signed, big endian |
| [LongTag](src/main/java/io/github/ensgijs/nbt/tag/LongTag.java)           | [NumberTag](src/main/java/io/github/ensgijs/nbt/tag/NumberTag.java)   | 4  | 8 bytes / 64 bits, signed, big endian |
| [FloatTag](src/main/java/io/github/ensgijs/nbt/tag/FloatTag.java)         | [NumberTag](src/main/java/io/github/ensgijs/nbt/tag/NumberTag.java)   | 5  | 4 bytes / 32 bits, signed, big endian, IEEE 754-2008, binary32 |
| [DoubleTag](src/main/java/io/github/ensgijs/nbt/tag/DoubleTag.java)       | [NumberTag](src/main/java/io/github/ensgijs/nbt/tag/NumberTag.java)   | 6  | 8 bytes / 64 bits, signed, big endian, IEEE 754-2008, binary64 |
| [ByteArrayTag](src/main/java/io/github/ensgijs/nbt/tag/ByteArrayTag.java) | [ArrayTag](src/main/java/io/github/ensgijs/nbt/tag/ArrayTag.java)     | 7  | `IntTag` payload *size*, then *size* `ByteTag` payloads |
| [StringTag](src/main/java/io/github/ensgijs/nbt/tag/StringTag.java)       | [Tag](src/main/java/io/github/ensgijs/nbt/tag/Tag.java)               | 8  | `ShortTag` payload *length*, then a UTF-8 string with size *length* |
| [ListTag](src/main/java/io/github/ensgijs/nbt/tag/ListTag.java)           | [Tag](src/main/java/io/github/ensgijs/nbt/tag/Tag.java)               | 9  | `ByteTag` payload *tagId*, then `IntTag` payload *size*, then *size* tags' payloads, all of type *tagId* |
| [CompoundTag](src/main/java/io/github/ensgijs/nbt/tag/CompoundTag.java)   | [Tag](src/main/java/io/github/ensgijs/nbt/tag/Tag.java)               | 10 | Fully formed tags, followed by an `EndTag` |
| [IntArrayTag](src/main/java/io/github/ensgijs/nbt/tag/IntArrayTag.java)   | [ArrayTag](src/main/java/io/github/ensgijs/nbt/tag/ArrayTag.java)     | 11 | `IntTag` payload *size*, then *size* `IntTag` payloads |
| [LongArrayTag](src/main/java/io/github/ensgijs/nbt/tag/LongArrayTag.java) | [ArrayTag](src/main/java/io/github/ensgijs/nbt/tag/ArrayTag.java)     | 12 | `IntTag` payload *size*, then *size* `LongTag` payloads |

* The `EndTag` is only used to mark the end of a `CompoundTag` in its serialized state or an empty `ListTag`.

* The maximum depth of the NBT structure is 512. If the depth exceeds this restriction during serialization, deserialization or String conversion, a `MaxDepthReachedException` is thrown. This usually happens when a circular reference exists in the NBT structure. The NBT specification does not allow circular references, as there is no tag to represent this.

<!--
### Add the library as a dependency using Gradle:
Add Jitpack to your `repositories`:
```
repositories {
	...
	maven { url 'https://jitpack.io/' }
}
```
And then add it as a dependency as usual:
```
dependencies {
	...
	implementation 'com.github.ens-gijs:NBT:1.0'
}
```

### Add the library as a dependency using Maven:
Add Jitpack:
```
<repositories>
	<repository>
		<id>jitpack.io</id>
		<url>https://jitpack.io</url>
	</repository>
</repositories>
```
Dependency:
```
<dependency>
	<groupId>com.github.ens-gijs</groupId>
	<artifactId>NBT</artifactId>
	<version>1.0</version>
</dependency>
```

---
### Example usage:
The following code snippet shows how to create a `CompoundTag`:
```java
CompoundTag ct = new CompoundTag();

ct.put("byte", new ByteTag((byte) 1));
ct.put("double", new DoubleTag(1.234));
ct.putString("string", "stringValue");
```
An example how to use a `ListTag`:
```java
ListTag<FloatTag> fl = new ListTag<>(FloatTag.class);

fl.add(new FloatTag(1.234f);
fl.addFloat(5.678f);
```

#### Nesting
All methods serializing instances or deserializing data track the nesting levels to prevent circular references or malicious data which could, when deserialized, result in thousands of instances causing a denial of service.

These methods have a parameter for the maximum nesting depth they are allowed to traverse. A value of `0` means that only the object itself, but no nested object may be processed.

If an instance is nested further than allowed, a [MaxDepthReachedException](src/main/java/io/github/ensgijs/io/MaxDepthReachedException.java) will be thrown. A negative maximum depth will cause an `IllegalArgumentException`.

Some methods do not provide a parameter to specify the maximum depth, but instead use `Tag.DEFAULT_MAX_DEPTH` (`512`) which is also the maximum used in Minecraft.

---
### Utility
There are several utility methods to make your life easier if you use this library.
#### NBTUtil
`NBTUtil.write()` lets you write a Tag into a gzip compressed or uncompressed file in one line (not counting exception handling). Files are gzip compressed by default.

Example usage:
```java
NBTUtil.write(namedTag, "filename.dat");
```
`NBTUtil.read()` reads any file containing NBT data. No worry about compression, it will automatically uncompress gzip compressed files.

Example usage:
```java
NamedTag namedTag = NBTUtil.read("filename.dat");
```
#### Playing Minecraft?
Each tag can be converted into an NBT String (SNBT) used in Minecraft commands.

Example usage:
```java
CompoundTag c = new CompoundTag();
c.putByte("blah", (byte) 5);
c.putString("foo", "bär");
ListTag<StringTag> s = new ListTag<>(StringTag.class);
s.addString("test");
s.add(new StringTag("text"));
c.add("list", s);
System.out.println(SNBTUtil.toSNBT(c)); // {blah:5b,foo:"bär",list:[test,text]}

```
There is also a tool to read, change and write MCA files.

Here are some examples:
```java
// This changes the InhabitedTime field of the chunk at x=68, z=81 to 0
MCAFile mcaFile = MCAUtil.readMCAFile("r.2.2.mca");
Chunk chunk = mcaFile.getChunk(68, 81);
chunk.setInhabitedTime(0);
MCAUtil.writeMCAFile("r.2.2.mca", mcaFile);
```
There is also an optimized api to retrieve and set block information (BlockStates) in MCA files.

Example:
```java
// Retrieves block information from the MCA file
CompoundTag blockState = mcaFile.getBlockStateAt(1090, 25, 1301);

// Retrieves block information from a single chunk
CompoundTag blockState = chunk.getBlockStateAt(2, 25, 5);

// Set block information
CompoundTag stone = new CompoundTag();
stone.putString("Name", "minecraft:stone");
mcaFile.setBlockStateAt(1090, 25, 1301, stone, false);
```
To ensure good performance even when setting a lot of blocks and / or editing sections with a huge palette of block states, the size of the BlockStates array is only updated when the size of the palette requires it. This means there might be blocks in the palette that are not actually used in the BlockStates array.
You can trigger a cleanup process by calling one of the following three methods, depending on the desired depth:
```java
mcaFile.cleanupPalettesAndBlockStates();
chunk.cleanupPalettesAndBlockStates();
section.cleanupPaletteAndBlockStates();
```
-->