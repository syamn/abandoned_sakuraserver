/**
 * sakuraserver - Package: syam.SakuraServer.commands
 * Created: 2012/10/30 19:40:17
 */
package syam.SakuraServer.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import syam.util.Actions;

/**
 * CommandSyamn (CommandSyamn.java)
 * @author syam(syamn)
 */
public class CommandSyamn extends BaseCommand{
	public CommandSyamn(){
		bePlayer = false;
		name = "syamn";
		argLength = 0;
		usage = "syamn :3 ?";
	}

	@Override
	public void execute() {
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
	}

	@Override
	public boolean permission(CommandSender sender) {
		return true;
	}
}
