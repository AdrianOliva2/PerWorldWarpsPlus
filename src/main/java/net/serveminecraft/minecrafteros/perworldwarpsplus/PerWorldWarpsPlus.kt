package net.serveminecraft.minecrafteros.perworldwarpsplus

import io.papermc.paper.plugin.configuration.PluginMeta
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.serveminecraft.minecrafteros.perworldwarpsplus.commands.*
import net.serveminecraft.minecrafteros.perworldwarpsplus.configs.CustomConfig
import net.serveminecraft.minecrafteros.perworldwarpsplus.managers.InventoryManager
import net.serveminecraft.minecrafteros.perworldwarpsplus.tabcompleters.SetWarpCompleter
import net.serveminecraft.minecrafteros.perworldwarpsplus.tabcompleters.SubCommandsCompleter
import net.serveminecraft.minecrafteros.perworldwarpsplus.tabcompleters.WarpsCompleter
import org.bukkit.command.PluginCommand
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.java.JavaPlugin

class PerWorldWarpsPlus : JavaPlugin() {

    companion object{
        fun getInstance(): PerWorldWarpsPlus {
            return getPlugin(PerWorldWarpsPlus::class.java)
        }
    }

    @Suppress("UnstableApiUsage")
    private val pluginMeta: PluginMeta = getPluginMeta()
    private val pluginName: String = pluginMeta.name
    private val pluginVersion: String = pluginMeta.version
    private lateinit var warpsConfig: CustomConfig
    lateinit var warpsConfigFile: FileConfiguration
    private lateinit var messagesConfig: CustomConfig
    lateinit var messagesConfigFile: FileConfiguration
    var prefix: String = ""

    override fun onEnable() {
        registerConfig()
        prefix = messagesConfigFile.getString("prefix", "")!!
        registerCommands()
        registerEvents()
        server.consoleSender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&8[&b$pluginName&8] &aHas been enabled on version &8[&b$pluginVersion&8]"))
    }

    private fun registerConfig() {
        config.options().copyDefaults()
        saveDefaultConfig()
        warpsConfig = CustomConfig("warps.yml")
        warpsConfigFile = warpsConfig.getConfig()
        messagesConfig = CustomConfig("messages.yml")
        messagesConfigFile = messagesConfig.getConfig()
    }

    private fun registerCommands() {
        val perWorldWarpsCommand: PluginCommand? = getCommand("perworldwarps")
        perWorldWarpsCommand?.aliases = listOf("pww")
        perWorldWarpsCommand?.tabCompleter = SubCommandsCompleter()
        perWorldWarpsCommand?.setExecutor(PerWorldWarpsCommand())
        getCommand("warp")?.setExecutor(WarpCommand())
        getCommand("warp")?.tabCompleter = WarpsCompleter()
        getCommand("warps")?.setExecutor(WarpsCommand())
        getCommand("setwarp")?.setExecutor(SetWarpCommand())
        getCommand("setwarp")?.tabCompleter = SetWarpCompleter()
        getCommand("delwarp")?.setExecutor(DelWarpCommand())
        getCommand("delwarp")?.tabCompleter = WarpsCompleter()
    }

    private fun registerEvents() {
        val pm: PluginManager = server.pluginManager
        val inventoryManager: InventoryManager = InventoryManager.getInstance()
        pm.registerEvents(inventoryManager, this)
    }

    fun saveWarpsConfig() {
        warpsConfig.saveConfig()
    }

    fun reloadWarpsConfig() {
        warpsConfig.reloadConfig()
        saveWarpsConfig()
        warpsConfigFile = warpsConfig.getConfig()
    }

    fun reloadMessagesConfig() {
        messagesConfig.reloadConfig()
        messagesConfig.saveConfig()
        messagesConfigFile = messagesConfig.getConfig()
        prefix = messagesConfigFile.getString("prefix", "")!!
    }

    override fun onDisable() {
        this.server.consoleSender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&8[&b$pluginName&8] &cHas been disabled on version &8[&b$pluginVersion&8]"))
    }

}
