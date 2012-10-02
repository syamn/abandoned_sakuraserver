package syam.SakuraServer.commands;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import syam.SakuraServer.SakuraPlayer;
import syam.SakuraServer.SakuraServer;
import syam.util.Actions;

public class MiscCommand implements CommandExecutor {
	public final static Logger log = SakuraServer.log;
	private static final String logPrefix = SakuraServer.logPrefix;
	private static final String msgPrefix = SakuraServer.msgPrefix;

	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
		if (command.getName().equalsIgnoreCase("colors") || command.getName().equalsIgnoreCase("col")){
			Actions.message(sender, null, "&cカラーコードリスト: ");
			sender.sendMessage(" \u00A70 &0 \u00A71 &1 \u00A72 &2 \u00A73 &3");
			sender.sendMessage(" \u00A74 &4 \u00A75 &5 \u00A76 &6 \u00A77 &7");
			sender.sendMessage(" \u00A78 &8 \u00A79 &9 \u00A7a &a \u00A7b &b");
			sender.sendMessage(" \u00A7c &c \u00A7d &d \u00A7e &e \u00A7f &f");
			return true;
		}
		if (command.getName().equalsIgnoreCase("yes")){
			Actions.message(sender, null, "&a賛成投票しました！");
			return true;
		}
		if (command.getName().equalsIgnoreCase("no")){
			Actions.message(sender, null, "&c反対&a投票しました！");
			return true;
		}
		if (command.getName().equalsIgnoreCase("mfmf") || command.getName().equalsIgnoreCase("もふもふ")){
			// コンソールからは拒否する
			if (!(sender instanceof Player)){
				Actions.message(sender, null, "&cこのコマンドはコンソールから実行出来ません！");
				return true;
			}
			Player player = (Player) sender;

			if (args.length <= 0){
				Actions.message(sender, null, " &aもふもふ...？&7 /もふもふ <プレイヤー名>");
				Actions.message(sender, null, " &aあなたは今までに"+ SakuraServer.playerData.get(player).getmfmfCount() + "回もふもふされました！");
			}else{
				Player target = Bukkit.getPlayer(args[0]);
				// ターゲットチェック
				if (target != null && target.isOnline()){
					if (target == player){
						Actions.message(sender, null, "&c自分をもふもふできません！");
						return true;
					}

					// Actions.checkMoneyは必要無い？
					if (Actions.takeMoney(player.getName(), 150)){
						SakuraPlayer sp = SakuraServer.playerData.get(target);

						if (sp == null) {
							Actions.message(sender, null, "&cエラーが発生しました！相手に一度ログアウトしてもらってください！");
							return true;
						}

						Actions.addMoney(target.getName(), 100); // チェックはしない
						int added = sp.addMofCount(); // もふもふカウント++

						Actions.message(null, target, " &6'"+player.getName()+"'&aにもふもふされました！(+100Coin)("+added+"回目)");
						Actions.message(null, player, " &6'"+target.getName()+"'&aをもふもふしました！&c(-150Coin)");
					}else{
						Actions.message(null, target, " &6'"+player.getName()+"'&aにもふもふされました！");
						Actions.message(null, player, " &6'"+target.getName()+"'&aをもふもふしました！");
					}
				}
				// 相手が見つからない
				else{
					Actions.message(sender, null, " &6もふ...？ 相手が見つからないです...。");
				}
			}
			return true;
		}
		if (command.getName().equalsIgnoreCase("flagworld") || command.getName().equalsIgnoreCase("fw")){
			// コンソールからは拒否する
			if (!(sender instanceof Player)){
				Actions.message(sender, null, "&cこのコマンドはコンソールから実行出来ません！");
				return true;
			}
			Player player = (Player) sender;

			// 無効にする
			if (true){
				Actions.message(sender, null, "&cこのコマンドは現在無効です");
				return true;
			}

			if(!player.hasPermission("sakuraserver.citizen")){
				Actions.message(sender, null, "&c住民以上の権限が必要です！");
				return true;
			}

			World fw = Bukkit.getWorld("flaggame");
			if (fw == null){
				Actions.message(sender, null, "&cフラッグワールドが見つかりません..");
				return true;
			}

			player.teleport(fw.getSpawnLocation(), TeleportCause.COMMAND);
			player.setGameMode(GameMode.CREATIVE);
			Actions.message(sender, null, "&aテレポートしました！");

			return true;
		}
		if (command.getName().equalsIgnoreCase("syam") || command.getName().equalsIgnoreCase("syamn") ||
				command.getName().equalsIgnoreCase("しゃむ") ){
			OfflinePlayer syamn = Bukkit.getServer().getOfflinePlayer("syamn");
			String status = null;
			if (syamn.isOnline()){
				status = "&aオンライン";
			}else{
				status = "&cオフライン";
			}
			Actions.message(sender, null, "&c===================================");
			Actions.message(sender, null, "&6        :: &a管理人連絡先 &6::");
			Actions.message(sender, null, "&b Player :  &csyamn &f:現在"+status+"&fです");
			Actions.message(sender, null, "&b Twitter: &f@mc_sakuraserver &7-- サーバのアカウント");
			Actions.message(sender, null, "&b        : &f@starlightp &7-- 中の人のアカウント");
			Actions.message(sender, null, "&b Mail   : &fadmin@sakura-server.net");
			Actions.message(sender, null, "&c===================================");
			return true;
		}
		return false;
	}
}
