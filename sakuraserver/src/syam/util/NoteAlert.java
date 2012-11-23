package syam.util;

import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import syam.SakuraServer.SakuraServer;

public class NoteAlert {
	private enum Phase { PLAY_NOTE, TIDY_UP };

	private final Player player;
	private final Location loc;
	private final List<Note> notes;
	private final long delay;
	private final Iterator<Note> iter;

	private int taskId;
	private Phase phase;

	public NoteAlert(Player player, Location loc, long delay, List<Note> notes) {
		this.player = player;
		this.loc = loc;
		this.notes = notes;
		this.delay = delay;
		iter = this.notes.iterator();
	}

	public int start() {
		player.sendBlockChange(loc, Material.NOTE_BLOCK, (byte)0);
		phase = Phase.PLAY_NOTE;

		taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(SakuraServer.getInstance(), new Runnable() {
			@Override
			public void run() {
				switch(phase) {
					case PLAY_NOTE:
						if (iter.hasNext()) {
							Note note = iter.next();
							if (note != null) {
								player.playNote(loc, Instrument.PIANO, note);
							}
						} else {
							phase = Phase.TIDY_UP;
						}
						break;
					case TIDY_UP:
						cancel();
						break;
				}
			}
		}, 0L, delay);

		return taskId;
	}

	public void cancel() {
		Block lastBlock = loc.getBlock();
		player.sendBlockChange(loc, lastBlock.getType(), lastBlock.getData());
		Bukkit.getScheduler().cancelTask(taskId);
	}
}