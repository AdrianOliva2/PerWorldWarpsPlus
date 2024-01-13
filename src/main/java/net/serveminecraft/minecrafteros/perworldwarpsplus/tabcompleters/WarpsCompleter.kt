package net.serveminecraft.minecrafteros.perworldwarpsplus.tabcompleters

import net.serveminecraft.minecrafteros.perworldwarpsplus.PerWorldWarpsPlus
import net.serveminecraft.minecrafteros.perworldwarpsplus.commands.WarpsCommand
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class WarpsCompleter(val plugin: PerWorldWarpsPlus): TabCompleter {

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>?): MutableList<String>? {
        val warpsCommand = WarpsCommand(plugin)
        if (sender !is Player){
            if (label != "delwarp") return null

            if (args != null) {
                if (args.size == 1) {
                    val worlds: MutableList<World> = Bukkit.getWorlds()
                    val worldNames: MutableList<String> = emptyList<String>().toMutableList()
                    for (world: World in worlds) {
                        worldNames += world.name
                    }
                    return worldNames
                } else if (args.size == 2) {
                    val world: World? = Bukkit.getWorld(args[0])

                    if (world == null) return null

                    return warpsCommand.getAvailableWarpsForConsole(world)
                }
            }

            return null
        } else {
            val player: Player = sender
            return warpsCommand.getAvailableWarpsForPlayer(player)
        }
    }

}