package syam.SakuraServer.commands;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import syam.SakuraServer.Actions;
import syam.SakuraServer.SakuraPlayer;
import syam.SakuraServer.SakuraServer;

public class FlymodeCommand implements CommandExecutor {
	public final static Logger log = SakuraServer.log;
	private static final String logPrefix = SakuraServer.logPrefix;
	private static final String msgPrefix = SakuraServer.msgPerfix;

	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
		if (command.getName().equalsIgnoreCase("flymode")){
			Player player = null;

			// 価格などの情報表示
			if (args.length >= 1 && args[0].equalsIgnoreCase("info")){
				if (sender instanceof Player) {
					player = (Player) sender;
					if(!SakuraServer.playerData.containsKey(player)){
						SakuraPlayer sakuraPlayer = new SakuraPlayer(player.getName());
						SakuraServer.playerData.put(player, sakuraPlayer);
					}
					SakuraServer.playerData.get(player).setCheckedFlyCommand(true);
				}
				Actions.flyInfo(sender);
				return true;
			}

			// 飛行権付与
			if (args.length == 1 && args[0].equalsIgnoreCase("fly")){
				if (!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.RED + "コンソールからは使えません！");
					return true;
				}
				player = (Player) sender;
				if (!player.hasPermission("sakuraserver.citizen")){
					Actions.message(sender, player, "&cこのコマンドを実行する権限がありません");
					return true;
				}
				if (SakuraServer.flyingPlayerList.contains(player.getName())){
					Actions.message(sender, player, "&cあなたは既に飛行可能状態です！");
					return true;
				}
				if (SakuraServer.flyingPlayerList.size() >= SakuraServer.configIntegerMap.get("FlyModePlayersAtOneTime")){
					Actions.message(sender, player, "&c同時に飛行を許可しているユーザーの最大数に達しています！&f("
							+SakuraServer.flyingPlayerList.size()+"/"+SakuraServer.configIntegerMap.get("FlyModePlayersAtOneTime")+")");
					return true;
				}
				if (player.getWorld() != Bukkit.getWorld("new") && player.getWorld() != Bukkit.getWorld("tropic")){
					Actions.message(sender, player, "&cこのワールドでは飛行が許可されていません！");
					return true;
				}
				if(player.getLocation().getY() > 257 || player.getLocation().getY() < 0){
					Actions.message(sender, player, "&cあなたの位置からこのコマンドは使えません！");
					return true;
				}

				// メッセージ確認チェック 1回目は情報だけ返す
				if(SakuraServer.playerData.containsKey(player)){
					if (!SakuraServer.playerData.get(player).getCheckedFlyCommand()){
						Actions.flyInfo(sender);
						SakuraServer.playerData.get(player).setCheckedFlyCommand(true);
						return true;
					}else{
						SakuraServer.playerData.get(player).setCheckedFlyCommand(false);
					}
				}else{
					SakuraPlayer sakuraPlayer = new SakuraPlayer(player.getName());
					SakuraServer.playerData.put(player, sakuraPlayer);
					Actions.flyInfo(sender);
					SakuraServer.playerData.get(player).setCheckedFlyCommand(true);
					return true;
				}

				// 支払い
				int cost = SakuraServer.configIntegerMap.get("FlyModeCost");
				if (!Actions.checkMoney(player.getName(), (double) cost)){
					Actions.message(sender, player, "&cお金が足りません！ "+cost+"Coinが必要です！");
					return true;
				}
				if (!Actions.takeMoney(player.getName(), (double) cost)){
					Actions.message(sender, player, "&c支払いにエラーが発生しました");
					return true;
				}

				int minute = SakuraServer.configIntegerMap.get("FlyModeTimeOfMinute");

				Actions.flyPlayer(player, minute);
				Actions.message(null, player,msgPrefix+"&a"+cost+"Coin&fを払い&a"+minute+"分間&f飛行できるようになりました！");

				Actions.permcastMessage("sakuraserver.helper", "&c[通知] &6"+player.getName()+" &fが飛行モードになりました");
				log.info(logPrefix+player.getName() + " is now flying mode!");
				String loc = player.getWorld().getName()+":"+player.getLocation().getX()+","+player.getLocation().getY()+","+player.getLocation().getZ();
				Actions.log("FlyMode.log", "Player " + player.getName() + " is enabled flying mode("+cost+"Coin:"+minute+"minute) at " + loc);

				return true;
			}

			// 現在飛行可能なプレイヤー人数と一覧を表示
			if (args.length >= 1 && args[0].equalsIgnoreCase("list")){
				if ((sender instanceof Player) && !sender.hasPermission("sakuraserver.visitor")){
					Actions.message(sender, null, "&cこのコマンドを実行する権限がありません");
					return true;
				}
				int size = SakuraServer.flyingPlayerList.size();
				if (size==0){
					Actions.message(sender, null, "&c現在飛行モードが有効なプレイヤーはいません");
				}else{
					StringBuilder sb = new StringBuilder("");
					for (int i = 0; i < SakuraServer.flyingPlayerList.size(); i++) {
						String playerName = SakuraServer.flyingPlayerList.get(i);
						if ((Bukkit.getServer().getPlayer(playerName) != null) && (Bukkit.getServer().getPlayer(playerName).isOnline())){
							sb.append(ChatColor.AQUA + playerName + ChatColor.WHITE + ", ");
						}else{
							sb.append(ChatColor.GRAY + playerName + ChatColor.WHITE + ", ");
						}
					}
					sb.delete(sb.length() - 2, sb.length());
					Actions.message(sender, null, "&c飛行モードが有効なプレイヤー数 : "+size+" &f(Max: "+SakuraServer.configIntegerMap.get("FlyModePlayersAtOneTime")+")");
					Actions.message(sender, null, sb.toString());
				}
				return true;
			}
			Actions.flyModeHelp(sender);
			return true;
		}
		return false;
	}
}
