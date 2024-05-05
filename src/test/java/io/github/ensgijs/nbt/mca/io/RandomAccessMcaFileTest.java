package io.github.ensgijs.nbt.mca.io;

import io.github.ensgijs.nbt.mca.McaTestCase;

import java.io.IOException;
import  io.github.ensgijs.nbt.mca.io.RandomAccessMcaFile.SectorManager;
import  io.github.ensgijs.nbt.mca.io.RandomAccessMcaFile.SectorManager.SectorBlock;

public class RandomAccessMcaFileTest extends McaTestCase {

    public void testSectorManager_sanity() throws IOException {
        SectorManager sm = new SectorManager();
        assertEquals(2, sm.appendAtSector);  // default is sector 2

        int[] sectorTable = new int[1024];
        sectorTable[0] = new SectorBlock(5, 1).pack();
        sectorTable[33] = new SectorBlock(18, 1).pack();
        sectorTable[64] = new SectorBlock(9, 4).pack();
        sectorTable[1] = new SectorBlock(2, 1).pack();
        sectorTable[32] = new SectorBlock(3, 2).pack();
        sm.sync(sectorTable);

        assertEquals(19, sm.appendAtSector);
        assertEquals(2, sm.freeSectors.size());
        assertEquals(new SectorBlock(6, 3), sm.freeSectors.get(0));
        assertEquals(new SectorBlock(13, 5), sm.freeSectors.get(1));

        // take from first free block
        assertEquals(new SectorBlock(6, 1), sm.allocate(1));
        assertEquals(2, sm.freeSectors.size());
        assertEquals(new SectorBlock(7, 2), sm.freeSectors.get(0));
        assertEquals(new SectorBlock(13, 5), sm.freeSectors.get(1));
        assertEquals(19, sm.appendAtSector);

        // take from second free block
        assertEquals(new SectorBlock(13, 4), sm.allocate(4));
        assertEquals(2, sm.freeSectors.size());
        assertEquals(new SectorBlock(7, 2), sm.freeSectors.get(0));
        assertEquals(new SectorBlock(17, 1), sm.freeSectors.get(1));
        assertEquals(19, sm.appendAtSector);

        // no free block big enough - take off the end
        assertEquals(new SectorBlock(19, 4), sm.allocate(4));
        assertEquals(2, sm.freeSectors.size());
        assertEquals(new SectorBlock(7, 2), sm.freeSectors.get(0));
        assertEquals(new SectorBlock(17, 1), sm.freeSectors.get(1));
        assertEquals(23, sm.appendAtSector);

        // release and merge into second free block
        sm.release(13, 4);
        assertEquals(2, sm.freeSectors.size());
        assertEquals(new SectorBlock(7, 2), sm.freeSectors.get(0));
        assertEquals(new SectorBlock(13, 5), sm.freeSectors.get(1));
        assertEquals(23, sm.appendAtSector);

        // release and merge into second free block case 2
        sm.release(18, 1);
        assertEquals(2, sm.freeSectors.size());
        assertEquals(new SectorBlock(7, 2), sm.freeSectors.get(0));
        assertEquals(new SectorBlock(13, 6), sm.freeSectors.get(1));
        assertEquals(23, sm.appendAtSector);

        // release last block which touches the current appendAtSector
        sm.release(19, 4);
        assertEquals(13, sm.appendAtSector);
        assertEquals(1, sm.freeSectors.size());
        assertEquals(new SectorBlock(7, 2), sm.freeSectors.get(0));

        // taking the last free sector should be safe too
        assertEquals(new SectorBlock(7, 1), sm.allocate(1));
        assertEquals(new SectorBlock(8, 1), sm.allocate(1));
        assertEquals(0, sm.freeSectors.size());
        assertEquals(13, sm.appendAtSector);

        // allocating with no free sectors also works
        assertEquals(new SectorBlock(13, 1), sm.allocate(1));
        assertEquals(0, sm.freeSectors.size());
        assertEquals(14, sm.appendAtSector);


        // nothing in table in sector 2
        sectorTable = new int[1024];
        sectorTable[547] = new SectorBlock(5, 1).pack();
        sm.sync(sectorTable);
        assertEquals(1, sm.freeSectors.size());
        assertEquals(new SectorBlock(2, 3), sm.freeSectors.get(0));
        assertEquals(6, sm.appendAtSector);


        // release between free sectors
        sm.freeSectors.clear();
        sm.freeSectors.add(new SectorBlock(2, 1));
        sm.freeSectors.add(new SectorBlock(20, 1));
        sm.appendAtSector = 42;
        sm.release(10, 2);

        assertEquals(3, sm.freeSectors.size());
        assertEquals(new SectorBlock(10, 2), sm.freeSectors.get(1));
    }

    public void testSectorManager_scan_throwsWhenGivenWrongSizedArray() {
        assertThrowsException(() -> new SectorManager().sync(null), NullPointerException.class);
        assertThrowsException(() -> new SectorManager().sync(new int[256]), IllegalArgumentException.class);
        assertThrowsException(() -> new SectorManager().sync(new int[4096]), IllegalArgumentException.class);
    }
}
