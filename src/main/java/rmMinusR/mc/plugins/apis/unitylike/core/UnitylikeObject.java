package rmMinusR.mc.plugins.apis.unitylike.core;

import java.util.UUID;

import rmMinusR.mc.plugins.apis.unitylike.Debug;

public abstract class UnitylikeObject {
	
	private UUID id;
	
	public UUID GetID() { return UUID.fromString(id.toString()); } //A stupider version of clone()
	
	public UnitylikeObject() {
		id = UUID.randomUUID();
	}
	
	public UnitylikeObject(UUID id) {
		this.id = id;
	}
	
	private boolean _isAwake = false;
	public void Awake() {}
	public final boolean IsAwake() { return _isAwake; }
	protected final void _Awake() {
		if(!_isAwake) {
			_isAwake = true;
			try {
				Awake();
			} catch(Throwable t) { Debug.Log("Error calling Awake() on "+this); t.printStackTrace(); }
		}
	}
	
	private boolean _isDestroyed = false;
	public void Destroy() {}
	public final boolean IsDestroyed() { return _isDestroyed; }
	protected final void _Destroy() {
		if(!_isDestroyed) {
			_isDestroyed = true;
			try {
				Destroy();
			} catch(Throwable t) { Debug.Log("Error calling Destroy() on "+this); t.printStackTrace(); }
		}
	}
	
}