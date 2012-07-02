package syam.SakuraServer.commands;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import syam.SakuraServer.Actions;
import syam.SakuraServer.SakuraSecurity;
import syam.SakuraServer.SakuraMySqlManager;
import syam.SakuraServer.SakuraServer;
import syam.util.Encrypter;

public class PasswordCommand implements CommandExecutor {
	public final static Logger log = SakuraServer.log;
	private static final String logPrefix = SakuraServer.logPrefix;
	private static final String msgPrefix = SakuraServer.msgPerfix;

	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
		if (command.getName().equalsIgnoreCase("password")){
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "コンソールからは使えません！");
				return true;
			}
			Player player = (Player) sender;
			// 登録処理開始
			if (args.length == 2){
				Actions.message(null, player, "&6登録情報の更新を行っています..");
				// 既に登録されていないか確認する
				SakuraServer.dbm.changeDatabase(SakuraMySqlManager.db_web);
				Connection conn = null;
				try {
					conn = SakuraServer.dbm.getVPSConnection();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
				Boolean isExist = SakuraServer.dbm.isExistRow(conn, "SELECT * FROM `"+SakuraMySqlManager.table_userdata+"` WHERE `player_name` = \""+player.getName()+"\"");
				if(!isExist){
					Actions.message(null, player, "&cあなたは登録されていません！登録は /register コマンドです。");
					return true;
				}

				if (!args[0].trim().equals(args[1].trim())){
					Actions.message(null, player, "&cパスワードが一致しませんでした！");
					return true;
				}

				String msg = Actions.isPasswordValid(args[0].trim());
				if (msg != null){
					Actions.message(null, player, "&c"+msg);
					return true;
				}

				String encrypted = Encrypter.getHash(args[0], Encrypter.ALG_SHA512);
				boolean result = SakuraServer.dbm.changePassWordOnAuthTable(encrypted, player.getName());

				if (result){
					Actions.message(null, player, "&aパスワード変更に成功しました！");
				}else{
					Actions.message(null, player, "&cパスワード変更に失敗しました！");
				}

				SakuraSecurity.checkPassword(player, args, encrypted);
				return true;
			}

			Actions.message(sender, null, "&c/password [パスワード] [パスワード(確認)]");
			return true;
		}
		return false;
	}
}
