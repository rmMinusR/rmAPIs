package rmMinusR.mc.plugins.apis.unitylike.core;

public abstract class Component extends UnitylikeObject {
	
	public GameObject gameObject;
	
	public Component(GameObject gameObject) {
		this.gameObject = gameObject;
	}
	
}
