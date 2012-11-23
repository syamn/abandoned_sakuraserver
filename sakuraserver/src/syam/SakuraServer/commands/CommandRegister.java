/**
 * sakuraserver - Package: syam.SakuraServer.commands
 * Created: 2012/10/30 19:30:27
 */
package syam.SakuraServer.commands;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;

import syam.SakuraServer.SakuraMySqlManager;
import syam.SakuraServer.SakuraSecurity;
import syam.SakuraServer.SakuraServer;
import syam.util.Actions;
import syam.util.Encrypter;

/**
 * CommandRegister (CommandRegister.java)
 * @author syam(syamn)
 */
public class CommandRegister extends BaseCommand{
	public CommandRegister(){
		bePlayer = true;
		name = "register";
		argLength = 0;
		usage = "register web!";
	}

	@Override
	public void execute() {
		if (args.size() != 2 && args.size() != 3){
			Actions.message(sender, null, "&c/register [パスワード] [パスワード(確認)] [メールアドレス]");
			return;
		}

		// 登録処理開始
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
			return;
		}

		String email = "";
		if (args.size() == 3){
			Pattern pattern = Pattern.compile("[\\w\\.\\-]+@(?:[\\w\\-]+\\.)+[\\w\\-]+");
			Matcher matcher = pattern.matcher(args.get(2).trim());
			if(!matcher.matches()){
				Actions.message(null, player, "&cメールアドレスの形式が不正です！");
				return;
			}
			// 既にメールアドレスが登録されていないか確認する
			Boolean isExist2 = SakuraServer.dbm.isExistRow(conn, "SELECT * FROM `"+SakuraMySqlManager.table_userdata+"` WHERE `email` = \""+args.get(2).trim()+"\"");
			if(isExist2){
				Actions.message(null, player, "&cそのメールアドレスは既に登録されています。");
				return;
			}
			email = args.get(2).trim();
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
		boolean result = SakuraServer.dbm.insertAuthTable(Actions.getDatetime(), player.getName(), encrypted, email);

		if (result){
			Actions.message(null, player, "&a登録に成功しました！");
		}else{
			Actions.message(null, player, "&c登録に失敗しました！");
		}

		SakuraSecurity.checkRegister(player, args, encrypted, email);
	}

	@Override
	public boolean permission(CommandSender sender) {
		return sender.hasPermission("sakuraserver.visitor");
	}
}
