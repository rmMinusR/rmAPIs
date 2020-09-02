package rmMinusR.mc.plugins.apis.unitylike.core;

import java.util.Collection;
import java.util.HashSet;

public interface IGameObjectHolder {
	
	@SuppressWarnings("unchecked")
	public default <T> Collection<T> FindObjectsOfType(Class<T> clazz) {
		boolean iterComponents = Component.class.isAssignableFrom(clazz);
		Collection<T> out = new HashSet<T>();
		
		for(GameObject o : GetGameObjects()) {
			if(clazz.isAssignableFrom(o.getClass())) out.add((T)o);
			if(iterComponents) for(Component c : o.GetComponents()) if(clazz.isAssignableFrom(c.getClass())) out.add((T)c);
		}
		
		return out;
	}
	
	@SuppressWarnings("unchecked")
	public default <T> T FindObjectOfType(Class<T> clazz) {
		boolean iterComponents = Component.class.isAssignableFrom(clazz);
		
		for(GameObject o : GetGameObjects()) {
			if(clazz.isAssignableFrom(o.getClass())) return (T)o;
			if(iterComponents) for(Component c : o.GetComponents()) if(clazz.isAssignableFrom(c.getClass())) return (T)c;
		}
		
		return null;
	}
	
	public Collection<GameObject> GetGameObjects();
	public <T extends GameObject> T Instantiate(T gameObject);
	public <T extends GameObject> void Destroy(T gameObject);
	
}
