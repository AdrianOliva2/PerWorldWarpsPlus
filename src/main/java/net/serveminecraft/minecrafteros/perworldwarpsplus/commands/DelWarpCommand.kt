package net.serveminecraft.minecrafteros.perworldwarpsplus.commands

import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.serveminecraft.minecrafteros.perworldwarpsplus.utils.MessagesUtil
import net.serveminecraft.minecrafteros.perworldwarpsplus.PerWorldWarpsPlus
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player

class DelWarpCommand(private val plugin: PerWorldWarpsPlus): CommandExecutor {

    //TODO: Review why residual lines are left in the warps.yml file when deleting a warp

    private val replaces: HashMap<String, String> = HashMap()
    private val messagesConfig: FileConfiguration = plugin.messagesConfigFile

    init {
        replaces["%prefix%"] = plugin.prefix
    }

    private fun removeWarp(warpName: String, warpsConfig: FileConfiguration, player: Player) {
        val warpsSection: MutableMap<String, Any> = warpsConfig.getConfigurationSection("Worlds.${player.world.name}")!!.getValues(true)
        val warpKeys: List<String> = warpsSection.keys.toList()
        val warpValues: List<Any> = warpsSection.values.toList()
        for (i in 0..<warpsSection.size) {
            val warpKey: String? = warpKeys.elementAtOrNull(i)
            val warpValue: Any? = warpValues.elementAtOrNull(i)
            if (!warpKey.isNullOrEmpty() && warpValue != null) {
                val warpKeyName: String = warpKey.split('.')[0]
                val warpValueClassNameSplit: List<String> = warpValue.javaClass.name.split('.')
                val warpValueClassName: String = warpValueClassNameSplit[warpValueClassNameSplit.size-1]
                if (warpKeyName.equals(warpName, false) || !warpValueClassName.equals("MemorySection", true)) {
                    warpsSection.remove(warpKey)
                }
            }
        }
        warpsConfig.set("Worlds.${player.world.name}", warpsSection)
        plugin.saveWarpsConfig()
        plugin.reloadWarpsConfig()
        replaces["%warp%"] = warpName
        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(MessagesUtil.getFullStringFromConfig(messagesConfig, "warp-deleted-successfully", replaces)))
        replaces.remove("%warp%")
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) {
            val message: TextComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(MessagesUtil.getFullStringFromConfig(messagesConfig, "console-command-error", replaces))
            sender.sendMessage(message)
            return false
        } else {
            val player: Player = sender
            if (player.isOp || player.hasPermission("perworldwarps.delwarp")) {
                if (args != null && args.size == 1) {
                    val warpsConfig: FileConfiguration = plugin.warpsConfigFile
                    replaces["%warp%"] = args[0]
                    if (warpsConfig.contains("Worlds.${player.world.name}.${args[0]}")) {
                        removeWarp(args[0], warpsConfig, player)
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