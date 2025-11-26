/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.nms.v1_12_R1.packets;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo;

import java.util.Collections;

/**
 * @author Jitse Boonstra
 */
public class PacketPlayOutPlayerInfoWrapper {

    public PacketPlayOutPlayerInfo create(PacketPlayOutPlayerInfo.EnumPlayerInfoAction action, GameProfile gameProfile, String name) {
        // Create a dummy EntityPlayer to pass to the constructor
        net.minecraft.server.v1_12_R1.MinecraftServer server = net.minecraft.server.v1_12_R1.MinecraftServer.getServer();
        net.minecraft.server.v1_12_R1.WorldServer world = server.getWorldServer(0);
        net.minecraft.server.v1_12_R1.EntityPlayer dummyPlayer = new net.minecraft.server.v1_12_R1.EntityPlayer(
            server, world, gameProfile, new net.minecraft.server.v1_12_R1.PlayerInteractManager(world)
        );

        // Set custom name using JSON formatting
        String customName = "[NPC] " + name;
        String json = "{\"text\":\"" + customName + "\"\",\"color\":\"dark_gray\"}";
        dummyPlayer.setCustomNameVisible(true);
        dummyPlayer.setCustomName(json);

        // Create the packet with the action and the entity
        return new PacketPlayOutPlayerInfo(action, Collections.singletonList(dummyPlayer));
    }
}