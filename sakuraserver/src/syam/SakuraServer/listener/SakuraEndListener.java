package syam.SakuraServer.listener;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityCreatePortalEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import syam.SakuraServer.SakuraServer;
import syam.util.Actions;

public class SakuraEndListener implements Listener {
    public final static Logger log = SakuraServer.log;
    private static final String logPrefix = SakuraServer.logPrefix;
    private static final String msgPrefix = SakuraServer.msgPrefix;
    
    private final SakuraServer plugin;
    
    // ドラゴン識別用リスト<EID, HP> 再起動時に自動で初期化
    HashMap<Integer, Integer> StageMap = new HashMap<Integer, Integer>();
    
    public SakuraEndListener(SakuraServer plugin) {
        this.plugin = plugin;
    }
    
    /* 登録するイベントはここから下に */
    
    /**
     * エンティティが死んだ
     * 
     * @param event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        // ドラゴン討伐メッセージ
        if (event.getEntity().getType() == EntityType.ENDER_DRAGON && event.getEntity().getKiller() != null) {
            
            int normal_end_DragonExp = 10000;
            int hard_end_DragonExp = 30000;
            
            if (event.getEntity().getWorld() == Bukkit.getWorld("new_the_end")) {
                event.setDroppedExp(normal_end_DragonExp); // ノーマルエンドドラゴン経験値設定
                Actions.broadcastMessage(msgPrefix + "&6" + event.getEntity().getKiller().getName() + " &bさんがドラゴンを倒しました！");
                Actions.broadcastMessage("&aエンドワールドは6時間後に自動再生成されます！");
                Actions.worldcastMessage(event.getEntity().getWorld(), "&aメインワールドに戻るには&f /spawn &aコマンドを使ってください！");
            } else if (event.getEntity().getWorld() == Bukkit.getWorld("hard_end")) {
                event.setDroppedExp(hard_end_DragonExp); // ハードエンドドラゴン経験値設定
                Actions.broadcastMessage(msgPrefix + "&6" + event.getEntity().getKiller().getName() + " &bさんがハードエンドでドラゴンを倒しました！");
                Actions.worldcastMessage(event.getEntity().getWorld(), "&aメインワールドに戻るには&f /spawn &aコマンドを使ってください！");
            }
            
            Actions.log("End.log", "Player " + event.getEntity().getKiller().getName() + " killed the EnderDragon at world " + event.getEntity().getWorld().getName());
        }
    }
    
    /**
     * エンティティがダメージを受けた
     * 
     * @param event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity ent = event.getEntity();
        
        if (ent.getWorld() == Bukkit.getWorld("hard_end")) {
            if (ent.getType() == EntityType.ENDER_DRAGON || ent.getType() == EntityType.COMPLEX_PART) {
                // 爆発でのダメージを無視する
                if (event.getCause() == DamageCause.ENTITY_EXPLOSION || event.getCause() == DamageCause.BLOCK_EXPLOSION) {
                    event.setCancelled(true);
                    event.setDamage(0);
                }
            }
            // ドラゴンそのものがダメージを受けた
            if (ent.getType() == EntityType.ENDER_DRAGON) {
                World hard_end = ent.getWorld();
                
                // 今のドラゴンのEIDと体力を取得 初期値:200
                int EID = ent.getEntityId();
                int dragonHP = ((LivingEntity) ent).getHealth();
                
                // マップに登録されていなければ登録
                if (!StageMap.containsKey(EID)) {
                    StageMap.put(EID, 1); // ステージ1へ
                }
                
                short num = 0;
                boolean change = false;
                if (dragonHP < 150 && StageMap.get(EID) == 1) {
                    // 3つクリスタルをスポーンさせる
                    // num = 3;
                    Location targetLoc = null;
                    for (short i = 0; i < num; i++) {
                        // スポーンさせる足場のある座標をランダムで取得
                        targetLoc = Actions.getRandomLocation(hard_end, 130, -130, 130, -130);
                        Actions.spawnECforHardEnd(targetLoc, 0, 10);
                    }
                    // ステージ2へ
                    StageMap.put(EID, 2);
                    change = true;
                } else if (dragonHP < 100 && StageMap.get(EID) == 2) {
                    // 4つクリスタルをスポーンさせる
                    num = 4;
                    Location targetLoc = null;
                    for (short i = 0; i < num; i++) {
                        // スポーンさせる足場のある座標をランダムで取得
                        targetLoc = Actions.getRandomLocation(hard_end, 130, -130, 130, -130);
                        Actions.spawnECforHardEnd(targetLoc, 5, 20);
                    }
                    
                    // ステージ3へ
                    StageMap.put(EID, 3);
                    change = true;
                } else if (dragonHP < 50 && StageMap.get(EID) == 3) {
                    // 5つクリスタルをスポーンさせる
                    num = 5;
                    Location targetLoc = null;
                    for (short i = 0; i < num; i++) {
                        // スポーンさせる足場のある座標をランダムで取得
                        targetLoc = Actions.getRandomLocation(hard_end, 130, -130, 130, -130);
                        Actions.spawnECforHardEnd(targetLoc, 10, 40);
                    }
                    
                    // ステージ4へ
                    StageMap.put(EID, 4);
                    change = true;
                }
                
                if (num > 0) {
                    // メッセージ表示
                    Actions.worldcastMessage(hard_end, "&c ** 新たに" + num + "つのクリスタルが出現しました！ **");
                }
                if (change) {
                    int stage = StageMap.get(EID);
                    short hp = 200;
                    switch (stage) {
                        case 2:
                            hp = 150;
                            break;
                        case 3:
                            hp = 100;
                            break;
                        case 4:
                            hp = 50;
                            Actions.broadcastMessage("&a * ハードエンドのドラゴンHPが一定値以下になりました");
                            Actions.broadcastMessage("&a * 今後新規プレイヤーはこのワールドに入場できません");
                            break;
                    }
                    Actions.worldcastMessage(hard_end, "&a ** ドラゴンの体力が &b" + hp + " &aを切りました！ **");
                }
            }
            
            // エンダークリスタルが爆発によってダメージを受けた
            if (ent.getType() == EntityType.ENDER_CRYSTAL) {
                if (event.getCause() == DamageCause.BLOCK_EXPLOSION || event.getCause() == DamageCause.ENTITY_EXPLOSION) {
                    // キャンセル
                    event.setCancelled(true);
                    event.setDamage(0);
                }
            }
        }
    }
    
    /**
     * エンティティがエンティティによってダメージを受けた
     * 
     * @param event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity ent = event.getEntity();
        Entity attacker = event.getDamager();
        
        /* ハードエンドワールド ドラゴン強化ロジック */
        if (ent.getWorld() == Bukkit.getWorld("hard_end")) {
            // エンダードラゴンがダメージを受けた
            if (ent.getType() == EntityType.ENDER_DRAGON) {
                Location dragonLocation = ent.getLocation();
                List<Player> inWorldPlayers = ent.getWorld().getPlayers();
                
                // エンダーマン2匹ランダムターゲットで召還
                for (short i = 0; i < 3; i++) {
                    LivingEntity spawnedEntity = ent.getWorld().spawnCreature(dragonLocation, EntityType.ENDERMAN);
                    spawnedEntity.setNoDamageTicks(200);
                    // Creature型にキャストしてターゲットをランダム設定
                    Creature spawnedCreature = (Creature) spawnedEntity;
                    Random rnd = new Random(); // 乱数宣言
                    if (inWorldPlayers.size() == 0) break;
                    Player targetPlayer = inWorldPlayers.get(rnd.nextInt(inWorldPlayers.size())); // ターゲットプレイヤー確定
                    spawnedCreature.setTarget(targetPlayer);
                    Actions.message(null, targetPlayer, "&c ** あなたはエンダーマンのターゲットになりました！ **");
                }
                
                // スケルトン2匹ランダムターゲットで召還
                for (short i = 0; i < 2; i++) {
                    LivingEntity spawnedEntity = ent.getWorld().spawnCreature(dragonLocation, EntityType.SKELETON);
                    spawnedEntity.setNoDamageTicks(200);
                }
                
                // クリーパー3匹召還
                for (short i = 0; i < 3; i++) {
                    LivingEntity spawnedEntity = ent.getWorld().spawnCreature(dragonLocation, EntityType.CREEPER);
                    spawnedEntity.setNoDamageTicks(200);
                }
                
                // ガスト召還
                LivingEntity spawnedEntity = ent.getWorld().spawnCreature(dragonLocation, EntityType.GHAST);
                
                // ランダムプレイヤーの真上にTNTをスポーン
                for (short i = 0; i < 3; i++) {
                    Random rnd = new Random(); // 乱数宣言
                    if (inWorldPlayers.size() < 1) return;
                    Location targetLoc = inWorldPlayers.get(rnd.nextInt(inWorldPlayers.size())).getLocation(); // ターゲットプレイヤー確定と座標取得
                    Location tntloc = new Location(targetLoc.getWorld(), targetLoc.getX(), dragonLocation.getY(), targetLoc.getZ());
                    ent.getWorld().spawn(tntloc, TNTPrimed.class);
                }
            }
            
            // エンダークリスタルが矢によってダメージを受けた
            if (ent.getType() == EntityType.ENDER_CRYSTAL && attacker.getType() == EntityType.ARROW) {
                Projectile arrow = (Arrow) attacker;
                // スケルトンの矢とプレイヤーの矢を区別
                if (arrow.getShooter() instanceof Player) {
                    // プレイヤーが打った矢ならキャンセルする
                    Player shooter = (Player) arrow.getShooter();
                    
                    Actions.message(null, shooter, "&c矢ではクリスタルを破壊できません！");
                    event.setDamage(0);
                    event.setCancelled(true);
                }
            }
        }
    }
    
    /**
     * プレイヤーが右クリックした
     * 
     * @param event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        // ハードエンド強化ロジック
        if (player.getWorld() == Bukkit.getWorld("hard_end")) {
            Block block = event.getClickedBlock();
            
            // ベッドを右クリックした場合
            if (block.getType() == Material.BED_BLOCK && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
                /*
                 * // 爆発させる
                 * block.getWorld().createExplosion(block.getLocation(), (float)
                 * 60.0, true); block.getWorld().playEffect(block.getLocation(),
                 * Effect.SMOKE, 100);
                 * 
                 * // 確実にプレイヤーを倒す if (!player.isDead()){
                 * player.damage(player.getHealth() - 1);
                 * player.setFireTicks(200); }
                 */
            }
        }
    }
    
    // 爆発時の負荷対策
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();
        if (item.getWorld() == Bukkit.getWorld("hard_end")) {
            if (item.getItemStack().getType() == Material.ENDER_STONE) {
                // 爆発による採掘を防ぐためと負荷対策のため、エンドストーンはハードエンドでドロップしない
                event.setCancelled(true);
            }
        }
        
        // ドラゴンエッグの新規回収禁止
        if (item.getWorld().getEnvironment().equals(Environment.THE_END) && item.getItemStack().getType() == Material.DRAGON_EGG) {
            event.setCancelled(true);
        }
    }
    
    /**
     * 爆発が発生しようとした
     * 
     * @param event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        // ハードエンド強化
        if (event.getEntity().getWorld() == Bukkit.getWorld("hard_end")) {
            // 爆発時の威力を高める
            // デフォルト: CREEPER:3.0 / PRIMED_TNT:4.0 / FIREBALL:1.0(Fire:true)
            switch (event.getEntityType()) {
                case CREEPER: // クリーパー
                    event.setRadius((float) 9.0);
                    event.setFire(true);
                    break;
                /*
                 * TODO:Breaking 1.4.2 case FIREBALL: // ガストの火の玉
                 * event.setRadius((float) 3.0); event.setFire(true); break;
                 */
                case PRIMED_TNT: // TNT
                    event.setRadius((float) 14.0);
                    event.setFire(true);
                    break;
            }
        }
    }
    
    // 爆発が発生した
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        // ハードエンド強化
        if (event.getLocation().getWorld() == Bukkit.getWorld("hard_end")) {
            // TNT
            if (event.getEntity() != null && event.getEntityType().equals(EntityType.PRIMED_TNT)) {
                Location baseLoc = event.getLocation().getBlock().getRelative(BlockFace.DOWN, 1).getLocation();
                
                // 基準座標を元に 3x3 を下まで走査する
                for (int x = baseLoc.getBlockX() - 1; x <= baseLoc.getBlockX() + 1; x++) {
                    for (int z = baseLoc.getBlockZ() - 1; z <= baseLoc.getBlockZ() + 1; z++) {
                        // 真下ブロック削除
                        // 3x3の中央は岩盤まで抜くため無視する
                        if (x != baseLoc.getBlockX() || z != baseLoc.getBlockZ()) {
                            Actions.removeDownObsidian(baseLoc.getWorld().getBlockAt(x, baseLoc.getBlockY(), z));
                            
                            // 周囲26ブロックは確実に空気に変える
                            for (int y = baseLoc.getBlockY() - 1; y <= baseLoc.getBlockY() + 1; y++) {
                                Block block = baseLoc.getWorld().getBlockAt(x, y, z);
                                if (block.getType() != Material.AIR && block.getType() != Material.BEDROCK) {
                                    block.setType(Material.AIR);
                                }
                            }
                        }
                    }
                }
                
                // 真下は奈落まで空気に変える
                Block relative = baseLoc.getBlock().getRelative(BlockFace.DOWN, 1);
                for (int y = baseLoc.getBlockY(); y >= 0; y--) {
                    Block block = baseLoc.getWorld().getBlockAt(baseLoc.getBlockX(), y, baseLoc.getBlockZ());
                    if (block.getType() != Material.AIR && block.getType() != Material.BEDROCK) {
                        block.setType(Material.AIR);
                    }
                }
            }
        }
    }
    
    /**
     * 矢が何かに当たった
     * 
     * @param event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        // ハードエンド強化
        if (event.getEntity().getWorld() == Bukkit.getWorld("hard_end")) {
            if (event.getEntityType() == EntityType.ARROW && (((Arrow) event.getEntity()).getShooter().getType() == EntityType.SKELETON)) {
                // 規模1.0の炎有りの爆発をスケルトンの弓に与える
                event.getEntity().getWorld().createExplosion(event.getEntity().getLocation(), (float) 2.0, true);
                event.getEntity().remove();
            }
        }
    }
    
    /**
     * プレイヤーが死んだ
     * 
     * @param event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity().getWorld() == Bukkit.getWorld("hard_end")) {
            Player player = event.getEntity();
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        // ハードエンドに移動したら10秒間無敵
        if (player.getWorld() == Bukkit.getWorld("hard_end")) {
            player.setNoDamageTicks(200);
        }
    }
    
    /**
     * プレイヤーがテレポートしようとした
     * 
     * @param event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (event.getTo().getWorld() == Bukkit.getWorld("new_the_end") || event.getTo().getWorld() == Bukkit.getWorld("hard_end")) {
            if (!player.hasPermission("sakuraserver.citizen")) {
                Actions.message(null, player, msgPrefix + "エンドに行くためには住民以上の権限が必要です");
                event.setCancelled(true);
                return;
            }
            
        }
    }
    
    /**
     * エンティティによってポータルが作られた
     * 
     * @param event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityCreatePortal(EntityCreatePortalEvent event) {
        if (!event.getEntityType().equals(EntityType.ENDER_DRAGON)) { return; }
        event.setCancelled(true);
    }
    
    /**
     * チャンクがアンロードされた
     * 
     * @param event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent event) {
        /* エンダーワールドでドラゴンが居るチャンクはアンロードしない */
        
        // TheEndディメンション以外は無視
        if (event.getWorld().getEnvironment() != Environment.THE_END) { return; }
        // 設定で無効になっていれば無視
        if (!SakuraServer.configBooleanMap.get("CancelUnloadDragonChunk")) { return; }
        
        Chunk chunk = event.getChunk();
        for (Entity entity : chunk.getEntities()) {
            // アンロードするチャンクにドラゴンが居る場合はキャンセル
            if (entity.getType() == EntityType.ENDER_DRAGON) {
                event.setCancelled(true);
            }
        }
    }
}