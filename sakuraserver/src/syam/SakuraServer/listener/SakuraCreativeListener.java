/**
 * sakuraserver - Package: syam.SakuraServer.listener
 * Created: 2012/11/24 6:07:35
 */
package syam.SakuraServer.listener;

import java.util.logging.Logger;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import syam.SakuraServer.SakuraServer;
import syam.util.Actions;

/**
 * SakuraCreativeListener (SakuraCreativeListener.java)
 * @author syam(syamn)
 */
public class SakuraCreativeListener implements Listener{
	private static final Logger log = SakuraServer.log;
	private static final String logPrefix = SakuraServer.logPrefix;
	private static final String msgPrefix = SakuraServer.msgPrefix;

	private final SakuraServer plugin;
	public SakuraCreativeListener(final SakuraServer plugin){
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onCreatureSpawn(final CreatureSpawnEvent event){
		final Entity ent = event.getEntity();
		if (ent.getWorld().getName().equals("creative")){
			switch (event.getSpawnReason()){
				case SPAWNER:
				case SPAWNER_EGG:
				case CUSTOM:
				case SLIME_SPLIT:
				case LIGHTNING:
				case JOCKEY:
					break;
				default:
					event.setCancelled(true);
					break;
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerItemHeldEvent(final PlayerItemHeldEvent event){
		final Player player = event.getPlayer();
		if (!player.getWorld().getName().equals("creative") || player.hasPermission("sakuraserver.admin")){
			return;
		}

		final Inventory inv = player.getInventory();
		final ItemStack item = inv.getItem(event.getNewSlot());
		if (item == null) return;
		if (isNotAllowedItem(item)){
			//event.getPlayer().setItemInHand(null);
			inv.setItem(event.getNewSlot(), null);
			Actions.message(null, player, "&cこのアイテムは使用できません！");
		}
	}

	private boolean isNotAllowedItem(final ItemStack is){
		if (is == null) return false;

		switch(is.getType()){
			case EXP_BOTTLE:
			case SNOW_BALL:
			case MONSTER_EGG:
			case POTION:
			case FIREBALL:
			case ENDER_CHEST:
			case ENDER_PEARL:
			case EYE_OF_ENDER:
				return true;
			default:
				return false;
		}
	}
}
