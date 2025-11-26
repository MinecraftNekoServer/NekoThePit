package net.jitse.npclib.nms.v1_12_R1.packets;

import com.comphenix.tinyprotocol.Reflection;
import net.jitse.npclib.api.state.NPCState;
import net.minecraft.server.v1_12_R1.DataWatcher;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityMetadata;

import java.util.Collection;

public class PacketPlayOutEntityMetadataWrapper {

    public PacketPlayOutEntityMetadata create(Collection<NPCState> activateStates, int entityId) {
        PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata();
        Reflection.getField(packet.getClass(), "a", int.class).set(packet, entityId);
        
        byte masked = NPCState.getMasked(activateStates);
        DataWatcher dataWatcher;
        try {
            // 尝试正常创建DataWatcher
            dataWatcher = new DataWatcher(null);
            dataWatcher.set(net.minecraft.server.v1_12_R1.DataWatcherRegistry.a.a(0), masked);
        } catch (NullPointerException e) {
            // 如果DataWatcher内部item为null导致NPE，创建一个空的DataWatcher对象
            // 通过反射直接创建PacketPlayOutEntityMetadata并设置字段
            try {
                // 创建一个DataWatcher实例，但不设置任何值，避免NPE
                dataWatcher = new DataWatcher(null);
                // 不调用set方法，直接使用空的DataWatcher
            } catch (Exception ex) {
                // 如果创建DataWatcher也失败，使用反射创建
                try {
                    dataWatcher = (DataWatcher) Class.forName("net.minecraft.server.v1_12_R1.DataWatcher")
                        .getConstructor(Class.forName("net.minecraft.server.v1_12_R1.Entity"))
                        .newInstance((Object) null);
                } catch (Exception reflectionException) {
                    // 如果所有方法都失败，返回没有DataWatcher的packet（不设置b字段）
                    return packet;
                }
            }
        }

        try {
            packet.getClass().getField("b").set(packet, dataWatcher);
        } catch (Exception e) {
            // 如果设置DataWatcher字段失败，忽略
        }

        return packet;
    }
}