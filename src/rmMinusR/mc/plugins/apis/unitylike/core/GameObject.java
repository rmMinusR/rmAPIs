package rmMinusR.mc.plugins.apis.unitylike.core;

import java.util.ArrayList;

import rmMinusR.mc.plugins.apis.RmApisPlugin;

public class GameObject {
	
	private ArrayList<Component> components;
	
	public GameObject() {
		components = new ArrayList<Component>();
	}
	
	public final void Instantate() { Instantiate(this); }
	
	public static final void Instantiate(GameObject o) {
		if(RmApisPlugin.INSTANCE == null
		|| RmApisPlugin.INSTANCE.unitylikeEnv == null) throw new IllegalStateException("Tried to instantiate GameObject, but found no valid GameObjectManager!");
		
		RmApisPlugin.INSTANCE.unitylikeEnv.Instantiate(o);
	}
	
	public void AddComponent(Component c) {
		if(components.contains(c)) throw new IllegalArgumentException("Already added this component!");
		
		components.add(c);
	}
	
	public void RemoveComponent(Class<? extends Component> rem) {
		ArrayList<Component> matches = new ArrayList<Component>();
		for(Component c : components) if(c.getClass().isAssignableFrom(rem)) matches.add(c);
		components.removeAll(matches);
	}
	
	public <T extends Component> T GetComponent() {
		for(Component c : components) {
			try {
				@SuppressWarnings("unchecked")
				T out = (T)c;
				return out;
			} catch(ClassCastException e) {}
		}
		
		return null;
	}
	
	public ArrayList<Component> GetComponents(Class<? extends Component> clazz) {
		ArrayList<Component> matches = new ArrayList<Component>();
		
		for(Component c : components) {
			//Class comparison
			if(clazz.isAssignableFrom(c.getClass())) {
				matches.add(c);
			} else {
				//Interface comparison
				boolean breakLoop = false;
				Class<?>[] vals = clazz.getInterfaces();
				for(int i = 0; i < vals.length || breakLoop; i++) {
					if(clazz.isAssignableFrom(vals[i])) {
						matches.add(c);
						breakLoop = true;
					}
				}
			}
		}
		
		return matches;
	}
}
