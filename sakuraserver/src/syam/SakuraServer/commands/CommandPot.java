/**
 * sakuraserver - Package: syam.SakuraServer.commands
 * Created: 2012/10/30 19:08:17
 */
package syam.SakuraServer.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import syam.SakuraServer.SakuraPlayer;
import syam.SakuraServer.SakuraServer;
import syam.util.Actions;

/**
 * CommandPot (CommandPot.java)
 * @author syam(syamn)
 */
public class CommandPot extends BaseCommand{
	public CommandPot(){
		bePlayer = true;
		name = "pot";
		argLength = 0;
		usage = "pot command";
	}

	@Override
	public void execute() {
		// フラッグワールド無効
		if (Bukkit.getWorld("flag") != null){
			if (player.getWorld() == Bukkit.getWorld("flag")){
				Actions.message(null, player, "&cこのワールドでこのコマンドは使えません！");
				return;
			}
		}

		if(args.size() == 1 || args.size() == 2){
			// ポーション名チェック
			PotionEffectType potion = Actions.validPotion(args.get(0));
			if (potion == null){
				Actions.message(null, player, "&cそのPot名は使えません！");
				return;
			}else{
				// ポーション初期効果
				int duration = 12000; // 仮:10分
				int amplifier = 1;    // 仮:Lv1

				double cost = Actions.potionMap.get(potion.getName());

				if (args.size() == 2){
					try {
						amplifier = Integer.parseInt(args.get(1));
						cost = cost * amplifier;
					}catch(NumberFormatException e){
						Actions.message(sender, player, "&cレベルが整数ではありません！");
						return;
					}
				}

				// 支払い
				if (!Actions.potionPurchase((Player)sender, cost))
					return;

				if (amplifier <= 0 || amplifier >= 11){
					Actions.message(sender, player, "&cそのレベルは使えません！1～10の数を入力してください！");
					return;
				}

				if (cost < 0){
					return;
				}

				// 効果付与
				Actions.addPotionEffect(player, potion, duration, amplifier - 1);
				Actions.message(null, player, msgPrefix+"&a"+(int)cost+"Coin&fを払い特殊効果を得ました！");
				return;
			}
		}else{
			Actions.potHelp(sender);
		}
	}

	@Override
	public boolean permission(CommandSender sender) {
		return sender.hasPermission("sakuraserver.visitor");
	}
}
