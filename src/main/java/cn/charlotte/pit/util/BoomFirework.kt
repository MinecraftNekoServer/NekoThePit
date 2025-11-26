package cn.charlotte.pit.util

import net.minecraft.server.v1_12_R1.EntityFireworks
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityStatus
import net.minecraft.server.v1_12_R1.World
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer

class BoomFirework(world: World) :
    EntityFireworks(world) {

    val players = Bukkit.getOnlinePlayers()
    var gone = false
    
    override fun B_() {
        super.B_()
        if (gone) {
            die()
            return
        }
        if (!world.isClientSide) {
            gone = true
            if (players.isNotEmpty()) {
                for (player in players) {
                    (player as CraftPlayer).handle.playerConnection.sendPacket(
                        PacketPlayOutEntityStatus(this, 17.toByte())
                    )
                }
            } else {
                world.broadcastEntityEffect(this, 17.toByte())
                die()
            }
        }
    }
}