package syam.SakuraServer.listener;

import java.util.logging.Logger;

import org.bukkit.Chunk;
import org.bukkit.World.Environment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

import syam.SakuraServer.SakuraServer;

public class SakuraWorldListener implements Listener {
	public final static Logger log = SakuraServer.log;
	private static final String logPrefix = SakuraServer.logPrefix;
	private static final String msgPrefix = SakuraServer.msgPrefix;

	private final SakuraServer plugin;

	public SakuraWorldListener(SakuraServer plugin){
		this.plugin = plugin;
	}

	/* 登録するイベントはここから下に */

}
