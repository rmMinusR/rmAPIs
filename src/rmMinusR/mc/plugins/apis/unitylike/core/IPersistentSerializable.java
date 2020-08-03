package rmMinusR.mc.plugins.apis.unitylike.core;

import de.tr7zw.nbtapi.NBTCompound;

public interface IPersistentSerializable {
	
	public void DataToPersistent(NBTCompound out);
	public void DataFromPersistent(NBTCompound in);
	
}
