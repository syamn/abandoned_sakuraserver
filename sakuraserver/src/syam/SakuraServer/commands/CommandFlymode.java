/**
 * sakuraserver - Package: syam.SakuraServer.commands
 * Created: 2012/10/28 2:45:28
 */
package syam.SakuraServer.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import syam.SakuraServer.SakuraPlayer;
import syam.SakuraServer.SakuraServer;
import syam.util.Actions;

/**
 * CommandFlymode (CommandFlymode.java)
 * @author syam(syamn)
 */
public class CommandFlymode extends BaseCommand{
	public CommandFlymode(){
		bePlayer = false;
		name = "flymode";
		argLength = 0;
		usage = "flymode!";
	}

	@Override
	public void execute() {
		if (args.size() == 0){
			Actions.flyModeHelp(sender);
			return;
		}

		// 価格などの情報表示
		if (args.get(0).equalsIgnoreCase("info")){
			if (player != null) {
				if(!SakuraServer.playerData.containsKey(player)){
					SakuraPlayer sakuraPlayer = new SakuraPlayer(player.getName());
					SakuraServer.playerData.put(player, sakuraPlayer);
				}
				SakuraServer.playerData.get(player).setCheckedFlyCommand(true);
			}
			Actions.flyInfo(sender);
			return;
		}

		// 飛行権付与
		if (args.get(0).equalsIgnoreCase("fly")){
			if (player == null) {
				sender.sendMessage(ChatColor.RED + "コンソールからは使えません！");
				return;
			}
			if (!player.hasPermission("sakuraserver.citizen")){
				Actions.message(sender, player, "&cこのコマンドを実行する権限がありません");
				return;
			}
			if (SakuraServer.flyingPlayerList.contains(player.getName())){
				Actions.message(sender, player, "&cあなたは既に飛行可能状態です！");
				return;
			}
			if (SakuraServer.flyingPlayerList.size() >= SakuraServer.configIntegerMap.get("FlyModePlayersAtOneTime")){
				Actions.message(sender, player, "&c同時に飛行を許可しているユーザーの最大数に達しています！&f("
						+SakuraServer.flyingPlayerList.size()+"/"+SakuraServer.configIntegerMap.get("FlyModePlayersAtOneTime")+")");
				return;
			}
			if (player.getWorld() != Bukkit.getWorld("new") && player.getWorld() != Bukkit.getWorld("tropic")){
				Actions.message(sender, player, "&cこのワールドでは飛行が許可されていません！");
				return;
			}
			if(player.getLocation().getY() > 257 || player.getLocation().getY() < 0){
				Actions.message(sender, player, "&cあなたの位置からこのコマンドは使えません！");
				return;
			}

			// メッセージ確認チェック 1回目は情報だけ返す
			if(SakuraServer.playerData.containsKey(player)){
				if (!SakuraServer.playerData.get(player).getCheckedFlyCommand()){
					Actions.flyInfo(sender);
					SakuraServer.playerData.get(player).setCheckedFlyCommand(true);
					return;
				}else{
					SakuraServer.playerData.get(player).setCheckedFlyCommand(false);
				}
			}else{
				SakuraPlayer sakuraPlayer = new SakuraPlayer(player.getName());
				SakuraServer.playerData.put(player, sakuraPlayer);
				Actions.flyInfo(sender);
				SakuraServer.playerData.get(player).setCheckedFlyCommand(true);
				return;
			}

			// 支払い
			int cost = SakuraServer.configIntegerMap.get("FlyModeCost");
			if (!Actions.checkMoney(player.getName(), (double) cost)){
				Actions.message(sender, player, "&cお金が足りません！ "+cost+"Coinが必要です！");
				return;
			}
			if (!Actions.takeMoney(player.getName(), (double) cost)){
				Actions.message(sender, player, "&c支払いにエラーが発生しました");
				return;
			}

			int minute = SakuraServer.configIntegerMap.get("FlyModeTimeOfMinute");

			Actions.flyPlayer(player, minute);
			Actions.message(null, player,msgPrefix+"&a"+cost+"Coin&fを払い&a"+minute+"分間&f飛行できるようになりました！");

			Actions.permcastMessage("sakuraserver.helper", "&c[通知] &6"+player.getName()+" &fが飛行モードになりました");
			log.info(logPrefix+player.getName() + " is now flying mode!");
			String loc = player.getWorld().getName()+":"+player.getLocation().getX()+","+player.getLocation().getY()+","+player.getLocation().getZ();
			Actions.log("FlyMode.log", "Player " + player.getName() + " is enabled flying mode("+cost+"Coin:"+minute+"minute) at " + loc);

			return;
		}

		// 現在飛行可能なプレイヤー人数と一覧を表示
		if (args.get(0).equalsIgnoreCase("list")){
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
			return;
		}
	}

	@Override
	public boolean permission(CommandSender sender) {
		return sender.hasPermission("sakuraserver.visitor");
	}
}
