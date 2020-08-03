package rmMinusR.mc.plugins.apis.unitylike.core;

import de.tr7zw.nbtapi.NBTCompound;

public interface IPersistentSerializable {
	
	public void ToPersistent(NBTCompound out);
	public void FromPersistent(NBTCompound in);
	
	public static IPersistentSerializable GetUnpopulated(); //Works in JRE 8+
	
}
