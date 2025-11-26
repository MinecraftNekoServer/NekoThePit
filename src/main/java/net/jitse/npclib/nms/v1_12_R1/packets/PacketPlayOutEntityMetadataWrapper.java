package net.jitse.npclib.nms.v1_12_R1.packets;

import net.jitse.npclib.api.state.NPCState;
import net.minecraft.server.v1_12_R1.DataWatcher;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityMetadata;

import java.util.Collection;

public class PacketPlayOutEntityMetadataWrapper {

    public PacketPlayOutEntityMetadata create(Collection<NPCState> activateStates, int entityId) {
        DataWatcher dataWatcher = new DataWatcher(null);
        byte masked = NPCState.getMasked(activateStates);
        dataWatcher.set(net.minecraft.server.v1_12_R1.DataWatcherRegistry.a.a(0), masked);

        return new PacketPlayOutEntityMetadata(entityId, dataWatcher, true);
    }
}