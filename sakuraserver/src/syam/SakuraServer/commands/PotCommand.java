package syam.SakuraServer.commands;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import syam.SakuraServer.SakuraServer;
import syam.util.Actions;

public class PotCommand implements CommandExecutor {
	public final static Logger log = SakuraServer.log;
	private static final String logPrefix = SakuraServer.logPrefix;
	private static final String msgPrefix = SakuraServer.msgPrefix;

	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
		if (command.getName().equalsIgnoreCase("pot")){
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "コンソールからは使えません！");
				return true;
			}

			Player player = (Player)sender;

			// フラッグワールド無効
			if (Bukkit.getWorld("flag") != null){
				if (player.getWorld() == Bukkit.getWorld("flag")){
					Actions.message(null, player, "&cこのワールドでこのコマンドは使えません！");
					return true;
				}
			}

			if(args.length == 1 || args.length == 2){
				// ポーション名チェック
				PotionEffectType potion = Actions.validPotion(args[0]);
				if (potion == null){
					Actions.message(null, player, "&cそのPot名は使えません！");
					return true;
				}else{
					// ポーション初期効果
					int duration = 12000; // 仮:10分
					int amplifier = 1;    // 仮:Lv1

					double cost = Actions.potionMap.get(potion.getName());

					if (args.length == 2){
						try {
							amplifier = Integer.parseInt(args[1]);
							cost = cost * amplifier;
						}catch(NumberFormatException e){
							Actions.message(sender, player, "&cレベルが整数ではありません！");
							return true;
						}
					}

					// 支払い
					if (!Actions.potionPurchase((Player)sender, cost))
						return true;

					if (amplifier <= 0 || amplifier >= 11){
						Actions.message(sender, player, "&cそのレベルは使えません！1～10の数を入力してください！");
						return true;
					}

					if (cost < 0){
						return true;
					}

					// 効果付与
					Actions.addPotionEffect(player, potion, duration, amplifier - 1);
					Actions.message(null, player, msgPrefix+"&a"+(int)cost+"Coin&fを払い特殊効果を得ました！");
					return true;
				}
			}
			Actions.potHelp(sender);
			return true;
		}
		return false;
	}
}
