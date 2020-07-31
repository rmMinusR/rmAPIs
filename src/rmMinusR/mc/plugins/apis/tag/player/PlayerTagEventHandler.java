package rmMinusR.mc.plugins.apis.tag.player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import rmMinusR.mc.plugins.apis.RmApisPlugin;

public class PlayerTagEventHandler implements Listener {
	
	public File workingDirectory;
	
	public ArrayList<PlayerTagCollection> loadedTags;
	
	public PlayerTagEventHandler(File workingDirectory) {
		this.workingDirectory = workingDirectory;
		if(!workingDirectory.exists()) workingDirectory.mkdirs();
		loadedTags = new ArrayList<PlayerTagCollection>();
	}
	
	public void onEnable() {
		for(Player p : Bukkit.getOnlinePlayers()) {
			playerConnect(new PlayerJoinEvent(p, ""));
		}
	}
	
	public void onDisable() {
		for(Player p : Bukkit.getOnlinePlayers()) {
			playerDisconnect(new PlayerQuitEvent(p, ""));
		}
		for(PlayerTagCollection c : loadedTags) {
			try {
				c.save(fileOf(c.host));
			} catch (IOException e) {
				RmApisPlugin.INSTANCE.logger.log(Level.SEVERE, "An unknown error occured while player tag data for player "+c.host.getDisplayName()+". Their tags have been rolled back to the last functional save. Please report this to your mod developer.");
				e.printStackTrace();
			}
		}
	}
	
	@EventHandler
	public void playerConnect(PlayerJoinEvent event) {
		loadedTags.add(PlayerTagCollection.fromFile(fileOf(event.getPlayer())));
	}
	
	@EventHandler
	public void playerDisconnect(PlayerQuitEvent event) {
		try {
			PlayerTagCollection tagCollection = fetch(event.getPlayer());
			loadedTags.remove(tagCollection);
			tagCollection.save(fileOf(event.getPlayer()));
		} catch (IOException e) {
			RmApisPlugin.INSTANCE.logger.log(Level.SEVERE, "An unknown error occured while player tag data for player "+event.getPlayer().getDisplayName()+". Their tags have been rolled back to the last functional save. Please report this to your mod developer.");
			e.printStackTrace();
		}
	}
	
	public PlayerTagCollection fetch(Player player) {
		for(PlayerTagCollection i : loadedTags) {
			if(i.host.equals(player)) return i;
		}
		return null;
	}
	
	public File fileOf(Player player) {
		return new File(workingDirectory+"/"+player.getUniqueId().toString());
	}
}
