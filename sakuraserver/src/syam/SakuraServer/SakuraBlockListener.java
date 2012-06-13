package syam.SakuraServer;

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


public class SakuraBlockListener implements Listener {
	public final static Logger log = SakuraServer.log;
	private static final String logPrefix = SakuraServer.logPrefix;
	private static final String msgPrefix = SakuraServer.msgPerfix;

	private final SakuraServer plugin;

	public SakuraBlockListener(SakuraServer plugin){
		this.plugin = plugin;
	}

	/* 登録するイベントはここから下に */


	@EventHandler(priority = EventPriority.HIGH)
	public void onStopLavaToObsidianInNether(BlockPhysicsEvent event){
		// ここには何も書かない
		if (event.isCancelled()){
			return;
		}

		Block block = event.getBlock();
		Material toMaterial = event.getChangedType();
		if(block.getWorld().getEnvironment().equals(Environment.NETHER)){
			if(block.getType()==Material.STATIONARY_LAVA &&
					toMaterial==Material.WATER){
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockBreak(BlockBreakEvent event){
		// ここには何も書かない
		if (event.isCancelled()){
			return;
		}
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
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
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
		if (ran < 20){ // 0-19 → 20%
			player.setNoDamageTicks(40); // 爆発時にダメージを受けないよう2秒間無敵
			block.getWorld().createExplosion(block.getLocation(), (float) 4.0, false);
		}
	}

	// ピストンが展開した
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockPistonExtend(BlockPistonExtendEvent event){
		// ここには何も書かない
		if (event.isCancelled()){
			return;
		}
		Block block = event.getBlock();
		BlockFace direction = event.getDirection();
		Block headBlock = block.getRelative(direction, 1);

		// 押した先のブロックが空気(0:AIR)の場合
		if (headBlock.getType() == Material.AIR){
			// 飛ばす強さ(Y軸方向のベクトル) 真下の看板によって変更されない場合はこの値
			double flyVector = 3.0D;
			// 横方向へ飛ばす強さ(XZ軸方向のベクトルへ掛ける) 変更されない場合は1.0倍
			double sideMultiply = 1.0D;

			// 落下死対策のジャンプポーション効果時間(sec)
			int potionDurationInSec = 6;

			// 例外ピストン用の看板チェック
			// 真下に以下の看板があれば無効、または飛ばす強さを調整
			BlockState checkBlock = block.getRelative(BlockFace.DOWN, 1).getState();
			if (checkBlock instanceof Sign){
				Sign sign = (Sign)checkBlock;

				for (int i=0; i < 4; i++){
					if(sign.getLine(i).trim().equalsIgnoreCase("飛ばない") ||
					   sign.getLine(i).trim().equalsIgnoreCase("とばない") ||
					   sign.getLine(i).trim().equalsIgnoreCase("Can't Fly") ||
					   sign.getLine(i).trim().equalsIgnoreCase("Cant Fly") ||
					   sign.getLine(i).trim().equalsIgnoreCase("Can not Fly") ||
					   sign.getLine(i).trim().equalsIgnoreCase("Cannot Fly")){
						return;
					}
					// flyVector はY軸方向のベクトル
					// sideMultiply をXZ軸方向のベクトルに掛ける
					if (i == 3) continue;
					if (sign.getLine(i).trim().equalsIgnoreCase("[つよさ]") ||
						sign.getLine(i).trim().equalsIgnoreCase("§1[つよさ]") ||
						sign.getLine(i).trim().equalsIgnoreCase("[Power]")){
						if (sign.getLine(i+1).trim().equalsIgnoreCase("つよい") ||
							sign.getLine(i+1).trim().equalsIgnoreCase("High")){
							flyVector = 4.0D;
							sideMultiply = 1.5; // つよい→1.5倍
							potionDurationInSec = 6;
						}else if (sign.getLine(i+1).trim().equalsIgnoreCase("よわい") ||
								  sign.getLine(i+1).trim().equalsIgnoreCase("Low")){
							flyVector = 2.0D;
							sideMultiply = 0.5; // よわい→0.5倍
							potionDurationInSec = 4;
						}else if (sign.getLine(i+1).trim().equalsIgnoreCase("ふつう") ||
								  sign.getLine(i+1).trim().equalsIgnoreCase("Middle")){
							flyVector = 3.0D;
							sideMultiply = 1.0D; // ふつう→1.0倍(デフォルト)
							potionDurationInSec = 5;
						}else{
							continue;
						}
						sign.setLine(i, "§1[つよさ]");
						sign.update();
					}
				}
			}

			// 上向きのピストンの場合
			if (direction == BlockFace.UP){
				// add(0.5, 0.0, 0.5) は上向きの場合？
				Location headBlockLoc = headBlock.getLocation().add(0.5, 0.0, 0.5);
				// オンラインプレイヤーを走査
				for (Player player : Bukkit.getServer().getOnlinePlayers()){
					Location playerLoc = player.getLocation();

					if (playerLoc.getWorld() != headBlockLoc.getWorld()){
						continue;
					}
					// ピストンに押されたブロックの座標とプレイヤーの座標を計算
					double distance = playerLoc.distance(headBlockLoc);
					if (distance >= 1.0){
						continue;
					}

					// プレイヤーのベクトルを初期値に
					Vector dir = player.getVelocity();
					Vector vect = new Vector(dir.getX() * 3.0D, flyVector, dir.getZ() * 3.0D);

					// 上手く飛ぶようにプレイヤーを浮かす
					player.teleport(playerLoc.add(0, 0.5, 0));
					player.setVelocity(vect); // 飛ばす

					// 落下死対策
					if (player.hasPotionEffect(PotionEffectType.JUMP))
						player.removePotionEffect(PotionEffectType.JUMP);
					player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, potionDurationInSec * 20, 0));

					Actions.message(null, player, "Fly!");
				}
			}

			// 上と下向き以外のピストンの場合
			if (direction != BlockFace.UP && direction != BlockFace.DOWN){
				// TODO:後で調整する プレイヤーの座標とブロックの座標間計算の元になるブロック座標
				Location headBlockLoc = headBlock.getLocation().add(0.5, 0.0, 0.5);

				for (Player player : Bukkit.getServer().getOnlinePlayers()){
					Location playerLoc = player.getLocation();

					if (playerLoc.getWorld() != headBlockLoc.getWorld()){
						continue;
					}

					double distance = playerLoc.distance(headBlockLoc);
					if (distance >= 1.0){
						continue;
					}

					Vector dir = player.getVelocity();

					// ピストンの向きによって飛ばす方向を変える
					// 元になるベクトル
					Vector vect = null;

					/* リファクタリング済み */

					if (direction == BlockFace.EAST){
						// 東向き→実際には北向き？ Z軸を負に
						vect = new Vector(0.0D * sideMultiply, 0, -2.0D * sideMultiply);
					}else if(direction == BlockFace.WEST){
						// 西向き→実際には南 Z軸を正に
						vect = new Vector(0.0D * sideMultiply, 0, 2.0D * sideMultiply);
					}else if(direction == BlockFace.SOUTH){
						// 南向き→東 X軸を正に
						vect = new Vector(2.0D * sideMultiply, 0, 0.0D * sideMultiply);
					}else if(direction == BlockFace.NORTH){
						// 北向き→西 X軸を負に
						vect = new Vector(-2.0D * sideMultiply, 0, 0.0D * sideMultiply);
					}

					// 上手く飛ぶように0.5ブロック浮かす
					player.teleport(playerLoc.add(0, 0.5, 0));
					player.setVelocity(vect); // 飛ばす

					// 横向きは落下死対策しない
					/*
					if (player.hasPotionEffect(PotionEffectType.JUMP))
						player.removePotionEffect(PotionEffectType.JUMP);
					player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, potionDurationInSec * 20, 0));
					*/

					Actions.message(null, player, "Fly!");
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockPlace(BlockPlaceEvent event){
		// ここには何も書かない
		if (event.isCancelled()){
			return;
		}

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

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockDamage(BlockDamageEvent event){
		if (event.getBlock().getType() == Material.BEDROCK){
			SakuraServer.bedrockConfig.put(event.getPlayer(), event.getBlock().getLocation());
			//Actions.message(null, event.getPlayer(), msgPrefix+"このブロックを右クリックすると黒曜石に変換できます！");
		}
	}



	@EventHandler(priority = EventPriority.NORMAL)
	public void onSignChange(SignChangeEvent event){
		// ここには何も書かない
		if (event.isCancelled()){
			return;
		}

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
