package rmMinusR.mc.plugins.apis.unitylike.core;

public final class IPersistentSerializableExt {
	
	private IPersistentSerializableExt() {}
	
	//FIXME interface static. This workaround suuuucks
	//Reflectively invoked in WrappedPlayer.LoadPersistent()
	public static IPersistentSerializable GetBlank(Class<? extends IPersistentSerializable> clazz) throws Exception {
		try {
			return (IPersistentSerializable) clazz.getMethod("GetBlank").invoke(null);
		} catch(Throwable t) {
			System.err.println(clazz.getName()+", a child of IPersistentSerializable, must implement GetBlank() but no such static method was found!");
			throw t;
		}
	}
	
}
