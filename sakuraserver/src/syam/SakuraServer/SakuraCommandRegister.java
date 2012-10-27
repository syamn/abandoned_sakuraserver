package syam.SakuraServer;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;

import syam.SakuraServer.oldcommands.AdminCommand;
import syam.SakuraServer.oldcommands.FlymodeCommand;
import syam.SakuraServer.oldcommands.MiscCommand;
import syam.SakuraServer.oldcommands.PasswordCommand;
import syam.SakuraServer.oldcommands.PotCommand;
import syam.SakuraServer.oldcommands.RegisterCommand;
import syam.SakuraServer.oldcommands.SakuraCommand;


public class SakuraCommandRegister {
	public final static Logger log = SakuraServer.log;
	private static final String logPrefix = SakuraServer.logPrefix;
	private static final String msgPrefix = SakuraServer.msgPrefix;

	public static SakuraServer plugin;

	public static void registerCommands(){
		registerCommand("admin", new AdminCommand());
		registerCommand("flymode", new FlymodeCommand());
		registerCommand("jailapply", new FlymodeCommand());
		registerCommand("register", new RegisterCommand());
		registerCommand("password", new PasswordCommand());
		registerCommand("pot", new PotCommand());
		registerCommand("sakura", new SakuraCommand());

		CommandExecutor miscExec = new MiscCommand();
		registerCommand("syamn", miscExec);
		registerCommand("colors", miscExec);
		registerCommand("mfmf", miscExec);
		registerCommand("yes", miscExec);
		registerCommand("no", miscExec);

		log.info(logPrefix+"Initialized Commands.");
	}
	public static void registerCommand(String command, CommandExecutor executor){
		Bukkit.getServer().getPluginCommand(command).setExecutor(executor);
	}
}
