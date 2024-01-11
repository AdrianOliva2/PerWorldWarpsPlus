package net.serveminecraft.minecrafteros.perworldwarpsplus.commands

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.serveminecraft.minecrafteros.perworldwarpsplus.utils.ListUtils
import net.serveminecraft.minecrafteros.perworldwarpsplus.utils.MessagesUtil
import net.serveminecraft.minecrafteros.perworldwarpsplus.PerWorldWarpsPlus
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class SetWarpCommand(private val plugin: PerWorldWarpsPlus): CommandExecutor {

    private val replaces: HashMap<String, String> = HashMap()
    private val messagesConfig: FileConfiguration = plugin.messagesConfigFile

    init {
        replaces["%prefix%"] = plugin.prefix
    }

    private fun setWarp(player: Player, warpName: String, permission: Boolean = false) {
        replaces["%warp%"] = warpName
        var item: ItemStack? = player.inventory.itemInMainHand
        if (item == null || item.isEmpty || item.type == Material.AIR) {
            val config: FileConfiguration = plugin.config
            item = ItemStack(Material.getMaterial(config.getString("Items.warpsDefaultItem.type")!!)!!, 1)
            val itemMeta: ItemMeta = item.itemMeta
            itemMeta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(MessagesUtil.getFullStringFromConfig(config, "Items.warpsDefaultItem.display-name", replaces)))
            itemMeta.lore(ListUtils.stringListToComponentList(MessagesUtil.colorizeList(MessagesUtil.getFullStringListFromConfig(config, "Items.warpsDefaultItem.lore", replaces))))
            item.itemMeta = itemMeta
        }
        val warpsConfig: FileConfiguration = plugin.warpsConfigFile
        val location: Location = player.location
        val world: World = location.world
        if (warpsConfig.get("Worlds.${world.name}.$warpName") == null) {
            val x: Double = location.x
            val y: Double = location.y
            val z: Double = location.z
            val yaw: Float = location.yaw
            val pitch: Float = location.pitch
            val itemType: String = item.type.name
            val itemMeta: ItemMeta = item.itemMeta
            val itemDisplayName: String = if (itemMeta.hasDisplayName() && itemMeta.displayName() != null)
                LegacyComponentSerializer.legacyAmpersand().serialize(itemMeta.displayName()!!)
            else
                LegacyComponentSerializer.legacyAmpersand().serialize(item.displayName())
            val itemLore: MutableList<String>? = MessagesUtil.colorizeList(ListUtils.componentListToStringList(itemMeta.lore()))
            warpsConfig.set("Worlds.${world.name}.$warpName.x", x)
            warpsConfig.set("Worlds.${world.name}.$warpName.y", y)
            warpsConfig.set("Worlds.${world.name}.$warpName.z", z)
            warpsConfig.set("Worlds.${world.name}.$warpName.yaw", yaw)
            warpsConfig.set("Worlds.${world.name}.$warpName.pitch", pitch)
            warpsConfig.set("Worlds.${world.name}.$warpName.item.type", itemType)
            warpsConfig.set("Worlds.${world.name}.$warpName.item.display-name", itemDisplayName)

            if (!itemLore.isNullOrEmpty())
                warpsConfig.set("Worlds.${world.name}.$warpName.item.lore", itemLore)

            if (permission)
                warpsConfig.set("Worlds.${world.name}.$warpName.permission", "perworldwarps.warp.$warpName")

            plugin.saveWarpsConfig()
            plugin.reloadWarpsConfig();
            val message = LegacyComponentSerializer.legacyAmpersand().deserialize(MessagesUtil.getFullStringFromConfig(messagesConfig, "warp-created-successfully", replaces))
            player.sendMessage(message)
        } else {
            val message = LegacyComponentSerializer.legacyAmpersand().deserialize(MessagesUtil.getFullStringFromConfig(messagesConfig, "warp-already-exists", replaces))
            player.sendMessage(message)
        }
        replaces.remove("%warp%")
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) {
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(MessagesUtil.getFullStringFromConfig(messagesConfig, "console-command-error", replaces)))
        } else {
            val player: Player = sender
            if (player.isOp || player.hasPermission("perworldwarps.setwarp")) {
                if (args != null) {
                    when (args.size) {
                        1 -> {
                            setWarp(player, args[0])
                        }
                        2 -> {
                            if (args[1] == "true" || args[1] == "false") {
                                val permission: Boolean = args[1].toBoolean()
                                setWarp(player, args[0], permission)
                            } else {
                                val message = LegacyComponentSerializer.legacyAmpersand().deserialize(MessagesUtil.getFullStringFromConfig(messagesConfig, "setwarp-command-help", replaces))
                                player.sendMessage(message)
                            }
                        }
                        else -> {
                            val message = LegacyComponentSerializer.legacyAmpersand().deserialize(MessagesUtil.getFullStringFromConfig(messagesConfig, "setwarp-command-help", replaces))
                            player.sendMessage(message)
                        }
                    }
                } else {
                    val message = LegacyComponentSerializer.legacyAmpersand().deserialize(MessagesUtil.getFullStringFromConfig(messagesConfig, "setwarp-command-help", replaces))
                    player.sendMessage(message)
                }
            } else {
                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(MessagesUtil.getFullStringFromConfig(messagesConfig, "no-permission", replaces)))
                return false
            }
        }
        return true
    }

}