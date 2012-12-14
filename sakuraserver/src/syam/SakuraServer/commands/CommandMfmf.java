/**
 * sakuraserver - Package: syam.SakuraServer.commands Created: 2012/10/30
 * 19:42:59
 */
package syam.SakuraServer.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import syam.SakuraServer.SakuraPlayer;
import syam.SakuraServer.SakuraServer;
import syam.util.Actions;

/**
 * CommandMfmf (CommandMfmf.java)
 * 
 * @author syam(syamn)
 */
public class CommandMfmf extends BaseCommand {
    public CommandMfmf() {
        bePlayer = true;
        name = "mfmf";
        argLength = 0;
        usage = "mfmf!";
    }
    
    @Override
    public void execute() {
        if (args.size() == 0) {
            Actions.message(sender, null, " &aもふもふ...？&7 /もふもふ <プレイヤー名>");
            Actions.message(sender, null, " &aあなたは今までに" + SakuraServer.playerData.get(player).getmfmfCount() + "回もふもふされました！");
        } else {
            Player target = Bukkit.getPlayer(args.get(0));
            // ターゲットチェック
            if (target != null && target.isOnline()) {
                if (target == player) {
                    Actions.message(sender, null, "&c自分をもふもふできません！");
                    return;
                }
                
                // Actions.checkMoneyは必要無い？
                if (Actions.takeMoney(player.getName(), 150)) {
                    SakuraPlayer sp = SakuraServer.playerData.get(target);
                    
                    if (sp == null) {
                        Actions.message(sender, null, "&cエラーが発生しました！相手に一度ログアウトしてもらってください！");
                        return;
                    }
                    
                    Actions.addMoney(target.getName(), 100); // チェックはしない
                    int added = sp.addMofCount(); // もふもふカウント++
                    
                    Actions.message(null, target, " &6'" + player.getName() + "'&aにもふもふされました！(+100Coin)(" + added + "回目)");
                    Actions.message(null, player, " &6'" + target.getName() + "'&aをもふもふしました！&c(-150Coin)");
                } else {
                    Actions.message(null, target, " &6'" + player.getName() + "'&aにもふもふされました！");
                    Actions.message(null, player, " &6'" + target.getName() + "'&aをもふもふしました！");
                }
            }
            // 相手が見つからない
            else {
                Actions.message(sender, null, " &6もふ...？ 相手が見つからないです...。");
            }
        }
    }
    
    @Override
    public boolean permission(CommandSender sender) {
        return true;
    }
}
