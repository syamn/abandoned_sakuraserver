/**
 * sakuraserver - Package: syam.SakuraServer.commands
 * Created: 2012/10/30 17:38:49
 */
package syam.SakuraServer.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import syam.SakuraServer.SakuraServer;
import syam.util.Actions;

/**
 * BaseCommand (BaseCommand.java)
 * @author syam(syamn)
 */
public abstract class BaseCommand {
	// Logger
	protected static final Logger log = SakuraServer.log;
	protected static final String logPrefix = SakuraServer.logPrefix;
	protected static final String msgPrefix = SakuraServer.msgPrefix;

	public SakuraServer plugin;
	public String name;

	/* コマンド関係 */
	public CommandSender sender;
	public List<String> args = new ArrayList<String>();
	public int argLength = 0;
	public String usage;
	public boolean bePlayer = true;
	public Player player;
	public boolean isPlayer = false;
	public String command;

	public boolean run(SakuraServer plugin, CommandSender sender, String cmd, String[] preArgs) {
		if (name == null){
			Actions.message(sender, null, "&cThis command not loaded properly!");
			return true;
		}

		this.sender = sender;
		this.command = cmd;

		// 引数をソート
		args.clear();
		for (String arg : preArgs)
			args.add(arg);

		// 引数からコマンドの部分を取り除く
		// (コマンド名に含まれる半角スペースをカウント、リストの先頭から順にループで取り除く)
		//for (int i = 0; i < name.split(" ").length && i < args.size(); i++)
		//	args.remove(0);

		// 引数の長さチェック
		if (argLength > args.size()){
			sendUsage();
			return true;
		}

		// 実行にプレイヤーであることが必要かチェックする
		if (bePlayer && !(sender instanceof Player)){
			Actions.message(sender, null, "&cThis command cannot run from Console!");
			return true;
		}
		if (sender instanceof Player){
			player = (Player)sender;
			isPlayer = true;
		}

		// 権限チェック
		if (!permission(sender)){
			Actions.message(sender, null, "&cYou don't have permission to use this!");
			return true;
		}

		// 実行
		try {
			execute();
		}
		catch (Exception ex) {
			Throwable error = ex;
			/*
			while (error instanceof Exception){
				Actions.message(sender, null, error.getMessage());
				error = error.getCause();
			}
			*/
		}

		return true;
	}

	/**
	 * コマンドを実際に実行する
	 * @return 成功すればtrue それ以外はfalse
	 * @throws CommandException CommandException
	 */
	public abstract void execute();

	protected List<String> tabComplete(SakuraServer plugin, final CommandSender sender, String cmd, String[] preArgs){
		return null;
	}

	/**
	 * コマンド実行に必要な権限を持っているか検証する
	 * @return trueなら権限あり、falseなら権限なし
	 */
	public abstract boolean permission(CommandSender sender);

	/**
	 * コマンドの使い方を送信する
	 */
	public void sendUsage(){
		Actions.message(sender, null, "&c/"+this.command+" "+name+" "+usage);
	}
}
