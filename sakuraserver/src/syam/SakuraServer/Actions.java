package syam.SakuraServer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.apache.commons.lang.time.DateUtils;

import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import ru.tehkode.permissions.PermissionUser;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Stairs;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import syam.util.TextFileHandler;
import syam.util.NoteAlert;

public class Actions {
	public final static Logger log = SakuraServer.log;
	private static final String logPrefix = SakuraServer.logPrefix;
	private static final String msgPrefix = SakuraServer.msgPerfix;

	/****************************************/
	// 設定関係
	/****************************************/
	static ConcurrentHashMap<String, Double> potionMap = SakuraServer.potionMap;

	public static Vault vault = null;
	public static Economy economy = null;

	public static SakuraServer plugin;
	public Actions(SakuraServer instance){
		plugin = instance;
	}

	/*
	 * TODO:
	 *
	 * 特定のポーションの効果をコマンドで購入可能に？
	 *
	 * 管理用のTwitterアカウントを登録し、管理用→メイン(@mc_sakuraserver)宛にゲーム内からリプライを送る
	 * エンドワールドによってドラゴンの経験値を変更
	 *
	 * エンダードラゴン倒した人に一定期間持続する特殊効果(通常得られないポーションの効果)を与える
	 * 討伐回数記録して順位表
	 *
	 */

	/****************************************/
	/* メインクラスからの引き継ぎ */
	/****************************************/
	/**
	 * Vaultにフック
	 * @param plugin
	 * @param econProvider
	 * @return 成功ならtrue 失敗ならfalse
	 */
	public static boolean hookVault(Plugin plugin, RegisteredServiceProvider<Economy> econProvider){
        try{
        	vault = (Vault) plugin;
        	economy = econProvider.getProvider();
        }catch(Exception e){
        	return false;
        }
        return true;
	}


	/****************************************/
	/* 所持金操作系関数 */
	/****************************************/
	/**
	 * 指定したユーザーからお金を引く
	 * @param name ユーザー名
	 * @param amount 金額
	 * @return 成功ならtrue、失敗ならfalse
	 */
	public static boolean takeMoney(String name, double amount){
		EconomyResponse r = economy.withdrawPlayer(name, amount);
		if(r.transactionSuccess()) {
			return true;
		} else {
			return false;
		}
	}
	/**
	 * 指定したユーザーがお金を持っているか
	 * @param name ユーザー名
	 * @param amount 金額
	 * @return 持っていればtrue、無ければfalse
	 */
	public static boolean checkMoney(String name, double amount){
		return (economy.has(name, amount));
	}

	/****************************************/
	// 権限操作系関数
	/****************************************/
	/**
	 * プレイヤーに権限を与える
	 * @param player プレイヤー
	 * @param permission 権限ノード
	 * @param worldName 対象ワールド (""で全ワールド)
	 */
	public static void addPermissions(Player player, String permission, String worldName){
		PermissionUser permissionUser = PermissionsEx.getUser(player);
		permissionUser.addPermission(permission, worldName);
	}
	/**
	 * プレイヤーから権限を剥奪する
	 * @param player プレイヤー
	 * @param permission 剥奪する権限ノード
	 * @param worldName 対象ワールド (""で全ワールド)
	 */
	public static void removePermission(Player player, String permission, String worldName){
		PermissionUser permissionUser = PermissionsEx.getUser(player);
		permissionUser.removePermission(permission, worldName);
	}
	public static void removePermission(String player, String permission, String worldName){
		PermissionUser permissionUser = PermissionsEx.getUser(player);
		permissionUser.removePermission(permission, worldName);
	}
	/**
	 * 一定時間だけプレイヤーに権限を与える
	 * @param player プレイヤー
	 * @param permission 権限ノード
	 * @param worldName 対象ワールド (""で全ワールド)
	 * @param lifetime 期限(秒)
	 */
	public static void addTimedPermission(Player player, String permission, String worldName ,int lifetime){
		PermissionUser permissionUser = PermissionsEx.getUser(player);
		permissionUser.addTimedPermission(permission, worldName, lifetime);
	}
	/**
	 * 一定時間だけ与えた権限を剥奪する
	 * @param player プレイヤー
	 * @param permission 剥奪する権限ノード
	 * @param worldName 対象ワールド (""で全ワールド)
	 */
	public static void removeTimedPermission(Player player, String permission, String worldName){
		PermissionUser permissionUser = PermissionsEx.getUser(player);
		permissionUser.removeTimedPermission(permission,worldName);
	}

	/****************************************/
	// メッセージ送信系関数
	/****************************************/
	/**
	 * メッセージをユニキャスト
	 * @param sender Sender (null可)
	 * @param player Player (null可)l
	 * @param message メッセージ
	 */
	public static void message(CommandSender sender, Player player, String message){
		if (message != null){
			message = message
					.replaceAll("&([0-9a-fk-or])", "\u00A7$1")
					.replaceAll("%version", SakuraServer.getInstance().getDescription().getVersion());
			if (player != null){
				player.sendMessage(message);
			}
			else if (sender != null){
				sender.sendMessage(message);
			}
		}
	}
	/**
	 * メッセージをブロードキャスト
	 * @param message メッセージ
	 */
	public static void broadcastMessage(String message){
		if (message != null){
			message = message
					.replaceAll("&([0-9a-fk-or])", "\u00A7$1")
					.replaceAll("%version", SakuraServer.getInstance().getDescription().getVersion());
			Bukkit.broadcastMessage(message);
		}
	}
	/**
	 * メッセージをワールドキャスト
	 * @param world
	 * @param message
	 */
	public static void worldcastMessage(World world, String message){
		if (world != null && message != null){
			message = message
					.replaceAll("&([0-9a-fk-or])", "\u00A7$1")
					.replaceAll("%version", SakuraServer.getInstance().getDescription().getVersion());
			for(Player player: world.getPlayers()){
				log.info("[Worldcast]["+world.getName()+"]: " + message);
				player.sendMessage(message);
			}
		}
	}
	/**
	 * メッセージをパーミッションキャスト(指定した権限ユーザにのみ送信)
	 * @param permission 受信するための権限ノード
	 * @param message メッセージ
	 */
	public static void permcastMessage(String permission, String message){
		// 動かなかった どうして？
		//int i = Bukkit.getServer().broadcast(message, permission);

		// OK
		int i = 0;
		for (Player player : Bukkit.getServer().getOnlinePlayers()){
			if (player.hasPermission(permission)){
				Actions.message(null, player, message);
				i++;
			}
		}

		log.info("Received "+i+"players: "+message);
	}

	/****************************************/
	/* ヘルプメッセージ */
	/****************************************/
	public static void sakuraHelp(CommandSender sender){
		message(sender, null, "&c===================================");
		message(sender, null, "&bSakuraServer Plugin version &3%version &bby syamn");
		message(sender, null, " &b<>&f = 必要, &b[]&f = オプション");
		message(sender, null, " /sakura help &7- このヘルプを表示");
		message(sender, null, " /pot &7- 特殊効果購入のヘルプを表示");
		message(sender, null, " /flymode,/fm &7- 飛行に関するヘルプを表示");
		message(sender, null, " &7/admin - 管理者用ヘルプを表示");
		message(sender, null, " :スタッフのTabリスト色自動変更");
		message(sender, null, " :TheEndワールド追加");
		message(sender, null, "&c===================================");
	}
	public static void potHelp(CommandSender sender){
		message(sender, null, "&c===================================");
		message(sender, null, "&bSakuraServer Plugin version &3%version &bby syamn");
		message(sender, null, "&a/pot <Pot名> [効果レベル]&7- &f指定したポーションの効果(10分)を買います");
		message(sender, null, "&f効果レベルは1～9まで有効です。数が大きいほど強い効果を得られます。");
		message(sender, null, "&fただし、必要なお金は1レベルあたりの値段です。");
		message(sender, null, "&f3レベルの効果を買う場合は必要なお金も3倍になりますのでご注意ください。");
		message(sender, null, "&d------ &f< 有効なPotリスト > &d------");
		message(sender, null, " &bjump &f- &7ジャンプ力アップ(落下ダメージ注意！) &f- &b" + potionMap.get("JUMP").intValue() + " Coin");
		message(sender, null, " &bspeed &f- &7移動速度アップ &f- &b" + potionMap.get("SPEED").intValue() + " Coin");
		message(sender, null, " &bpoison &f- &7毒・x・ &f- &b" + potionMap.get("POISON").intValue() + " Coin");
		message(sender, null, "&c===================================");
	}
	public static void adminHelp(CommandSender sender){
		message(sender, null, "&c===================================");
		message(sender, null, "&bSakuraServer Plugin version &3%version &bby syamn");
		message(sender, null, " 管理用コマンドです");
		message(sender, null, " /admin reloadconfig &7- 設定ファイルを再読み込み");
		message(sender, null, " /admin ccmd <コマンド> &7- コンソールからコマンド実行");
		message(sender, null, " /admin fly <プレイヤー> <時間> &7- プレイヤーに飛行権限を付与");
		message(sender, null, " /admin flydisable <プレイヤー>&7- プレイヤーの飛行権限を剥奪");
		message(sender, null, " /admin bcast <メッセージ> &7- ブロードキャスト");
		message(sender, null, " /admin name <名前> <色> &7- TabList表示名変更");
		message(sender, null, " /admin color <色> &7- TabList表示色変更");
		message(sender, null, " /admin test &7- テスト・デバッグ用");
		message(sender, null, "&c===================================");
	}
	public static void flyModeHelp(CommandSender sender){
		message(sender, null, "&c===================================");
		message(sender, null, "&bSakuraServer Plugin version &3%version &bby syamn");
		message(sender, null, " &b<>&f = 必要, &b[]&f = オプション");
		message(sender, null, " /flymode info &7- 飛行のための情報を表示します");
		message(sender, null, " /flymode fly &7- 飛行権限を購入します");
		message(sender, null, " /flymode list &7- 現在飛行中のプレイヤーのリストを表示します");
		message(sender, null, "&d------ &f< あなたの現在情報 > &d------");
		if (sender instanceof Player){
			Player player = (Player) sender;
			if(!SakuraServer.flyingPlayerList.contains(player.getName())){
				message(sender, null, " 飛行モード： &c無効");
				message(sender, null, " 　&7残り時間： **分");
			}else{
				if(!SakuraServer.playerData.containsKey(player)){
					SakuraPlayer sakuraPlayer = new SakuraPlayer(player.getName());
					SakuraServer.playerData.put(player, sakuraPlayer);
				}
				long limit = SakuraServer.playerData.get(player).getFlyLimitDate().getTime();
				long ansMinute = (limit - new Date().getTime()) / (1000 * 60);
				message(sender, null, " 飛行モード： &a有効");
				message(sender, null, " 　残り時間： &a"+(int)ansMinute+"分");
			}
		}else{
			message(sender, null, " あなたはコンソールです！");
		}

		message(sender, null, "&c===================================");
	}
	public static void flyInfo(CommandSender sender){
		int cost = SakuraServer.configIntegerMap.get("FlyModeCost");
		int minute = SakuraServer.configIntegerMap.get("FlyModeTimeOfMinute");

		message(sender, null, "&c===================================");
		message(sender, null, "&bSakuraServer Plugin version &3%version &bby syamn");
		message(sender, null, "&c現在 &a"+cost+"Coin&c で &a"+minute+"分間 &c空を飛ぶことができます");
		message(sender, null, "&bもう一度&a/flymode fly&bコマンドで支払いを行い続行します");
		message(sender, null, "&f飛行が可能なプレイヤーはTabリストのプレイヤー名が黄色で表示されます");
		message(sender, null, "&a/flymode list &fコマンドで現在使用中のプレイヤーを見ることができます");
		message(sender, null, "&c【注意】&f・サーバ/プレイヤーのオフラインに関係なく時間は経過します");
		message(sender, null, "　　　　&f・期限満了後、通常の落下ダメージを受けることがあります");
		message(sender, null, "　　　　&f・Y座標0～257より外には移動できません");
		message(sender, null, "　　　　&f・new, tropicワールド以外では無効になります");
		message(sender, null, "&c===================================");
	}

	/****************************************/
	// ユーティリティ
	/****************************************/
	/**
	 * 文字配列をまとめる
	 * @param s つなげるString配列
	 * @param glue 区切り文字 通常は半角スペース
	 * @return
	 */
	public static String combine(String[] s, String glue)
    {
      int k = s.length;
      if (k == 0){ return null; }
      StringBuilder out = new StringBuilder();
      out.append(s[0]);
      for (int x = 1; x < k; x++){
        out.append(glue).append(s[x]);
      }
      return out.toString();
    }
	/**
	 * コマンドをコンソールから実行する
	 * @param command
	 */
	public static void executeCommandOnConsole(String command){
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
	}
	/**
	 * 真上のブロックを操作してポータルブロックなら削除
	 * @param block
	 */
	public static void removeUpPortalBlock(Block block){
		Block relative = block.getRelative(BlockFace.UP, 1);

		if(relative.getType()==Material.PORTAL){
			// 真上のブロックがポータルブロックなら、更にその上のブロックもチェックしてポータルなら削除
			removeUpPortalBlock(relative);
		}
		if(block.getType()==Material.PORTAL){
			block.setType(Material.AIR);
		}
	}

	/**
	 * 文字列の中に全角文字が含まれているか判定
	 * @param s 判定する文字列
	 * @return 1文字でも全角文字が含まれていればtrue 含まれていなければfalse
	 * @throws UnsupportedEncodingException
	 */
	public static boolean containsZen(String s)
			throws UnsupportedEncodingException {
		for (int i = 0; i < s.length(); i++) {
			String s1 = s.substring(i, i + 1);
			if (URLEncoder.encode(s1,"MS932").length() >= 4) {
				return true;
			}
		}
		return false;
	}

	/**
	 * パスワード文字列が有効かどうかチェックする
	 * @param pass チェックするパスワード
	 * @return 正常終了ならnull エラーならエラーメッセージ
	 */
	public static String isPasswordValid(String pass){
		if (pass.length() < 6){
			return "パスワードが短すぎます！6文字以上で入力してください！";
		}
		if (pass.length() > 20){
			return "パスワードが長すぎます！20文字以下で入力してください！";
		}
		try {
			if (containsZen(pass)){
				return "パスワードに全角文字が含まれています！";
			}
		} catch (UnsupportedEncodingException e) {
			return "内部エラー: "+e.getMessage();
		}

		return null;
	}

	/**
	 * 現在の日時を yyyy-MM-dd HH:mm:ss 形式の文字列で返す
	 * @return
	 */
	public static String getDatetime(){

		Date date = new Date();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return df.format(date);
	}

	/**
	 * 音ブロックをならす
	 * @param playerName 対象のプレイヤー名
	 * @param notes ならす音
	 * @param delay 遅延
	 */
	public static void playNote(String playerName, List<Note> notes, long delay){
		OfflinePlayer offlinePlayer = Bukkit.getServer().getOfflinePlayer(playerName);
		if (offlinePlayer != null && offlinePlayer.isOnline()) {
			playNote((Player) offlinePlayer, notes, delay);
		}
	}
	public static void playNote(Player player, List<Note> notes, long delay){
		if (player != null && player.isOnline()) {
			// 偽の音ブロックをプレイヤーの2ブロック下で鳴らす
			Location loc = player.getLocation().clone().add(0, -2, 0);
			NoteAlert alert = new NoteAlert(player, loc, delay, notes);
			alert.start();
		}
	}

	/**
	 * 座標データを ワールド名:x, y, z の形式の文字列にして返す
	 * @param loc
	 * @return
	 */
	public static String returnLocationString(Location loc){
		return loc.getWorld().getName()+":"+loc.getX()+","+loc.getY()+","+loc.getZ();
	}
	public static String returnBlockLocationString(Location loc){
		return loc.getWorld().getName()+":"+loc.getBlockX()+","+loc.getBlockY()+","+loc.getBlockZ();
	}

	/**
	 * 指定した個数分アイテムを減らして返す
	 * @param itemStack 対象のItemStackオブジェクト
	 * @param amount 減らす個数が 0 以下なら何もせずに返す
	 * @return 減らした後のItemStackオブジェクト もし0なら new ItemStack(Material.AIR) を返す
	 */
	public static ItemStack decrementItem(ItemStack itemStack, int amount){
		// 減らすアイテム個数が0以下
		if (amount <= 0){
			return itemStack;
		}

		// 今のアイテム個数
		int nowAmount = itemStack.getAmount();

		// 減らした後のアイテムの個数が0以下
		if ((nowAmount - amount) <= 0){
			return (new ItemStack(Material.AIR));
		}

		// 通常処理
		itemStack.setAmount(nowAmount - amount);
		return itemStack;
	}

	/****************************************/
	/* ポーション系 */
	/****************************************/
	/**
	 * ポーション効果購入
	 * @param player プレイヤー名
	 * @param potionName ポーション名
	 * @return 支払い成功ならtrue, 失敗ならプレイヤーに通知後false
	 */
	public static double potionPurchase(Player player, String potionName){
		double cost = potionMap.get(potionName);
		if (!checkMoney(player.getName(), cost)){
			message(null, player, "&cお金が足りません！ " + (int)cost + "Coinが必要です！");
			return -1;
		}
		if (!takeMoney(player.getName(), cost)){
			message(null, player, "&c支払いにエラーが発生しました");
			return -1;
		}
		return cost;
	}
	/**
	 * ポーション効果付与
	 * @param player 対象Player
	 * @param cost 支払ったお金
	 * @param potion 対象のPotionEffectType
	 * @param duration 長さ(ticks:1sec=20ticks)
	 * @param amplifier レベル(？)
	 */
	public static void addPotionEffect(Player player, PotionEffectType potion, int duration, int amplifier){
		player.addPotionEffect(new PotionEffect(potion, duration, amplifier));
	}
	/**
	 * ポーション名が有効かどうかをチェック
	 * @param potion ポーション名
	 * @return 有効ならPotionEffectType 無効ならnull
	 */
	public static PotionEffectType validPotion(String potion){
		if (potion.equalsIgnoreCase("jump"))
			return PotionEffectType.JUMP;
		if (potion.equalsIgnoreCase("speed"))
			return PotionEffectType.SPEED;
		if (potion.equalsIgnoreCase("poison"))
			return PotionEffectType.POISON;
		return null;
	}

	/****************************************/
	/* 飛行系 */
	/****************************************/
	/**
	 * ログイン時に自動で飛行状態にする
	 * @param player プレイヤー
	 */
	static void joinAutoFlyPlayer(Player player){
		if(!SakuraServer.flyingPlayerList.contains(player.getName())){
			return;
		}

		if (player.getWorld() == Bukkit.getWorld("new") ||
				player.getWorld() == Bukkit.getWorld("tropic")){
			player.setAllowFlight(true);
		}
		Actions.message(null, player, msgPrefix+"&aあなたは飛行モードの有効時間が残っています！");

		// ヘルパー以上ならTab色は変更しない
		if(!player.hasPermission("sakuraserver.helper")){
			Actions.changeTabColor(player.getName(), "YELLOW");
		}
	}
	/**
	 * プレイヤーを飛行状態にする
	 * @param player プレイヤー
	 * @param minute 飛行状態にする時間(分)
	 */
	public static void flyPlayer(final Player player, final int minute){
		// 終了時間を記録
		Date limitDate = DateUtils.addMinutes(new Date(), minute);
		if(!SakuraServer.playerData.containsKey(player)){
			SakuraPlayer sakuraPlayer = new SakuraPlayer(player.getName());
			SakuraServer.playerData.put(player, sakuraPlayer);
		}
		SakuraServer.playerData.get(player).setFlyLimitDate(limitDate);
		SakuraServer.playerData.get(player).save();

		// 権限を与えてflyモードにする
		SakuraServer.flyingPlayerList.add(player.getName());
		player.setAllowFlight(true);

		// ヘルパー以上ならTab色は変更しない
		if(!player.hasPermission("sakuraserver.helper")){
			Actions.changeTabColor(player.getName(), "YELLOW");
		}
	}
	/**
	 * オンラインプレイヤーの飛行状態を解除する
	 * @param player プレイヤー
	 */
	public static void expireFlyPlayer(Player player){
		// クリエイティブプレイヤーは無視
		if(player.getGameMode() == GameMode.CREATIVE){
			Actions.changeTabNameDefault(player.getName());
			return;
		}
		// 解除
		player.setAllowFlight(false);

		// 10秒無敵に (256ブロック真下落下で7秒程度)
		player.setNoDamageTicks(10 * 20);

		if(player.isFlying()){
			player.setFlying(false);
		}
		// Tabリストを戻す
		Actions.changeTabNameDefault(player.getName());
	}
	/**
	 * プレイヤーの飛行時間期限切れチェック
	 */
	static void checkExpireFlyPlayer(){
		for (int i = 0; i < SakuraServer.flyingPlayerList.size(); i++) {
			String name = SakuraServer.flyingPlayerList.get(i);

			if(SakuraServer.playerData.containsKey(Bukkit.getOfflinePlayer(name))){
				// オンラインのとき → 比較してリストから外し、安全に飛行状態を解除する
				if(SakuraServer.playerData.get(Bukkit.getOfflinePlayer(name)).getFlyLimitDate().before(new Date())){
					// リストから外す
					SakuraServer.flyingPlayerList.remove(name);

					Player player = Bukkit.getServer().getPlayer(name);
					if(player != null && player.isOnline()){

						Actions.expireFlyPlayer(player);

						// Tabリストを戻す
						Actions.changeTabNameDefault(player.getName());

						Actions.message(null, player, msgPrefix+"飛行可能時間が終了しました。");
						Actions.permcastMessage("sakuraserver.helper", "&c[通知] &6"+player.getName()+" &fの飛行可能時間が終了しました。");
						log.info(logPrefix+"Online player "+ name + " is expired flying mode!");
					}
					String loc = player.getWorld().getName()+":"+player.getLocation().getX()+","+player.getLocation().getY()+","+player.getLocation().getZ();
					Actions.log("FlyMode.log", "Player " + name + " is expired flying mode at " + loc);
				}else{
					// オンラインかつまだ飛行時間が残っている 60秒以内なら通知
					Player player = Bukkit.getServer().getPlayer(name);
					if(player != null && player.isOnline()){
						long limit = SakuraServer.playerData.get(Bukkit.getOfflinePlayer(name)).getFlyLimitDate().getTime();
						long ansSeconds = (limit - new Date().getTime()) / (1000);
						if (ansSeconds <= 60){
							Actions.message(null, player, msgPrefix+"飛行可能時間が残り"+ansSeconds+"秒です。");
						}
					}
				}
			}else{
				// オフラインのとき → 比較してリストから外す
				SakuraPlayer sakuraPlayer = new SakuraPlayer(name);
				if (sakuraPlayer.getFlyLimitDate().before(new Date())){
					SakuraServer.flyingPlayerList.remove(name);
					log.info(logPrefix+"Offline player "+ name + " is expired flying mode!");
					Actions.log("FlyMode.log", "Player " + name + " is expired flying mode on Offline!");
					Actions.permcastMessage("sakuraserver.helper", "&c[通知] &6"+name+" &fの飛行可能時間がオフライン時に終了しました。");
				}
			}
		}
	}
	/**
	 * プラグインがロードされた時に以前の飛行モードプレイヤーをリストア
	 */
	static void restoreFlyPlayer(){
		int size = SakuraServer.flyingPlayerList.size();
		if (size==0){
			return; // 復元対象プレイヤーナシ
		}else{
			for (int i = 0; i < SakuraServer.flyingPlayerList.size(); i++) {
				String playerName = SakuraServer.flyingPlayerList.get(i);
				// プレイヤーがオンライン
				Player player = Bukkit.getServer().getPlayer(playerName);
				if ((player != null) && (player.isOnline())){
					// ログイン状態にさせるためにプレイヤーデータマッピング
					SakuraPlayer sakuraPlayer = new SakuraPlayer(player.getName());
					SakuraServer.playerData.put(player, sakuraPlayer);

					if (player.getWorld() == Bukkit.getWorld("new") ||
							player.getWorld() == Bukkit.getWorld("tropic")){
						player.setAllowFlight(true);
					}
					Actions.message(null, player, msgPrefix+"プラグインがロードされ飛行モードが再開されました");
				}
			}
		}
	}

	/****************************************/
	/* ログ操作系 */
	/****************************************/
	/**
	 * ログファイルに書き込み
	 * @param file ログファイル名
	 * @param line ログ内容
	 */
	public static void log(String file, String line){
		String filename = SakuraServer.logDir + System.getProperty("file.separator") + file;
		TextFileHandler r = new TextFileHandler(filename);
		try{
			r.appendLine("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "] " + line);
		} catch (IOException ex) {}
	}

	/****************************************/
	/* 看板操作系 */
	/****************************************/
	/**
	 * 指定した看板を編集します
	 * @param sign Signオブジェクト
	 * @param line 行番号(0～3)
	 * @param text テキスト
	 */
	static void editSign(Sign sign, int line, String text){
		if (line < 0 || line > 3) {
			return;
		}
		sign.setLine(line, text);
		// ここでロギングする？
		// String loc =  ((Block) sign).getWorld().getName()+":"+((Block) sign).getX()+","+((Block) sign).getY()+","+((Block) sign).getZ();
	}

	/****************************************/
	/* Tabリスト系 */
	/****************************************/
	// TabListの色を変更
	public static void changeTabColor(String name, String color){
		Player player = Bukkit.getServer().getPlayerExact(name);
		if (player != null){
			String newName = ChatColor.valueOf(color.toUpperCase()) + player.getDisplayName();
			// 名前が16文字を超えていれば省略する
			if (newName.length() > 16){
				newName = newName.substring(0, 12) + ChatColor.WHITE + "..";
			}
			player.setPlayerListName(newName);
		}
	}
	// TabListの名前を指定した色と名前に変更
	public static void changeTabName(String name, String newname, String color){
		Player player = Bukkit.getServer().getPlayerExact(name);
		if (player != null){
			String newName = ChatColor.valueOf(color.toUpperCase()) + newname;
			// 名前が16文字を超えていれば省略する
			if (newName.length() > 16){
				newName = newName.substring(0, 12) + ChatColor.WHITE + "..";
			}
			player.setPlayerListName(newName);
			message(null, player, ChatColor.GREEN + "あなたの名前は " + newName + ChatColor.GREEN + " に変更されました！");
		}
	}
	// スタッフメンバー、ビジターの色をログイン時に自動変更
	static void changeTabNameDefault(String name){
		Player player = Bukkit.getServer().getPlayerExact(name);
		String prefix = null;

		// スタッフメンバーは名前の色を変更
		if (player.hasPermission("sakuraserver.admin")){
			prefix = ChatColor.RED.toString();
		}else if (player.hasPermission("sakuraserver.mod")){
			prefix = ChatColor.LIGHT_PURPLE.toString();
		}else if (player.hasPermission("sakuraserver.helper")){
			prefix = ChatColor.AQUA.toString();
		}else if (player.hasPermission("sakuraserver.vip")){
			// VIPは何もしない
		}else if (player.hasPermission("sakuraserver.citizen")){
			// Citizenは何もしない
		}else if (player.hasPermission("sakuraserver.visitor")){
			prefix = ChatColor.GRAY.toString();
		}
		String newName;

		if (prefix != null){
			newName = prefix + player.getDisplayName();
			//名前が16文字を超えていれば省略
			if (newName.length() > 16){
				newName = newName.substring(0, 12) + ChatColor.WHITE + "..";
			}
		}else{
			newName = player.getDisplayName();
		}
		player.setPlayerListName(newName);
	}
	/**
	 * 色の名前が有効かどうかをチェック
	 * @param color カラー名
	 * @return 有効ならtrue 無効ならfalse
	 */
	public static boolean validColor(String color){
		for (ChatColor value : ChatColor.values()){
			if(color.equalsIgnoreCase(value.name().toLowerCase())){
				return true;
			}
		}
		return false;
	}

	/****************************************/
	/* 資源ワールドチェック */
	/****************************************/
	// ログイン時に資源ワールドが新しいものかチェック
	static void checkResourceWorld(Player player){
		// 資源ワールドが存在しなければ(ちょうど更新中なら)メッセージだけ表示して何もしない
		if (Bukkit.getWorld("resource") == null){
			Actions.message(null, player,msgPrefix+"現在資源ワールドはリセット作業中です！");
			return;
		}

		// [WARNING] Task of 'SakuraServer' generated an exception 対策 6/2
		if (player == null || !player.isOnline()){
			return;
		}
		long currentResourceSeed = Bukkit.getWorld("resource").getSeed();
		long playerResourceSeed = SakuraServer.playerData.get(player).getResourceSeed();

		if (player.getWorld() == Bukkit.getWorld("resource")){
			if (playerResourceSeed != currentResourceSeed){
				// プレイヤーが持つ資源のシード値と現行のシード値が違えばワールドが新しくなったのでテレポート
				player.teleport(Bukkit.getServer().getWorlds().get(0).getSpawnLocation());
				Actions.message(null, player, msgPrefix+"資源ワールドリセット前に資源ワールドでログアウトしていたため、メインワールドへテレポートされました");
			}
		}

		// 現行資源ワールドバージョンを設定
		SakuraServer.playerData.get(player).setResourceSeed(currentResourceSeed);
	}

}
