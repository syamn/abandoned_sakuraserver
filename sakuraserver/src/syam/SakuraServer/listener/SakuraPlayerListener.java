package syam.SakuraServer.listener;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.minecraft.server.Packet201PlayerInfo;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import syam.SakuraServer.SakuraPlayer;
import syam.SakuraServer.SakuraServer;
import syam.util.Actions;

public class SakuraPlayerListener implements Listener {
	public final static Logger log = SakuraServer.log;
	private static final String logPrefix = SakuraServer.logPrefix;
	private static final String msgPrefix = SakuraServer.msgPrefix;

	private final SakuraServer plugin;

	public SakuraPlayerListener(SakuraServer plugin){
		this.plugin = plugin;
	}

	/* 登録するイベントはここから下に */

	// @EventHandler (priority,ignoreCancelled)

	/**
	 * プレイヤーがゲームに参加した
	 * @param event
	 */
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoin(PlayerJoinEvent event){
		final Player player = event.getPlayer();
		// Tabリスト色変更
		Actions.changeTabNameDefault(player.getName());

		// Tabリスト追加
		for (String addName : SakuraServer.fakeJoinedPlayerList){
			((CraftPlayer)player).getHandle().netServerHandler.sendPacket(new Packet201PlayerInfo(addName, true, ((CraftPlayer)player).getHandle().ping));
		}

		// メモリマッピング
		SakuraPlayer sakuraPlayer = new SakuraPlayer(player.getName());
		SakuraServer.playerData.put(player, sakuraPlayer);

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(SakuraServer.getInstance(), new Runnable() {
			public void run() {
				// 重い処理はタイマー内の別タスク内で行う
				// 飛行モード有効チェック
				Actions.joinAutoFlyPlayer(player);

				// 資源ワールドのバージョンチェック
				Actions.checkResourceWorld(player);
			}
		}, 0L);
	}

	/**
	 * プレイヤーがゲームから離脱した
	 * @param event
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerQuit(PlayerQuitEvent event){
		Player player = event.getPlayer();
		if(SakuraServer.playerData.containsKey(player)){
			SakuraServer.playerData.get(player).save();
			SakuraServer.playerData.remove(player);
		}

		// 基本的にログアウト移動時にはAdmin以外クリエイティブを外す
		if (player.getGameMode() == GameMode.CREATIVE){
			if (!player.hasPermission("sakuraserver.admin")){
				player.setGameMode(GameMode.SURVIVAL);
			}
		}
	}

	/**
	 * プレイヤーのワールドが変わった
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChangedWorld(PlayerChangedWorldEvent event){
		Player player = event.getPlayer();
		// エンド進入時にメッセージ表示
		if (player.getWorld() == Bukkit.getWorld("new_the_end")){
			Actions.message(null, player, "&b ここは定期的にリセットされるエンドワールドです");
			Actions.message(null, player, "&b メインワールドに戻るには &f/spawn &bコマンドを使ってください");
		}

		// ハードエンド
		if (player.getWorld() == Bukkit.getWorld("hard_end")){
			player.setFireTicks(0);
			if (player.getHealth() < 20){
				player.setHealth(player.getHealth() + 1);
			}
			Actions.message(null, player, "&b ここはハードエンドです！&c危険です！");
			Actions.message(null, player, "&b メインワールドに戻るには &f/spawn &bコマンドを使ってください");
		}

		// 飛行モードが有効なプレイヤーが new, tropicワールドに移動した場合は飛行モードに
		if (player.getWorld() == Bukkit.getWorld("new") || player.getWorld() == Bukkit.getWorld("tropic")){
			if (SakuraServer.flyingPlayerList.contains(player.getName())){
				player.setAllowFlight(true);
			}
		}else{
			// 他のワールドなら飛行モード解除
			if(player.getGameMode() != GameMode.CREATIVE){
				player.setAllowFlight(false);
			}
		}

		// 基本的にワールド移動時にはAdmin以外クリエイティブを外す
		if (player.getGameMode() == GameMode.CREATIVE){
			if (!player.hasPermission("sakuraserver.admin")){
				player.setGameMode(GameMode.SURVIVAL);
			}
		}
	}

	/**
	 *
	 * @param event
	 */
	@SuppressWarnings("unused")
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent event){
		// ここには何も書かない
		if (event.isCancelled()){
			return;
		}
		Player player = event.getPlayer();
		Block block = null;
		if (event.hasBlock()){
			block = event.getClickedBlock();
		}else{
			// ブロック以外のアクションはreturn
			return;
		}

		/* 岩盤右クリック */

		if ((block.getType() == Material.BEDROCK) && (event.getAction() == Action.RIGHT_CLICK_BLOCK)){
			if(SakuraServer.bedrockConfig.containsKey(player)){
				if(block.getLocation().equals(SakuraServer.bedrockConfig.get(player))){
					if(player.hasPermission("sakuraserver.admin")){
						block.setType(Material.OBSIDIAN);
						Actions.message(null, player, msgPrefix+"黒曜石に変換しました！");
						String loc = block.getWorld().getName()+":"+block.getX()+","+block.getY()+","+block.getZ();
						Actions.log("Bedrock.log", "Player " + player.getName() + " changed Bedrock to Obsidian at " + loc);
					}else{
						Actions.message(null, player, msgPrefix+"権限がありません");
					}
				}

				SakuraServer.bedrockConfig.remove(player);
			}
		}

		/* 看板右クリック */
		if ((block.getState() instanceof Sign) && (event.getAction() == Action.RIGHT_CLICK_BLOCK)){
			Sign sign = (Sign) block.getState();
			/* 回復看板化 */
			if (sign.getLine(0).equals("§1[HealSign]")){
				double cost = SakuraServer.configIntegerMap.get("HealSignCost").doubleValue();

				if (!(Actions.checkMoney(player.getName(), cost))){ // お金が足りない
					Actions.message(null, player, "&cHeal看板を設置するには"+ (int)cost +"Coinが必要です！");
				}else{
					if (!(Actions.takeMoney(player.getName(), cost))){ // 引き落としエラー
						Actions.message(null, player, "&cお金の引き落とし処理にエラーが発生しました！");
					}else{ // OK
						String onSignName = player.getName();
						if (onSignName.length() > 15){
							onSignName = onSignName.substring(0, 15);
						}
						sign.setLine(0, "§1[Heal]");
						sign.setLine(1, "");
						sign.setLine(2, onSignName);
						sign.setLine(3, "§cSakuraServer");
						sign.update();
						Actions.message(null, player, "&bHeal看板を設置し"+(int)cost+"Coinを支払いました！");
						String loc = "("+block.getWorld().getName()+":"+block.getX()+","+block.getY()+","+block.getZ()+")";
						Actions.permcastMessage("sakuraserver.helper", "&c[通知] &6"+player.getName()+" &fが回復看板を設置しました"+loc);
						log.info(logPrefix + player.getName() + " put the [Heal] at "+loc);
						Actions.log("Sign.log", "Player " + player.getName() + " put the [Heal] sign at "+loc);
					}
				}
			}
		}

		/* デバッグ用 */
		if (false && player == Bukkit.getServer().getPlayer("syamn")){
			log.info("==Debug==");
			log.info("getType(): "+block.getType());
			log.info("getData(): "+block.getData());
			log.info("getTypeId():"+block.getTypeId());
		}
	}

	/**
	 * プレイヤーがテレポートした
	 * @param event
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerTeleport(PlayerTeleportEvent event){
		// ここには何も書かない
		if (event.isCancelled()){
			return;
		}

		// もし他のプレイヤーが上に乗っていれば降ろす
		Player player = event.getPlayer();
		if (player.getPassenger() != null &&
				(player.getPassenger() instanceof Player)){
			player.eject();
		}
	}


	public void onPlayerRespawn(PlayerRespawnEvent event){

	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerAnimation(PlayerAnimationEvent event){
		// ここには何も書かない
		if (event.isCancelled()){
			return;
		}

	}

	/**
	 * プレイヤーが空島から落ちた
	 * @param event
	 */
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerFallOfSkyland(PlayerMoveEvent event){
		Player player = event.getPlayer();

		// 空島以外は処理しない
		if (player.getWorld() == Bukkit.getWorld("skylands")){
			Location toLoc = event.getTo();
			if (toLoc.getY() <= -50.0D){
				// Y=-50以下に落ちたらメインワールドの空へテレポート
				Location newSky = new Location(Bukkit.getWorld("new"), toLoc.getX(), 500.0D, toLoc.getZ());
				player.teleport(newSky, TeleportCause.PLUGIN);
			}
		}
	}

	/**
	 * プレイヤーが動いた
	 * @param event
	 */
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event){
		Player player = event.getPlayer();
		Location fromLoc = event.getFrom();
		Location toLoc = event.getTo();

		// 飛行モードの間、Y=0～256外への移動を制限
		if (SakuraServer.flyingPlayerList.contains(player.getName())){
			// あまりに離れている場合
			if(toLoc.getY() > 257 || toLoc.getY() < 0){
				event.setCancelled(true);
				Actions.message(null, player, msgPrefix+"飛行モード時の制約で移動できません");
			}
		}

		// 移動後の座標から移動前の座標を引く
		Location diffLoc = toLoc.clone().subtract(fromLoc);
		// 進行方向のブロックを取得する (下半身部と上半身部)
		Location checkLoc = toLoc.clone().add(diffLoc.clone().multiply(3.0D));
		checkLoc.setY(toLoc.getY());
		Block checkUnderBlock =checkLoc.getBlock();
		Block checkUpperBlock = checkUnderBlock.getRelative(BlockFace.UP, 1);

		if (checkUnderBlock.getType() == Material.DIAMOND_BLOCK ||
			checkUpperBlock.getType() == Material.DIAMOND_BLOCK){

			Actions.message(null, player, "&c Touch Diamond block!");

			//音を鳴らす
			List<Note> notes = new ArrayList<Note>();
			notes.add(new Note(20));
			Actions.playNote(player, notes, 0L);

			Vector dir = diffLoc.getDirection();
			Vector vect = new Vector((-(dir.getX())) * 5.0D, 2.0D, (-(dir.getZ())) * 5.0D);

			player.setVelocity(vect);
		}
	}

	/**
	 * プレイヤーがキックされた
	 * @param event
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerKick(PlayerKickEvent event){
		// ここには何も書かない
		if (event.isCancelled()){
			return;
		}
		Player player = event.getPlayer();
		String reason = event.getReason();


		String newMessage = msgPrefix+"&6"+player.getDisplayName()+" &aはKickされました: "+event.getReason();
		newMessage = newMessage.replaceAll("&([0-9a-fk-or])", "\u00A7$1");

		event.setLeaveMessage(newMessage);
	}

}
