package rmMinusR.mc.plugins.apis.unitylike.physics;

import rmMinusR.mc.plugins.apis.unitylike.data.Vector3;

public final class Line {
	
	public Vector3 origin;
	public Vector3 direction;
	
	public Line(Vector3 origin, Vector3 direction) {
		this.origin = origin;
		this.direction = direction;
	}
	
	public Vector3 GetByDistance(float dist) {
		return origin.Add(direction.WithMagnitude(dist));
	}
	
	public Vector3 GetByT(float t) {
		//v = o + d*t
		return origin.Add(direction.Mul(t));
	}
	
}
