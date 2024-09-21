package dev.skh6075.bukkit.safeshulker

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.ShulkerBox
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID

class Main : JavaPlugin(), Listener {
    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerInteractEvent(event: PlayerInteractEvent) {
        val player = event.player
        val handItem = player.inventory.itemInMainHand
        if (isShulkerBoxItem(handItem) && event.action == Action.RIGHT_CLICK_AIR) {
            if (handItem.itemMeta is BlockStateMeta) {
                val itemMeta = handItem.itemMeta as BlockStateMeta
                if (itemMeta.blockState is ShulkerBox) {
                    val shulkerBox = itemMeta.blockState as ShulkerBox
                    val inventory = Bukkit.createInventory(null, 27, "셜커상자")
                    inventory.contents = shulkerBox.inventory.contents
                    player.openInventory(inventory)
                    player.playSound(player.location, Sound.BLOCK_SHULKER_BOX_OPEN, 1.0F, 1.0F)
                    containerMap[player.uniqueId] = inventory
                }
            }
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onInventoryCloseEvent(event: InventoryCloseEvent) {
        val player = (event.player as? Player) ?: return
        val inventory = containerMap[player.uniqueId] ?: return
        if (event.inventory == inventory) {
            val handItem = player.inventory.itemInMainHand
            if (isShulkerBoxItem(handItem)) {
                syncShulkerBoxItemUpdate(player, handItem, event.inventory)
                player.playSound(player.location, Sound.BLOCK_SHULKER_BOX_CLOSE, 1.0F, 1.0F)
            }
            containerMap.remove(player.uniqueId)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerDropItemEvent(event: PlayerDropItemEvent) {
        val player = event.player
        val item = event.itemDrop
        val inventory = containerMap[player.uniqueId] ?: return
        if (isShulkerBoxItem(item.itemStack)) {
            player.sendMessage(WARNING_TAG + "셜커 상자 인벤토리 확인 도중에는 셜커 상자를 버릴 수 없습니다!")
            event.isCancelled = true
            return
        }
        val handItem = player.inventory.itemInMainHand
        if (isShulkerBoxItem(handItem)) {
            syncShulkerBoxItemUpdate(player, handItem, inventory)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val inventory = containerMap[player.uniqueId] ?: return
        if (isShulkerBoxItem(event.currentItem ?: ItemStack(Material.AIR))) {
            player.sendMessage(WARNING_TAG + "셜커 상자 인벤토리 확인 도중에는 셜커 상자를 옮길 수 없습니다!")
            event.isCancelled = true
            return
        }
        val handItem = player.inventory.itemInMainHand
        if (isShulkerBoxItem(handItem)) {
            syncShulkerBoxItemUpdate(player, handItem, inventory)
        }
    }

    companion object {
        const val WARNING_TAG = "§c§l 오류: §r§7"

        private val containerMap: MutableMap<UUID, Inventory> = mutableMapOf()

        @JvmStatic
        fun isShulkerBoxItem(itemStack: ItemStack): Boolean = itemStack.type.name.endsWith("SHULKER_BOX")

        @JvmStatic
        fun syncShulkerBoxItemUpdate(player: Player, shulkerBoxItem: ItemStack, inventory: Inventory) {
            val itemMeta = (shulkerBoxItem.itemMeta as? BlockStateMeta) ?: return
            val shulkerBox = (itemMeta.blockState as? ShulkerBox) ?: return
            shulkerBox.inventory.contents = inventory.contents
            itemMeta.blockState = shulkerBox
            shulkerBoxItem.itemMeta = itemMeta
            player.inventory.setItemInMainHand(shulkerBoxItem)
        }
    }
}