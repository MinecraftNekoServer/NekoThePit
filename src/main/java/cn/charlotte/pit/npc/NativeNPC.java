package cn.charlotte.pit.npc;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.util.chat.CC;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 原生NPC实现，不依赖任何外部库
 */
public class NativeNPC {
    private EntityPlayer npc;
    private final int entityId;
    private final String name;
    private final Location location;
    private final String skinTexture;
    private final String skinSignature;
    private final List<String> textLines;
    private final ItemStack heldItem;
    private final Set<UUID> shownPlayers = new HashSet<>();
    private static final Random random = new Random();

    public NativeNPC(String name, Location location, String skinTexture, String skinSignature, List<String> textLines, ItemStack heldItem) {
        this.entityId = 1000000 + random.nextInt(9000000); // 随机ID避免冲突
        this.name = name;
        this.location = location;
        this.skinTexture = skinTexture;
        this.skinSignature = skinSignature;
        this.textLines = textLines != null ? new ArrayList<>(textLines) : new ArrayList<>();
        this.heldItem = heldItem;
        this.npc = createNPC();
    }

    private EntityPlayer createNPC() {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();

        // 创建带皮肤的GameProfile
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), name);
        if (skinTexture != null && skinSignature != null) {
            gameProfile.getProperties().put("textures", new com.mojang.authlib.properties.Property("textures", skinTexture, skinSignature));
        }

        EntityPlayer entityPlayer = new EntityPlayer(server, world, gameProfile, new PlayerInteractManager(world));
        entityPlayer.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        // 设置实体标签始终可见 (显示名称)
        entityPlayer.setCustomNameVisible(true);

        return entityPlayer;
    }

    public void spawn() {
        // 发送数据包给所有在线玩家
        for (Player player : Bukkit.getOnlinePlayers()) {
            showToPlayer(player);
        }
    }

    public void showToPlayer(Player player) {
        if (shownPlayers.contains(player.getUniqueId())) {
            return; // 已经显示过了
        }

        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;

        // 1. 发送玩家信息包 (显示玩家)
        PacketPlayOutPlayerInfo playerInfoPacket = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, npc);
        connection.sendPacket(playerInfoPacket);

        // 2. 发送实体生成包
        PacketPlayOutNamedEntitySpawn spawnPacket = new PacketPlayOutNamedEntitySpawn(npc);
        connection.sendPacket(spawnPacket);

        // 3. 发送自定义名称包
        PacketPlayOutEntityMetadata metadataPacket = new PacketPlayOutEntityMetadata(npc.getId(), npc.getDataWatcher(), true);
        connection.sendPacket(metadataPacket);

        // 4. 发送手持物品包
        if (heldItem != null) {
            PacketPlayOutEntityEquipment equipmentPacket = new PacketPlayOutEntityEquipment(npc.getId(), EnumItemSlot.MAINHAND, org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack.asNMSCopy(heldItem));
            connection.sendPacket(equipmentPacket);
        }

        shownPlayers.add(player.getUniqueId());
    }

    public void hideFromPlayer(Player player) {
        if (!shownPlayers.contains(player.getUniqueId())) {
            return; // 未显示
        }

        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;

        // 发送实体销毁包
        PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(npc.getId());
        connection.sendPacket(destroyPacket);

        shownPlayers.remove(player.getUniqueId());
    }

    public void destroy() {
        // 向所有已显示的玩家发送销毁包
        for (UUID uuid : new HashSet<>(shownPlayers)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                hideFromPlayer(player);
            }
        }
        shownPlayers.clear();
    }

    public void updateText(Player player) {
        if (!shownPlayers.contains(player.getUniqueId())) {
            return;
        }

        // 发送更新文本的元数据包
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        DataWatcher dataWatcher = new DataWatcher(npc);

        // 更新自定义名称
        List<String> playerTextLines = getPlayerTextLines(player);
        if (!playerTextLines.isEmpty()) {
            String customName = String.join("\n", playerTextLines);
            // 使用正确的DataWatcherObject创建方式 (Minecraft 1.12.2)
            // 注意：DataWatcherObject的构造函数接受索引和数据类型注册器，泛型类型由注册器决定
            dataWatcher.set((DataWatcherObject<String>) new DataWatcherObject(3, DataWatcherRegistry.b), CC.translate(customName)); // 3是自定义名称字段 (String)
            dataWatcher.set((DataWatcherObject<Boolean>) new DataWatcherObject(4, DataWatcherRegistry.h), true); // 4是是否显示自定义名称字段 (Boolean)
        }

        PacketPlayOutEntityMetadata metadataPacket = new PacketPlayOutEntityMetadata(npc.getId(), dataWatcher, true);
        connection.sendPacket(metadataPacket);
    }

    public void updateForAllPlayers() {
        for (UUID uuid : shownPlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                updateText(player);
            }
        }
    }

    private List<String> getPlayerTextLines(Player player) {
        List<String> lines = new ArrayList<>();
        for (String line : textLines) {
            lines.add(CC.translate(line.replace("{player}", player.getName())));
        }
        return lines;
    }

    public void teleport(Location newLocation) {
        this.location.setX(newLocation.getX());
        this.location.setY(newLocation.getY());
        this.location.setZ(newLocation.getZ());
        this.location.setYaw(newLocation.getYaw());
        this.location.setPitch(newLocation.getPitch());

        npc.setLocation(newLocation.getX(), newLocation.getY(), newLocation.getZ(), newLocation.getYaw(), newLocation.getPitch());

        // 向所有已显示的玩家发送位置更新
        for (UUID uuid : shownPlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;

                // 发送位置更新包
                PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport(npc);
                connection.sendPacket(teleportPacket);
            }
        }
    }

    public EntityPlayer getNpc() {
        return npc;
    }

    public int getEntityId() {
        return entityId;
    }

    public Location getLocation() {
        return location;
    }

    public Set<UUID> getShownPlayers() {
        return new HashSet<>(shownPlayers);
    }

    public boolean isShownToPlayer(Player player) {
        return shownPlayers.contains(player.getUniqueId());
    }
}