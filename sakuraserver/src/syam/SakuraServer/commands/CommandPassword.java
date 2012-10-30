/**
 * sakuraserver - Package: syam.SakuraServer.commands
 * Created: 2012/10/30 19:27:39
 */
package syam.SakuraServer.commands;

import java.sql.Connection;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import syam.SakuraServer.SakuraMySqlManager;
import syam.SakuraServer.SakuraSecurity;
import syam.SakuraServer.SakuraServer;
import syam.util.Actions;
import syam.util.Encrypter;

/**
 * CommandPassword (CommandPassword.java)
 * @author syam(syamn)
 */
public class CommandPassword extends BaseCommand{
	public CommandPassword(){
		bePlayer = true;
		name = "password";
		argLength = 0;
		usage = "change web password";
	}

	@Override
	public void execute() {
		if (args.size() != 2){
			Actions.message(sender, null, "&c/password [パスワード] [パスワード(確認)]");
			return;
		}

		// 登録処理開始
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
			return;
		}

		if (!args.get(0).trim().equals(args.get(1).trim())){
			Actions.message(null, player, "&cパスワードが一致しませんでした！");
			return;
		}

		String msg = Actions.isPasswordValid(args.get(0).trim());
		if (msg != null){
			Actions.message(null, player, "&c"+msg);
			return;
		}

		String encrypted = Encrypter.getHash(args.get(0), Encrypter.ALG_SHA512);
		boolean result = SakuraServer.dbm.changePassWordOnAuthTable(encrypted, player.getName());

		if (result){
			Actions.message(null, player, "&aパスワード変更に成功しました！");
		}else{
			Actions.message(null, player, "&cパスワード変更に失敗しました！");
		}

		SakuraSecurity.checkPassword(player, args, encrypted);
	}

	@Override
	public boolean permission(CommandSender sender) {
		return sender.hasPermission("sakuraserver.visitor");
	}

}
