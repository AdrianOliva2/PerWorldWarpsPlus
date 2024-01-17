package net.serveminecraft.minecrafteros.perworldwarpsplus.commands

import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.serveminecraft.minecrafteros.perworldwarpsplus.managers.InventoryManager
import net.serveminecraft.minecrafteros.perworldwarpsplus.utils.MessagesUtil
import net.serveminecraft.minecrafteros.perworldwarpsplus.PerWorldWarpsPlus
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import java.lang.StringBuilder

class WarpsCommand(private val plugin: PerWorldWarpsPlus): CommandExecutor {

    private val replaces: HashMap<String, String> = HashMap()
    private var messagesConfig: FileConfiguration = plugin.messagesConfigFile

    init {
        replaces["%prefix%"] = plugin.prefix
    }

    fun getAvailableWarpsForPlayer(player: Player): MutableList<String>? {
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

    fun getAvailableWarpsForConsole(world: World): MutableList<String>? {
        val warpsConfig: FileConfiguration = plugin.warpsConfigFile
        val warps: MutableSet<String>? = warpsConfig.getConfigurationSection("Worlds.${world.name}")?.getKeys(false)

        if (warps != null) {
            val warpsList: MutableList<String> = mutableListOf()
            for (warp: String in warps) {
                warpsList += warp
            }
            return warpsList.sorted().toMutableList()
        }

        return null
    }

    private fun getAvailableWarpsMessageForConsole(world: World): TextComponent? {
        val availableWarps: MutableList<String>? = getAvailableWarpsForConsole(world)
        if (availableWarps.isNullOrEmpty()) return null

        var warpsMessage = StringBuilder()
        for (warp: String in availableWarps) {
            warpsMessage.append("$warp, ")
        }
        warpsMessage = warpsMessage.deleteRange(warpsMessage.length-2, warpsMessage.length)
        replaces["%warps%"] = warpsMessage.toString()
        val fullMessage: TextComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(MessagesUtil.getFullStringFromConfig(messagesConfig, "console-available-warps", replaces))
        replaces.remove("%warps%")
        return fullMessage
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        messagesConfig = plugin.messagesConfigFile
        replaces["%prefix%"] = plugin.prefix
        if (sender !is Player) {
            if (args != null && args.size == 1){
                val world: World? = Bukkit.getWorld(args[0])
                return if (world != null) {
                    val availableWarps: TextComponent? = getAvailableWarpsMessageForConsole(world)
                    if (availableWarps != null) {
                        sender.sendMessage(availableWarps)
                        true
                    } else {
                        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(MessagesUtil.getFullStringFromConfig(messagesConfig, "console-no-available-warps", replaces)))
                        false
                    }
                } else {
                    replaces["%world%"] = args[0]
                    sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(MessagesUtil.getFullStringFromConfig(messagesConfig, "console-world-not-exists", replaces)))
                    replaces.remove("%world%")
                    false
                }
            } else {
                sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(MessagesUtil.getFullStringFromConfig(messagesConfig, "console-command-error", replaces)))
                return false
            }
        } else {
            val player: Player = sender
            val inventoryManager = InventoryManager.getInstance(plugin)
            val inventory: Inventory = inventoryManager.createInventory(player)
            player.openInventory(inventory)
        }
        return true
    }

}