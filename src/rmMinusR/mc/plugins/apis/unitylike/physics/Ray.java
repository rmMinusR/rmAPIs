package rmMinusR.mc.plugins.apis.unitylike.physics;

import rmMinusR.mc.plugins.apis.unitylike.data.Vector3;

public final class Ray {
	
	public Vector3 origin;
	public Vector3 direction;
	
	public Ray(Vector3 origin, Vector3 direction) {
		this.origin = origin;
		this.direction = direction;
	}
	
	public Vector3 GetPoint(float dist) {
		return origin.Add(direction.WithMagnitude(dist));
	}
	
}
