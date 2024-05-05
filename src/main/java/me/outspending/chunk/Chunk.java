package me.outspending.chunk;

import me.outspending.protocol.writer.PacketWriter;
import org.jetbrains.annotations.NotNull;

import java.util.BitSet;

<<<<<<< Updated upstream
public class Chunk {
=======
@Getter
public class Chunk implements Writable {
    private static final ChunkMap CHUNK_MAP = new ChunkMap();

    private final int chunkX;
    private final int chunkZ;
    private final ChunkSection[] chunkSections;

    private ChunkSection[] generateSections() {
        ChunkSection[] generated = new ChunkSection[24];
        for (int i = 0; i < 24; i++) {
            generated[i] = new ChunkSection(new int[4096]);
        }
        return generated;
    }

    public Chunk(int chunkX, int chunkZ) {
        this.chunkSections = generateSections();
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;

        CHUNK_MAP.saveChunk(chunkX, chunkZ, this);
    }

    @Override
    public void write(@NotNull PacketWriter writer) {
        for (ChunkSection section : chunkSections) {
            section.write(writer);
        }
    }
>>>>>>> Stashed changes
}
