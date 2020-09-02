package rmMinusR.mc.plugins.apis.unitylike.core;

import de.tr7zw.nbtapi.NBTCompound;

public final class SerializationFactory {
	
	private SerializationFactory() {}
	
	public static final String KEY_CLASS = "@__class",
							   KEY_DATA  = "@__data";
	
	@SuppressWarnings("unchecked")
	public static <T> T ReadFromNBTAs(NBTCompound data, Class<T> cast) throws ClassCastException, ClassNotFoundException {
		IPersistentSerializable s = ReadFromNBT(data);
		if(cast.isAssignableFrom(s.getClass())) return (T)s;
		else throw new ClassCastException("Cannot cast "+s.getClass().getName()+" to "+cast.getName());
	}
	
	public static IPersistentSerializable ReadFromNBT(NBTCompound data) throws ClassNotFoundException {
		Class<?> clazz = Class.forName(data.getString(KEY_CLASS));
		Object o = data.getObject(KEY_DATA, clazz);
		return (IPersistentSerializable) o;
	}
	
	public static void WriteToNBT(IPersistentSerializable ser, NBTCompound data) {
		if(!ser.DoSerialize()) return;
		
		data.setString(KEY_CLASS, ser.getClass().getName());
		data.setObject(KEY_DATA, ser);
	}
	
}
