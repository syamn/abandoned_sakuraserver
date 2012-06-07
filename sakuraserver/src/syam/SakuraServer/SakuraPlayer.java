package syam.SakuraServer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class SakuraPlayer {
	public final static Logger log = SakuraServer.log;
	private static final String logPrefix = SakuraServer.logPrefix;
	private static final String DATE_PATTERN = "yyyy/MM/dd HH:mm:ss";

	private boolean saved = true;
	private FileConfiguration configurationFile = new YamlConfiguration();
	private File file;

	private String playerName = null;


	/* プレイヤー固有のプラグインが持つデータ */
	/**
	 * 最後に殺された人の名前
	 */
	private String lastKillerName = null;
	/**
	 * 最後にクリックした岩盤ブロックの座標 (未実装)
	 */
	private Location bedrockLocation = null;
	/**
	 * Flyコマンドを一度入力したかどうか ファイルへの保存は行わない
	 */
	private boolean checkedFlyCommand = false;
	/**
	 * Flyモードの終了日時
	 */
	private Date flyLimitDate = null;
	/**
	 * 最後にログアウトしたときの資源ワールドのシード値
	 */
	private long resourceSeed = 0;

	public SakuraPlayer(String name){
		this.playerName = name;
		String filename = SakuraServer.getInstance().getDataFolder() + System.getProperty("file.separator") +
							"userData" + System.getProperty("file.separator") + name + ".yml";
		this.file = new File(filename);

		load();
	}

	/**
	 * 設定ファイルからプレイヤーデータを読み込む
	 * @return 成功でtrue
	 */
	public boolean load(){
		if (!file.exists()){
			if (!file.getParentFile().exists()){
				file.getParentFile().mkdir();
			}
			saved = false;
			if (!save()){
				throw new IllegalArgumentException("新しいプレイヤーデータを作れませんでした");
			}
		}

		try{
			configurationFile.load(file);

			// 読むデータキーをここに
			lastKillerName = configurationFile.getString("lastKillerName", null);
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
			if (configurationFile.getString("flyLimitDate") != null){
				flyLimitDate = sdf.parse(configurationFile.getString("flyLimitDate"));
			}
			resourceSeed = configurationFile.getLong("resourceSeed", 0);

		}catch (Exception e){
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * 設定ファイルにデータを保存する
	 * @return 成功でtrue
	 */
	public boolean save(){
		if (!saved){
			try{
				// 保存するデータをここに
				configurationFile.set("lastKillerName", lastKillerName);

				if (flyLimitDate != null){
					SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
					String flyLimitDateString = sdf.format(flyLimitDate);
					configurationFile.set("flyLimitDate", flyLimitDateString);
				}

				configurationFile.set("resourceSeed", resourceSeed);

				// 保存
				configurationFile.save(file);
			}catch (Exception e){
				return false;
			}
		}
		return true;
	}


	//
	// ************ 以下 setter/getter ************
	//

	public boolean isSaved(){
		return saved;
	}
	public String getPlayerName(){
		return playerName;
	}

	public void setLastKillerName(String killerName){
		saved = false;
		lastKillerName = killerName;
	}
	public String getLastKillerName(){
		return lastKillerName;
	}

	public void setBedrockLocation(Location location){
		saved = false;
		bedrockLocation = location;
	}
	public Location getBedrockLocation(){
		return bedrockLocation;
	}

	public void setCheckedFlyCommand(boolean bool){
		checkedFlyCommand = bool;
	}
	public boolean getCheckedFlyCommand(){
		return checkedFlyCommand;
	}

	public void setFlyLimitDate(Date limitDate){
		saved = false;
		flyLimitDate = limitDate;
	}
	public void setFlyLimitDate(String limitDate){
		saved = false;
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
		try {
			flyLimitDate = sdf.parse(limitDate);
		} catch (ParseException e) {
			log.info(logPrefix+"Can not parse String to Date:");
			e.printStackTrace();
		}
	}
	public Date getFlyLimitDate(){
		return flyLimitDate;
	}

	public void setResourceSeed(long version){
		saved = false;
		resourceSeed = version;
	}
	public long getResourceSeed(){
		return resourceSeed;
	}
}
