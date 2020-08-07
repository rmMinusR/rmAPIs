package rmMinusR.mc.plugins.apis.forgelike;

import java.util.HashSet;

public final class CustomMaterial {
	
	public final String name;
	public final Class<? extends CustomItem> clazz;
	
	private CustomMaterial(String name, Class<? extends CustomItem> clazz) {
		this.name = name;
		this.clazz = clazz;
	}
	
	//REGISTRY PATTERN
	private static HashSet<CustomMaterial> registry;
	public static void InitRegistry() {
		if(registry != null) registry.clear();
		else registry = new HashSet<CustomMaterial>();
	}
	
	public static CustomMaterial ByName(String name) {
		for(CustomMaterial i : registry) if(i.name == name) return i;
		return null;
	}
	
	public static CustomMaterial ByClass(Class<? extends CustomItem> clazz) {
		for(CustomMaterial i : registry) if(i.clazz == clazz) return i;
		return null;
	}
	
	public static CustomMaterial Register(String name, Class<? extends CustomItem> clazz) {
		if(ByName (name ) != null) throw new IllegalArgumentException("Duplicate name: "+name);
		if(ByClass(clazz) != null) throw new IllegalArgumentException("Duplicate class: "+clazz.getName());
		
		CustomMaterial out = new CustomMaterial(name, clazz);
		registry.add(out);
		return out;
	}
	
	//FIXME potential security hole?
	public static CustomMaterial GetOrInstantiate(String name, Class<? extends CustomItem> clazz) {
		CustomMaterial out = ByName(name);
		if(out != null) return out;
		
		out = Register(name, clazz);
		
		return out;
	}
	
}
