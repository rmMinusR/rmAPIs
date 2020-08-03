package rmMinusR.mc.plugins.apis.unitylike.core;

import java.io.File;
import java.io.IOException;

import org.bukkit.entity.Player;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTCompoundList;
import de.tr7zw.nbtapi.NBTFile;
import de.tr7zw.nbtapi.NBTListCompound;
import rmMinusR.mc.plugins.apis.RmApisPlugin;

public class WrappedPlayer extends WrappedLivingEntity {
	
	private static final String KEY_CLASS = "type";
	private static final String KEY_DATA  = "data";
	
	public Player player;
	
	public WrappedPlayer(Player player) {
		super(player);
		this.player = player;
		
		try {
			LoadPersistent();
		} catch(Exception e) {
			RmApisPlugin.INSTANCE.logger.warning("Failed to load Unitylike data for player "+player.getDisplayName());
			e.printStackTrace();
		}
	}
	
	public File GetPersistentSaveLoc() throws IOException {
		File out = new File(RmApisPlugin.INSTANCE.unitylikeEnv.getDataFolder(), player.getUniqueId().toString()+".dat");
		out.mkdirs();
		
		return out;
	}
	
	public void SavePersistent() throws IOException {
		NBTFile nbtFile = new NBTFile(new File(RmApisPlugin.INSTANCE.unitylikeEnv.getDataFolder(), player.getUniqueId().toString()+".dat"));
		
		NBTCompoundList nbtComponents = nbtFile.getCompoundList("components");
		
		for(Component c : GetComponents(Component.class)) {
			if(c instanceof IPersistentSerializable) {
				IPersistentSerializable s = (IPersistentSerializable)c;
				NBTCompound cnbt = nbtComponents.addCompound();
				
				cnbt.setString(KEY_CLASS, s.getClass().getName());
				s.DataToPersistent(cnbt.addCompound(KEY_DATA));
			}
		}
		
		nbtFile.save();
	}
	
	@SuppressWarnings("unchecked")
	public void LoadPersistent() throws IOException {
		NBTFile nbtFile = new NBTFile(GetPersistentSaveLoc());
		
		NBTCompoundList nbtComponents = nbtFile.getCompoundList("components");
		
		for(NBTListCompound nc : nbtComponents) {
			
			String r_class = nc.getString(KEY_CLASS);
			NBTCompound r_data = nc.getCompound(KEY_DATA);
			
			Class<? extends IPersistentSerializable> c = null;
			try {
				c = (Class<? extends IPersistentSerializable>) Class.forName(r_class);
			} catch(ClassNotFoundException e) { RmApisPlugin.INSTANCE.logger.warning("Failed to find class "+r_class); }
			
			//c should never be null here
			
			try {
				//Attempt to get unpopulated serializable
				IPersistentSerializable s = IPersistentSerializableExt.GetBlank(c);
				//Attempt deserialization
				s.DataFromPersistent(r_data);
			} catch (Exception e) {
				RmApisPlugin.INSTANCE.logger.warning("Failed to load data for Unitylike Component \""+r_class+"\" for player "+player.getDisplayName());
				e.printStackTrace();
			}
		}
	}

}
