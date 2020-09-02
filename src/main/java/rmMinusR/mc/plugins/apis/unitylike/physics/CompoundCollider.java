package rmMinusR.mc.plugins.apis.unitylike.physics;

import rmMinusR.mc.plugins.apis.unitylike.core.GameObject;
import rmMinusR.mc.plugins.apis.unitylike.data.Vector3;

public class CompoundCollider extends AbstractCollider {
	
	private AbstractCollider[] colliders;
	
	public CompoundCollider(GameObject gameObject, AbstractCollider... colliders) {
		super(gameObject);
		this.colliders = colliders;
	}
	
	@Override
	public boolean IsWithin(Vector3 point) {
		for(AbstractCollider c : colliders) if(c.IsWithin(point)) return true;
		return false;
	}

	@Override
	public Vector3 GetClosestPoint(Vector3 global_point) {
		Vector3 out = null;
		for(AbstractCollider c : colliders) {
			Vector3 v = c.GetClosestPoint(global_point);
			if(out == null || global_point.Distance(out) > global_point.Distance(v)) out = v;
		}
		return out;
	}

	@Override
	public RaycastHit TryRaycast(Line ray, float max_dist) {
		RaycastHit closest = null;
		for(AbstractCollider c : colliders) {
			RaycastHit hit = c.TryRaycast(ray, max_dist);
			if(hit != null && (closest == null || closest.point.Distance(ray.origin) > hit.point.Distance(ray.origin))) closest = hit;
		}
		if(closest != null) closest.collider = this;
		return closest;
	}

	@Override
	public void DebugRender() {
		for(AbstractCollider c : colliders) c.DebugRender();
	}
	
}
