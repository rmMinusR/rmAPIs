package rmMinusR.mc.plugins.apis.unitylike.physics;

import rmMinusR.mc.plugins.apis.unitylike.core.Component;
import rmMinusR.mc.plugins.apis.unitylike.core.GameObject;
import rmMinusR.mc.plugins.apis.unitylike.data.Vector3;

public abstract class AbstractCollider extends Component {
	
	public AbstractCollider(GameObject gameObject) {
		super(gameObject);
	}
	
	public abstract boolean IsWithin(Vector3 point);
	public abstract Vector3 GetClosestPoint(Vector3 global_point);
	public abstract RaycastHit TryRaycast(Line ray);

	public final float DistanceTo(Vector3 global_point) {
		return GetClosestPoint(global_point).Distance(global_point);
	}
}
