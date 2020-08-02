package rmMinusR.mc.plugins.apis.unitylike.physics;

import rmMinusR.mc.plugins.apis.unitylike.core.Component;
import rmMinusR.mc.plugins.apis.unitylike.core.GameObject;
import rmMinusR.mc.plugins.apis.unitylike.data.Vector3;

public abstract class Collider extends Component {
	
	public Collider(GameObject gameObject) {
		super(gameObject);
	}
	
	public abstract boolean IsWithin(Vector3 point);
	public abstract boolean Collides(Ray ray);
	public abstract RaycastHit GetCollision(Ray ray);
	
}
