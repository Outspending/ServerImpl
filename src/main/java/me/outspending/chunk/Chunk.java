package me.outspending.chunk;

import me.outspending.entity.Entity;
import me.outspending.position.Pos;
import me.outspending.protocol.Writable;
import me.outspending.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Chunk extends Writable {

    static @NotNull Chunk create(int chunkX, int chunkZ, @NotNull World world, @NotNull ChunkSection[] sections) {
        return new ChunkImpl(chunkX, chunkZ, world, sections);
    }

    static @NotNull Chunk create(int chunkX, int chunkZ, @NotNull World world) {
        return new ChunkImpl(chunkX, chunkZ, world);
    }

    default int getChunkIndex(int x, int z) {
        return (x << 4) | z;
    }

    void setBlock(int x, int y, int z, int blockID);

    int getBlock(int x, int y, int z);

    @NotNull ChunkSection[] getSections();

    @NotNull World getWorld();

    int getChunkX();

    int getChunkZ();

    int getHighestBlock(int x, int z);

    @Nullable ChunkSection getSectionAt(int y);

    @NotNull ChunkSection[] getSectionsBelow(int y);

    @NotNull ChunkSection[] getSectionsAbove(int y);

    @NotNull List<Entity> getEntities();

    boolean isLoaded();

    void setIsLoaded(boolean isLoaded);

    void load();

    void unload();

}