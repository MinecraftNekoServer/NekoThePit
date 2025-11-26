/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.nms.v1_12_R1.packets;

import com.comphenix.tinyprotocol.Reflection;
import net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardTeam;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Jitse Boonstra
 */
public class PacketPlayOutScoreboardTeamWrapper {

    public PacketPlayOutScoreboardTeam createRegisterTeam(String name) {
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam();

        Reflection.getField(packetPlayOutScoreboardTeam.getClass(), int.class, 0)
                .set(packetPlayOutScoreboardTeam, 0);
        Reflection.getField(packetPlayOutScoreboardTeam.getClass(), String.class, 0)
                .set(packetPlayOutScoreboardTeam, name);
        Reflection.getField(packetPlayOutScoreboardTeam.getClass(), String.class, 1)
                .set(packetPlayOutScoreboardTeam, name);
        Reflection.getField(packetPlayOutScoreboardTeam.getClass(), String.class, 2)
                .set(packetPlayOutScoreboardTeam, "never");
        Reflection.getField(packetPlayOutScoreboardTeam.getClass(), int.class, 1)
                .set(packetPlayOutScoreboardTeam, 1);
        Reflection.FieldAccessor<Collection> collectionFieldAccessor = Reflection.getField(
                packetPlayOutScoreboardTeam.getClass(), Collection.class, 0);
        collectionFieldAccessor.set(packetPlayOutScoreboardTeam, Collections.singletonList(name));

        return packetPlayOutScoreboardTeam;
    }

    public PacketPlayOutScoreboardTeam createUnregisterTeam(String name) {
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam();

        Reflection.getField(packetPlayOutScoreboardTeam.getClass(), int.class, 0)
                .set(packetPlayOutScoreboardTeam, 1);
        Reflection.getField(packetPlayOutScoreboardTeam.getClass(), String.class, 0)
                .set(packetPlayOutScoreboardTeam, name);

        return packetPlayOutScoreboardTeam;
    }
}