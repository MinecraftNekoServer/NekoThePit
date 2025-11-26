package cn.charlotte.pit.npc;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.event.PitProfileLoadedEvent;
import cn.charlotte.pit.npc.runnable.NpcRunnable;
import cn.charlotte.pit.parm.AutoRegister;
import lombok.SneakyThrows;
import net.jitse.npclib.NPCLib;
import net.jitse.npclib.api.NPC;
import net.jitse.npclib.api.events.NPCInteractEvent;
import net.jitse.npclib.api.state.NPCSlot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @Author: EmptyIrony
 * @Date: 2020/12/30 22:35
 */
@AutoRegister
public class NpcFactory implements Listener {
    private static final List<AbstractPitNPC> pitNpc = new ArrayList<>();

    public static List<AbstractPitNPC> getPitNpc() {
        return NpcFactory.pitNpc;
    }

    @SneakyThrows
    public void init(Collection<Class<? extends AbstractPitNPC>> classes) {
        NPCLib npcLib = new NPCLib(ThePit.getInstance());

        for (Class<?> clazz : classes) {
            if (!ThePit.isDEBUG_SERVER()) {
                if (clazz.getName().contains("InfinityItemNPC")) {
                    continue;
                }

            }
            if (AbstractPitNPC.class.isAssignableFrom(clazz)) {
                AbstractPitNPC abstractPitNPC = (AbstractPitNPC) clazz.newInstance();

                NPC npc = npcLib.createNPC();
                npc.setLocation(abstractPitNPC.getNpcSpawnLocation());

                if (abstractPitNPC.getNpcHeldItem() != null) {
                    npc.setItem(NPCSlot.MAINHAND, abstractPitNPC.getNpcHeldItem());
                }

                abstractPitNPC.setNpc(npc);
                abstractPitNPC.initSkin(npc);

                pitNpc.add(abstractPitNPC);
            }
        }

        new NpcRunnable().runTaskTimerAsynchronously(ThePit.getInstance(), 20, 20);
    }

    @EventHandler
    @SneakyThrows
    public void onPlayerJoin(PitProfileLoadedEvent event) {
        Player player = Bukkit.getPlayer(event.getPlayerProfile().getPlayerUuid());
        if (player == null || !player.isOnline()) {
            return;
        }

        for (AbstractPitNPC pitNpc : pitNpc) {
            NPC npc = pitNpc.getNpc();
            if (npc == null) {
                continue;
            }
            
            // 只有在NPC未创建时才创建
            if (!npc.isCreated()) {
                npc.create();
            }
            
            if (!npc.isShown(player)) {
                try {
                    npc.show(player);
                } catch (Exception e) {
                    // 记录异常但不中断其他NPC的加载
                    ThePit.getInstance().getLogger().warning("Failed to show NPC " + pitNpc.getNpcInternalName() + " to player " + player.getName() + ": " + e.getMessage());
                }
            }
            
            try {
                npc.setText(player, pitNpc.getNpcTextLine(player));
            } catch (Exception e) {
                ThePit.getInstance().getLogger().warning("Failed to set NPC text for " + pitNpc.getNpcInternalName() + " to player " + player.getName() + ": " + e.getMessage());
            }
        }

    }

    @EventHandler
    public void onInteract(NPCInteractEvent event) {
        for (AbstractPitNPC abstractPitNPC : pitNpc) {
            if (abstractPitNPC.getNpc().getUniqueId().equals(event.getNPC().getUniqueId())) {
                abstractPitNPC.handlePlayerInteract(event.getWhoClicked());
            }
        }
    }

}
