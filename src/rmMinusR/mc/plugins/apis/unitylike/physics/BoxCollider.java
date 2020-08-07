package rmMinusR.mc.plugins.apis.unitylike.physics;

import rmMinusR.mc.plugins.apis.unitylike.core.GameObject;
import rmMinusR.mc.plugins.apis.unitylike.data.BlockVector3;
import rmMinusR.mc.plugins.apis.unitylike.data.Mathf;
import rmMinusR.mc.plugins.apis.unitylike.data.Plane;
import rmMinusR.mc.plugins.apis.unitylike.data.Vector3;

public class BoxCollider extends AbstractCollider {
	
	public Vector3 size;
	public Vector3 offset;
	public BoxCollider(GameObject gameObject, Vector3 size, Vector3 offset) {
		super(gameObject);
		this.size = size;
		this.offset = offset;
	}
	
	protected Vector3 GetLocalMin() {
		return new Vector3(
				offset.x-size.x*0.5f,
				offset.y-size.y*0.5f,
				offset.z-size.z*0.5f
				);
	}
	
	protected Vector3 GetLocalMax() {
		return new Vector3(
				offset.x+size.x*0.5f,
				offset.y+size.y*0.5f,
				offset.z+size.z*0.5f
				);
	}
	
	@Override
	public final boolean IsWithin(Vector3 global_point) {
		Vector3 local_point = gameObject.GetTransform().GetWorldToLocalMatrix().TransformPoint(global_point);
		return  local_point.x == Mathf.Clamp(local_point.x, GetLocalMin().x, GetLocalMax().x) &&
				local_point.y == Mathf.Clamp(local_point.y, GetLocalMin().y, GetLocalMax().y) &&
				local_point.z == Mathf.Clamp(local_point.z, GetLocalMin().z, GetLocalMax().z);
	}

	@Override
	public final Vector3 GetClosestPoint(Vector3 global_point_in) {
		Vector3 local_point_in = gameObject.GetTransform().GetWorldToLocalMatrix().TransformPoint(global_point_in);
		Vector3 local_point_out = local_point_in.clone();
		if(local_point_in.x < GetLocalMin().x) local_point_in.x = GetLocalMin().x;
		if(local_point_in.y < GetLocalMin().y) local_point_in.y = GetLocalMin().y;
		if(local_point_in.z < GetLocalMin().z) local_point_in.z = GetLocalMin().z;
		if(local_point_in.x > GetLocalMax().x) local_point_in.x = GetLocalMax().x;
		if(local_point_in.y > GetLocalMax().y) local_point_in.y = GetLocalMax().y;
		if(local_point_in.z > GetLocalMax().z) local_point_in.z = GetLocalMax().z;
		Vector3 global_point_out = gameObject.GetTransform().GetLocalToWorldMatrix().TransformPoint(local_point_out);
		System.out.println(global_point_in+" "+local_point_in+" "+local_point_out+" "+global_point_out);
		return global_point_out;
	}

	@Override
	public final RaycastHit TryRaycast(Line ray) {
		Vector3[] planecasts = new Vector3[] {
				new Plane(GetLocalMin(), Vector3.  right()).GetIntercept(ray),
				new Plane(GetLocalMin(), Vector3.forward()).GetIntercept(ray),
				new Plane(GetLocalMin(), Vector3.     up()).GetIntercept(ray),
				new Plane(GetLocalMax(), Vector3.  right()).GetIntercept(ray),
				new Plane(GetLocalMax(), Vector3.forward()).GetIntercept(ray),
				new Plane(GetLocalMax(), Vector3.     up()).GetIntercept(ray)
		};
		
		Vector3 closest = null;
		for(Vector3 i : planecasts) {
			if(i == null || !IsWithin(i)) continue;
			if(closest == null) { closest = i; continue; }
			if(Vector3.Distance(i, ray.origin) < Vector3.Distance(closest, ray.origin)) closest = i;
		}
		
		if(closest == null) return null;
		
		RaycastHit cast = new RaycastHit();
		cast.collider = this;
		cast.point = closest;
		cast.normal = GetNormal(closest.ToBlockVector3(), closest);
		
		return cast;
	}
	
	public static Vector3 GetNormal(BlockVector3 block, Vector3 pos) {
		return pos.Sub(block.GetCenterOfBlock()).ProjToAxis().Normalize();
	}
	
}
