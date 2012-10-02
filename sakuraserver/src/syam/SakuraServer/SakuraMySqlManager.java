package syam.SakuraServer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.logging.Logger;

public class SakuraMySqlManager {
	public final static Logger log = SakuraServer.log;
	private static final String logPrefix = SakuraServer.logPrefix;
	private static final String msgPrefix = SakuraServer.msgPrefix;

	/* MySQL環境設定 */
	private final static String mySqlServer = SakuraSecurity.mySqlServer;
	private final static String mySqlUser = SakuraSecurity.mySqlUser;
	private final static String mySqlPass = SakuraSecurity.mySqlPass;
	private final static String mySqlServerVPS = SakuraSecurity.mySqlServerVPS;
	private final static String mySqlUserVPS = SakuraSecurity.mySqlUserVPS;
	private final static String mySqlPassVPS = SakuraSecurity.mySqlPassVPS;
	public static String database = "minecraft"; // カレントデータベース
	public final static String db_minecraft = "minecraft";
	public final static String db_web = "sakura_web";
	public final static String table_Log = "sakura_log";
	public final static String table_userdata = "user_data";
	/* クエリ */
	private static String sql_CreateTable1 = "CREATE TABLE IF NOT EXISTS `"+table_Log+"` (" +
				"`data_id` int(11) NOT NULL auto_increment, " +
				"`date` datetime NOT NULL, " +
				"`player_name` varchar(32) default NULL, " +
				"`action` int(11) NOT NULL, " +
				"`world_name` varchar(255), " +
				"`x` double default NULL, " +
				"`y` double default NULL, " +
				"`z` double default NULL, " +
				"`data` varchar(500) default NULL, " +
				"PRIMARY KEY (`data_id`))";
	private static String sql_CreateTable2 = "CREATE TABLE IF NOT EXISTS `"+table_userdata+"` (" +
			"`player_id` int(8) NOT NULL auto_increment, " +
			"`player_name` varchar(32) NOT NULL, " +
			"`password` varchar(256) NOT NULL, " +
			"`nickname` varchar(32) default NULL, " +
			"`email` varchar(100) default NULL, " +
			"`twitter` varchar(100) default NULL, " +
			"`user_level` int(2) NOT NULL default 0, " +
			"`status` int(5) NOT NULL default 0, " +
			"`updatedate` datetime default NULL, " +
			"`regdate` datetime NOT NULL, " +
			"`lastlogindate` datetime default NULL, " +
			"`autoLoginKey` varchar(256) default NULL, " +
			"PRIMARY KEY  (`player_id`), " +
			"UNIQUE KEY `player_name` (`player_name`))";

	private static String sql_InsertLogTable = "INSERT INTO " +table_Log+ " " +
				"(`date`, `player_name`, `action`, `world_name`, `x`, `y`, `z`, `data`) " +
				"VALUES " +
				"(?, ?, ?, ?, ?, ?, ?, ?)";

	private static String sql_InsertAuthTable = "INSERT INTO " +table_userdata+ " " +
				"(`player_name`, `password`, `email`, `updatedate`, `regdate`) " +
				"VALUES " +
				"(?, ?, ?, ?, ?)";
	private static String sql_ChangePassword = "UPDATE " +table_userdata+ " SET " +
			"`password` = ?, " +
			"`updatedate` = ? " +
			"WHERE `player_name` = ?";

	/**
	 * 初期化
	 * @return 正常終了でtrue 例外でfalse
	 */
	public boolean init(){
		try {
			// JDBCドライバのロード
			Class.forName("com.mysql.jdbc.Driver");
		}catch (Exception ex) {}

		return createTable();
	}

	/**
	 * DBに接続するためのConnectionオブジェクトを取得する
	 * @return Connectionオブジェクト
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException{
		Connection conn = null;
		try{
			conn = DriverManager.getConnection(mySqlServer + database, mySqlUser, mySqlPass);
		}catch(Exception e){
			log.severe(logPrefix+"MySQL connect error: "+e.getMessage());
		}
		checkConnection(conn);
		return conn;
	}
	public Connection getConnection(String dbname) throws SQLException{
		Connection conn = null;
		try{
			conn = DriverManager.getConnection(mySqlServer + dbname, mySqlUser, mySqlPass);
		}catch(Exception e){
			log.severe(logPrefix+"MySQL connect error: "+e.getMessage());
		}
		checkConnection(conn);
		return conn;
	}
	public Connection getVPSConnection() throws SQLException{
		Connection conn = null;
		try{
			conn = DriverManager.getConnection(mySqlServerVPS + database, mySqlUserVPS, mySqlPassVPS);
		}catch(Exception e){
			log.severe(logPrefix+"MySQL connect error (VPS): "+e.getMessage());
		}
		checkConnection(conn);
		return conn;
	}

	/**
	 * データベースに接続中であるかを返します
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	private boolean checkConnection (Connection conn) throws SQLException{
		if (conn == null){
			log.severe(logPrefix+"Could not connect to the database.Please check the settings.");
			throw new SQLException();
		}
		if(!conn.isValid(5)){
			log.severe(logPrefix+"Could not connect to the database.");
			throw new SQLException();
		}
		return true;
	}

	public void changeDatabase(String dbname){
		database = dbname;
	}

	public boolean insertLogTable (String dateString, String player_name, int action, String world_name, double x, double y, double z, String data){
		Connection conn = null;
		PreparedStatement ps = null;
		int result = 0;

		// 日時をTimestamp(datetime)型に変換する
		Timestamp date = null;
		try{
			 date = Timestamp.valueOf(dateString);
		}catch(IllegalArgumentException ex){
			log.warning(logPrefix+"Could not convert Datetime: "+ex.getMessage());
			return false;
		}

		// 座標を小数点第一位までに丸める
		BigDecimal bi = new BigDecimal(String.valueOf(x));
		double simpleX = bi.setScale(1, RoundingMode.HALF_UP).doubleValue();
		bi = new BigDecimal(String.valueOf(y));
		double simpleY = bi.setScale(1, RoundingMode.HALF_UP).doubleValue();
		bi = new BigDecimal(String.valueOf(z));
		double simpleZ = bi.setScale(1, RoundingMode.HALF_UP).doubleValue();

		try{
			conn = getConnection();
			ps = conn.prepareStatement(sql_InsertLogTable);

			// 各種値設定
			ps.setTimestamp(1, date);
			ps.setString(2, player_name);
			ps.setInt(3, action);
			ps.setString(4, world_name);
			ps.setDouble(5, simpleX);
			ps.setDouble(6, simpleY);
			ps.setDouble(7, simpleZ);
			ps.setString(8, data);

			// 実行 返り値は行数
			result = ps.executeUpdate();

		}catch(SQLException ex){
			// エラー。falseを返す。
			log.severe(logPrefix+"error on execute insert sql: "+ex.getMessage());
			return false;
		}finally{
			try{
				if(ps != null)
					ps.close();
				if(conn != null)
					conn.close();
			}catch(SQLException ex){
				log.severe(logPrefix+"error: "+ex.getMessage());
			}
		}

		// 1行以上変更があれば成功＝trueを返す
		if (result <= 0){
			return false;
		}else{
			return true;
		}
	}

	public boolean insertAuthTable (String regdate, String player_name, String password, String email){
		Connection conn = null;
		PreparedStatement ps = null;
		int result = 0;

		// 日時をTimestamp(datetime)型に変換する
		Timestamp date = null;
		try{
			date = Timestamp.valueOf(regdate);
		}catch(IllegalArgumentException ex){
			log.warning(logPrefix+"Could not convert Datetime: "+ex.getMessage());
			return false;
		}

		String email_fixed = "";
		if (email != null){
			email_fixed = email;
		}

		try{
			conn = getConnection(db_web);
			ps = conn.prepareStatement(sql_InsertAuthTable);

			// 各種値設定
			ps.setString(1, player_name);
			ps.setString(2, password);
			ps.setString(3, email_fixed);
			ps.setTimestamp(4, date);
			ps.setTimestamp(5, date);

			// 実行 返り値は行数
			result = ps.executeUpdate();

		}catch(SQLException ex){
			// エラー。falseを返す。
			log.severe(logPrefix+"error on execute insert sql: "+ex.getMessage());
			return false;
		}finally{
			try{
				if(ps != null)
					ps.close();
				if(conn != null)
					conn.close();
			}catch(SQLException ex){
				log.severe(logPrefix+"error: "+ex.getMessage());
			}
		}

		// 1行以上変更があれば成功＝trueを返す
		if (result <= 0){
			return false;
		}else{
			return true;
		}
	}

	public boolean changePassWordOnAuthTable (String password, String player_name){
		Connection conn = null;
		PreparedStatement ps = null;
		int result = 0;

		// 日時をTimestamp(datetime)型に変換する
		Timestamp date = null;
		try{
			date = Timestamp.valueOf(Actions.getDatetime());
		}catch(IllegalArgumentException ex){
			log.warning(logPrefix+"Could not convert Datetime: "+ex.getMessage());
			return false;
		}


		try{
			conn = getVPSConnection();
			ps = conn.prepareStatement(sql_ChangePassword);

			// 各種値設定
			ps.setString(1, password);
			ps.setTimestamp(2, date);
			ps.setString(3, player_name);

			// 実行 返り値は行数
			result = ps.executeUpdate();

		}catch(SQLException ex){
			// エラー。falseを返す。
			log.severe(logPrefix+"error on execute insert sql: "+ex.getMessage());
			return false;
		}finally{
			try{
				if(ps != null)
					ps.close();
				if(conn != null)
					conn.close();
			}catch(SQLException ex){
				log.severe(logPrefix+"error: "+ex.getMessage());
			}
		}

		// 1行以上変更があれば成功＝trueを返す
		if (result <= 0){
			return false;
		}else{
			return true;
		}
	}

	// クエリを実行してヒットするかどうかを返します
	public boolean isExistRow (Connection conn, String sql_select){
		PreparedStatement ps =null;
		ResultSet rs = null;

		try{
			ps = conn.prepareStatement(sql_select);
			rs = ps.executeQuery();

			return rs.next();

		}catch(SQLException ex){
			log.severe(logPrefix+"error on execute sql: "+ex.getMessage());
			return false;
		}finally{
			try{
				if(ps != null)
					ps.close();
				if(conn != null)
					conn.close();
				if(rs != null)
					rs.close();
			}catch(SQLException ex){
				log.severe(logPrefix+"error: "+ex.getMessage());
			}
		}
	}

	public boolean execute(String sql){
		return execute(sql, null);
	}
	private boolean execute(String sql, String player){
		Connection conn = null;
		PreparedStatement ps =null;
		try{
			conn = getConnection();
			ps = conn.prepareStatement(sql);

			if (player != null && !player.equalsIgnoreCase("")){
				ps.setString(1, player);
			}

			if (ps.execute()){
				return true;
			}
		}catch(SQLException ex){
			log.severe(logPrefix+"error on execute sql: "+ex.getMessage());
			String msg = logPrefix+"could not execute the sql \"" + sql + "\"";
			if (player != null){
				msg += "  ?=" +player;
			}
			log.severe(msg);
		}finally{
			try{
				if(ps != null)
					ps.close();
				if(conn != null)
					conn.close();
			}catch(SQLException ex){
				log.severe(logPrefix+"error: "+ex.getMessage());
			}
		}
		return false;
	}

	/**
	 * テーブルを作ります。 CREATE TABLE IF NOT EXISTS で、既に存在する場合は無視されます
	 * @return 正常終了＝true 例外発生＝false
	 */
	private boolean createTable(){
		Connection conn = null;
		Statement s = null;
		ResultSet  rs = null;
		try{
			// データベースに接続
			conn = getConnection();
			// ステートメントオブジェクトを作成
			s = conn.createStatement();
			// SQL実行
			s.executeUpdate(sql_CreateTable1);

			// 一度破棄
			conn.close();
			s.close();
			// データベース変更
			conn = getConnection(db_web);
			// ステートメントオブジェクトを作成
			s = conn.createStatement();
			// SQL実行
			s.executeUpdate(sql_CreateTable2);
		}catch(SQLException ex){
			log.severe(logPrefix+"Sql error on createTable(): "+ex.getMessage());
			return false;
		}finally{
			try{
				// オブジェクトを解放
				if(rs != null)
					rs.close();
				if(s != null)
					s.close();
				if(conn != null)
					conn.close();
			}catch(SQLException ex){
				log.severe(logPrefix+"error: "+ex.getMessage());
			}
		}
		return true;
	}
}
