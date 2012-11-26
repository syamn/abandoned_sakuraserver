/**
 * sakuraserver - Package: syam.SakuraServer.commands
 * Created: 2012/10/30 19:14:42
 */
package syam.SakuraServer.commands;

import java.util.Map;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Packet201PlayerInfo;
import net.minecraft.server.Packet62NamedSoundEffect;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import syam.SakuraServer.SakuraServer;
import syam.util.Actions;
import syam.util.Util;

/**
 * CommandAdmin (CommandAdmin.java)
 * @author syam(syamn)
 */
public class CommandAdmin extends BaseCommand{
	public CommandAdmin(){
		bePlayer = false;
		name = "admin";
		argLength = 0;
		usage = "admin commands!";
	}

	@Override
	public void execute() {
		if (args.size() == 0){
			Actions.adminHelp(sender);
			return;
		}

		// 設定ファイルを再読み込み
		if (args.get(0).equalsIgnoreCase("reloadconfig")){
			SakuraServer.getInstance().loadConfig();
			Actions.message(sender, null, "&a設定を再読み込みしました");
			return;
		}

		// コンソールでコマンド実行
		if (args.size() >= 2 && args.get(0).equalsIgnoreCase("ccmd")){
			if (!isPlayer) {
				sender.sendMessage(ChatColor.RED + "コンソールからは使えません！");
				return;
			}

			// 本体
			String consoleCmd = args.get(1);
			int i = 2;
			while(i < args.size()){
				consoleCmd = consoleCmd + " " + args.get(i);
				i++;
			}
			// 実行
			Actions.executeCommandOnConsole(consoleCmd);
			Actions.message(sender, null, "&aコマンド &f"+consoleCmd+" &aをコンソールから実行しました");

			return;
		}

		// 飛行権限付与
		if (args.size() == 3 && args.get(0).equalsIgnoreCase("fly")){
			OfflinePlayer targetOfflinePlayer = Bukkit.getOfflinePlayer(args.get(1));
			int minute;

			if(SakuraServer.flyingPlayerList.contains(targetOfflinePlayer.getName())){
				Actions.message(sender, null, "&cプレイヤー &f"+targetOfflinePlayer.getName()+" &cは既に飛行権限を持っています！");
				Actions.message(sender, null, "&f飛行権限を持っているリストの表示は /sakura flylist です");
				return;
			}
			Player targetPlayer = targetOfflinePlayer.getPlayer();
			if ((targetPlayer == null) || !(targetPlayer.isOnline())){
				Actions.message(sender, null, "&cプレイヤー &f"+targetOfflinePlayer.getName()+" &cはオフラインです！");
				return;
			}
			if(targetPlayer.getLocation().getY() > 257 || targetPlayer.getLocation().getY() < 0){
				Actions.message(sender, null, "&cプレイヤー &f"+targetOfflinePlayer.getName()+" &cの位置が許可されない地点です");
				return;
			}

			try{
				minute = Integer.parseInt(args.get(2));
			}catch (NumberFormatException e){
				Actions.message(sender, null, "&c3番目のパラメータが整数値に変換できませんでした！");
				return;
			}
			if (minute <= 0){
				Actions.message(sender, null, "&c3番目の数値が0以下です！");
				return;
			}

			Actions.flyPlayer(targetPlayer, minute);
			Actions.message(sender, null,msgPrefix+"&a"+args.get(1)+" &fを&a"+minute+"分間&f飛行できるようにしました！");

			return;
		}

		// 飛行権限剥奪
		if (args.size() == 2 && args.get(0).equalsIgnoreCase("flydisable")){
			OfflinePlayer targetOfflinePlayer = Bukkit.getOfflinePlayer(args.get(1));

			if(!SakuraServer.flyingPlayerList.contains(targetOfflinePlayer.getName())){
				Actions.message(sender, null, "&cプレイヤー &f"+targetOfflinePlayer.getName()+" &cは飛行権限を持っていません！");
				Actions.message(sender, null, "&f飛行権限を持っているリストの表示は /sakura flylist です");
				return;
			}

			// 剥奪処理
			SakuraServer.flyingPlayerList.remove(targetOfflinePlayer.getName());

			if(targetOfflinePlayer != null && targetOfflinePlayer.isOnline()){
				Player targetPlayer = targetOfflinePlayer.getPlayer();
				// オンラインなら飛行状態を解除
				Actions.expireFlyPlayer(targetPlayer);
				Actions.message(null, targetPlayer, "&cあなたは"+sender.getName()+"によって飛行権限を剥奪されました");
				Actions.log("FlyMode.log", "Player " + targetPlayer.getName() + " is disabled flying mode by "+sender.getName());
			}else{
				Actions.log("FlyMode.log", "(Offline)Player " + targetOfflinePlayer.getName() + " is disabled flying mode by "+sender.getName());
			}

			Actions.message(sender, null, "&bプレイヤー &f"+targetOfflinePlayer.getName()+" &bの飛行権限を剥奪しました");

			return;
		}

		// ブロードキャストメッセージ
		if (args.size() >= 2 && args.get(0).equalsIgnoreCase("bcast")){
			args.remove(0);
			Actions.broadcastMessage(Util.join(args, " "));
			return;
		}

		// TabListの表示名を変更
		if (args.size() >= 3 && args.get(0).equalsIgnoreCase("name")){
			if (!isPlayer) {
				sender.sendMessage(ChatColor.RED + "コンソールからは使えません！");
				return;
			}
			if (Actions.validColor(args.get(2))){
				Actions.changeTabName(sender.getName(), args.get(1), args.get(2));
			}else{
				Actions.message(sender, null, "&cその色はデータにありません！");
			}
			return;
		}

		// TabListの表示色を変更
		if (args.size() >= 2 && args.get(0).equalsIgnoreCase("color")){
			if (!isPlayer) {
				sender.sendMessage(ChatColor.RED + "コンソールからは使えません！");
				return;
			}
			if (Actions.validColor(args.get(1)) == true){

				/* 禁止色(スタッフタグ) */
				ChatColor check = ChatColor.valueOf(args.get(1).toUpperCase());
				if (check == ChatColor.RED || check == ChatColor.AQUA || check == ChatColor.LIGHT_PURPLE){
					Actions.message(sender, null, "&cその色はスタッフカラーです！");
					return;
				}

				Actions.changeTabColor(sender.getName(), args.get(1));
				Actions.message(sender, null, ChatColor.GREEN + "あなたの色は " + check + args.get(1) + ChatColor.GREEN + " に変更されました！");
			}else{
				Actions.message(sender, null, "&cその色はデータにありません！");
			}
			return;
		}

		// TabListの表示名追加
		if (args.size() >= 2 && args.get(0).equalsIgnoreCase("add")){
			String addName = Actions.coloring(args.get(1));

			if (SakuraServer.fakeJoinedPlayerList.contains(addName)){
				Actions.message(sender, null, "&aそのプレイヤーは既に追加されていますが、追加パケットを再送信します");
			}else{
				SakuraServer.fakeJoinedPlayerList.add(addName);
			}

			if (addName.length() > 16){
				Actions.message(sender, null, "&cプレイヤーリスト名は16文字以上にできません！");
				return;
			}

			for (Player player : Bukkit.getServer().getOnlinePlayers()){
				((CraftPlayer)player).getHandle().netServerHandler.sendPacket(new Packet201PlayerInfo(addName, true, ((CraftPlayer)player).getHandle().ping));
			}

			Actions.message(sender, null, "&a'"+addName+"&a'をTabリストに追加しました");

			return;
		}

		// TabListの表示名削除
		if (args.size() >= 2 && args.get(0).equalsIgnoreCase("remove")){
			String removeName = Actions.coloring(args.get(1));

			if (SakuraServer.fakeJoinedPlayerList.contains(removeName)){
				SakuraServer.fakeJoinedPlayerList.remove(removeName);
			}else{
				Actions.message(sender, null, "&aそのプレイヤーは追加されていませんが、削除パケットを送信します");
			}

			if (removeName.length() > 16){
				Actions.message(sender, null, "&cプレイヤーリスト名は16文字以上にできません！");
				return;
			}

			for (Player player : Bukkit.getServer().getOnlinePlayers()){
				((CraftPlayer)player).getHandle().netServerHandler.sendPacket(new Packet201PlayerInfo(removeName, false, 9999));
			}

			Actions.message(sender, null, "&a'"+removeName+"&a'をTabリストから削除しました");

			return;
		}

		// テスト・デバッグ用
		if (args.size() >= 1 && args.get(0).equalsIgnoreCase("test")){
			if (args.size() < 3) return;

			Player p = Bukkit.getPlayer(args.get(1));
			if (p == null || !p.isOnline()) return;

			Location loc = p.getLocation();
			CraftPlayer cp = (CraftPlayer)p;
			cp.getHandle().netServerHandler.sendPacket(
					new Packet62NamedSoundEffect(args.get(3).trim(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), Float.parseFloat(args.get(2)), 1.0F)
					);
			Actions.message(null, p, "にゃー");

			//MinecraftServer.getServer().getServerConfigurationManager().sendpack..

			return;
		}
	}

	@Override
	public boolean permission(CommandSender sender) {
		return sender.hasPermission("sakuraserver.admin");
	}
}
