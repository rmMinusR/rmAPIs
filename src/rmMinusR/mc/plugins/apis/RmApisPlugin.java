package rmMinusR.mc.plugins.apis;

import java.util.logging.Logger;

import com.comphenix.protocol.wrappers.BlockPosition;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import de.tr7zw.nbtapi.NBTItem;
import rmMinusR.mc.plugins.apis.debug.DebugItemNamespace;
import rmMinusR.mc.plugins.apis.debug.DebugUtils;
import rmMinusR.mc.plugins.apis.forgelike.CustomItem;
import rmMinusR.mc.plugins.apis.forgelike.CustomItemManager;
import rmMinusR.mc.plugins.apis.forgelike.CustomMaterial;
import rmMinusR.mc.plugins.apis.particle.Image;
import rmMinusR.mc.plugins.apis.particle.ParticleGraphics;
import rmMinusR.mc.plugins.apis.simpleillusion.IllusionManager;
import rmMinusR.mc.plugins.apis.simpleillusion.IllusoryOverlay;
import rmMinusR.mc.plugins.apis.unitylike.core.Component;
import rmMinusR.mc.plugins.apis.unitylike.core.GameObject;
import rmMinusR.mc.plugins.apis.unitylike.core.UnitylikeEnvironmentManager;

public class RmApisPlugin extends JavaPlugin {
	
	public static final String KEY_ENTITY_LOAD_RADIUS = "unitylike.entityLoadRadius";
	
	public Logger logger;
	public IllusionManager illusionManager;
	public UnitylikeEnvironmentManager unitylikeEnv;
	
	public static RmApisPlugin INSTANCE;
	
	@Override
	public void onEnable() {
		INSTANCE = this;
		
		try {
			FileConfiguration config = getConfig();

			config.addDefault(KEY_ENTITY_LOAD_RADIUS, 16);
			
			saveConfig();
			reloadConfig();
		} catch(Throwable t) { t.printStackTrace(); }
		
		try {
			if(logger == null) logger = Logger.getLogger("rmAPIs");
		} catch(Throwable t) { t.printStackTrace(); }
		
		try {
			logger.info("Initializing illusions");
			illusionManager = new IllusionManager();
			illusionManager.OnEnable();
		} catch(Throwable t) { t.printStackTrace(); }
		
		try {
			logger.info("Initializing Unitylike ECS");
			unitylikeEnv = new UnitylikeEnvironmentManager(getConfig().getInt(KEY_ENTITY_LOAD_RADIUS));
			unitylikeEnv.OnEnable();
		} catch(Throwable t) { t.printStackTrace(); }
		
		try {
			logger.info("Initializing Forgelike items");
			CustomMaterial.InitRegistry();
		} catch(Throwable t) { t.printStackTrace(); }
		
		try {
			logger.info("Registering debug items");
			DebugItemNamespace.RegisterItems();
		} catch(Throwable t) { t.printStackTrace(); }
	}
	
	@Override
	public void onDisable() {
		try {
			saveConfig();
		} catch(Throwable t) { t.printStackTrace(); }
		
		try {
			if(illusionManager != null) {
				logger.info("Disabling illusions");
				illusionManager.OnDisable();
				illusionManager = null;
			}
		} catch(Throwable t) { t.printStackTrace(); }
		
		try {
			if(unitylikeEnv != null) {
				logger.info("Disabling Unitylike");
				unitylikeEnv.OnDisable();
				unitylikeEnv = null;
			}
		} catch(Throwable t) { t.printStackTrace(); }
		
		Bukkit.getScheduler().cancelTasks(this);
		HandlerList.unregisterAll(this);
		
		logger = null;
		INSTANCE = null;
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
			IllusoryOverlay iw = illusionManager.GetIllusoryWorld(sender);
			if(args[1].equalsIgnoreCase("rst")) {
				iw.ShowReality();
			} else if(args[1].equalsIgnoreCase("disp")) {
				iw.RenderQueue();
			} else if(args[1].equalsIgnoreCase("rr")) {
				iw.Rerender();
			} else {
				Material m = Material.getMaterial(args[1]);
				
				BlockPosition target = new BlockPosition(sender.rayTraceBlocks(5).getHitPosition());
				
				iw.QueueIllusionBlock(sender.getWorld(), target, m, 0, null);
			}
			
			return true;
		}
		
		if(args[0].equalsIgnoreCase("unitylike-components")) {
			for(Component i : unitylikeEnv.Wrap(sender).GetComponents()) unvalidatedSender.sendMessage(i.toString());
			return true;
		}
		
		if(args[0].equalsIgnoreCase("unitylike-objects")) {
			for(GameObject i : unitylikeEnv.GetAllObjects()) unvalidatedSender.sendMessage(i.toString());
			return true;
		}
		
		if(args[0].equalsIgnoreCase("item")) {
			if(args[1].equalsIgnoreCase("cache")) {
				for(CustomItem c : CustomItemManager.cache) unvalidatedSender.sendMessage(c.toString());
				return true;
			} else if(args[1].equalsIgnoreCase("info")) {
				unvalidatedSender.sendMessage("Item: "+sender.getInventory().getItemInMainHand().getType());
				unvalidatedSender.sendMessage( DebugUtils.ToString(new NBTItem(sender.getInventory().getItemInMainHand())).split("\n") );
				return true;
			} else if(args[1].equalsIgnoreCase("create")) {
				CustomMaterial mat = CustomMaterial.ByName(args[2]);
				if(mat != null) {
					CustomItem.CreateNewBlank(sender.getInventory().getItemInMainHand(), mat.clazz);
				} else {
					unvalidatedSender.sendMessage("No item with the name \""+args[2]+"\" found");
					for(CustomMaterial i : CustomMaterial.registry) unvalidatedSender.sendMessage(i.name);
				}
				return true;
			}
		}
		
		//END TESTING
		
		return false;
	}
}
