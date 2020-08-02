package rmMinusR.mc.plugins.apis.unitylike;

import java.util.ArrayList;

import rmMinusR.mc.plugins.apis.RmApisPlugin;

public class GameObject {
	
	private ArrayList<BehaviourComponent> components;
	
	public GameObject() {
		components = new ArrayList<BehaviourComponent>();
	}
	
	public final void Instantate() { Instantiate(this); }
	
	public static final void Instantiate(GameObject o) {
		if(RmApisPlugin.INSTANCE == null
		|| RmApisPlugin.INSTANCE.unitylikeGOM == null) throw new IllegalStateException("Tried to instantiate GameObject, but found no valid GameObjectManager!");
		
		RmApisPlugin.INSTANCE.unitylikeGOM.Instantiate(o);
	}
	
	public void AddComponent(BehaviourComponent c) {
		if(components.contains(c)) throw new IllegalArgumentException("Already added this component!");
		
		components.add(c);
	}
	
	public void RemoveComponent(Class<? extends BehaviourComponent> rem) {
		ArrayList<BehaviourComponent> matches = new ArrayList<BehaviourComponent>();
		for(BehaviourComponent c : components) if(c.getClass().isAssignableFrom(rem)) matches.add(c);
		components.removeAll(matches);
	}
	
	public <T extends BehaviourComponent> T GetComponent() {
		for(BehaviourComponent c : components) {
			try {
				@SuppressWarnings("unchecked")
				T out = (T)c;
				return out;
			} catch(ClassCastException e) {}
		}
		
		return null;
	}
	
	public <T extends BehaviourComponent> ArrayList<T> GetComponents() {
		ArrayList<T> matches = new ArrayList<T>();
		
		for(BehaviourComponent c : components) {
			try {
				@SuppressWarnings("unchecked")
				T tmp = (T)c;
				matches.add(tmp);
			} catch(ClassCastException e) {}
		}
		
		return matches;
	}
	
}
