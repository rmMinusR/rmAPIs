package rmMinusR.mc.plugins.apis.tag.item;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import rmMinusR.mc.plugins.apis.RmApisPlugin;

public class ItemTagEventHandler implements Listener {
	
	public File workingDirectory;
	
	public ArrayList<ItemTagCollection> loadedTags;
	
	public ItemTagEventHandler(File workingDirectory) {
		this.workingDirectory = workingDirectory;
		if(!workingDirectory.exists()) workingDirectory.mkdirs();
		loadedTags = new ArrayList<ItemTagCollection>();
	}
	
	@EventHandler
	public void playerConnect(PlayerJoinEvent event) {
		for(ItemStack i : event.getPlayer().getInventory().getContents()) {
			if(isValid(i) && fetch(i)==null) {
				loadedTags.add(ItemTagCollection.fromFile(fileOf(i)));
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onItemPickup(PlayerPickupItemEvent event) {
		if(isValid(event.getItem().getItemStack()) && fetch(event.getItem().getItemStack())==null) {
			loadedTags.add(ItemTagCollection.fromFile(fileOf(event.getItem().getItemStack())));
		}
	}
	
	@EventHandler
	public void playerDisconnect(PlayerQuitEvent event) {
		ArrayList<ItemTagCollection> processed = new ArrayList<ItemTagCollection>();
		for(ItemStack i : event.getPlayer().getInventory().getContents()) {
			if(isValid(i)) {
				try {
					fetch(i).save(fileOf(i));
					processed.add(fetch(i));
				} catch (IOException e) {
					RmApisPlugin.INSTANCE.logger.log(Level.SEVERE, "An unknown error occured while item tag data for item #"+ItemTagCollection.getID(i)+". Its tags have been rolled back to the last functional save. Please report this to your mod developer.");
					e.printStackTrace();
				}
			}
		}
		loadedTags.removeAll(processed);
	}
	
	@EventHandler
	public void onItemDeath(EntityDeathEvent event) {
		if(event.getEntityType() == EntityType.DROPPED_ITEM) {
			if(isValid(((Item)event.getEntity()).getItemStack())) delete(((Item)event.getEntity()).getItemStack());
		}
	}
	
	@EventHandler
	public void onItemDespawn(ItemDespawnEvent event) {
		if(isValid(event.getEntity().getItemStack())) delete(event.getEntity().getItemStack());
	}
	
	@EventHandler
	public void inventoryLoad(InventoryOpenEvent event) {
		for(ItemStack i : event.getView().getBottomInventory()) {
			if(isValid(i) && fetch(i)==null) {
				loadedTags.add(ItemTagCollection.fromFile(fileOf(i)));
			}
		}
		
		if(event.getView().getTopInventory() != null) {
			for(ItemStack i : event.getView().getTopInventory()) {
				if(isValid(i) && fetch(i)==null) {
					loadedTags.add(ItemTagCollection.fromFile(fileOf(i)));
				}
			}
		}
	}
	
	public ItemTagCollection fetch(String host) {
		for(ItemTagCollection i : loadedTags) {
			if(i.host.equals(host)) return i;
		}
		return null;
	}
	
	public ItemTagCollection fetch(ItemStack host) {
		return fetch(ItemTagCollection.getID(host));
	}
	
	public File fileOf(ItemStack host) {
		return new File(workingDirectory+"/"+ItemTagCollection.getID(host));
	}
	
	public File fileOf(String host) {
		return new File(workingDirectory+"/"+host);
	}
	
	public boolean isValid(ItemStack host) {
		try {
			return fileOf(host).exists() || fetch(host)!=null; //Valid if the tag data exists
		} catch(NullPointerException e) {
			return false;
		}
	}
	
	public void delete(ItemStack host) {
		loadedTags.remove(fetch(host));
		fileOf(host).delete();
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
		for(ItemTagCollection c : loadedTags) {
			try {
				c.save(fileOf(c.host));
			} catch (IOException e) {
				RmApisPlugin.INSTANCE.logger.log(Level.SEVERE, "An unknown error occured while item tag data for item #"+c.host+". Its tags have been rolled back to the last functional save. Please report this to your mod developer.");
				e.printStackTrace();
			}
		}
	}
}
