package spg.lgdev.handler;

import net.minecraft.server.v1_12_R1.Packet;
import net.minecraft.server.v1_12_R1.PlayerConnection;

/**
 * Created by EmptyIrony on 2021/6/20.
 */
public interface PacketHandler {

    void handleReceivedPacket(PlayerConnection var1, Packet var2);

    default boolean handleReceivedPacketCancellable(PlayerConnection var1, Packet var2) {
        return true;
    }

    void handleSentPacket(PlayerConnection var1, Packet var2);

    default boolean handleSentPacketCancellable(PlayerConnection var1, Packet var2) {
        return true;
    }

}
