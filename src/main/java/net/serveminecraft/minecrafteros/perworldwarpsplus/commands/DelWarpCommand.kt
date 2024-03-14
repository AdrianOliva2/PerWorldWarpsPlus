package net.serveminecraft.minecrafteros.perworldwarpsplus.commands

import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.serveminecraft.minecrafteros.perworldwarpsplus.PerWorldWarpsPlus
import net.serveminecraft.minecrafteros.perworldwarpsplus.managers.InventoryManager
import net.serveminecraft.minecrafteros.perworldwarpsplus.utils.MessagesUtil
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player

class DelWarpCommand: CommandExecutor {

    private val plugin: PerWorldWarpsPlus = PerWorldWarpsPlus.getInstance()
    private val replaces: HashMap<String, String> = HashMap()
    private var messagesConfig: FileConfiguration = plugin.messagesConfigFile

    init {
        replaces["%prefix%"] = plugin.prefix
    }

    private fun removeWarp(warpName: String, warpsConfig: FileConfiguration, world: World) {
        val warpsSection: MutableMap<String, Any> = warpsConfig.getConfigurationSection("Worlds.${world.name}")!!.getValues(true)
        val warpKeys: List<String> = warpsSection.keys.toList()
        val warpValues: List<Any> = warpsSection.values.toList()
        for (i in 0..<warpsSection.size) {
            val warpKey: String? = warpKeys.elementAtOrNull(i)
            val warpValue: Any? = warpValues.elementAtOrNull(i)
            if (!warpKey.isNullOrEmpty() && warpValue != null) {
                val warpKeyName: String = warpKey.split('.')[0]
                val warpValueClassNameSplit: List<String> = warpValue.javaClass.name.split('.')
                val warpValueClassName: String = warpValueClassNameSplit[warpValueClassNameSplit.size-1]
                if (warpKeyName.equals(warpName, false) || warpKey.contains(".item", false) || !warpValueClassName.equals("MemorySection", true)) {
                    warpsSection.remove(warpKey)
                }
            }
        }
        warpsConfig.set("Worlds.${world.name}", warpsSection)
        plugin.saveWarpsConfig()
        plugin.reloadWarpsConfig()
        val inventoryManager = InventoryManager.getInstance()
        inventoryManager.reloadAllWarpInventories()
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        messagesConfig = plugin.messagesConfigFile
        replaces["%prefix%"] = plugin.prefix
        val warpsConfig: FileConfiguration = plugin.warpsConfigFile
        if (sender !is Player) {
            if (args != null && args.size == 2) {
                val world: World? = Bukkit.getWorld(args[0])
                if (world != null) {
                    replaces["%warp%"] = args[1]
                    if (warpsConfig.contains("Worlds.${world.name}.${args[1]}")) {
                        removeWarp(args[1], warpsConfig, world)
                        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(MessagesUtil.getFullStringFromConfig(messagesConfig, "warp-deleted-successfully", replaces)))
                    } else {
                        val message: TextComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(MessagesUtil.getFullStringFromConfig(messagesConfig, "warp-not-exists", replaces))
                        sender.sendMessage(message)
                    }
                    replaces.remove("%warp%")
                } else {
                    replaces["%world%"] = args[0]
                    sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(MessagesUtil.getFullStringFromConfig(messagesConfig, "console-world-not-exists", replaces)))
                    replaces.remove("%world%")
                }
                return true
            } else {
                sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(MessagesUtil.getFullStringFromConfig(messagesConfig, "console-delwarp-command-help", replaces)))
            }
            return false
        } else {
            val player: Player = sender
            if (player.isOp || player.hasPermission("perworldwarps.delwarp")) {
                if (args != null && args.size == 1) {
                    replaces["%warp%"] = args[0]
                    if (warpsConfig.contains("Worlds.${player.world.name}.${args[0]}")) {
                        removeWarp(args[0], warpsConfig, player.world)
                        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(MessagesUtil.getFullStringFromConfig(messagesConfig, "warp-deleted-successfully", replaces)))
                    } else {
                        val message: TextComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(MessagesUtil.getFullStringFromConfig(messagesConfig, "warp-not-exists", replaces))
                        player.sendMessage(message)
                    }
                    replaces.remove("%warp%")
                } else {
                    val message: TextComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(MessagesUtil.getFullStringFromConfig(messagesConfig, "delwarp-command-help", replaces))
                    player.sendMessage(message)
                    return false
                }
            } else {
                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(MessagesUtil.getFullStringFromConfig(messagesConfig, "no-permission", replaces)))
                return false
            }
        }
        return true
    }

}