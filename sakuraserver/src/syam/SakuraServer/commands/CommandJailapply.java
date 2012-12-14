/**
 * sakuraserver - Package: syam.SakuraServer.commands Created: 2012/10/30
 * 19:12:14
 */
package syam.SakuraServer.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import syam.SakuraServer.SakuraServer;
import syam.util.Actions;

/**
 * CommandJailapply (CommandJailapply.java)
 * 
 * @author syam(syamn)
 */
public class CommandJailapply extends BaseCommand {
    public CommandJailapply() {
        bePlayer = true;
        name = "jailapply";
        argLength = 0;
        usage = "jail griefer!";
    }
    
    @Override
    public void execute() {
        if (SakuraServer.playerData.containsKey(player) && SakuraServer.playerData.get(player).getLastKillerName() != null) {
            // 投獄
            String damager = SakuraServer.playerData.get(player).getLastKillerName();
            Actions.executeCommandOnConsole("jail " + damager + " 60 m You killed player " + player.getName());
            Actions.broadcastMessage(msgPrefix + "&e" + damager + "&a は &e" + player.getName() + " &aが被害申告したため自動投獄されました(60分)");
            if (Bukkit.getServer().getPlayer(damager).isOnline()) {
                Actions.message(null, Bukkit.getServer().getPlayer(damager), "あなたは殺人容疑により60分間投獄されました。これが不当なものであると感じる場合、サーバスタッフまで連絡してください。");
            }
            SakuraServer.playerData.get(player).setLastKillerName(null);
        } else {
            // 過去に殺されたデータなし
            Actions.message(sender, player, "&c被害申告のための履歴がありません");
        }
    }
    
    @Override
    public boolean permission(CommandSender sender) {
        return sender.hasPermission("sakuraserver.citizen");
    }
}