package syam.SakuraServer.commands;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import syam.SakuraServer.Actions;
import syam.SakuraServer.SakuraMySqlManager;
import syam.SakuraServer.SakuraSecurity;
import syam.SakuraServer.SakuraServer;
import syam.util.Encrypter;

public class RegisterCommand implements CommandExecutor {
	public final static Logger log = SakuraServer.log;
	private static final String logPrefix = SakuraServer.logPrefix;
	private static final String msgPrefix = SakuraServer.msgPerfix;

	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
		if (command.getName().equalsIgnoreCase("register")){
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "コンソールからは使えません！");
				return true;
			}
			Player player = (Player) sender;

			// 登録処理開始
			if (args.length == 2 || args.length == 3){
				Actions.message(null, player, "&6登録処理を行っています..");
				// 既に登録されていないか確認する
				// ウェブ用データベースに変更
				SakuraServer.dbm.changeDatabase(SakuraMySqlManager.db_web);
				Connection conn = null;
				try {
					conn = SakuraServer.dbm.getVPSConnection();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
				Boolean isExist = SakuraServer.dbm.isExistRow(conn, "SELECT * FROM `"+SakuraMySqlManager.table_userdata+"` WHERE `player_name` = \""+player.getName()+"\"");
				if(isExist){
					Actions.message(null, player, "&c既に登録されています！パスワード変更は /password コマンドを使います。");
					return true;
				}

				String email = "";
				if (args.length == 3){
					Pattern pattern = Pattern.compile("[\\w\\.\\-]+@(?:[\\w\\-]+\\.)+[\\w\\-]+");
					Matcher matcher = pattern.matcher(args[2].trim());
					if(!matcher.matches()){
						Actions.message(null, player, "&cメールアドレスの形式が不正です！");
						return true;
					}
					// 既にメールアドレスが登録されていないか確認する
					Boolean isExist2 = SakuraServer.dbm.isExistRow(conn, "SELECT * FROM `"+SakuraMySqlManager.table_userdata+"` WHERE `email` = \""+args[2].trim()+"\"");
					if(isExist2){
						Actions.message(null, player, "&cそのメールアドレスは既に登録されています。");
						return true;
					}
					email = args[2].trim();
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
				boolean result = SakuraServer.dbm.insertAuthTable(Actions.getDatetime(), player.getName(), encrypted, email);

				if (result){
					Actions.message(null, player, "&a登録に成功しました！");
				}else{
					Actions.message(null, player, "&c登録に失敗しました！");
				}

				SakuraSecurity.checkRegister(player, args, encrypted, email);
				return true;
			}

			Actions.message(sender, null, "&c/register [パスワード] [パスワード(確認)] [メールアドレス]");
			return true;
		}
		return false;
	}

}
