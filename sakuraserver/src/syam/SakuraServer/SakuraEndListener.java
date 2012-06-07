package syam.SakuraServer;

import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCreatePortalEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import com.avaje.ebeaninternal.server.cluster.mcast.Message;

public class SakuraEndListener implements Listener {
	public final static Logger log = SakuraServer.log;
	private static final String logPrefix = SakuraServer.logPrefix;
	private static final String msgPrefix = SakuraServer.msgPerfix;

	private final SakuraServer plugin;

	public SakuraEndListener(SakuraServer plugin){
		this.plugin = plugin;
	}

	/* 登録するイベントはここから下に */

	/**
	 * エンティティが死んだ
	 * @param event
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDeath(EntityDeathEvent event){
		// ドラゴン討伐メッセージ
		if (event.getEntity().getType() == EntityType.ENDER_DRAGON && event.getEntity().getKiller() != null){

			int normal_end_DragonExp = 10000;
			int hard_end_DragonExp = 30000;

			if(event.getEntity().getWorld() == Bukkit.getWorld("new_the_end")){
				event.setDroppedExp(normal_end_DragonExp); // ノーマルエンドドラゴン経験値設定
				Actions.broadcastMessage(msgPrefix + event.getEntity().getKiller().getName() + " さんがドラゴンを倒しました！");
				Actions.broadcastMessage("&aエンドワールドは全員が退出してから3時間後に自動再生成されます！");
			}else if(event.getEntity().getWorld() == Bukkit.getWorld("hard_end")){
				event.setDroppedExp(hard_end_DragonExp); // ハードエンドドラゴン経験値設定
				Actions.broadcastMessage(msgPrefix + event.getEntity().getKiller().getName() + " さんがハードエンドでドラゴンを倒しました！");
				Actions.broadcastMessage("&aメインワールドに戻るには&f /spawn &aコマンドを使ってください！");
			}

			Actions.permcastMessage("sakuraserver.helper", "&c[Log.Helper] Catch killed the EnderDragon event.");
			Actions.permcastMessage("sakuraserver.helper", "&c[Log.Helper] event.getDroppedExp(); :"+event.getDroppedExp());

			Actions.log("End.log", "Player " + event.getEntity().getKiller().getName() + " killed the EnderDragon at world " + event.getEntity().getWorld().getName());
		}
	}

	/**
	 * エンティティがダメージを受けた
	 * @param event
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDamage(EntityDamageEvent event){
		// ここには何も書かない
		if (event.isCancelled()){
			return;
		}

		Entity ent = event.getEntity();

		if (ent.getWorld() == Bukkit.getWorld("hard_end")){
			if(ent.getType() == EntityType.ENDER_DRAGON || ent.getType() == EntityType.COMPLEX_PART){
				if(event.getCause() == DamageCause.BLOCK_EXPLOSION){
					if(event.getDamage() > 10){
						log.info("Prevent damage:"+event.getDamage()+" To entity:"+ent.getType().getName());
						event.setDamage(0);
						event.setCancelled(true);
						return;
					}

					LivingEntity dragon = null;
					if (ent.getType() == EntityType.ENDER_DRAGON) { dragon = (LivingEntity) ent; }
					else{
						for (Entity e : ent.getWorld().getEntities()){
							if(e.getType() == EntityType.ENDER_DRAGON){
								dragon = (LivingEntity) e;
							}
						}
					}

					for(Player p: dragon.getWorld().getPlayers())
					{
						PotionEffect potion = new PotionEffect(PotionEffectType.SLOW, 1200, 5);
						if (!(p.getActivePotionEffects().contains(potion))){
							Actions.message(null, p, "&7 ** あなたは呪われてしまった...");
							p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 1200, 5));
							p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 0));
						}
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDamageByBlock(EntityDamageByBlockEvent event){
		// ここには何も書かない
		if (event.isCancelled()){
			return;
		}
		// do stuff
	}

	/**
	 * エンティティがエンティティによってダメージを受けた
	 * @param event
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event){
		// ここには何も書かない
		if (event.isCancelled()){
			return;
		}

		Entity ent = event.getEntity();

		/* ハードエンドワールド ドラゴン強化ロジック */
		if(ent.getWorld() == Bukkit.getWorld("hard_end")){
			if(ent.getType() == EntityType.ENDER_DRAGON){

				Location dragonLocation = ent.getLocation();
				for(short i = 0; i < 3; i++){
					// エンダーマン召還
					LivingEntity spawnedEntity = ent.getWorld().spawnCreature(dragonLocation, EntityType.ENDERMAN);
					spawnedEntity.setNoDamageTicks(200);
					// Creature型にキャストしてターゲットをランダム設定
					Creature spawnedCreature = (Creature) spawnedEntity;
					Random rnd = new Random(); // 乱数宣言
					// ↓0人対策する
					List<Player> inWorldPlayers = spawnedCreature.getWorld().getPlayers(); // ワールド内プレイヤー取得
					Player targetPlayer = inWorldPlayers.get(rnd.nextInt(inWorldPlayers.size())); // ターゲットプレイヤー確定
					spawnedCreature.setTarget(targetPlayer);
					Actions.message(null, targetPlayer, "&c ** あなたはエンダーマンのターゲットになりました！ **");
				}
				for(short i = 0; i < 2; i++){
					// スケルトン召還
					LivingEntity spawnedEntity = ent.getWorld().spawnCreature(dragonLocation, EntityType.SKELETON);
					spawnedEntity.setNoDamageTicks(200);
					// Creature型にキャストしてターゲットをランダム設定
					Creature spawnedCreature = (Creature) spawnedEntity;
					Random rnd = new Random(); // 乱数宣言
					List<Player> inWorldPlayers = spawnedCreature.getWorld().getPlayers(); // ワールド内プレイヤー取得
					Player targetPlayer = inWorldPlayers.get(rnd.nextInt(inWorldPlayers.size())); // ターゲットプレイヤー確定
					spawnedCreature.setTarget(targetPlayer);
					Actions.message(null, targetPlayer, "&c ** あなたはスケルトンのターゲットになりました！ **");
				}
				for(short i = 0; i < 2; i++){
					// クリーパー召還
					LivingEntity spawnedEntity = ent.getWorld().spawnCreature(dragonLocation, EntityType.CREEPER);
					spawnedEntity.setNoDamageTicks(200);
				}

				// ガスト召還
				LivingEntity spawnedEntity = ent.getWorld().spawnCreature(dragonLocation, EntityType.GHAST);
			}
		}
	}

	/**
	 * 爆発が発生した
	 * @param event
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onExplosionPrime(ExplosionPrimeEvent event){
		// ここには何も書かない
		if (event.isCancelled()){
			return;
		}

		// ハードエンド強化
		if (event.getEntity().getWorld() == Bukkit.getWorld("hard_end")){
			if (event.getEntityType() == EntityType.CREEPER){
				event.setRadius((float) 9.0); //デフォルト CREEPER:3.0 PRIMED_TNT:4.0 FIREBALL:1.0(Fire:true)
				event.setFire(true);
			}
			if (event.getEntityType() == EntityType.FIREBALL){
				event.setRadius((float) 3.0); //デフォルト 1.0
				event.setFire(true);
			}
		}
	}

	/**
	 * 矢が何かに当たった
	 * @param event
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onProjectileHit(ProjectileHitEvent event){
		// ハードエンド強化
		if (event.getEntity().getWorld() == Bukkit.getWorld("hard_end")){
			if (event.getEntityType() == EntityType.ARROW &&
					(((Arrow)event.getEntity()).getShooter().getType() == EntityType.SKELETON)){
				// 規模1.0の炎有りの爆発をスケルトンの弓に与える
				event.getEntity().getWorld().createExplosion(event.getEntity().getLocation(), (float) 2.0, true);
				event.getEntity().remove();
			}
		}
	}

	/**
	 * プレイヤーが死んだ
	 * @param event
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerDeath(PlayerDeathEvent event){
		if (event.getEntity().getWorld() == Bukkit.getWorld("hard_end")){
			Player player = event.getEntity();
		}
	}

	/**
	 * プレイヤーがテレポートしようとした
	 * @param event
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerTeleport(PlayerTeleportEvent event){
		// ここには何も書かない
		if (event.isCancelled()){
			return;
		}

		Player player = event.getPlayer();
		if(event.getTo().getWorld() == Bukkit.getWorld("new_the_end") || event.getTo().getWorld() == Bukkit.getWorld("hard_end")){
			if(!player.hasPermission("sakuraserver.citizen")){
				Actions.message(null, player, msgPrefix+"エンドに行くためには住民以上の権限が必要です");
				event.setCancelled(true);
				return;
			}
		}
	}

	/**
	 * エンティティによってポータルが作られた
	 * @param event
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityCreatePortal(EntityCreatePortalEvent event){
		if(!event.getEntityType().equals(EntityType.ENDER_DRAGON)){
			return;
		}
		event.setCancelled(true);
	}

	/**
	 * チャンクがアンロードされた
	 * @param event
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onChunkUnload(ChunkUnloadEvent event){
		// ここには何も書かない
		if (event.isCancelled()){
			return;
		}
		/* エンダーワールドでドラゴンが居るチャンクはアンロードしない */

		// TheEndディメンション以外は無視
		if (event.getWorld().getEnvironment() != Environment.THE_END){
			return;
		}
		// 設定で無効になっていれば無視
		if (!SakuraServer.configBooleanMap.get("CancelUnloadDragonChunk")){
			return;
		}

		Chunk chunk = event.getChunk();
		for (Entity entity : chunk.getEntities()){
			// アンロードするチャンクにドラゴンが居る場合はキャンセル
			if(entity.getType() == EntityType.ENDER_DRAGON){
				event.setCancelled(true);
			}
		}
	}
}