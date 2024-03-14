package net.serveminecraft.minecrafteros.perworldwarpsplus.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.serveminecraft.minecrafteros.perworldwarpsplus.PerWorldWarpsPlus
import net.serveminecraft.minecrafteros.perworldwarpsplus.managers.InventoryManager
import net.serveminecraft.minecrafteros.perworldwarpsplus.utils.ListUtils
import net.serveminecraft.minecrafteros.perworldwarpsplus.utils.MessagesUtil
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player

class PerWorldWarpsCommand: CommandExecutor {

    private val plugin: PerWorldWarpsPlus = PerWorldWarpsPlus.getInstance()
    private val replaces: HashMap<String, String> = HashMap()
    private var messagesConfig: FileConfiguration = plugin.messagesConfigFile

    init {
        replaces["%prefix%"] = plugin.prefix
    }

    private fun help(sender: CommandSender): Boolean {
        return if ((sender is Player && sender.isOp || sender.hasPermission("perworldwarps.help")) || sender !is Player) {
            val messageList: MutableList<Component>? = ListUtils.stringListToComponentList(MessagesUtil.getFullStringListFromConfig(plugin.messagesConfigFile, "help", replaces))
            if (messageList != null) {
                for (message in messageList) {
                    sender.sendMessage(message)
                }
            }
            true
        } else {
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(MessagesUtil.getFullStringFromConfig(messagesConfig, "no-permission", replaces)))
            false
        }
    }

    private fun reload() {
        plugin.reloadConfig()
        plugin.saveDefaultConfig()
        plugin.saveConfig()
        plugin.reloadWarpsConfig()
        plugin.reloadMessagesConfig()
        messagesConfig = plugin.messagesConfigFile
        replaces["%prefix%"] = plugin.prefix
        val inventoryManager: InventoryManager = InventoryManager.getInstance()
        inventoryManager.reloadAllWarpInventories()
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (args.isNullOrEmpty()) {
            if (!help(sender))
                return false
        } else {
            if (args.size == 1){
                when(args[0]) {
                    "help" -> {
                        if (!help(sender))
                            return false
                    }
                    "reload" -> {
                        if ((sender is Player && sender.isOp || sender.hasPermission("perworldwarps.reload")) || sender !is Player) {
                            reload()
                            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(MessagesUtil.getFullStringFromConfig(messagesConfig, "plugin-reloaded-successfully", replaces)))
                        } else {
                            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(MessagesUtil.getFullStringFromConfig(messagesConfig, "no-permission", replaces)))
                            return false
                        }
                    }
                    else -> {
                        if (!help(sender))
                            return false
                    }
                }
            }
        }
        return true
    }

}