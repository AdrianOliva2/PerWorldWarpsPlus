package net.serveminecraft.minecrafteros.perworldwarpsplus.tabcompleters

import net.serveminecraft.minecrafteros.perworldwarpsplus.PerWorldWarpsPlus
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class SubCommandsCompleter(val plugin: PerWorldWarpsPlus): TabCompleter {

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>?): MutableList<String> {
        if (sender !is Player || sender.isOp || (sender.hasPermission("perworldwarps.reload") && sender.hasPermission("perworldwarps.help")))
            return listOf("help", "reload").toMutableList()

        val player: Player = sender

        if (player.hasPermission("perworldwarps.reload"))
            return listOf("reload").toMutableList()

        if (player.hasPermission("perworldwarps.help"))
            return listOf("help").toMutableList()

        return emptyList<String>().toMutableList()
    }

}