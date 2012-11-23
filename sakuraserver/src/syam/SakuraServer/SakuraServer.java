package syam.SakuraServer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.swing.Timer;

import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import syam.SakuraServer.commands.BaseCommand;
import syam.SakuraServer.commands.CommandAdmin;
import syam.SakuraServer.commands.CommandColors;
import syam.SakuraServer.commands.CommandFlymode;
import syam.SakuraServer.commands.CommandJailapply;
import syam.SakuraServer.commands.CommandMfmf;
import syam.SakuraServer.commands.CommandPassword;
import syam.SakuraServer.commands.CommandPot;
import syam.SakuraServer.commands.CommandRegister;
import syam.SakuraServer.commands.CommandSakura;
import syam.SakuraServer.commands.CommandSyamn;
import syam.SakuraServer.commands.SakuraCommandHandler;
import syam.SakuraServer.listener.SakuraCreativeListener;
import syam.SakuraServer.listener.SakuraInventoryListener;
import syam.SakuraServer.listener.SakuraBlockListener;
import syam.SakuraServer.listener.SakuraEndListener;
import syam.SakuraServer.listener.SakuraEntityListener;
import syam.SakuraServer.listener.SakuraPlayerListener;
import syam.util.Actions;

public class SakuraServer extends JavaPlugin{
	public final static Logger log = Logger.getLogger("Minecraft");
	public final static String logPrefix = "[SakuraServer] ";
	public final static String msgPrefix = "&c[SakuraServer] &f";

	public static String logDir;

	private final SakuraPlayerListener playerListener = new SakuraPlayerListener(this);
	private final SakuraBlockListener blockListener = new SakuraBlockListener(this);
	private final SakuraEntityListener entityListener = new SakuraEntityListener(this);
	private final SakuraEndListener endListener = new SakuraEndListener(this);
	private final SakuraInventoryListener invListener = new SakuraInventoryListener(this);
	private final SakuraCreativeListener creativeListener = new SakuraCreativeListener(this);

	private static SakuraServer instance;

	public static void setLogPath(String logDir){
		SakuraServer.logDir = logDir;
	}

	//タイマー
	private Timer timer;
	private int UpdateTime = 0;

	// データベース
	public static SakuraMySqlManager dbm;

	// コマンド
	private SakuraCommandHandler commandHandler = null;
	private Map<String, BaseCommand> commands = new HashMap<String, BaseCommand>();

	// 設定関係
	public static ConcurrentHashMap<String, Double> potionMap = new ConcurrentHashMap<String,Double>();
	public static ConcurrentHashMap<String, Boolean> configBooleanMap = new ConcurrentHashMap<String,Boolean>();
	public static ConcurrentHashMap<String, Integer> configIntegerMap = new ConcurrentHashMap<String,Integer>();

	// ユーザーごとの設定
	public static ConcurrentHashMap<Player, Location> bedrockConfig = new ConcurrentHashMap<Player, Location>();
	public static ConcurrentHashMap<Player, SakuraPlayer> playerData = new ConcurrentHashMap<Player, SakuraPlayer>();

	// 飛行モードが有効なプレイヤーリスト
	public static ArrayList<String> flyingPlayerList = new ArrayList<String>();

	// FakeJoined タブリストプレイヤーリスト
	public static ArrayList<String> fakeJoinedPlayerList = new ArrayList<String>();

	// FakeJoined タブリストプレイヤーリスト
	public static ArrayList<String> bedTiedPlayerList = new ArrayList<String>();

	/*
	public static ConcurrentHashMap<String, ConcurrentHashMap<Integer, Double>> potionMap2 = new ConcurrentHashMap<String, ConcurrentHashMap<Integer, Double>>();
	public static ConcurrentHashMap<Integer, Double> jumpPotionMap = new ConcurrentHashMap<Integer, Double>();
	public static ConcurrentHashMap<Integer, Double> speedPotionMap = new ConcurrentHashMap<Integer, Double>();
	public static ConcurrentHashMap<Integer, Double> poisonPotionMap = new ConcurrentHashMap<Integer, Double>();
	 */

	@Override
	public void onDisable(){
		// タイマーストップ
		timer.stop();

		// アンロード時に保存されていないプレイヤーデータは保存する
		for (SakuraPlayer sp : playerData.values()){
			// if (!sp.isSaved()) // save関数内でチェックを行うため不要
			sp.save();
		}

		if(!flyingPlayerList.isEmpty()){
			this.getConfig().set("FlyingPlayers", flyingPlayerList);
		}else{
			this.getConfig().set("FlyingPlayers", null);
		}
		this.saveConfig();

		// 飛行可能ユーザーパーミッションを削除
		log.info(logPrefix+flyingPlayerList.size()+" flying players found! Removing permission.");
		for (int i = 0; i < flyingPlayerList.size(); i++) {
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(flyingPlayerList.get(i));
			if(offlinePlayer != null && offlinePlayer.isOnline()){
				Player player = offlinePlayer.getPlayer();
				// オンラインなら飛行状態を解除
				Actions.expireFlyPlayer(player);
				Actions.message(null, player, msgPrefix+"プラグインがアンロードされるため飛行状態が解除されました");
				Actions.log("FlyMode.log", "Player " + player.getName() + " is disabled flying mode because unloading plugin.");
			}else{
				Actions.log("FlyMode.log", "(Offline)Player " + offlinePlayer.getName() + " is disabled flying mode because unloading plugin.");
			}
		}
		log.info(logPrefix+"Removed flying permissions!");

		PluginDescriptionFile pdfFile=this.getDescription();
		log.info("SakuraServerPlugin ["+pdfFile.getName()+"] version "+pdfFile.getVersion()+" is disabled!");
	}

	@Override
	public void onEnable(){
		instance = this;

		logDir = getDataFolder() + System.getProperty("file.separator") + "log";

		loadConfig();
		if (!configBooleanMap.get("Enable")){
			log.severe(logPrefix + "プラグインは設定ファイルで無効になっています！");
			Bukkit.getServer().getPluginManager().disablePlugin(this);
		}

		// データベース連携
		setupDatabase();

		// Vaultにフックする
		Plugin plugin = this.getServer().getPluginManager().getPlugin("Vault");
		if(plugin != null & plugin instanceof Vault) {
			RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
			if(economyProvider==null){
				log.warning(logPrefix+"Economy plugin not Fount. Disabling plugin.");
				getPluginLoader().disablePlugin(this);
				return;
			}
			// Vaultと経済概念にフックする
			if(!Actions.hookVault(plugin, economyProvider)){ // Hook
				log.warning(logPrefix+"Could NOT be hook to Vault. Disabling plugin.");
				getPluginLoader().disablePlugin(this);
				return;
			}
			log.info(logPrefix+"Hooked to Vault!");

			if (economyProvider != null){

			}
		} else {
			log.warning(logPrefix+"Vault was NOT found! Disabling plugin.");
			getPluginLoader().disablePlugin(this);
			return;
		}

		// イベントを登録
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(blockListener, this);
		pm.registerEvents(playerListener, this);
		pm.registerEvents(entityListener, this);
		pm.registerEvents(endListener, this);
		pm.registerEvents(invListener, this);
		pm.registerEvents(creativeListener, this);

		// コマンドを登録
		//SakuraCommandRegister.registerCommands();
		commandHandler = new SakuraCommandHandler(this);
		registerCommands();

		// タイマースタート
		timer = new Timer(1000, action);
		timer.start();

		// 飛行ユーザーを復元
		Actions.restoreFlyPlayer();

		// メッセージ表示
		PluginDescriptionFile pdfFile=this.getDescription();
		log.info("SakuraServerPlugin ["+pdfFile.getName()+"] version "+pdfFile.getVersion()+" is enabled!");
	}

	ActionListener action = new ActionListener(){
		@Override
		public void actionPerformed (ActionEvent event){
			if (UpdateTime == 0){
				// 10秒ごとに呼ばれる
				getServer().getScheduler().scheduleSyncDelayedTask(SakuraServer.instance, new Runnable() {
					@Override
					public void run() {
						// 重い処理はタイマー内の別タスク内で行う
						Actions.checkExpireFlyPlayer();
					}
				}, 1L);

				UpdateTime++;
			}else{
				// 10秒ごとのタイマー
				UpdateTime++;
				if (UpdateTime > 9){
					UpdateTime = 0;
				}
			}
			// 1秒ごとに呼ばれる
			// Do stuff..
		}
	};

	private void registerCommands(){
		// general
		commandHandler.registerCommand(new CommandFlymode());
		commandHandler.registerCommand(new CommandPot());
		commandHandler.registerCommand(new CommandJailapply());

		// web
		commandHandler.registerCommand(new CommandPassword());
		commandHandler.registerCommand(new CommandRegister());

		// multi
		commandHandler.registerCommand(new CommandSakura());
		commandHandler.registerCommand(new CommandAdmin());

		// misc
		commandHandler.registerCommand(new CommandMfmf());
		commandHandler.registerCommand(new CommandColors());
		commandHandler.registerCommand(new CommandSyamn());
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args){
		return commandHandler.onCommand(sender, command, label, args);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args){
		return commandHandler.onTabComplete(sender, command, alias, args);
	}

	/**
	 * データベースのセットアップ
	 */
	private void setupDatabase(){
		dbm = new SakuraMySqlManager();

		if(!dbm.init()){
			log.severe(logPrefix+"データベースの接続を初期化できませんでした");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
	}

	/**
	 * 設定ファイルを読み込みます
	 */
	public void loadConfig()
	{
		//設定ファイル読み込み
		String filename = getDataFolder() + System.getProperty("file.separator") + "config.yml";
		File file = new File(filename);

		if (!file.exists()){
			if (!newConfig(file)){
				throw new IllegalArgumentException("新しい設定ファイルを作れませんでした");
			}
		}
		reloadConfig();

		// Boolean型の設定をマップ (true / false)
		configBooleanMap.put("Enable", getConfig().getBoolean("Enable", true));
		configBooleanMap.put("BroadcastOnMobDeath", getConfig().getBoolean("BroadcastOnMobDeath", false));
		configBooleanMap.put("CancelOnZombieBreakDoorEvent", getConfig().getBoolean("CancelOnZombieBreakDoorEvent", true));
		configBooleanMap.put("CancelUnloadDragonChunk", getConfig().getBoolean("CancelUnloadDragonChunk", true));

		// Integer型の設定をマップ
		configIntegerMap.put("HealSignCost", getConfig().getInt("HealSignCost", 10000));
		configIntegerMap.put("FlyModeTimeOfMinute", getConfig().getInt("FlyModeTimeOfMinute", 30));
		configIntegerMap.put("FlyModeCost", getConfig().getInt("FlyModeCost", 5000));
		configIntegerMap.put("FlyModePlayersAtOneTime", getConfig().getInt("FlyModePlayersAtOneTime", 10));

		// FlyingPlayerListをマップ
		if ((flyingPlayerList.size() == 0) && (getConfig().getList("FlyingPlayers") != null)){
			for (int i = 0, n = getConfig().getList("FlyingPlayers").size(); i < n; i++){
				flyingPlayerList.add(getConfig().getList("FlyingPlayers").get(i).toString());
			}
		}

		/* ポーション関係の設定をマップ */
		MemorySection potionSection = (MemorySection)getConfig().get("Potions");

		for (String potionString: potionSection.getKeys(false)){
			Double potionCost = getConfig().getDouble("Potions."+potionString+".Cost");
			potionMap.put(potionString, potionCost);

			// 以下失敗作
			/*
			MemorySection potionSection2 = (MemorySection)getConfig().get("Potions."+potionString);
			for (String num: potionSection2.getKeys(false)){
				int potionAmplifier = getConfig().getInt("Potions."+potionString+"."+num+".Amplifier");
				Double potionCost = getConfig().getDouble("Potions."+potionString+"."+num+".Cost");

				if (potionString=="JUMP"){
					jumpPotionMap.put(potionAmplifier, potionCost);
				}else if (potionString=="SPEED"){
					speedPotionMap.put(potionAmplifier, potionCost);
				}else if (potionString=="POISON"){
					poisonPotionMap.put(potionAmplifier, potionCost);
				}
				log.info(potionString+" / "+potionAmplifier+" / "+potionCost);
			}
			 */
		}
	}


	/**
	 * 設定ファイルが無い場合は作ります
	 * @param file
	 * @return
	 */
	private boolean newConfig(File file){
		FileWriter fileWriter;
		if (!file.getParentFile().exists()){
			file.getParentFile().mkdir();
		}

		try{
			fileWriter =new FileWriter(file);
		}catch (IOException e){
			log.severe("設定ファイルを書けませんでした:"+e.getMessage());
			Bukkit.getServer().getPluginManager().disablePlugin(this);
			return false;
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(getResource("config.yml"))));
		BufferedWriter writer = new BufferedWriter(fileWriter);
		try{
			String line = reader.readLine();
			while (line != null){
				writer.write(line + System.getProperty("line.separator"));
				line = reader.readLine();
			}
			log.info("デフォルトの設定ファイルを作成しました");
		}catch (IOException e){
			log.severe("設定ファイルを書けませんでした:"+e.getMessage());
		}finally{
			try{
				writer.close();
				reader.close();
			}catch (IOException e){
				log.severe("設定ファイル保存中にエラーが発生しました:"+e.getMessage());
				Bukkit.getServer().getPluginManager().disablePlugin(this);
			}
		}
		return true;
	}


	/**
	 * シングルトンパターンでない/ プラグインがアンロードされたイベントでnullを返す
	 * @return シングルトンインスタンス 何もない場合はnull
	 */
	public static SakuraServer getInstance() {
		return instance;
	}
}