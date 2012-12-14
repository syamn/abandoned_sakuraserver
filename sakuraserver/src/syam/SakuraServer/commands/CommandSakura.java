/**
 * sakuraserver - Package: syam.SakuraServer.commands Created: 2012/10/30
 * 19:35:13
 */
package syam.SakuraServer.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import syam.util.Actions;

/**
 * CommandSakura (CommandSakura.java)
 * 
 * @author syam(syamn)
 */
public class CommandSakura extends BaseCommand {
    public CommandSakura() {
        bePlayer = false;
        name = "sakura";
        argLength = 0;
        usage = "sakura common command!";
    }
    
    @Override
    public void execute() {
        if (args.size() == 0) {
            Actions.sakuraHelp(sender);
            return;
        }
        
        // ヘルプを表示
        if (args.get(0).equalsIgnoreCase("help")) {
            Actions.sakuraHelp(sender);
            return;
        }
        
        // 乗っているプレイヤーを降ろす
        if (args.get(0).equalsIgnoreCase("eject")) {
            if (!isPlayer) {
                sender.sendMessage(ChatColor.RED + "コンソールからは使えません！");
                return;
            }
            
            if (player.getPassenger() != null) {
                player.eject();
                Actions.message(sender, null, "&b乗っているプレイヤーを降ろしました！");
            } else {
                Actions.message(sender, null, "&cあなたに乗っているプレイヤーはいません！");
            }
            return;
        }
        
        Actions.message(sender, null, "&c有効なサブコマンドがありません！");
        Actions.sakuraHelp(sender);
    }
    
    @Override
    public boolean permission(CommandSender sender) {
        return sender.hasPermission("sakuraserver.visitor");
    }
    
}
