package rmMinusR.mc.plugins.apis.unitylike.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.bukkit.World;

import rmMinusR.mc.plugins.apis.unitylike.Debug;
import rmMinusR.mc.plugins.apis.unitylike.data.Transform;
import rmMinusR.mc.plugins.apis.unitylike.physics.AbstractCollider;

public class GameObject extends UnitylikeObject implements IComponentHolder {
	
	private final Set<Component> components;
	public Scene scene;
	
	public GameObject(World w) { this(Scene.GetOrNew(w)); }
	
	public GameObject(Scene scene) {
		components = new HashSet<Component>();
		try { AddComponent(new Transform()); } catch(IllegalArgumentException ignored) {}
		this.scene = scene;
	}
	
	//Components quickref
	
	@SuppressWarnings("deprecation")
	public void SetTransform(Transform t) {
		GetTransform().matrix.CopyDataFrom(t.matrix);
	}
	
	public Transform GetTransform() {
		Transform out = GetComponent(Transform.class);
		return out != null ? out : new Transform();
	}

	public AbstractCollider GetCollider() {
		return GetComponent(AbstractCollider.class);
	}
	
	//Components management
	
	@Override
	public Collection<Component> GetComponents() {
		return new HashSet<Component>(components);
	}

	@Override
	public <T extends Component> T AddComponent(T component) {
		if(!components.contains(component)) {
			components.add(component);
			component._Awake();
			return component;
		} else return null;
	}

	@Override
	public <T extends Component> void RemoveComponent(T component) {
		if(components.contains(component)) {
			components.remove(component);
			component._Destroy();
		}
	}
	
	//Ticker methods
	
	@SerializedName(value="@__isStarted")
	@Expose
	private boolean _isStarted = false;
	public void Start() {}
	public void Update() {}
	protected final void _Update() {
		if(!_isStarted) {
			_isStarted = true;
			try {
				Start();
			} catch(Throwable t) { Debug.Log("Error calling Start() on "+this); t.printStackTrace(); }
		}
		
		try {
			Update();
		} catch(Throwable t) { Debug.Log("Error calling Update() on "+this); t.printStackTrace(); }
	}
	
	public void PhysicsUpdate() {}
	protected final void _PhysicsUpdate() {
		try {
			PhysicsUpdate();
		} catch(Throwable t) { Debug.Log("Error calling PhysicsUpdate() on "+this); t.printStackTrace(); }
	}
	
}
