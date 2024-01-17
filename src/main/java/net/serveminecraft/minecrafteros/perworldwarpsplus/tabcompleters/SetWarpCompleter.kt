package net.serveminecraft.minecrafteros.perworldwarpsplus.tabcompleters

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class SetWarpCompleter: TabCompleter {

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>?): MutableList<String> {
        return emptyList<String>().toMutableList()
    }

}