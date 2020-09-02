package rmMinusR.mc.plugins.apis.unitylike.core;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import rmMinusR.mc.plugins.apis.unitylike.Debug;

public abstract class Component extends UnitylikeObject {
	
	public GameObject gameObject;
	
	protected Component(GameObject gameObject) {
		this.gameObject = gameObject;
	}
	
	@SerializedName(value="@__isStarted")
	@Expose(serialize=true, deserialize=true)
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
