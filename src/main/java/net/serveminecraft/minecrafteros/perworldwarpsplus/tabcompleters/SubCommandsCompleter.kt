package net.serveminecraft.minecrafteros.perworldwarpsplus.tabcompleters

import net.serveminecraft.minecrafteros.perworldwarpsplus.PerWorldWarpsPlus
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class SubCommandsCompleter(val plugin: PerWorldWarpsPlus): TabCompleter {

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>?): MutableList<String> {
        return listOf("help", "reload").toMutableList()
    }

}