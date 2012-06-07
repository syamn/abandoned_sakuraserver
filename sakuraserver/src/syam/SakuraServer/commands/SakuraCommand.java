package syam.SakuraServer.commands;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import syam.SakuraServer.Actions;
import syam.SakuraServer.SakuraServer;

public class SakuraCommand implements CommandExecutor {
	public final static Logger log = SakuraServer.log;
	private static final String logPrefix = SakuraServer.logPrefix;
	private static final String msgPrefix = SakuraServer.msgPerfix;

	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
		if (command.getName().equalsIgnoreCase("sakura")){
			// ヘルプを表示
			if (args.length > 0 && args[0].equalsIgnoreCase("help")){
				Actions.sakuraHelp(sender);
				return true;
			}
			if (args.length > 0 && args[0].equalsIgnoreCase("eject")){
				if (!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.RED + "コンソールからは使えません！");
					return true;
				}
				Player player = (Player) sender;
				if (player.getPassenger() != null){
					player.eject();
					Actions.message(sender, null, "&b乗っているプレイヤーを降ろしました！");
					return true;
				}else{
					Actions.message(sender, null, "&cあなたに乗っているプレイヤーはいません！");
					return true;
				}
			}

			Actions.message(sender, null, "&c有効なサブコマンドがありません！");
			Actions.sakuraHelp(sender);
			return true;
		}
		return false;
	}
}
