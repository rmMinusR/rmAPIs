package rmMinusR.mc.plugins.apis.unitylike.core;

import java.util.ArrayList;

import rmMinusR.mc.plugins.apis.RmApisPlugin;
import rmMinusR.mc.plugins.apis.unitylike.data.Transform;

public class GameObject extends UnitylikeObject {
	
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
	
	public Transform GetTransform() {
		Transform out = (Transform) GetComponent(Transform.class);
		return out != null ? out : new Transform();
	}
	
	@SuppressWarnings("deprecation")
	public void SetTransform(Transform t) {
		GetTransform().matrix.CopyDataFrom(t.matrix);
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
	
	public Component GetComponent(Class<? extends Component> clazz) {
		for(Component c : components) {
			//Class comparison
			if(clazz.isAssignableFrom(c.getClass())) {
				return c;
			}
		}
		
		return null;
	}
	
	public ArrayList<Component> GetComponents(Class<? extends Component> clazz) {
		ArrayList<Component> matches = new ArrayList<Component>();
		
		for(Component c : components) {
			//Class comparison
			if(clazz.isAssignableFrom(c.getClass())) {
				matches.add(c);
			}
		}
		
		return matches;
	}
}
