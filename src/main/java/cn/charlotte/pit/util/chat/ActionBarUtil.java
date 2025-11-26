package cn.charlotte.pit.util.chat;

import cn.charlotte.pit.ThePit;
import net.minecraft.server.v1_12_R1.ChatComponentText;
import net.minecraft.server.v1_12_R1.PacketPlayOutChat;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * @Author: EmptyIrony
 * @Date: 2021/1/1 12:37
 */
public class ActionBarUtil {

    public static void sendActionBar(Player player, String message) {
        ThePit.getInstance().getActionBarManager().addToQueue(player, message);
    }

    public static void sendActionBar0(Player player, String message) {
        ChatComponentText components = new ChatComponentText(CC.translate(message));
        PacketPlayOutChat packet = new PacketPlayOutChat(components, net.minecraft.server.v1_12_R1.ChatMessageType.GAME_INFO);

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

}
