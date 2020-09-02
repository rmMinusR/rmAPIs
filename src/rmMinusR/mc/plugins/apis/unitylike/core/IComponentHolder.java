package rmMinusR.mc.plugins.apis.unitylike.core;

import java.util.Collection;
import java.util.HashSet;

public interface IComponentHolder {
	
	@SuppressWarnings("unchecked")
	public default <T> Collection<T> GetComponents(Class<T> clazz) {
		Collection<T> out = new HashSet<T>();
		
		if(clazz.isAssignableFrom(this.getClass())) out.add((T)this);
		for(Component o : GetComponents()) {
			if(clazz.isAssignableFrom(o.getClass())) out.add((T)o);
		}
		
		return out;
	}
	
	@SuppressWarnings("unchecked")
	public default <T> T GetComponent(Class<T> clazz) {
		if(clazz.isAssignableFrom(this.getClass())) return (T)this;
		for(Component o : GetComponents()) {
			if(clazz.isAssignableFrom(o.getClass())) return (T)o;
		}
		
		return null;
	}
	
	public Collection<Component> GetComponents();
	public <T extends Component> T AddComponent(T component);
	public <T extends Component> void RemoveComponent(T component);
	
}
