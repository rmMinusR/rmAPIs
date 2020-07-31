package rmMinusR.mc.plugins.apis;

import java.io.File;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import rmMinusR.mc.plugins.apis.tag.data.Value;
import rmMinusR.mc.plugins.apis.tag.data.ValueNumber;
import rmMinusR.mc.plugins.apis.tag.data.ValueString;
import rmMinusR.mc.plugins.apis.tag.item.ItemTagCollection;
import rmMinusR.mc.plugins.apis.tag.item.ItemTagEventHandler;
import rmMinusR.mc.plugins.apis.tag.player.PlayerTagEventHandler;

public class RmApisPlugin extends JavaPlugin {
	
	public Logger logger = Logger.getLogger("rmAPIs");
	public PlayerTagEventHandler playerTagManager;
	public ItemTagEventHandler itemTagManager;
	
	public static RmApisPlugin INSTANCE;
	
	@Override
	public void onEnable() {
		INSTANCE = this;
		
		logger.info("Loading player tags");
		playerTagManager = new PlayerTagEventHandler(new File(this.getDataFolder().getPath()+File.separator+"players"));
		playerTagManager.onEnable();
		Bukkit.getPluginManager().registerEvents(playerTagManager, this);
		
		logger.info("Loading item tags");
		itemTagManager = new ItemTagEventHandler(new File(this.getDataFolder().getPath()+File.separator+"items"));
		itemTagManager.onEnable();
		Bukkit.getPluginManager().registerEvents(itemTagManager, this);
		ItemTagCollection.detectID();
	}
	
	@Override
	public void onDisable() {
		logger.info("Saving player tags");
		playerTagManager.onDisable();
		
		logger.info("Saving item tags");
		itemTagManager.onDisable();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		Player target;
		if(sender instanceof Player) {
			target = (Player)sender;
		} else {
			target = Bukkit.getPlayer(args[args.length-1]);
		}
			
		if(args[0].equalsIgnoreCase("player")) {
			
			if(args[1].equalsIgnoreCase("setstr")) {
				if(playerTagManager.fetch(target).tags.containsKey(args[2])) playerTagManager.fetch(target).tags.remove(args[2]);
				playerTagManager.fetch(target).tags.put(args[2], new ValueString(args[3]));
				return true;
			}
			
			if(args[1].equalsIgnoreCase("setnum")) {
				if(playerTagManager.fetch(target).tags.containsKey(args[2])) playerTagManager.fetch(target).tags.remove(args[2]);
				playerTagManager.fetch(target).tags.put(args[2], new ValueNumber(Double.parseDouble(args[3])));
				return true;
			}
			
			if(args[1].equalsIgnoreCase("list")) {
				for(Entry<String,Value> t : playerTagManager.fetch(target).tags.entrySet()) {
					sender.sendMessage(t.getKey()+" = "+t.getValue());
				}
				return true;
			}
			
			if(args[1].equalsIgnoreCase("clear")) {
				playerTagManager.fetch(target).tags.remove(args[2]);
				return true;
			}
			
		}
		
		if(args[0].equalsIgnoreCase("item")) {
			
			ItemStack targetItem = target.getInventory().getItemInMainHand();
			
			if(args[1].equalsIgnoreCase("create")) {
				ItemTagAPI.enableTags(target.getInventory().getItemInMainHand());
				return true;
			}
			
			if(args[1].equalsIgnoreCase("setstr")) {
				if(itemTagManager.fetch(targetItem).tags.containsKey(args[2])) itemTagManager.fetch(targetItem).tags.remove(args[2]);
				itemTagManager.fetch(targetItem).tags.put(args[2], new ValueString(args[3]));
				return true;
			}
			
			if(args[1].equalsIgnoreCase("setnum")) {
				if(itemTagManager.fetch(targetItem).tags.containsKey(args[2])) itemTagManager.fetch(targetItem).tags.remove(args[2]);
				itemTagManager.fetch(targetItem).tags.put(args[2], new ValueNumber(Double.parseDouble(args[3])));
				return true;
			}
			
			if(args[1].equalsIgnoreCase("list")) {
				System.out.println(itemTagManager);
				System.out.println(itemTagManager.loadedTags);
				System.out.println(itemTagManager.fetch(targetItem));
				for(Entry<String,Value> t : itemTagManager.fetch(targetItem).tags.entrySet()) {
					sender.sendMessage(t.getKey()+" = "+t.getValue());
				}
				return true;
			}
			
			if(args[1].equalsIgnoreCase("clear")) {
				itemTagManager.delete(targetItem);
				return true;
			}
			
			if(args[1].equalsIgnoreCase("newid")) { //SPECIFICALLY for use immediately after buying from a shop
				ItemTagCollection oldMgr = itemTagManager.fetch(targetItem);
				
				ItemMeta im = targetItem.getItemMeta();
				im.setLore(new ArrayList<String>());
				targetItem.setItemMeta(im);
				
				ItemTagCollection newMgr = ItemTagCollection.generateAndBindNew(targetItem);
				for(Entry<String,Value> kv : oldMgr.tags.entrySet()) {
					newMgr.tags.put(kv.getKey()+"", kv.getValue().clone());
				}
			}
			
		}
		
		return false;
	}
}
