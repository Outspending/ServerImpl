package me.outspending.protocol.packets.client.play;

import me.outspending.protocol.types.ClientPacket;
import me.outspending.protocol.writer.PacketWriter;
import org.jetbrains.annotations.NotNull;

public record ClientSetTickingStatePacket(float tickRate, boolean frozen) implements ClientPacket {

    @Override
    public void write(@NotNull PacketWriter writer) {
        writer.writeFloat(tickRate);
        writer.writeBoolean(frozen);
    }

    @Override
    public int id() {
        return 0x71;
    }

}
