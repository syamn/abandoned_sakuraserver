package syam.SakuraServer.commands;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import syam.SakuraServer.Actions;
import syam.SakuraServer.SakuraServer;

public class MiscCommand implements CommandExecutor {
	public final static Logger log = SakuraServer.log;
	private static final String logPrefix = SakuraServer.logPrefix;
	private static final String msgPrefix = SakuraServer.msgPerfix;

	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
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
