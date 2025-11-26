/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.nms.v1_8_R3.packets;

import com.comphenix.tinyprotocol.Reflection;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityMetadata;

/**
 * @author Jitse Boonstra - modified to work with v1_12_R1 environment
 */
public class PacketPlayOutEntityMetadataWrapper {

    public PacketPlayOutEntityMetadata create(int entityId, byte masked) {
        PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata();
        Reflection.getField(packet.getClass(), "a", int.class).set(packet, entityId);
        // Create a simple datawatcher that works with v1_12_R1
        try {
            Object dataWatcher = Class.forName("net.minecraft.server.v1_12_R1.DataWatcher")
                .getConstructor(Class.forName("net.minecraft.server.v1_12_R1.Entity")).newInstance((Object) null);
            packet.getClass().getField("b").set(packet, dataWatcher);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return packet;
    }
}