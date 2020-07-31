package rmMinusR.mc.plugins.apis;

import java.io.File;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.logging.Logger;
import com.comphenix.protocol.wrappers.BlockPosition;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import rmMinusR.mc.plugins.apis.blockillusion.IllusionManager;
import rmMinusR.mc.plugins.apis.blockillusion.IllusoryWorld;
import rmMinusR.mc.plugins.apis.particle.Image;
import rmMinusR.mc.plugins.apis.particle.ParticleGraphics;
import rmMinusR.mc.plugins.apis.tag.data.Value;
import rmMinusR.mc.plugins.apis.tag.data.ValueNumber;
import rmMinusR.mc.plugins.apis.tag.data.ValueString;
import rmMinusR.mc.plugins.apis.tag.item.ItemTagCollection;
import rmMinusR.mc.plugins.apis.tag.item.ItemTagEventHandler;
import rmMinusR.mc.plugins.apis.tag.player.PlayerTagEventHandler;

public class RmApisPlugin extends JavaPlugin {
	
	public Logger logger;
	public PlayerTagEventHandler playerTagManager;
	public ItemTagEventHandler itemTagManager;
	public IllusionManager illusionManager;
	
	public static RmApisPlugin INSTANCE;
	
	@Override
	public void onEnable() {
		INSTANCE = this;
		
		if(logger == null) {
			logger = Logger.getLogger("rmAPIs");
		}
		
		logger.info("Loading player tags");
		playerTagManager = new PlayerTagEventHandler(new File(this.getDataFolder().getPath()+File.separator+"players"));
		playerTagManager.onEnable();
		Bukkit.getPluginManager().registerEvents(playerTagManager, this);
		
		logger.info("Loading item tags");
		itemTagManager = new ItemTagEventHandler(new File(this.getDataFolder().getPath()+File.separator+"items"));
		itemTagManager.onEnable();
		Bukkit.getPluginManager().registerEvents(itemTagManager, this);
		ItemTagCollection.detectID();
		
		logger.info("Initializing illusions");
		illusionManager = new IllusionManager();
		illusionManager.OnEnable();
	}
	
	@Override
	public void onDisable() {
		logger.info("Disabling illusions");
		illusionManager.OnDisable();
		
		logger.info("Saving player tags");
		playerTagManager.onDisable();
		
		logger.info("Saving item tags");
		itemTagManager.onDisable();
		
		logger = null;
	}
	
	@Override
	public boolean onCommand(CommandSender unvalidatedSender, Command command, String label, String[] args) {
		
		Player sender;
		if(unvalidatedSender instanceof Player) {
			sender = (Player)unvalidatedSender;
		} else {
			sender = Bukkit.getPlayer(args[args.length-1]);
		}
		
		//TESTING
		if(args[0].equalsIgnoreCase("img")) {
			Image image = new Image(100, 100);
			for(int x = 0; x < image.w; x++) for(int y = 0; y < image.h; y++) {
				image.data[x][y] = Color.fromRGB((int)(255*x/(float)image.w), (int)(255*y/(float)image.h), 0);
			}
			ParticleGraphics.drawImage(image, sender.getWorld(), sender.getLocation().toVector(), new Vector(4, 0, 0), new Vector(0, 0, 4));
			return true;
		}
		
		if(args[0].equalsIgnoreCase("ill")) {
			IllusoryWorld iw = illusionManager.GetIllusoryWorld(sender);
			if(args[1].equalsIgnoreCase("rst")) {
				iw.ShowReality();
			} else if(args[1].equalsIgnoreCase("disp")) {
				iw.RenderQueue();
			} else if(args[1].equalsIgnoreCase("rr")) {
				iw.Rerender();
			} else {
				Material m = Material.getMaterial(args[1]);
				
				BlockPosition target1 = new BlockPosition(sender.rayTraceBlocks(5).getHitPosition());
				BlockPosition target2 = new BlockPosition(target1.getX(), target1.getY()+1, target1.getZ());
				
				iw.QueueIllusionBlock(
						sender.getWorld(), target1, m, 0, null
					);
				iw.QueueIllusionBlock(
						sender.getWorld(), target2, m, 0, null
					);
			}
			
			return true;
		}
		
		//END TESTING
		if(args[0].equalsIgnoreCase("player")) {
			
			if(args[1].equalsIgnoreCase("setstr")) {
				if(playerTagManager.fetch(sender).tags.containsKey(args[2])) playerTagManager.fetch(sender).tags.remove(args[2]);
				playerTagManager.fetch(sender).tags.put(args[2], new ValueString(args[3]));
				return true;
			}
			
			if(args[1].equalsIgnoreCase("setnum")) {
				if(playerTagManager.fetch(sender).tags.containsKey(args[2])) playerTagManager.fetch(sender).tags.remove(args[2]);
				playerTagManager.fetch(sender).tags.put(args[2], new ValueNumber(Double.parseDouble(args[3])));
				return true;
			}
			
			if(args[1].equalsIgnoreCase("list")) {
				for(Entry<String,Value> t : playerTagManager.fetch(sender).tags.entrySet()) {
					unvalidatedSender.sendMessage(t.getKey()+" = "+t.getValue());
				}
				return true;
			}
			
			if(args[1].equalsIgnoreCase("clear")) {
				playerTagManager.fetch(sender).tags.remove(args[2]);
				return true;
			}
			
		}
		
		if(args[0].equalsIgnoreCase("item")) {
			
			ItemStack targetItem = sender.getInventory().getItemInMainHand();
			
			if(args[1].equalsIgnoreCase("create")) {
				ItemTagAPI.enableTags(sender.getInventory().getItemInMainHand());
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
					unvalidatedSender.sendMessage(t.getKey()+" = "+t.getValue());
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
