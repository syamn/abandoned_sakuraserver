/**
 * sakuraserver - Package: syam.SakuraServer.listener
 * Created: 2012/11/24 5:47:08
 */
package syam.SakuraServer.listener;

import java.util.logging.Logger;

import org.bukkit.GameMode;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import syam.SakuraServer.SakuraServer;
import syam.util.Actions;

/**
 * SakurInventoryListener (SakurInventoryListener.java)
 * @author syam(syamn)
 */
public class SakuraInventoryListener implements Listener {
	private static final Logger log = SakuraServer.log;
	private static final String logPrefix = SakuraServer.logPrefix;
	private static final String msgPrefix = SakuraServer.msgPrefix;

	private final SakuraServer plugin;
	public SakuraInventoryListener(final SakuraServer plugin){
		this.plugin = plugin;
	}

	/**
	 * 手持ちのアイテムが替わった No ignoreCancel(無視しない)
	 * @param event
	 */
	@EventHandler(priority = EventPriority.LOW)
	public void onInventoryClick(final InventoryClickEvent event){
		final ItemStack item = event.getCurrentItem();
		if (item == null) return;

		switch (item.getType()){
			case MOB_SPAWNER:
			case MONSTER_EGG:
				boolean flag = false;
				for (final Enchantment e : item.getEnchantments().keySet()){
					item.removeEnchantment(e);
					flag = true;
				}
				if (flag){
					Player player = (Player) event.getWhoClicked();
					log.info(logPrefix+ "Player " + player.getName() + " clicked item has invalid enchant! Removed! item: " + item.getType().name());
					Actions.message(null, player, "&cクリックしたアイテムの不正なエンチャントを削除しました！");
				}
				break;
			default:
				break;
		}
	}
}
