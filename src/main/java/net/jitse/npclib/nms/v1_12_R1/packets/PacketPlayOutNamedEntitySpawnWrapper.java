/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.nms.v1_12_R1.packets;

import com.comphenix.tinyprotocol.Reflection;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_12_R1.DataWatcher;
import net.minecraft.server.v1_12_R1.PacketPlayOutNamedEntitySpawn;
import org.bukkit.Location;

import java.lang.reflect.Field;
import java.util.UUID;

/**
 * @author Jitse Boonstra
 */
public class PacketPlayOutNamedEntitySpawnWrapper {

    public PacketPlayOutNamedEntitySpawn create(UUID uuid, Location location, int entityId) {
        PacketPlayOutNamedEntitySpawn packetPlayOutNamedEntitySpawn = new PacketPlayOutNamedEntitySpawn();

        try {
            // 使用硬编码字段名方式，这是Minecraft 1.12.2中的标准字段名
            Reflection.getField(packetPlayOutNamedEntitySpawn.getClass(), "a", int.class)
                    .set(packetPlayOutNamedEntitySpawn, entityId);
            Reflection.getField(packetPlayOutNamedEntitySpawn.getClass(), "b", UUID.class)
                    .set(packetPlayOutNamedEntitySpawn, uuid);
            Reflection.getField(packetPlayOutNamedEntitySpawn.getClass(), "c", int.class)
                    .set(packetPlayOutNamedEntitySpawn, (int) Math.floor(location.getX() * 32.0D));
            Reflection.getField(packetPlayOutNamedEntitySpawn.getClass(), "d", int.class)
                    .set(packetPlayOutNamedEntitySpawn, (int) Math.floor(location.getY() * 32.0D));
            Reflection.getField(packetPlayOutNamedEntitySpawn.getClass(), "e", int.class)
                    .set(packetPlayOutNamedEntitySpawn, (int) Math.floor(location.getZ() * 32.0D));
            Reflection.getField(packetPlayOutNamedEntitySpawn.getClass(), "f", byte.class)
                    .set(packetPlayOutNamedEntitySpawn, (byte) ((int) (location.getYaw() * 256.0F / 360.0F)));
            Reflection.getField(packetPlayOutNamedEntitySpawn.getClass(), "g", byte.class)
                    .set(packetPlayOutNamedEntitySpawn, (byte) ((int) (location.getPitch() * 256.0F / 360.0F)));
        } catch (Exception e1) {
            // 如果字段名映射失败，使用直接反射方式
            try {
                Field[] fields = PacketPlayOutNamedEntitySpawn.class.getDeclaredFields();
                int intFieldIndex = 0;
                int byteFieldIndex = 0;
                int uuidFieldIndex = 0;
                int datawatcherFieldIndex = 0;
                
                for (Field field : fields) {
                    field.setAccessible(true);
                    Class<?> type = field.getType();
                    
                    if (type == int.class) {
                        switch (intFieldIndex) {
                            case 0: // entity ID
                                field.set(packetPlayOutNamedEntitySpawn, entityId);
                                break;
                            case 1: // x coordinate
                                field.set(packetPlayOutNamedEntitySpawn, (int) Math.floor(location.getX() * 32.0D));
                                break;
                            case 2: // y coordinate
                                field.set(packetPlayOutNamedEntitySpawn, (int) Math.floor(location.getY() * 32.0D));
                                break;
                            case 3: // z coordinate
                                field.set(packetPlayOutNamedEntitySpawn, (int) Math.floor(location.getZ() * 32.0D));
                                break;
                        }
                        intFieldIndex++;
                    } else if (type == UUID.class) {
                        field.set(packetPlayOutNamedEntitySpawn, uuid);
                        uuidFieldIndex++;
                    } else if (type == byte.class) {
                        switch (byteFieldIndex) {
                            case 0: // yaw
                                field.set(packetPlayOutNamedEntitySpawn, (byte) ((int) (location.getYaw() * 256.0F / 360.0F)));
                                break;
                            case 1: // pitch
                                field.set(packetPlayOutNamedEntitySpawn, (byte) ((int) (location.getPitch() * 256.0F / 360.0F)));
                                break;
                        }
                        byteFieldIndex++;
                    } else if (type == DataWatcher.class) {
                        field.set(packetPlayOutNamedEntitySpawn, new DataWatcher(null));
                        datawatcherFieldIndex++;
                    }
                }
            } catch (Exception e2) {
                // 如果直接反射方式也失败，抛出异常但提供更多信息
                throw new RuntimeException("无法设置PacketPlayOutNamedEntitySpawn字段，可能是由于混淆映射问题", e2);
            }
        }

        // 设置DataWatcher的值
        DataWatcher dataWatcher;
        try {
            // 先创建DataWatcher对象并将其分配给PacketPlayOutNamedEntitySpawn，然后再设置其内容
            // 这样可以避免在设置内容时DataWatcher还没有被正确关联到包对象的问题
            dataWatcher = new DataWatcher(null); // 先用null创建，避免实体创建问题

            // 将DataWatcher分配给PacketPlayOutNamedEntitySpawn对象
            try {
                Reflection.getField(packetPlayOutNamedEntitySpawn.getClass(), "i", DataWatcher.class)
                        .set(packetPlayOutNamedEntitySpawn, dataWatcher);
            } catch (Exception e) {
                // 如果字段名访问失败，尝试直接反射
                Field[] fields = PacketPlayOutNamedEntitySpawn.class.getDeclaredFields();
                for (Field field : fields) {
                    if (field.getType() == DataWatcher.class) {
                        field.setAccessible(true);
                        field.set(packetPlayOutNamedEntitySpawn, dataWatcher);
                        break;
                    }
                }
            }

            // 在DataWatcher被分配后，设置其内容
            dataWatcher.set(net.minecraft.server.v1_12_R1.DataWatcherRegistry.a.a(10), (byte) 127);
        } catch (Exception e1) {
            // 如果所有方法都失败，创建一个DataWatcher并分配给包对象，但不设置内容
            try {
                dataWatcher = new DataWatcher(null);
                Reflection.getField(packetPlayOutNamedEntitySpawn.getClass(), "i", DataWatcher.class)
                        .set(packetPlayOutNamedEntitySpawn, dataWatcher);
            } catch (Exception e2) {
                // 如果字段访问也失败，尝试直接反射
                try {
                    Field[] fields = PacketPlayOutNamedEntitySpawn.class.getDeclaredFields();
                    for (Field field : fields) {
                        if (field.getType() == DataWatcher.class) {
                            field.setAccessible(true);
                            field.set(packetPlayOutNamedEntitySpawn, new DataWatcher(null));
                            break;
                        }
                    }
                } catch (Exception e3) {
                    throw new RuntimeException("无法设置DataWatcher字段", e3);
                }
            }
        }

        return packetPlayOutNamedEntitySpawn;
    }
}