package syam.SakuraServer;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;

import syam.SakuraServer.commands.AdminCommand;
import syam.SakuraServer.commands.FlymodeCommand;
import syam.SakuraServer.commands.MiscCommand;
import syam.SakuraServer.commands.PasswordCommand;
import syam.SakuraServer.commands.PotCommand;
import syam.SakuraServer.commands.RegisterCommand;
import syam.SakuraServer.commands.SakuraCommand;

public class SakuraCommandRegister {
	public final static Logger log = SakuraServer.log;
	private static final String logPrefix = SakuraServer.logPrefix;
	private static final String msgPrefix = SakuraServer.msgPerfix;

	public static SakuraServer plugin;

	public static void registerCommands(){
		registerCommand("admin", new AdminCommand());
		registerCommand("flymode", new FlymodeCommand());
		registerCommand("jailapply", new FlymodeCommand());
		registerCommand("register", new RegisterCommand());
		registerCommand("password", new PasswordCommand());
		registerCommand("pot", new PotCommand());
		registerCommand("sakura", new SakuraCommand());
		registerCommand("syamn", new MiscCommand());
		log.info(logPrefix+"Initialized Commands.");
	}
	public static void registerCommand(String command, CommandExecutor executor){
		Bukkit.getServer().getPluginCommand(command).setExecutor(executor);
	}
}
