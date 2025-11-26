/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.nms.v1_8_R3.packets;

import com.comphenix.tinyprotocol.Reflection;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo;

import java.util.Collections;
import java.util.List;

/**
 * @author Jitse Boonstra - modified to work with v1_12_R1 environment
 */
public class PacketPlayOutPlayerInfoWrapper {

    public PacketPlayOutPlayerInfo create(PacketPlayOutPlayerInfo.EnumPlayerInfoAction action, GameProfile gameProfile, String name) {
        // Create the packet with the given action
        PacketPlayOutPlayerInfo packetPlayOutPlayerInfo = new PacketPlayOutPlayerInfo();
        Reflection.getField(packetPlayOutPlayerInfo.getClass(), "a", PacketPlayOutPlayerInfo.EnumPlayerInfoAction.class)
                .set(packetPlayOutPlayerInfo, action);

        // Create PlayerInfoData using reflection to avoid direct class dependency
        Object playerInfoData = createPlayerInfoData(gameProfile, name);

        // Set the list of players in the packet
        Reflection.FieldAccessor<List> fieldAccessor = Reflection.getField(packetPlayOutPlayerInfo.getClass(), "b", List.class);
        fieldAccessor.set(packetPlayOutPlayerInfo, Collections.singletonList(playerInfoData));

        return packetPlayOutPlayerInfo;
    }

    private Object createPlayerInfoData(GameProfile gameProfile, String name) {
        try {
            String customName = "[NPC] " + name;
            String json = "{\"text\":\"" + customName + "\",\"color\":\"dark_gray\"}";
            // Use v1_12_R1 classes since we're in a v1_12_R1 environment
            Object component = Class.forName("net.minecraft.server.v1_12_R1.IChatBaseComponent$ChatSerializer")
                    .getMethod("a", String.class)
                    .invoke(null, json);
            Class<?> playerInfoDataClass = Class.forName("net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo$PlayerInfoData");
            Class<?> enumGamemodeClass = Class.forName("net.minecraft.server.v1_12_R1.WorldSettings$EnumGamemode");
            Object notSetGamemode = enumGamemodeClass.getField("NOT_SET").get(null);
            return playerInfoDataClass.getConstructor(
                    GameProfile.class, int.class, enumGamemodeClass, Class.forName("net.minecraft.server.v1_12_R1.IChatBaseComponent")
            ).newInstance(gameProfile, 1, notSetGamemode, component);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}