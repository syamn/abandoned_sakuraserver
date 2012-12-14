/**
 * sakuraserver - Package: syam.SakuraServer.commands Created: 2012/10/28
 * 5:54:16
 */
package syam.SakuraServer.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import syam.SakuraServer.SakuraServer;
import syam.util.Actions;

/**
 * SakuraCommandHandler (SakuraCommandHandler.java)
 * 
 * @author syam(syamn)
 */
public class SakuraCommandHandler implements TabExecutor {
    private final SakuraServer plugin;
    private Map<String, BaseCommand> commands = new HashMap<String, BaseCommand>();
    
    public SakuraCommandHandler(final SakuraServer plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        final String commandName = command.getName().toLowerCase(Locale.ENGLISH);
        BaseCommand cmd = commands.get(commandName);
        if (cmd == null) {
            Actions.message(sender, null, "&cCommand not found!");
            return true;
        }
        
        // Run the command
        cmd.run(plugin, sender, commandLabel, args);
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String commandLabel, String[] args) {
        final String commandName = command.getName().toLowerCase(Locale.ENGLISH);
        BaseCommand cmd = commands.get(commandName);
        if (cmd == null) { return null; }
        
        // check permission
        if (sender != null && !cmd.permission(sender)) { return null; }
        
        // Get tab complete
        return cmd.tabComplete(plugin, sender, commandLabel, args);
    }
    
    public void registerCommand(BaseCommand bc) {
        if (bc.name != null) {
            commands.put(bc.name, bc);
        } else {
            SakuraServer.log.warning("Invalid command not registered! " + bc.getClass().getName());
        }
    }
}
