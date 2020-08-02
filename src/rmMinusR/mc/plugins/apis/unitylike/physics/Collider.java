package rmMinusR.mc.plugins.apis.unitylike.physics;

import rmMinusR.mc.plugins.apis.unitylike.core.Component;
import rmMinusR.mc.plugins.apis.unitylike.core.GameObject;
import rmMinusR.mc.plugins.apis.unitylike.data.Vector3;

public abstract class Collider extends Component {
	
	public Collider(GameObject gameObject) {
		super(gameObject);
	}
	
	public abstract boolean IsWithin(Vector3 point);
	public abstract Vector3 GetClosestPoint(Vector3 global_point);
	public abstract RaycastHit GetCollision(Ray ray);
	
}
