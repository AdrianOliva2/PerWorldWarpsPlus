package net.serveminecraft.minecrafteros.perworldwarpsplus.commands

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.serveminecraft.minecrafteros.perworldwarpsplus.managers.InventoryManager
import net.serveminecraft.minecrafteros.perworldwarpsplus.utils.MessagesUtil
import net.serveminecraft.minecrafteros.perworldwarpsplus.PerWorldWarpsPlus
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

class WarpsCommand(private val plugin: PerWorldWarpsPlus): CommandExecutor {

    private val replaces: HashMap<String, String> = HashMap()

    init {
        replaces["%prefix%"] = plugin.prefix
    }

    fun getAvailableWarps(player: Player): MutableList<String>? {
        val warpsConfig: FileConfiguration = plugin.warpsConfigFile
        val warps: MutableSet<String>? = warpsConfig.getConfigurationSection("Worlds.${player.world.name}")?.getKeys(false)

        if (warps != null) {
            val warpsList: MutableList<String> = mutableListOf()
            for (warp: String in warps) {
                if (warpsConfig.contains("Worlds.${player.world.name}.$warp.permission")) {
                    val permission: String = warpsConfig.getString("Worlds.${player.world.name}.$warp.permission")!!
                    if (player.isOp || player.hasPermission(permission))
                        warpsList += warp
                } else {
                    warpsList += warp
                }
            }
            return warpsList.sorted().toMutableList()
        }

        return null
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) {
            val messagesConfig: FileConfiguration = plugin.messagesConfigFile
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(MessagesUtil.getFullStringFromConfig(messagesConfig, "console-command-error", replaces)))
            return false
        } else {
            val player: Player = sender
            val inventoryManager = InventoryManager.getInstance(plugin)
            val inventory: Inventory = inventoryManager.createInventory(player)
            player.openInventory(inventory)
        }
        return true
    }

}