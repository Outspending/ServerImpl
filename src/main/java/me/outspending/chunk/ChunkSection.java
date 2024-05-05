package me.outspending.chunk;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import me.outspending.protocol.Writable;
import me.outspending.protocol.writer.PacketWriter;
import org.jetbrains.annotations.NotNull;

public class ChunkSection implements Writable {

    private static final int CHUNK_SECTION_SIZE = 24;
    private static final int SECTION_SIZE = 16 * 16 * 16;
    private static final byte GLOBAL_PALETTE_BIT_SIZE = 8; // Indirect
    private static final int BLOCKS_PER_LONG = 64 / GLOBAL_PALETTE_BIT_SIZE;

    private short count;
    private IntList palette;
    private long[] data;

    public static ChunkSection[] generateChunkSections() {
        ChunkSection[] sections = new ChunkSection[CHUNK_SECTION_SIZE];
        for (int i = 0; i < CHUNK_SECTION_SIZE; i++) {
            sections[i] = new ChunkSection(new int[SECTION_SIZE]);
        }

        return sections;
    }

    private void loadArray(int[] types) {
        try {
            this.count = 0;
            this.palette = new IntArrayList();
            for (int type : types) {
                if (type != 0) {
                    count++;
                }

                if (!palette.contains(type)) {
                    palette.add(type);
                }
            }

            this.data = new long[SECTION_SIZE / BLOCKS_PER_LONG];
            for (int i = 0; i < SECTION_SIZE; i++) {
                int index = i / BLOCKS_PER_LONG;
                int bitOffset = i % BLOCKS_PER_LONG;

                if (palette != null) {
                    data[index] |= (long) palette.indexOf(types[i]) << bitOffset;
                } else {
                    data[index] |= (long) types[i] << bitOffset;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ChunkSection(int[] types) {
        loadArray(types);
    }

    private int getBlockIndex(int x, int y, int z) {
        return (y * 16 * 16) + (z * 16) + x;
    }

    public void setBlock(int x, int y, int z, int blockID) {
        if (!palette.contains(blockID)) {
            palette.add(blockID);
        }

        int index = getBlockIndex(x, y, z);
        long l = this.data[index / BLOCKS_PER_LONG];
        long mask = (1 << GLOBAL_PALETTE_BIT_SIZE) - 1;
        int offset = GLOBAL_PALETTE_BIT_SIZE * (index % BLOCKS_PER_LONG);
        l &= ~(mask << offset);
        l |= ((long) blockID << offset);

        if (blockID == 0) this.count--;
        else this.count++;

        this.data[index / BLOCKS_PER_LONG] = l;
    }

    public long getBlock(int x, int y, int z) {
        return this.data[getBlockIndex(x, y, z)];
    }

    public void fill(int blockID) {
        if (!palette.contains(blockID)) return;

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    setBlock(x, y, z, blockID);
                }
            }
        }
    }

    public boolean isEmpty() {
        return this.count == 0;
    }

    @Override
    public void write(@NotNull PacketWriter writer) {
        // Block Count
        writer.writeShort(this.count);

        // Block Palette
        writer.writeByte(GLOBAL_PALETTE_BIT_SIZE);
        if (palette != null) {
            writer.writeVarInt(palette.size());
            IntListIterator iterator = palette.iterator();
            while (iterator.hasNext()) {
                writer.writeVarInt(iterator.nextInt());
            }
        }

        writer.writeVarInt(data.length);
        for (long datum : data) {
            writer.writeLong(datum);
        }

        // Biome Palette
        writer.writeByte((byte) 0);
        writer.writeVarInt(0);

        writer.writeVarInt(0);
    }

}