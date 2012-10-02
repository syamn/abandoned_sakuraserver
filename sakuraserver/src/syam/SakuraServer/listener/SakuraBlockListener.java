package syam.SakuraServer.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import syam.SakuraServer.SakuraServer;
import syam.util.Actions;


public class SakuraBlockListener implements Listener {
	public final static Logger log = SakuraServer.log;
	private static final String logPrefix = SakuraServer.logPrefix;
	private static final String msgPrefix = SakuraServer.msgPrefix;

	private final SakuraServer plugin;

	public SakuraBlockListener(SakuraServer plugin){
		this.plugin = plugin;
	}

	/* 登録するイベントはここから下に */


	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onStopLavaToObsidianInNether(BlockPhysicsEvent event){
		Block block = event.getBlock();
		Material toMaterial = event.getChangedType();
		if(block.getWorld().getEnvironment().equals(Environment.NETHER)){
			if(block.getType()==Material.STATIONARY_LAVA &&
					toMaterial==Material.WATER){
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event){
		Block block = event.getBlock();
		Player player = event.getPlayer();

		if (block.getType() == Material.DIAMOND_ORE){
			// 音を鳴らす
			List<Note> notes = new ArrayList<Note>();
			notes.add(new Note(16));
			Actions.playNote(player, notes, 0L);
		}
	}

	// レッドストーン鉱石を壊した
	//@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true) //debug TODO:一時的に無効 1.3.1
	public void onRedstoneOreBreak(BlockBreakEvent event){
		Player player = event.getPlayer();
		Block block = event.getBlock();

		// アイテムID 73:Redstone ore || 74:Glowing redstone ore 以外は返す
		if (block.getWorld() != Bukkit.getServer().getWorld("resource") || (block.getTypeId() != 73 && block.getTypeId() != 74)){
			return;
		}

		// クリエイティブも無視
		if (player.getGameMode() == GameMode.CREATIVE){
			return;
		}

		int ran = (int)(Math.random() * 100); // 0-99 random

		// 爆発させる
		if (ran < 10){ // 0-19 → 20%
			//player.setNoDamageTicks(40); // 爆発時にダメージを受けないよう2秒間無敵
			block.getWorld().createExplosion(block.getLocation(), (float) 1.0, false);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event){
		Player player = event.getPlayer();
		Block block = event.getBlock();

		// ネザーポータルを作った (もしエンドポータルや今後追加されるポータルが判定されるようなら、event.getBlockAgainst().getType() == Material.OBSIDIAN を条件に追加？
		if(block.getType() == Material.PORTAL){
			// new 以外で作った場合、管理権限を持っていなければ削除
			if((block.getWorld() != Bukkit.getWorld("new")) && !(player.hasPermission("sakuraserver.admin"))){
				String loc = block.getWorld().getName()+":"+block.getX()+","+block.getY()+","+block.getZ();
				Actions.executeCommandOnConsole("kick "+player.getName()+" ネザーポータル設置違反 at "+loc);

				// ブロックの真上にあるポータルブロックは削除
				Actions.removeUpPortalBlock(block);
				// 東西南北どの方向にもう一つのポータル列があるか分からないので全方向に対して走査を行う
				Actions.removeUpPortalBlock(block.getRelative(BlockFace.EAST, 1));
				Actions.removeUpPortalBlock(block.getRelative(BlockFace.NORTH, 1));
				Actions.removeUpPortalBlock(block.getRelative(BlockFace.SOUTH, 1));
				Actions.removeUpPortalBlock(block.getRelative(BlockFace.WEST, 1));

				// 他プラグインのためにキャンセルをする
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockDamage(BlockDamageEvent event){
		if (event.getBlock().getType() == Material.BEDROCK){
			SakuraServer.bedrockConfig.put(event.getPlayer(), event.getBlock().getLocation());
			//Actions.message(null, event.getPlayer(), msgPrefix+"このブロックを右クリックすると黒曜石に変換できます！");
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onSignChange(SignChangeEvent event){
		Player player = event.getPlayer();
		Block block = event.getBlock();
		BlockState state = event.getBlock().getState();

		if (state instanceof Sign){
			Sign sign = (Sign)state;

			/* ヒール看板設置 */
			if(event.getLine(0).trim().equalsIgnoreCase("[HealSign]")){
				double cost = SakuraServer.configIntegerMap.get("HealSignCost").doubleValue();

				event.setLine(0, "&1[HealSign]");
				event.setLine(1, "右クリックすると");
				event.setLine(2, (int)cost + " Coinを使い");
				event.setLine(3, "回復看板になります");

				Actions.message(null, player, "&7 * 看板を右クリックすると、"+(int)cost+" Coinを支払い回復看板になります。");
				Actions.message(null, player, "&7 * 間違えてクリックしないように、&cこの状態のまま放置しないでください&7。");

				String loc = "("+block.getWorld().getName()+":"+block.getX()+","+block.getY()+","+block.getZ()+")";
				Actions.permcastMessage("sakuraserver.helper", "&c[通知] &6"+player.getName()+" &fが回復看板を仮設置しました"+loc);
			}
			/* ヒール看板終わり */
		}
	}
}
