package cn.charlotte.pit.command

import cn.charlotte.pit.util.command.Command
import org.bukkit.command.CommandSender
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class CleanupNoDupeItemCommand {
    private val saveService = Executors.newScheduledThreadPool(8)
    private val globalService = Executors.newScheduledThreadPool(8)

    @Command(
        names = ["cleanUp-no-dupe-item"],
        permissionNode = "pit.admin"
    )
    fun execute(sender: CommandSender) {
        // TODO: Replace with MySQL implementation
        // For now, informing user that this feature is temporarily disabled
        sender.sendMessage("§c该命令暂时不可用，等待MySQL实现")
    }
}