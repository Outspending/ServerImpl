package me.outspending.protocol.codec;

import me.outspending.MinecraftServer;
import me.outspending.connection.GameState;
import me.outspending.protocol.packets.server.HandshakePacket;
import me.outspending.protocol.packets.server.configuration.AcknowledgeFinishConfigurationPacket;
import me.outspending.protocol.packets.server.configuration.ClientInformationPacket;
import me.outspending.protocol.packets.server.configuration.PluginMessageConfigurationPacket;
import me.outspending.protocol.packets.server.login.LoginAcknowledgedPacket;
import me.outspending.protocol.packets.server.login.LoginStartPacket;
import me.outspending.protocol.packets.server.play.*;
import me.outspending.protocol.packets.server.status.PingRequestPacket;
import me.outspending.protocol.packets.server.status.StatusRequestPacket;

import java.util.Map;

public class CodecHandler {
    public static final PacketCodec<GameState> CLIENT_CODEC = PacketCodec.builder(GameState.class)
            .protocolVersion(MinecraftServer.PROTOCOL)
            .minecraftVersion("1.20.4")
            .packetType(GameState.HANDSHAKE, Map.ofEntries(
                    Map.entry(0x00, HandshakePacket::read)
            ))
            .packetType(GameState.STATUS, Map.ofEntries(
                    Map.entry(0x00, StatusRequestPacket::read),
                    Map.entry(0x01, PingRequestPacket::read)
            ))
            .packetType(GameState.LOGIN, Map.ofEntries(
                    Map.entry(0x00, LoginStartPacket::read),
                    Map.entry(0x03, LoginAcknowledgedPacket::read)
            ))
            .packetType(GameState.CONFIGURATION, Map.ofEntries(
                    Map.entry(0x00, ClientInformationPacket::read),
                    Map.entry(0x01, PluginMessageConfigurationPacket::read),
                    Map.entry(0x02, AcknowledgeFinishConfigurationPacket::read)
            ))
            .packetType(GameState.PLAY, Map.ofEntries(
                    Map.entry(0x35, UseItemOnPacket::read),
                    Map.entry(0x33, SwingArmPacket::read),
                    Map.entry(0x17, SetPlayerPositionPacket::read),
                    Map.entry(0x18, SetPlayerPositionAndRotationPacket::read),
                    Map.entry(0x2C, SetHeldItemPacket::read),
                    Map.entry(0x2F, SetCreativeModeSlotPacket::read),
                    Map.entry(0x15, ServerKeepAlivePacket::read),
                    Map.entry(0x19, PlayerRotationPacket::read),
                    Map.entry(0x22, PlayerCommandPacket::read),
                    Map.entry(0x21, PlayerActionPacket::read),
                    Map.entry(0x20, PlayerAbilitiesPacket::read),
                    Map.entry(0x00, ConfirmTeleportPacket::read)
            ))
            .build();
}