package rmMinusR.mc.plugins.apis.unitylike.wrapping;

import java.io.File;
import java.io.IOException;

import org.bukkit.entity.Player;

import de.tr7zw.nbtapi.NBTCompoundList;
import de.tr7zw.nbtapi.NBTFile;
import de.tr7zw.nbtapi.NBTListCompound;
import rmMinusR.mc.plugins.apis.RmApisPlugin;
import rmMinusR.mc.plugins.apis.forgelike.CustomItemManager;
import rmMinusR.mc.plugins.apis.unitylike.Debug;
import rmMinusR.mc.plugins.apis.unitylike.core.*;

public class WrappedPlayer extends WrappedLivingEntity {
	
	public Player player;
	
	public WrappedPlayer(Player player) {
		super(player);
		this.player = player;
		
		AddComponent(new PlayerDataHelper(this));
		AddComponent(new CustomItemManager(this));
	}
	
	public static WrappedPlayer GetOrNew(Player ent) {
		//Try to find existing instance
		WrappedPlayer p = Get(ent);
		if(p != null) return p;
		
		//None exists, instantiate
		else return Scene.GetOrNew(ent.getWorld()).Instantiate(new WrappedPlayer(ent));
	}
	
	public static WrappedPlayer Get(Player ent) {
		for(WrappedPlayer i : UnitylikeEnvironment.GetInstance().FindObjectsOfType(WrappedPlayer.class)) if(i.entity.equals(ent)) return i;
		return null;
	}
	
	protected class PlayerDataHelper extends JavaBehaviour {
		
		public WrappedPlayer wrapped;
		
		public PlayerDataHelper(WrappedPlayer wrappedPlayer) {
			super(wrappedPlayer);
			wrapped = wrappedPlayer;
		}
		
		@Override
		public void OnEnable() {
			try {
				File saveloc = GetPersistentSaveLoc(wrapped.player);
				if(saveloc.exists()) {
					wrapped.LoadPersistent();
				} else {
					RmApisPlugin.INSTANCE.logger.warning("Unitylike data savefile does not exist for player "+wrapped.player.getDisplayName()+". Are they new?");
					saveloc.createNewFile();
				}
			} catch(IOException e) {
				RmApisPlugin.INSTANCE.logger.warning("Failed to load Unitylike data for player "+wrapped.player.getDisplayName());
				e.printStackTrace();
			}
		}
		
		@Override
		public void OnDisable() {
			try {
				wrapped.SavePersistent();
			} catch(IOException e) {
				RmApisPlugin.INSTANCE.logger.warning("Failed to save Unitylike data for player "+wrapped.player.getDisplayName());
				e.printStackTrace();
			}
		}
		
	}
	
	public static File getDataFolder() {
		File out = new File(UnitylikeEnvironment.GetInstance().getDataFolder(), "playerdata");
		out.mkdirs();
		return out;
	}
	
	public static File GetPersistentSaveLoc(Player player) throws IOException {
		File out = new File(getDataFolder(), player.getUniqueId().toString()+".dat");
		if(!out.exists()) out.createNewFile();
		return out;
	}
	
	public void SavePersistent() throws IOException {
		System.out.println("Writing player data: "+player.getDisplayName());
		NBTFile nbtFile = new NBTFile(GetPersistentSaveLoc(player));
		
		NBTCompoundList nbtComponents = nbtFile.getCompoundList("components");
		nbtComponents.clear();
		
		for(Component c : GetComponents()) {
			if(c instanceof IPersistentSerializable) {
				System.out.println("Writing "+c.getClass().getName()+"...");
				IPersistentSerializable s = (IPersistentSerializable)c;
				SerializationFactory.WriteToNBT(s, nbtComponents.addCompound());
			}
		}
		
		System.out.println("Wrote "+nbtComponents.size()+" component entries");
		nbtFile.save();
	}
	
	public void LoadPersistent() throws IOException {
		System.out.println("Reading player data: "+player.getDisplayName());
		NBTFile nbtFile = new NBTFile(GetPersistentSaveLoc(player));
		
		NBTCompoundList nbtComponents = nbtFile.getCompoundList("components");
		System.out.println("Found "+nbtComponents.size()+" component entries");
		
		for(NBTListCompound nc : nbtComponents) {
			System.out.println("Reading "+nc.getName()+"...");
			
			try {
				//Attempt to get populated serializable, and push to components
				//FIXME this may cause multiple calls to Awake() and OnEnable()
				Component deser = SerializationFactory.ReadFromNBTAs(nc, Component.class);
				AddComponent(deser);
			} catch (Exception e) {
				Debug.LogWarning("Failed to load data for Unitylike Component \""+nc.getName()+"\" for player "+player.getDisplayName());
				e.printStackTrace();
			}
		}
	}

}
