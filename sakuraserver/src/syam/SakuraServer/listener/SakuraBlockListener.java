package syam.SakuraServer.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.minecraft.server.v1_4_5.Packet62NamedSoundEffect;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.TravelAgent;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_4_5.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.PortalCreateEvent.CreateReason;
import org.bukkit.inventory.ItemStack;

import syam.SakuraServer.SakuraServer;
import syam.util.Actions;
import syam.util.Util;


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

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockBreak(final BlockBreakEvent event){
		final Block block = event.getBlock();
		final Player player = event.getPlayer();

		// ネザーなら氷を壊して水を出現させる
		if (block.getTypeId() == 79 && Environment.NETHER.equals(block.getLocation().getWorld().getEnvironment())){
			if (player.hasPermission("sakuraserver.citizen") && GameMode.SURVIVAL.equals(player.getGameMode())){
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
					public void run(){
						block.setTypeId(8, true); // set water
					}
				}, 0L);
			}
		}

		/*
		if (block.getType() == Material.DIAMOND_ORE){
			// 音を鳴らす
			List<Note> notes = new ArrayList<Note>();
			notes.add(new Note(16));
			Actions.playNote(player, notes, 0L);
		}
		*/
	}

	// カボチャを壊した
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPumpkinBreak(BlockBreakEvent event){
		final Player player = event.getPlayer();
		final Block block = event.getBlock();

		// アイテムID 73:Redstone ore || 74:Glowing redstone ore 以外は返す
		if (!block.getWorld().getName().toLowerCase().startsWith("resource") || (block.getTypeId() != 86 && block.getTypeId() != 91)){
			return;
		}

		// 棒以外無視
		ItemStack is = player.getItemInHand();
		if (is == null || is.getType() != Material.STICK){
			return;
		}

		// 爆発させる
		block.breakNaturally();
		block.getWorld().createExplosion(block.getLocation(), (float) 0.0, false);
		checkNextTicks(block, true);
	}
	final BlockFace[] oumpkinSearchDirs = new BlockFace[]{
			BlockFace.NORTH,
			BlockFace.EAST,
			BlockFace.SOUTH,
			BlockFace.WEST,
			BlockFace.UP,
			BlockFace.DOWN
	};
	private void checkNextTicks(final Block block, final boolean first){
		if (block == null || (!first && (block.getTypeId() != 86 && block.getTypeId() == 91))){
			return;
		}
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run(){
				List<Block> nexts = new ArrayList<Block>();

				Block check;
				for (final BlockFace face : oumpkinSearchDirs){
					check = block.getRelative(face);
					if (check.getTypeId() == 86 || check.getTypeId() == 91){
						if (Math.random() > 0.5) check.breakNaturally();
						else check.setTypeId(0);

						block.getWorld().createExplosion(block.getLocation(), 0.1F, false);
						nexts.add(check);
					}
				}
				for (final Block next : nexts){
					checkNextTicks(next, false);
				}
			}
		}, 4L);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event){
		Player player = event.getPlayer();
		Block block = event.getBlock();

		// ネザーポータルを作った (もしエンドポータルや今後追加されるポータルが判定されるようなら、event.getBlockAgainst().getType() == Material.OBSIDIAN を条件に追加？
		if(block.getType() == Material.PORTAL){
			// new 以外で作った場合、管理権限を持っていなければ削除
			if((!block.getWorld().getName().startsWith("new")) && !(player.hasPermission("sakuraserver.admin"))){
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

	//ポータル移動
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerPortal(final PlayerPortalEvent event){
		final Player player = event.getPlayer();
		final Location from = event.getFrom();
		final Environment fromEnv = from.getWorld().getEnvironment();
		if (Environment.THE_END.equals(fromEnv)){
			return;
		}

		int x = from.getBlockX();
		int z = from.getBlockZ();
		World world = null;
		if (Environment.NORMAL.equals(fromEnv)){
			world = Bukkit.getWorld("new_nether"); // goto nether
		}else if (Environment.NETHER.equals(fromEnv)){
			world = Bukkit.getWorld("new"); // goto main
		}
		if (world == null) return;

		int y = getFirtstPortalY(world, x, z, player);
		if (y < 0){
			Actions.message(null, player, "&c"+ world.getName() + "のxz座標("+x+","+z+")にポータルが見つかりません！");
			event.setCancelled(true);
			return;
		}

		final Location ploc = player.getLocation().clone();
		ploc.setWorld(world);
		ploc.setX(x);
		ploc.setY(y);
		ploc.setZ(z);

		event.useTravelAgent(false);
		event.setTo(ploc);
	}
	private int getFirtstPortalY(final World w, final int x, final int z, final Player player){
		if (!w.isChunkLoaded(x, z) && w.loadChunk(x, z, false)){
			return -1;
		}
		for (int y = 2; y < 256; y++){ // don't check y=0,1
			if (w.getBlockAt(x, y, z).getTypeId() == 90){
				return y + 1;
			}
		}
		return -1;
	}

	// ポータル生成キャンセル
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPortalCreate(final PortalCreateEvent event){
		if (CreateReason.OBC_DESTINATION.equals(event.getReason())){
			event.setCancelled(true);
			log.info("Portal auto-create event cancelled on World " + event.getWorld().getName());
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
			else if (event.getLine(0).contains("[Sound]")){
				if (player.hasPermission("sakuraserver.helper")){
					event.setLine(0, "§1[Sound]");
					Actions.message(null, player, "&1SoundEffect sign created!");
				}else{
					event.setLine(0, "§4Error!");
					Actions.message(null, player, "&1Permission Denied! :(");
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockRedstone(final BlockRedstoneEvent event){
		final Block block = event.getBlock();
		final BlockState state = event.getBlock().getState();

		if (state instanceof Sign && event.getNewCurrent() > 0){
			final Sign sign = (Sign)state;

			if (sign.getLine(0).equals("§1[Sound]")){
				final Location bloc = block.getLocation();

				// get sound
				final String sound = sign.getLine(1) + sign.getLine(2);

				// get volume, radius
				String[] line4 = sign.getLine(3).split(":");
				if (line4.length != 2 || !Util.isFloat(line4[0]) || !Util.isDouble(line4[1])) return;
				float vol = Float.parseFloat(line4[0]);
				final double radius = Double.parseDouble(line4[1]);

				if (sound.length() <= 0 || vol <= 0F || radius <= 0D) return;

				for (Player player : block.getWorld().getPlayers()){
					Location ploc = player.getLocation();
					if (ploc.distance(bloc) > radius){
						continue;
					}

					((CraftPlayer)player).getHandle().netServerHandler.sendPacket(
							new Packet62NamedSoundEffect(sound, ploc.getX(), ploc.getY(), ploc.getZ(), vol, 1.0F)
							);

				}
			}
		}
	}


}
