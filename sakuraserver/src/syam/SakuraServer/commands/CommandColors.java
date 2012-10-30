/**
 * sakuraserver - Package: syam.SakuraServer.commands
 * Created: 2012/10/30 19:41:54
 */
package syam.SakuraServer.commands;

import org.bukkit.command.CommandSender;

import syam.util.Actions;

/**
 * CommandColors (CommandColors.java)
 * @author syam(syamn)
 */
public class CommandColors extends BaseCommand{
	public CommandColors(){
		bePlayer = false;
		name = "colors";
		argLength = 0;
		usage = "see bukkit colors list";
	}

	@Override
	public void execute() {
		Actions.message(sender, null, "&cカラーコードリスト: ");
		sender.sendMessage(" \u00A70 &0 \u00A71 &1 \u00A72 &2 \u00A73 &3");
		sender.sendMessage(" \u00A74 &4 \u00A75 &5 \u00A76 &6 \u00A77 &7");
		sender.sendMessage(" \u00A78 &8 \u00A79 &9 \u00A7a &a \u00A7b &b");
		sender.sendMessage(" \u00A7c &c \u00A7d &d \u00A7e &e \u00A7f &f");
	}

	@Override
	public boolean permission(CommandSender sender) {
		return true;
	}
}
