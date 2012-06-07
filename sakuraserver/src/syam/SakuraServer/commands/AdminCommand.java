package syam.SakuraServer.commands;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import syam.SakuraServer.Actions;
import syam.SakuraServer.SakuraServer;

public class AdminCommand implements CommandExecutor {
	public final static Logger log = SakuraServer.log;
	private static final String logPrefix = SakuraServer.logPrefix;
	private static final String msgPrefix = SakuraServer.msgPerfix;

	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
		if (command.getName().equalsIgnoreCase("admin")){
			// 設定ファイルを再読み込み
			if (args.length >= 1 && args[0].equalsIgnoreCase("reloadconfig")){
				if ((sender instanceof Player) && !(sender.hasPermission("sakuraserver.admin"))) {
					Actions.message(sender, null, "&cこのコマンドを実行する権限がありません");
					return true;
				}
				SakuraServer.getInstance().loadConfig();
				Actions.message(sender, null, "&a設定を再読み込みしました");

				return true;
			}

			// コンソールでコマンド実行
			if (args.length >= 2 && args[0].equalsIgnoreCase("ccmd")){
				if (!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.RED + "コンソールからは使えません！");
					return true;
				}
				if (sender.hasPermission("sakuraserver.admin")){
					// 本体
					String consoleCmd = args[1];
					int i = 2;
					while(i < args.length){
						consoleCmd = consoleCmd + " " + args[i];
						i++;
					}
					// 実行
					Actions.executeCommandOnConsole(consoleCmd);
					Actions.message(sender, null, "&aコマンド &f"+consoleCmd+" &aをコンソールから実行しました");

				}else{
					Actions.message(sender, null, "&cこのコマンドを実行する権限がありません");
				}
				return true;
			}

			// 飛行権限付与
			if (args.length == 3 && args[0].equalsIgnoreCase("fly")){
				if (!sender.hasPermission("sakuraserver.admin")){
					Actions.message(sender, null, "&cこのコマンドを実行する権限がありません");
					return true;
				}

				OfflinePlayer targetOfflinePlayer = Bukkit.getOfflinePlayer(args[1]);
				int minute;

				if(SakuraServer.flyingPlayerList.contains(targetOfflinePlayer.getName())){
					Actions.message(sender, null, "&cプレイヤー &f"+targetOfflinePlayer.getName()+" &cは既に飛行権限を持っています！");
					Actions.message(sender, null, "&f飛行権限を持っているリストの表示は /sakura flylist です");
					return true;
				}
				Player targetPlayer = targetOfflinePlayer.getPlayer();
				if ((targetPlayer == null) || !(targetPlayer.isOnline())){
					Actions.message(sender, null, "&cプレイヤー &f"+targetOfflinePlayer.getName()+" &cはオフラインです！");
					return true;
				}
				if(targetPlayer.getLocation().getY() > 257 || targetPlayer.getLocation().getY() < 0){
					Actions.message(sender, null, "&cプレイヤー &f"+targetOfflinePlayer.getName()+" &cの位置が許可されない地点です");
					return true;
				}

				try{
					minute = Integer.parseInt(args[2]);
				}catch (NumberFormatException e){
					Actions.message(sender, null, "&c3番目のパラメータが整数値に変換できませんでした！");
					return true;
				}
				if (minute <= 0){
					Actions.message(sender, null, "&c3番目の数値が0以下です！");
					return true;
				}

				Actions.flyPlayer(targetPlayer, minute);
				Actions.message(sender, null,msgPrefix+"&a"+args[1]+" &fを&a"+minute+"分間&f飛行できるようにしました！");

				return true;
			}

			// 飛行権限剥奪
			if (args.length == 2 && args[0].equalsIgnoreCase("flydisable")){
				if (!sender.hasPermission("sakuraserver.admin")){
					Actions.message(sender, null, "&cこのコマンドを実行する権限がありません");
					return true;
				}

				OfflinePlayer targetOfflinePlayer = Bukkit.getOfflinePlayer(args[1]);

				if(!SakuraServer.flyingPlayerList.contains(targetOfflinePlayer.getName())){
					Actions.message(sender, null, "&cプレイヤー &f"+targetOfflinePlayer.getName()+" &cは飛行権限を持っていません！");
					Actions.message(sender, null, "&f飛行権限を持っているリストの表示は /sakura flylist です");
					return true;
				}

				// 剥奪処理
				SakuraServer.flyingPlayerList.remove(targetOfflinePlayer.getName());

				if(targetOfflinePlayer != null && targetOfflinePlayer.isOnline()){
					Player targetPlayer = targetOfflinePlayer.getPlayer();
					// オンラインなら飛行状態を解除
					Actions.expireFlyPlayer(targetPlayer);
					Actions.message(null, targetPlayer, "&cあなたは"+sender.getName()+"によって飛行権限を剥奪されました");
					Actions.log("FlyMode.log", "Player " + targetPlayer.getName() + " is disabled flying mode by "+sender.getName());
				}else{
					Actions.log("FlyMode.log", "(Offline)Player " + targetOfflinePlayer.getName() + " is disabled flying mode by "+sender.getName());
				}

				Actions.message(sender, null, "&bプレイヤー &f"+targetOfflinePlayer.getName()+" &bの飛行権限を剥奪しました");

				return true;
			}

			// ブロードキャストメッセージ
			if (args.length >=2 && args[0].equalsIgnoreCase("bcast")){
				if (!sender.hasPermission("sakuraserver.admin")){
					Actions.message(sender, null, "&cこのコマンドを実行する権限がありません");
					return true;
				}
				String message = args[1];
				int len = args.length;
				for (int i = 3; len >= i; i++){
					message = message + " " + args[i-1];
				}
				Actions.broadcastMessage(message);
				return true;
			}

			// TabListの表示名を変更
			if (args.length >= 3 && args[0].equalsIgnoreCase("name")){
				if (!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.RED + "コンソールからは使えません！");
					return true;
				}
				if (sender.hasPermission("sakuraserver.admin")){
					if (Actions.validColor(args[2]) == true){
						Actions.changeTabName(sender.getName(), args[1], args[2]);
					}else{
						Actions.message(sender, null, "&cその色はデータにありません！");
					}
				}else{
					Actions.message(sender, null, "&cこのコマンドを実行する権限がありません");
				}
				return true;
			}

			// TabListの表示色を変更
			if (args.length >= 2 && args[0].equalsIgnoreCase("color")){
				if (!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.RED + "コンソールからは使えません！");
					return true;
				}
				if (Actions.validColor(args[1]) == true){

					/* 禁止色(スタッフタグ) */
					ChatColor check = ChatColor.valueOf(args[1].toUpperCase());
					if (check == ChatColor.RED || check == ChatColor.AQUA || check == ChatColor.LIGHT_PURPLE){
						Actions.message(sender, null, "&cその色はスタッフカラーです！");
						return true;
					}

					Actions.changeTabColor(sender.getName(), args[1]);
					Actions.message(sender, null, ChatColor.GREEN + "あなたの色は " + check + args[1] + ChatColor.GREEN + " に変更されました！");
				}else{
					Actions.message(sender, null, "&cその色はデータにありません！");
				}
				return true;
			}

			// テスト・デバッグ用
			if (args.length >= 2 && args[0].equalsIgnoreCase("test")){
				if (!sender.hasPermission("sakuraserver.admin")){
					Actions.message(sender, null, "&cこのコマンドを実行する権限がありません");
					return true;
				}
				// ここからデバッグ用

				Player player = (Player)sender;
				OfflinePlayer targetOfflinePlayer = Bukkit.getServer().getOfflinePlayer(args[1]);

				if(targetOfflinePlayer == null || !targetOfflinePlayer.isOnline()){
					return true;
				}

				Player targetPlayer = (Player) targetOfflinePlayer;

				if (targetPlayer.getPassenger() != null){
					targetPlayer.eject();
				}

				targetPlayer.setPassenger(player);

				// ここまで
				return true;
			}

			Actions.adminHelp(sender);
			return true;
		}
		return false;
	}
}
