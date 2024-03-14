package net.serveminecraft.minecrafteros.perworldwarpsplus.tabcompleters

import net.serveminecraft.minecrafteros.perworldwarpsplus.commands.WarpsCommand
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class WarpsCompleter: TabCompleter {

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>?): MutableList<String>? {
        val warpsCommand = WarpsCommand()
        if (sender !is Player){
            if (label != "delwarp") return emptyList<String>().toMutableList()

            if (args != null) {
                if (args.size == 1) {
                    val worlds: MutableList<World> = Bukkit.getWorlds()
                    val worldNames: MutableList<String> = emptyList<String>().toMutableList()
                    for (world: World in worlds) {
                        worldNames += world.name
                    }
                    return worldNames
                } else if (args.size == 2) {
                    val world: World = Bukkit.getWorld(args[0]) ?: return emptyList<String>().toMutableList()

                    return warpsCommand.getAvailableWarpsForConsole(world)
                }
            }

            return emptyList<String>().toMutableList()
        } else {
            val player: Player = sender
            if (label == "delwarp" && (!player.isOp && !player.hasPermission("perworldwarps.delwarp")))
                return emptyList<String>().toMutableList()
            return warpsCommand.getAvailableWarpsForPlayer(player)
        }
    }

}