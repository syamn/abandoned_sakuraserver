package syam.SakuraServer.listener;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.PortalType;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityCreatePortalEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import syam.SakuraServer.SakuraMySqlManager;
import syam.SakuraServer.SakuraPlayer;
import syam.SakuraServer.SakuraServer;
import syam.util.Actions;

public class SakuraEntityListener implements Listener {
    public final static Logger log = SakuraServer.log;
    private static final String logPrefix = SakuraServer.logPrefix;
    private static final String msgPrefix = SakuraServer.msgPrefix;
    
    private final SakuraServer plugin;
    
    public SakuraEntityListener(SakuraServer plugin) {
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
        LivingEntity livEnt = event.getEntity();
        
        // 他人の動物を倒したら全員に警告表示
        if ((event.getEntity().getType() == EntityType.OCELOT || event.getEntity().getType() == EntityType.WOLF) && event.getEntity().getKiller() != null && ((Tameable) event.getEntity()).isTamed()) {
            
            OfflinePlayer tamer = (OfflinePlayer) ((Tameable) event.getEntity()).getOwner();
            
            if (tamer != event.getEntity().getKiller()) { // 自分のMOBでなければ
                String mob = "MOB";
                String killerName = event.getEntity().getKiller().getName();
                if (event.getEntity().getType() == EntityType.OCELOT) {
                    mob = "ネコ";
                } else if (event.getEntity().getType() == EntityType.WOLF) {
                    mob = "オオカミ";
                }
                // MOB座標代入
                String world = livEnt.getWorld().getName();
                double x = livEnt.getLocation().getX(), y = livEnt.getLocation().getY(), z = livEnt.getLocation().getZ();
                
                Actions.broadcastMessage("&c[警告] &e" + killerName + " &cが &e" + tamer.getName() + " &cの" + mob + "を倒しました！");
                String loc = world + ":" + x + "," + y + "," + z;
                // ログファイルにロギング
                Actions.log("AttackPet.log", "Player " + killerName + " Killed " + tamer.getName() + "'s " + livEnt.getType().getName() + " at " + loc);
                // SQLにロギング
                SakuraServer.dbm.changeDatabase(SakuraMySqlManager.db_minecraft);
                SakuraServer.dbm.insertLogTable(Actions.getDatetime(), killerName, 1, world, x, y, z, tamer.getName() + "@" + mob + "@0");
            }
        }
        
        // 倒された全MOBを全員に表示
        if (SakuraServer.configBooleanMap.get("BroadcastOnMobDeath") && event.getEntity().getLastDamageCause().getCause().toString() == "ENTITY_ATTACK" && event.getEntity().getKiller() != null) {
            Actions.broadcastMessage(msgPrefix + "MOB &c" + event.getEntity().getType().getName() + "&f Killed by &c" + event.getEntity().getKiller().getDisplayName());
        }
    }
    
    /**
     * エンティティがエンティティによってダメージを受けた
     * 
     * @param event
     */
    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity ent = event.getEntity();
        Entity damager = event.getDamager();
        
        /* 金のツールで匠を攻撃したら帯電 */
        if ((ent.getType() == EntityType.CREEPER && damager.getType() == EntityType.PLAYER)) {
            Player player = (Player) damager;
            ItemStack hold = player.getItemInHand();
            
            if (hold.getType() == Material.GOLD_AXE || hold.getType() == Material.GOLD_HOE || hold.getType() == Material.GOLD_PICKAXE || hold.getType() == Material.GOLD_SWORD || hold.getType() == Material.GOLD_SPADE) {
                ((Creeper) ent).setPowered(true);
            }
        }
        
        /* 他人の飼いMOBが攻撃を受けたら警告 */
        if ((ent.getType() == EntityType.OCELOT || ent.getType() == EntityType.WOLF) && damager.getType() == EntityType.PLAYER && ((Tameable) event.getEntity()).isTamed()) {
            
            String tamerString = ((OfflinePlayer) ((Tameable) ent).getOwner()).getName();
            Player tamer = Bukkit.getServer().getPlayer(tamerString);
            Player damagerPlayer = (Player) event.getDamager();
            if (damagerPlayer.getItemInHand().getType() == Material.BONE) {
                // mcMMO効果 骨を持った状態で殴るとMOB情報表示。リターン
                return;
            }
            String mob = "MOB";
            if (ent.getType() == EntityType.OCELOT) {
                mob = "ネコ";
            } else if (ent.getType() == EntityType.WOLF) {
                mob = "オオカミ";
            }
            
            String entLoc = ent.getWorld().getName() + ":" + ent.getLocation().getBlockX() + "," + ent.getLocation().getBlockY() + "," + ent.getLocation().getBlockZ();
            if (tamer != null) {
                if (tamer == damager) { return; } // 自分の動物なら何もしない
                
                if (tamer.isOnline()) {
                    Actions.message(null, tamer, "&b[情報] &e" + damagerPlayer.getName() + " &cがあなたの" + mob + "&f(" + entLoc + ")&cに攻撃しました！");
                }
            }
            Actions.message(null, damagerPlayer, "&c[注意] &fこれは他人の" + mob + "です。同意を得ていない殺傷は禁止です。");
            String world = ent.getWorld().getName();
            double x = ent.getLocation().getX(), y = ent.getLocation().getY(), z = ent.getLocation().getZ(); // MOB座標代入
            log.info(logPrefix + damagerPlayer.getName() + " Attacked " + tamerString + "'s cat!");
            Actions.permcastMessage("sakuraserver.helper", "&c[通知] &6" + damagerPlayer.getName() + " &fが &6" + tamerString + "&fの&6" + mob + "&fに攻撃しました:" + entLoc);
            String loc = world + ":" + x + "," + y + "," + z;
            // ファイルにロギング
            Actions.log("AttackPet.log", "Player " + damagerPlayer.getName() + " Attacked " + tamerString + "'s " + ent.getType().getName() + " at " + loc);
            
            // SQLにロギング
            SakuraServer.dbm.insertLogTable(Actions.getDatetime(), damagerPlayer.getName(), 0, world, x, y, z, tamerString + "@" + mob + "@" + ((LivingEntity) ent).getHealth());
        }
        
        // プレイヤーがクリーパーに攻撃した
        if ((damager instanceof Player) && (ent instanceof Creeper)) {
            Player player = (Player) damager;
            Creeper creeper = (Creeper) ent;
            
            ItemStack hand = player.getItemInHand();
            if (hand.getType() == Material.BONE && creeper.getPassenger() == null) {
                // 骨を一つ減らす
                player.setItemInHand(Actions.decrementItem(hand, 1));
                creeper.setPassenger(player);
                Actions.message(null, player, "&bクリーパーに乗りました！");
            }
        }
        
        // プレイヤーがプレイヤーに攻撃した
        if ((damager instanceof Player)) {
            Player player = (Player) damager;
            
            if (!(ent instanceof Player) && !player.hasPermission("sakuraserver.admin")) { return; }
            
            ItemStack hand = player.getItemInHand();
            // 手に持っているアイテムが骨
            if (hand.getType() == Material.BONE) {
                // 既に乗られているプレイヤーには乗らない
                if (ent.getPassenger() == null) {
                    
                    // 手に持っているアイテムを1つ減らす
                    player.setItemInHand(Actions.decrementItem(hand, 1));
                    
                    // 乗る
                    ent.setPassenger(player);
                    Actions.message(null, player, "&bプレイヤーに乗りました！");
                    
                    // 叩いたプレイヤーに乗っているプレイヤーが自分の場合は降りる
                } else if ((ent.getPassenger() instanceof Player) && (Player) ent.getPassenger() == player) {
                    ent.eject();
                    Actions.message(null, player, "&bプレイヤーから降りました！");
                    // 他人がそのプレイヤーに既にのっている
                } else {
                    Actions.message(null, player, "&c既に他人がそのプレイヤーにのっています！");
                }
                
                // ダメージ無効とイベントキャンセル
                event.setDamage(0);
                event.setCancelled(true);
            }
        }
        
        /* デバッグ用 */
        if (false && damager.getType() == EntityType.PLAYER && (OfflinePlayer) damager == Bukkit.getOfflinePlayer("syamn")) {
            if (ent.getType() == EntityType.WOLF && !((Tameable) ent).isTamed()) {
                AnimalTamer animaltamer = Bukkit.getOfflinePlayer("testPlayer");
                ((Tameable) ent).setOwner(animaltamer);
                ((Tameable) ent).setTamed(true);
            }
        }
    }
    
    /**
     * 敵モンスターに乗っているとき、乗っているモンスターのターゲットにしない
     * 
     * @param event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void targetCancelOnRideMonster(EntityTargetLivingEntityEvent event) {
        // ターゲットがプレイヤーでなければリターン
        if (!(event.getTarget() instanceof Player)) { return; }
        Player targetPlayer = (Player) event.getTarget();
        Entity ent = event.getEntity();
        
        // プレイヤーが乗ったモンスター
        if (ent.getPassenger() != null && (ent.getPassenger() instanceof Player)) {
            // 乗ったプレイヤーがターゲットプレイヤーかチェックする？
            event.setCancelled(true);
        }
    }
    
    // ゾンビスポナー無効化
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCreatureSpawn(final CreatureSpawnEvent event) {
        if (SpawnReason.SPAWNER.equals(event.getSpawnReason())) {
            Entity e = event.getEntity();
            switch (e.getType()) {
            // スポナー拒否
                case PIG:
                case COW:
                case CHICKEN:
                case ZOMBIE:
                    event.setCancelled(true);
                    return;
                default:
                    break;
            }
            if (EntityType.ZOMBIE.equals(e.getType())) {
                event.setCancelled(true);
            }
        }
    }
    
    /**
     * 金の装備を身につけている時はターゲッティングイベントをキャンセル
     * 
     * @param event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void targetCancelOnGoldEquipment(EntityTargetLivingEntityEvent event) {
        Entity ent = event.getEntity();
        LivingEntity targetEnt = event.getTarget();
        
        // 攻撃者がプレイヤー以外ならリターン
        if (targetEnt.getType() != EntityType.PLAYER) { return; }
        
        // 誰かから攻撃されたMOBならリターン
        if (ent.getLastDamageCause() != null) {
            if (ent.getLastDamageCause().getCause().equals(DamageCause.ENTITY_ATTACK)) { return; }
        }
        
        Player player = (Player) targetEnt;
        ItemStack hold = player.getItemInHand();
        ItemStack[] armors = player.getInventory().getArmorContents();
        
        // 手に持っているものが金装備以外ならリターン
        if (hold.getType() != Material.GOLD_AXE && hold.getType() != Material.GOLD_HOE && hold.getType() != Material.GOLD_PICKAXE && hold.getType() != Material.GOLD_SWORD && hold.getType() != Material.GOLD_SPADE) { return; }
        
        // アーマーが一つでも null または AIR ならリターン
        for (int i = 0; i < armors.length; ++i) {
            if (armors[i].getType() == null || armors[i].getType() == Material.AIR) { return; }
        }
        
        // アーマーが金装備ならターゲット拒否
        if (armors[0].getType() == Material.GOLD_BOOTS && armors[1].getType() == Material.GOLD_LEGGINGS && armors[2].getType() == Material.GOLD_CHESTPLATE && armors[3].getType() == Material.GOLD_HELMET) {
            event.setCancelled(true);
        }
    }
    
    /**
     * プレイヤーが死んだ
     * 
     * @param event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player deader = event.getEntity();
        
        // プレイヤーに殺された
        if (deader.getKiller() != null) {
            Player killer = deader.getKiller();
            
            // 住民以上でない場合は申告不可能
            if (!deader.hasPermission("sakuraserver.citizen")) { return; }
            // ヘルパー以上に攻撃された場合は申告不可能
            if (killer.hasPermission("sakuraserver.helper")) { return; }
            
            // ゲームワールド例外
            if (Bukkit.getWorld("flag") != null && deader.getWorld() == Bukkit.getWorld("flag")) { return; }
            if (Bukkit.getWorld("flaggame") != null && deader.getWorld() == Bukkit.getWorld("flaggame")) { return; }
            if (Bukkit.getWorld("race") != null && deader.getWorld() == Bukkit.getWorld("race")) { return; }
            
            if (SakuraServer.playerData.containsKey(deader)) {
                SakuraServer.playerData.get(deader).setLastKillerName(killer.getName());
            } else {
                SakuraPlayer sakuraPlayer = new SakuraPlayer(deader.getName());
                SakuraServer.playerData.put(deader, sakuraPlayer);
                SakuraServer.playerData.get(deader).setLastKillerName(killer.getName());
            }
            
            Actions.message(null, deader, msgPrefix + "&dプレイヤー &e" + killer.getName() + " &dに殺されました！");
            Actions.message(null, deader, "&d荒らしとして通報し投獄するには&f/jailapply&dコマンドを使います");
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityCreatePortal(EntityCreatePortalEvent event) {
        // ネザーポータルをプレイヤーが作った
        if (event.getEntityType() == EntityType.PLAYER) {
            log.info("player");
        }
        if (event.getPortalType() == PortalType.NETHER) {
            log.info("nether");
        }
        if (event.getEntity().getWorld() != Bukkit.getWorld("new")) {
            log.info("not new world");
        }
        
        if (event.getPortalType() == PortalType.NETHER && event.getEntityType() == EntityType.PLAYER) {
            Player player = (Player) event.getEntity();
            // メインワールド以外ならキャンセル
            if (player.getWorld() != Bukkit.getWorld("new")) {
                String loc = player.getWorld().getName() + ":" + player.getLocation().getBlockX() + "," + player.getLocation().getBlockY() + "," + player.getLocation().getBlockZ();
                Actions.executeCommandOnConsole("kick " + player.getName() + " ネザーポータル設置違反 at " + loc);
                event.setCancelled(true);
            }
        }
    }
}
