package rmMinusR.mc.plugins.apis;

import java.util.logging.Logger;
import com.comphenix.protocol.wrappers.BlockPosition;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import rmMinusR.mc.plugins.apis.illusion.block.IllusionManager;
import rmMinusR.mc.plugins.apis.illusion.block.IllusoryWorld;
import rmMinusR.mc.plugins.apis.particle.Image;
import rmMinusR.mc.plugins.apis.particle.ParticleGraphics;
import rmMinusR.mc.plugins.apis.unitylike.GameObjectManager;

public class RmApisPlugin extends JavaPlugin {
	
	public Logger logger;
	public IllusionManager illusionManager;
	public GameObjectManager unitylikeGOM;
	
	public static RmApisPlugin INSTANCE;
	
	@Override
	public void onEnable() {
		INSTANCE = this;
		
		try {
			if(logger == null) logger = Logger.getLogger("rmAPIs");
		} catch(Throwable t) { t.printStackTrace(); }
		
		try {
			logger.info("Initializing illusions");
			illusionManager = new IllusionManager();
			illusionManager.OnEnable();
		} catch(Throwable t) { t.printStackTrace(); }
		
		try {
			logger.info("Initializing Unitylike");
			unitylikeGOM = new GameObjectManager();
			unitylikeGOM.OnEnable();
		} catch(Throwable t) { t.printStackTrace(); }
	}
	
	@Override
	public void onDisable() {
		try {
			if(illusionManager != null) {
				logger.info("Disabling illusions");
				illusionManager.OnDisable();
				illusionManager = null;
			}
		} catch(Throwable t) { t.printStackTrace(); }
		
		try {
			if(unitylikeGOM != null) {
				logger.info("Disabling Unitylike");
				unitylikeGOM.OnDisable();
				unitylikeGOM = null;
			}
		} catch(Throwable t) { t.printStackTrace(); }
		
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
			IllusoryWorld iw = illusionManager.GetIllusoryWorld(sender);
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
		
		//END TESTING
		
		return false;
	}
}
