package rmMinusR.mc.plugins.apis.unitylike.core;

import de.tr7zw.nbtapi.NBTCompound;

public final class IPersistentSerializableExt {
	
	private IPersistentSerializableExt() {}
	
	//FIXME interface static. This workaround suuuucks
	//Reflectively invoked in WrappedPlayer.LoadPersistent()
	public static IPersistentSerializable GetFromNBT(GameObject target, NBTCompound data, Class<? extends IPersistentSerializable> clazz) throws Exception {
		try {
			IPersistentSerializable inst = null;
			try {
				inst = clazz.getConstructor(GameObject.class, NBTCompound.class).newInstance(target, data);
			} catch(NoSuchMethodException e) {
				inst = clazz.getConstructor(GameObject.class).newInstance(target);
				inst.DataFromPersistent(data);
			}
			return inst;
		} catch(Throwable t) {
			System.err.println(clazz.getName()+", a child of IPersistentSerializable, must implement a ctor(GameObject, NBTCompound) or ctor(GameObject) but none was found!");
			throw t;
		}
	}
	
}
