package rmMinusR.mc.plugins.apis.unitylike.physics;

import rmMinusR.mc.plugins.apis.unitylike.core.GameObject;
import rmMinusR.mc.plugins.apis.unitylike.data.Matrix;
import rmMinusR.mc.plugins.apis.unitylike.data.Vector3;

public class PlaneCollider extends Collider {

	public Vector3 dimensions;
	public Vector3 offset;
	
	public PlaneCollider(GameObject gameObject) {
		this(gameObject, Vector3.one(), Vector3.zero());
	}
	
	public PlaneCollider(GameObject gameObject, Vector3 dimensions) {
		this(gameObject, dimensions, Vector3.zero());
	}

	public PlaneCollider(GameObject gameObject, Vector3 dimensions, Vector3 offset) {
		super(gameObject);
		this.dimensions = dimensions;
		this.offset = offset;
	}

	@Override
	public boolean IsWithin(Vector3 point) { //Only a point that is on a plane can be considered "in" the plane
		Matrix toLocal = Matrix.Identity(4);
		if(gameObject != null && gameObject.GetTransform() != null) toLocal = gameObject.GetTransform().GetWorldToLocalMatrix();
		
		Vector3 local_point = toLocal.TransformPoint(point);
		
		return -dimensions.x/2+offset.x < local_point.x && local_point.x < dimensions.x/2+offset.x
			&& -dimensions.y/2+offset.y < local_point.y && local_point.y < dimensions.y/2+offset.y
			&& local_point.z == 0;
	}

	@Override
	public Vector3 GetClosestPoint(Vector3 global_point) {
		Matrix toLocal = Matrix.Identity(4);
		if(gameObject != null && gameObject.GetTransform() != null) toLocal = gameObject.GetTransform().GetLocalToWorldMatrix();
		
		Vector3 local_point = toLocal.TransformPoint(global_point);
		
		Matrix toGlobal = Matrix.Identity(4);
		if(gameObject != null && gameObject.GetTransform() != null) toGlobal = gameObject.GetTransform().GetLocalToWorldMatrix();
		
		return toGlobal.TransformPoint(new Vector3(
					Math.min(Math.max(-dimensions.x/2f, local_point.x), dimensions.x/2f),
					Math.min(Math.max(-dimensions.y/2f, local_point.y), dimensions.y/2f),
					0
				));
	}

	@Override
	public RaycastHit GetCollision(Ray ray) {
		// TODO Auto-generated method stub
		return null;
	}

}
