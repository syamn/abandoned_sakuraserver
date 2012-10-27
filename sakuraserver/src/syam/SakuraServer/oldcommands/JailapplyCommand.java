package syam.SakuraServer.oldcommands;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import syam.SakuraServer.SakuraServer;
import syam.util.Actions;

public class JailapplyCommand implements CommandExecutor {
	public final static Logger log = SakuraServer.log;
	private static final String logPrefix = SakuraServer.logPrefix;
	private static final String msgPrefix = SakuraServer.msgPrefix;

	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
		if (command.getName().equalsIgnoreCase("jailapply")){
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "コンソールからは使えません！");
				return true;
			}
			Player player = (Player) sender;

			if(SakuraServer.playerData.containsKey(player) &&
					SakuraServer.playerData.get(player).getLastKillerName() != null){
				// 投獄
				if(player.hasPermission("sakuraserver.citizen")){
					String damager = SakuraServer.playerData.get(player).getLastKillerName();
					Actions.executeCommandOnConsole("jail "+damager+" 60 m You killed player "+ player.getName());
					Actions.broadcastMessage(msgPrefix+"&e"+damager+"&a は &e"+player.getName()+" &aが被害申告したため自動投獄されました(60分)");
					if (Bukkit.getServer().getPlayer(damager).isOnline()){
						Actions.message(null, Bukkit.getServer().getPlayer(damager),
								"あなたは殺人容疑により60分間投獄されました。これが不当なものであると感じる場合、サーバスタッフまで連絡してください。");
					}
					SakuraServer.playerData.get(player).setLastKillerName(null);
					return true;
				}else{
					Actions.message(sender, player, "&c権限がありません");
					return true;
				}
			}else{
				// 過去に殺されたデータなし
				Actions.message(sender, player, "&c被害申告のための履歴がありません");
				return true;
			}
		}
		return false;
	}
}
