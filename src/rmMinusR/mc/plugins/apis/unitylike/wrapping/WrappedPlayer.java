package rmMinusR.mc.plugins.apis.unitylike.wrapping;

import java.io.File;
import java.io.IOException;

import org.bukkit.entity.Player;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTCompoundList;
import de.tr7zw.nbtapi.NBTFile;
import de.tr7zw.nbtapi.NBTListCompound;
import rmMinusR.mc.plugins.apis.RmApisPlugin;
import rmMinusR.mc.plugins.apis.forgelike.CustomItemManager;
import rmMinusR.mc.plugins.apis.unitylike.core.Component;
import rmMinusR.mc.plugins.apis.unitylike.core.IPersistentSerializable;
import rmMinusR.mc.plugins.apis.unitylike.core.IPersistentSerializableExt;
import rmMinusR.mc.plugins.apis.unitylike.core.JavaBehaviour;

public class WrappedPlayer extends WrappedLivingEntity {
	
	private static final String KEY_CLASS = "type";
	private static final String KEY_DATA  = "data";
	
	public Player player;
	
	public WrappedPlayer(Player player) {
		super(player);
		this.player = player;
		
		AddComponent(new PlayerDataHelper(this));
		AddComponent(new CustomItemManager(this));
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
	
	public static File GetPersistentSaveLoc(Player player) throws IOException {
		File out = new File(RmApisPlugin.INSTANCE.unitylikeEnv.getDataFolder(), player.getUniqueId().toString()+".dat");
		
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
				NBTCompound cnbt = nbtComponents.addCompound();
				
				cnbt.setString(KEY_CLASS, s.getClass().getName());
				s.DataToPersistent(cnbt.addCompound(KEY_DATA));
			}
		}
		
		System.out.println("Wrote "+nbtComponents.size()+" component entries");
		nbtFile.save();
	}
	
	@SuppressWarnings("unchecked")
	public void LoadPersistent() throws IOException {
		System.out.println("Reading player data: "+player.getDisplayName());
		NBTFile nbtFile = new NBTFile(GetPersistentSaveLoc(player));
		
		NBTCompoundList nbtComponents = nbtFile.getCompoundList("components");
		System.out.println("Found "+nbtComponents.size()+" component entries");
		
		for(NBTListCompound nc : nbtComponents) {
			
			String r_class = nc.getString(KEY_CLASS);
			NBTCompound r_data = nc.getCompound(KEY_DATA);
			
			System.out.println("Reading "+r_class+"...");
			
			Class<? extends IPersistentSerializable> c = null;
			try {
				c = (Class<? extends IPersistentSerializable>) Class.forName(r_class);
			} catch(ClassNotFoundException e) { RmApisPlugin.INSTANCE.logger.warning("Failed to find class "+r_class+" - did you remove a plugin?"); continue; }
			
			//c should never be null here
			
			try {
				//Attempt to get populated serializable, and push to components
				//FIXME this may cause multiple calls to Awake() and OnEnable()
				Component deser = (Component) IPersistentSerializableExt.GetFromNBT(this, r_data, c);
				AddComponent(deser, false);
			} catch (Exception e) {
				RmApisPlugin.INSTANCE.logger.warning("Failed to load data for Unitylike Component \""+r_class+"\" for player "+player.getDisplayName());
				e.printStackTrace();
			}
		}
	}

}
