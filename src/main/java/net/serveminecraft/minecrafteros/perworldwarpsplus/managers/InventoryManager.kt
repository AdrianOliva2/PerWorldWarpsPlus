package net.serveminecraft.minecrafteros.perworldwarpsplus.managers

import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.serveminecraft.minecrafteros.perworldwarpsplus.commands.WarpCommand
import net.serveminecraft.minecrafteros.perworldwarpsplus.commands.WarpsCommand
import net.serveminecraft.minecrafteros.perworldwarpsplus.namespacedkeys.Keys
import net.serveminecraft.minecrafteros.perworldwarpsplus.utils.ListUtils
import net.serveminecraft.minecrafteros.perworldwarpsplus.PerWorldWarpsPlus
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType

class InventoryManager private constructor(private val plugin: PerWorldWarpsPlus) : Listener {

    companion object {
        private lateinit var instance: InventoryManager
        fun getInstance(plugin: PerWorldWarpsPlus): InventoryManager {
            if (!this::instance.isInitialized) instance = InventoryManager(plugin)
            return instance
        }
    }

    private val actualPlayersPage: MutableMap<Player, Int>
    private val config: FileConfiguration
    private var warpsConfiguration: FileConfiguration
    private var opened = false

    init {
        actualPlayersPage = HashMap()
        config = plugin.config
        warpsConfiguration = plugin.warpsConfigFile
    }

    fun reloadAllWarpInventories() {
        warpsConfiguration = plugin.warpsConfigFile
        val playersWithWarpsInventoryOpened: MutableList<Player> = actualPlayersPage.keys.toMutableList()
        actualPlayersPage.clear()
        for (player: Player in playersWithWarpsInventoryOpened) {
            player.openInventory(createInventory(player))
        }
    }

    private fun getComponentsLoreList(originalLore: MutableList<*>?): MutableList<TextComponent>? {
        if (originalLore != null) {
            val stringLore: MutableList<TextComponent> = emptyList<TextComponent>().toMutableList()

            for (line: Any? in originalLore) {
                if (line != null) {
                    stringLore += LegacyComponentSerializer.legacyAmpersand().deserialize(line.toString())
                }
            }

            return stringLore
        }
        return null
    }

    private fun getItemFromWarp(world: World, warp: String): ItemStack {
        val item = ItemStack(Material.getMaterial(warpsConfiguration.getString("Worlds.${world.name}.$warp.item.type")!!)!!, 1)
        val itemMeta = item.itemMeta
        itemMeta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(warpsConfiguration.getString("Worlds.${world.name}.$warp.item.display-name")!!))
        itemMeta.lore(ListUtils.stringListToComponentList(warpsConfiguration.getStringList("Worlds.${world.name}.$warp.item.lore")))
        itemMeta.persistentDataContainer.set(Keys.WARPS_ITEMS, PersistentDataType.STRING, warp)
        item.itemMeta = itemMeta
        return item
    }

    private fun getWarpsItemsList(world: World, warps: MutableList<String>?): MutableList<ItemStack> {
        val warpsItemsList: MutableList<ItemStack> = emptyList<ItemStack>().toMutableList()
        if (!warps.isNullOrEmpty()) {
            for (warp: String in warps) {
                warpsItemsList += getItemFromWarp(world, warp)
            }
        }
        return warpsItemsList
    }

    private fun fillBorders(inventory: Inventory, rows: Int) {
        val material = config.getString("Menu.item.borderItem.type")!!
        val borderItem = ItemStack(Material.getMaterial(material)!!)
        val meta = borderItem.itemMeta
        val displayName = config.getString("Menu.item.borderItem.display-name")!!
        meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(displayName))
        val lore: MutableList<*>? = config.getList("Menu.item.borderItem.lore")
        meta.lore(getComponentsLoreList(lore))
        meta.addItemFlags(
            ItemFlag.HIDE_ATTRIBUTES,
            ItemFlag.HIDE_UNBREAKABLE,
            ItemFlag.HIDE_DESTROYS,
            ItemFlag.HIDE_DYE,
            ItemFlag.HIDE_ENCHANTS,
            ItemFlag.HIDE_PLACED_ON,
            ItemFlag.HIDE_ITEM_SPECIFICS
        )
        borderItem.itemMeta = meta
        for (i in 0..8) inventory.setItem(i, borderItem)
        for (i in inventory.size - 9 until inventory.size) inventory.setItem(i, borderItem)
        for (i in 1 until rows) {
            inventory.setItem(i * 9, borderItem)
            inventory.setItem(i * 9 + 8, borderItem)
        }
    }

    fun createInventory(player: Player): Inventory {
        var material = config.getString("Menu.item.previousPage.type")
        val previousPageItem = ItemStack(Material.getMaterial(material!!)!!)
        material = config.getString("Menu.item.nextPage.type")
        val nextPageItem = ItemStack(Material.getMaterial(material!!)!!)
        val warpsCommand = WarpsCommand(plugin)
        val warpsItems = getWarpsItemsList(player.world, warpsCommand.getAvailableWarpsForPlayer(player))
        val inventoryTitle = config.getString("Menu.title")!!
        val inventory = Bukkit.createInventory(player, 54, LegacyComponentSerializer.legacyAmpersand().deserialize(inventoryTitle))
        val actualPlayerPage: Int?
        if (actualPlayersPage.containsKey(player)) actualPlayerPage = actualPlayersPage[player] else {
            actualPlayerPage = 1
            actualPlayersPage[player] = 1
        }
        opened = true
        player.closeInventory()
        val rows: Int = inventory.size / 9
        fillBorders(inventory, rows)
        val numItemsBorder = (rows - 2) * 2 + 9 * 2
        val maxWarpsPerPage = inventory.size - numItemsBorder
        val maxPages: Int = if (warpsItems.size % maxWarpsPerPage == 0) {
            if (warpsItems.size / maxWarpsPerPage == 0) {
                1
            } else {
                warpsItems.size / maxWarpsPerPage
            }
        } else {
            warpsItems.size / maxWarpsPerPage + 1
        }
        material = config.getString("Menu.item.closeMenu.type")
        var item = ItemStack(Material.getMaterial(material!!)!!, 1)
        var meta = item.itemMeta
        var displayName = config.getString("Menu.item.closeMenu.display-name")!!
        meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(displayName))
        var lore: MutableList<*>? = config.getList("Menu.item.closeMenu.lore")
        meta.lore(getComponentsLoreList(lore))
        item.itemMeta = meta
        inventory.setItem(inventory.size - 1, item)
        if (actualPlayerPage!! < maxPages) {
            meta = nextPageItem.itemMeta
            displayName = config.getString("Menu.item.nextPage.display-name")!!
            meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(displayName))
            lore = config.getList("Menu.item.nextPage.lore")
            meta.lore(getComponentsLoreList(lore))
            nextPageItem.itemMeta = meta
            inventory.setItem(inventory.size - 3, nextPageItem)
        }
        material = config.getString("Menu.item.actualPage.type")
        item = ItemStack(Material.getMaterial(material!!)!!, actualPlayerPage)
        meta = item.itemMeta
        displayName = config.getString("Menu.item.actualPage.display-name")!!
        displayName = displayName.replace("%actualPlayerPage%".toRegex(), actualPlayerPage.toString())
        displayName = displayName.replace("%maxPage%".toRegex(), maxPages.toString())
        meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(displayName))
        lore = config.getList("Menu.item.actualPage.lore")
        meta.lore(getComponentsLoreList(lore))
        item.itemMeta = meta
        inventory.setItem(inventory.size - 5, item)
        if (actualPlayerPage > 1) {
            meta = previousPageItem.itemMeta
            displayName = config.getString("Menu.item.previousPage.display-name")!!
            meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(displayName))
            lore = config.getList("Menu.item.previousPage.lore")
            meta.lore(getComponentsLoreList(lore))
            previousPageItem.itemMeta = meta
            inventory.setItem(inventory.size - 7, previousPageItem)
        }
        var i = actualPlayerPage * maxWarpsPerPage - maxWarpsPerPage
        while (i < actualPlayerPage * maxWarpsPerPage && i < warpsItems.size) {
            inventory.setItem(inventory.firstEmpty(), warpsItems[i])
            i++
        }
        opened = false
        return inventory
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val item = event.currentItem
        val inventory = event.clickedInventory
        val playerClicked = event.whoClicked as Player
        if (item != null && item.type != Material.AIR && inventory!!.type != InventoryType.PLAYER) {
            val menuTitle = config.getString("Menu.title")!!
            if (event.view.title() == LegacyComponentSerializer.legacyAmpersand().deserialize(menuTitle)) {
                event.isCancelled = true
                val materialCloseItem = config.getString("Menu.item.closeMenu.type")!!
                val materialBorderItems = config.getString("Menu.item.borderItem.type")!!
                val materialPreviousPage = config.getString("Menu.item.previousPage.type")!!
                val materialActualPage = config.getString("Menu.item.actualPage.type")!!
                val materialNextPage = config.getString("Menu.item.nextPage.type")!!
                if (
                    item.type == Material.getMaterial(materialCloseItem)
                    || item.type == Material.getMaterial(materialBorderItems)
                    || item.type == Material.getMaterial(materialPreviousPage)
                    || item.type == Material.getMaterial(materialActualPage)
                    || item.type == Material.getMaterial(materialNextPage)
                ) {
                    if (item.type == Material.getMaterial(materialPreviousPage) || item.type == Material.getMaterial(materialNextPage)) {
                        var actualPage = actualPlayersPage[playerClicked]
                        if (actualPage == null) {
                            actualPage = 1
                        }
                        actualPlayersPage.remove(playerClicked)
                        if (item.type == Material.getMaterial(materialNextPage)) {
                            actualPlayersPage[playerClicked] = actualPage + 1
                        } else {
                            actualPlayersPage[playerClicked] = actualPage - 1
                        }
                        playerClicked.openInventory(createInventory(playerClicked))
                        return
                    }
                    if (item.type == Material.getMaterial(materialCloseItem)) {
                        playerClicked.closeInventory()
                        if (playerClicked.inventory.contains(item)) playerClicked.inventory.remove(item)
                    }
                } else {
                    val itemMeta: ItemMeta = item.itemMeta
                    val warp: String? = itemMeta.persistentDataContainer.get(Keys.WARPS_ITEMS, PersistentDataType.STRING)
                    if (!warp.isNullOrEmpty()) {
                        val warpCommand = WarpCommand(plugin)
                        warpCommand.teleport(playerClicked, warpsConfiguration, warp)
                    }
                }
            }
        }
    }

    @EventHandler
    fun onCloseInventoy(event: InventoryCloseEvent) {
        if (event.player.type == EntityType.PLAYER) {
            val player = event.player as Player
            if (actualPlayersPage.containsKey(player)) {
                if (!opened) actualPlayersPage.remove(player)
            }
        }
    }
}